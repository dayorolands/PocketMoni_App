package Utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Contactless {

    private static Activity activity;
    public static boolean initialize(Activity a){
        activity = a;
        if(preProcessing()){
            PseContactlessSupport();
        }else{
            CardInfo.StopTransaction(activity);
        }
        return true;
    }

    public static boolean preProcessing(){
        long transAmount = Long.parseLong(Emv.getMinorAmount());
        long floorLimit = Emv.ContactlessFloorLimit;
        long cvmReqLimit = (long)Double.parseDouble(Emv.ContactlessCVMLRequiredLimit);
        long cvmTransLimit = (long)Double.parseDouble(Emv.readerContactlessTransactionLimit);

        if(transAmount > cvmTransLimit){
            Toast.makeText(activity, "Contactless application not allowed", Toast.LENGTH_SHORT).show();
            CardInfo.StopTransaction(activity);
            return false;
        }

        if(Keys.checkIfBitIsSet(Emv.TerminalCapability,2,4)){
            Keys.setBits(Emv.TerminalTransQualifiers,2,7);
        }
        if(transAmount > floorLimit){
            //(‘Online cryptogram required’).
            Keys.setBits(Emv.TerminalTransQualifiers,2,8);
        }

        if(transAmount > cvmReqLimit){
            //(‘CVM required’)
            //No cvm i not supported.
            Emv.TerminalCapability = "9F33|E0F0C8";
            Keys.setBits(Emv.TerminalTransQualifiers,2,7);
        }else{
            //Only signature is supported
            Emv.TerminalCapability = "9F33|E008C8";
        }
        return true;
    }

    public static void PseContactlessSupport()
    {
        if(!preProcessing()){
            CardInfo.StopTransaction(activity);
            return;
        }
        List<AidClass> aidList = new ArrayList<>();
        try{
            String response = CardInfo.PerformCommand("00A404000E325041592E5359532E444446303100", true);
            Thread.sleep(10);
            if (response.isEmpty()) {
                //goto step 3
                if(aidList.size() == 0){
                    CardInfo.StopTransaction(activity);
                    return;
                }
            }
            else{
                String bfoc = Emv.getEmv("BF0C");
                String[] tags = bfoc.split("61");
                for (String tag : tags){
                    String re = Emv.parseResponse("61" + tag, "");
                    if(re.isEmpty()) continue;

                    String adfName = Emv.getEmv("4F");
                    if (adfName.isEmpty()) continue;
                    if (Keys.checkIfAidExists(adfName))
                    {
                        String reqKernelId = "";
                        String kernelIdentifier = Emv.getEmv("9F2A");
                        if (kernelIdentifier.isEmpty() || kernelIdentifier.equals("00"))
                        {
                            reqKernelId = Keys.returnKernelId(adfName).split("\\|")[1];
                        }
                        else
                        {
                            String byte1KernelId = kernelIdentifier.substring(0, 2);
                            String bit8bit7 = Keys.hexStringToBinary(byte1KernelId.substring(0,2)).substring(0, 2);
                            if ((bit8bit7.equals("00")) || (bit8bit7.equals("01")))
                            {
                                //Requested kernel id is equal to the byte one of kernel identifier.
                                reqKernelId = Keys.hexStringToBinary(byte1KernelId.substring(0,2));
                            }
                            if ((bit8bit7.equals("10")) || (bit8bit7.equals("11")))
                            {
                                if (kernelIdentifier.length() < 6) continue;
                                String shortkernelId = Keys.hexStringToBinary(reqKernelId.substring(0,2)).substring(2);
                                if (!shortkernelId.equals("000000"))
                                {
                                    //reqKernelId = kernelIdentifier.substring(0, 6);
                                    reqKernelId = Keys.hexStringToBinary(kernelIdentifier.substring(0,2));
                                }
                                if (shortkernelId.equals("000000")) break;
                            }
                        }
                        //If the value of requested kernel id is zero
                        if (reqKernelId.isEmpty()){
                            Log.d("Result", "Kernel is supported");
                        }

                        String kernelID = Keys.returnKernelId(adfName).split("\\|")[1];
                        if(!reqKernelId.isEmpty() && reqKernelId.equals(kernelID)){
                            Log.d("Result", "Kernel is supported");
                        }else continue;

                        String cardname = Keys.hexStringToASCII(Emv.getEmv("50"));
                        String hexIndicator = Emv.getEmv("87");
                        String extselection = Emv.getEmv("9F29");
                        int priority = Integer.parseInt(((!hexIndicator.isEmpty()) ? hexIndicator : "F"), 16);
                        AidClass ac = new AidClass();
                        ac.Adfname = adfName;
                        ac.Name = ((!cardname.isEmpty()) ? cardname : Keys.returnKernelId(adfName).split("\\|")[0]);
                        ac.Aid = Keys.returnAIDIfExists(adfName);
                        ac.kernelID = reqKernelId;
                        ac.PriorityIndicator = priority;
                        ac.extdSelection = extselection;
                        aidList.add(ac);
                    }
                    else continue;
                }
            }
            Emv.posEntryMode = "071";
            if (aidList.size() > 0)
            {
                String extSelection = Emv.getEmv("9F29");
                if(aidList.size() == 1){
                    String adfName = aidList.get(0).Aid;
                    if(extSelection.equals("01")){
                        adfName += extSelection;
                    }
                    SelectKernel(aidList.get(0).kernelID, adfName);
                }else{
                    Collections.sort(aidList, AidClass.SortByAid);
                    String adfName = aidList.get(0).Aid;
                    if(extSelection.equals("01")){
                        adfName += extSelection;
                    }
                    SelectKernel(aidList.get(0).kernelID, adfName);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(activity, "CARD WAS REMOVED", Toast.LENGTH_SHORT).show();
            CardInfo.StopTransaction(activity);
        }
        CardInfo.StopTransaction(activity);
        return;
    }

    protected static String kernelID = "";
    public static void SelectKernel(String kernelId, String aidselected){
        kernelID = kernelId;
        switch (kernelId){
            case "00000100":
                //do American Express;
                break;
            case "00000110":
                //do Discover
                break;
            case "00000101":
                //do JCB
                break;
            case "00000010":
                //do MasterCard
                MasterCard.Initialize(activity, aidselected);
            case "00000111":
                //do UnionPay
                break;
            case "00000011":
                //do Visa
                break;
            default:
                //default is 00000000
                //do Normal
                activity.runOnUiThread(()-> {
                    Toast.makeText(activity, "Could not find kernel for selected card", Toast.LENGTH_LONG).show();
                });
                CardInfo.StopTransaction(activity);
                break;
        }
    }


    public static void CompleteTransaction(){
        switch (kernelID){
            case "00000100":
                //do American Express;
                break;
            case "00000110":
                //do Discover
                break;
            case "00000101":
                //do JCB
                break;
            case "00000010":
                //do MasterCard
                MasterCard.CompleteTransaction();
            case "00000111":
                //do UnionPay
                break;
            case "00000011":
                //do Visa
                break;
            default:
                //default is 00000000
                //do Normal
                activity.runOnUiThread(()-> {
                    Toast.makeText(activity, "Could not find kernel for selected card", Toast.LENGTH_LONG).show();
                });
                CardInfo.StopTransaction(activity);
                break;
        }
    }
}
