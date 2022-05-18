package Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.horizonpay.smartpossdk.aidl.cardreader.IAidlCardReader;
import com.horizonpay.smartpossdk.aidl.cpucard.IAidlCpuCard;
import com.horizonpay.smartpossdk.aidl.cpucard.PApduEntity;
import com.horizonpay.smartpossdk.aidl.emv.AidlCheckCardListener;
import com.horizonpay.smartpossdk.aidl.magcard.TrackData;
import com.sdk.pocketmonisdk.Dialogs.AppSelectionDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardInfo {
    private static int icCardReset = 1;

    private static Context c;
    private static Activity activity;
    private static CardReadMode mCardReadMode = CardReadMode.MANUAL;
    private static IAidlCpuCard cpucard;
    private static IAidlCardReader cardreader;
    private static boolean isSupport;

    private static PosHandler posHandlerListener;
    public static void initialize(Activity a) {

        //Checks if there is any restriction for the agent
        if(Keys.isAgentRestricted(a)){
            StopTransaction(a);
            return;
        }

        activity = a;
        posHandlerListener = (PosHandler)a;
        try {
            if(cpucard != null) cpucard.powerOff();
            cardreader = MyApplication.getINSTANCE().getDevice().getCardReader();
            cpucard = MyApplication.getINSTANCE().getDevice().getCpuCard();
            isSupport = cardreader.isSupport();
            Thread.sleep(100);
            if (isSupport) {
                cardreader.searchCard(true, true, true, 30 * 1000, checkCardListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void StopTransaction(Activity activity) {
        try {
            Disconnect();
            if(posHandlerListener != null){
                posHandlerListener = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        activity.finish();
    }

    public static void Disconnect() {
        try {
            if(cpucard != null){
                posHandlerListener = null;
                cpucard.powerOff();
                cardreader.cancelSearchCard();
            }
            icCardReset = 1;
            Log.d("Result", "Card reader disconnected");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void CancelCardSearch() {
        //if (cardreader != null) cardreader.cancelSearchCard();
    }

    public static boolean isCardRemoved = false;
    public static List<AidClass> PseSupport(Context context)
    {
        try{
            Thread.sleep(10);
            String response = PerformCommand("00A404000E315041592E5359532E444446303100", false);
            if(response.equals("96")) return null;

            if (response.equals("6A81"))
                return null;
            if (response.length() == 4) return NoPseSupport(context);

            if (response.length() > 4)
            {
                Emv.parseResponse(response, "");
                String sfi = Emv.getEmv("88");
                if (!sfi.isEmpty())
                {
                    String selectAid = "";
                    String hexIndicator = ""; String aids = "";
                    String resp = ""; int i = Integer.parseInt(sfi);
                    List<AidClass> aidList = new ArrayList<AidClass>();
                    do
                    {
                        resp = PerformCommand("00 B2 " + Keys.padLeft(Integer.toHexString(i), 2, '0') + "0C 00", true);

                        //IF THE ENTRY IS FOR DDF
                        int count = Keys.findMatch(resp, "4F");
                        if (count > 1)
                        {
                            String[] tags = resp.split("61");
                            for (String data : tags)
                            {
                                String re = Emv.parseResponse("61" + data, "");
                                if (re.isEmpty()) continue;
                                aids = Emv.getEmv("4F");
                                selectAid = Keys.returnAIDIfExists(aids);
                                if (selectAid.isEmpty()) continue;
                                hexIndicator = Emv.getEmv("87");
                                int priorityInd = Integer.parseInt(hexIndicator, 16);
                                String cardLabe = Keys.hexStringToASCII(Emv.getEmv("50"));
                                AidClass aidclass = new AidClass();
                                aidclass.Aid = aids; aidclass.PriorityIndicator = priorityInd; aidclass.Name = cardLabe;
                                aidList.add(aidclass);
                            }
                            i++;
                            continue;
                        }

                        //IF THE ENTRY IS FOR ADF ONLY DO THIS IF DDF HAS NOT BEEN DONE
                        if (resp.isEmpty()) break;
                        aids = Emv.getEmv("4F");
                        selectAid = Keys.returnAIDIfExists(aids);
                        if (selectAid.isEmpty()) break;
                        hexIndicator = Emv.getEmv("87");
                        int priority = Integer.parseInt(hexIndicator, 16);
                        String cardLabel = Keys.hexStringToASCII(Emv.getEmv("50"));
                        AidClass aidclass = new AidClass();
                        aidclass.Aid = aids; aidclass.PriorityIndicator = priority; aidclass.Name = cardLabel;
                        aidList.add(aidclass);
                        i++;
                    }
                    while (true) ;

                    if (aidList.size() == 1)
                    {
                        if ((Keys.checkIfBitIsSet(hexIndicator, 1, 8) == false))
                        {
                            //This is what i check for to know if app selection was successful
                            aidList.clear();
                            AidClass aidclass = new AidClass();
                            aidclass.Aid = ""; aidclass.PriorityIndicator = 0; aidclass.Name = "";
                            aidList.add(aidclass);
                            NormalSelection(context, aids);
                        }
                        isCardRemoved = false;
                        return aidList;
                    }
                    else if (aidList.size() > 1)
                    {
                        isCardRemoved = false;
                        return aidList;
                    }
                    else return NoPseSupport(context);
                }
            }
            else { return NoPseSupport(context); }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    private static List<AidClass> NoPseSupport(Context context) {
        List<AidClass> aidList = new ArrayList<AidClass>();
        String selectAid = "", aidselected = ""; boolean toStep3 = false;

        for (AidClass aids : Emv.StoredAids)
        {
            //step 5
            selectAid = PerformCommand("00 A4 04 00 " + Keys.padLeft(Integer.toHexString (aids.Aid.length() / 2), 2, '0') + " " + aids.Aid + " 00", false);
            if (selectAid.equals("6A81")) //Step 2
                return null;

            boolean step5;
            while(true){
                //Step3:
                if ((selectAid.length() > 4) || (selectAid.substring(0, 2).equals("6283"))) //Step 3
                {
                    Emv.parseResponse(selectAid, "");
                    aidselected = Emv.getEmv("84");

                    if (aids.Aid.equals(aidselected)) //Do setep 4
                    {
                        //step4:
                        if (selectAid.length() > 4)
                        {
                            String cardLabel = Keys.hexStringToASCII(Emv.getEmv("50"));
                            String hexIndicator = Emv.getEmv("87");
                            int priority = Integer.parseInt((hexIndicator.isEmpty())? "0":hexIndicator, 16);
                            AidClass aidclass = new AidClass();
                            aidclass.Aid = aidselected; aidclass.PriorityIndicator = priority; aidclass.Name = cardLabel;
                            aidList.add(aidclass);
                            step5 = true;
                            break; //Goto step 5
                        }
                        else if (selectAid.equals("6283"))
                        {
                            step5 = true;
                            break; //Goto step 5
                        }
                    }
                    else if (aidselected.contains(aids.Aid)) //Do step 6
                    {
                        //step6:
                        if (selectAid.length() > 4)
                        {
                            String cardLabel = Keys.hexStringToASCII(Emv.getEmv("50"));
                            String hexIndicator = Emv.getEmv("87");
                            int priority = Integer.parseInt(hexIndicator, 16);
                            AidClass aidclass = new AidClass();
                            aidclass.Aid = aidselected; aidclass.PriorityIndicator = priority; aidclass.Name = cardLabel;
                            aidList.add(aidclass);
                            //goto step7;
                        }
                        else if (selectAid.length() == 4)
                        {
                            //goto step7;
                        }
                    }
                    else
                    {
                        step5 = true;
                        break; //Goto step 5
                    }
                }
                else{
                    step4:
                    if (selectAid.length() > 4)
                    {
                        String cardLabel = Keys.hexStringToASCII(Emv.getEmv("50"));
                        String hexIndicator = Emv.getEmv("87");
                        int priority = Integer.parseInt(hexIndicator, 16);
                        AidClass aidclass = new AidClass();
                        aidclass.Aid = aidselected; aidclass.PriorityIndicator = priority; aidclass.Name = cardLabel;
                        aidList.add(aidclass);
                        step5 = true;
                        break; //Goto step 5
                    }
                    else if (selectAid.equals("6283"))
                    {
                        step5 = true;
                        break; //Goto step 5
                    }
                }
                //step7:
                selectAid = PerformCommand("00 A4 04 02 " + Keys.padLeft(Integer.toHexString (aids.Aid.length() / 2), 2, '0') + " " + aids.Aid + " 00", false);
                if ((selectAid.length() > 4) || (selectAid.substring(0, 2).equals("62")) || (selectAid.substring(0, 2).equals("63")))
                {
                    //Goto step 3
                    continue;
                }
                else
                {
                    step5 = true;
                    break; //Goto step 5
                }
            }
            if(step5) continue;
        }
        isCardRemoved = false;
        if (aidList.size() == 1)
        {
            String hexIndicator = Emv.getEmv("87");
            if ((Keys.checkIfBitIsSet(hexIndicator, 1, 8) == false))
            {
                String aid = aidList.get(0).Aid;
                aidList.clear();
                AidClass aidclass = new AidClass();
                aidclass.Aid = ""; aidclass.PriorityIndicator = 0; aidclass.Name = "";
                aidList.add(aidclass);
                NormalSelection(context, aid);
                isCardRemoved = false;
                return aidList;
            }
            else {
                isCardRemoved = false;
                return aidList;
            }
        }
        isCardRemoved = false;
        return aidList;
    }


    public static boolean NormalSelection(Context context, String aids)
    {
        Emv.setEmv("4F", aids);
        String selectAid = PerformCommand("00 A4 04 00 " + Keys.padLeft(Integer.toHexString(aids.length() / 2), 2, '0') + " " + aids + " 00", true);
        if (selectAid.isEmpty())
        {
            StopTransaction(activity);
            //EventClass.OnTransactionCanceled("001");
            return false;
        }

        String pdolresp = Emv.getEmv("9F38");
        Log.d("Result", "PDOL: " + pdolresp);
        String pdol = Emv.formulatePDOL(pdolresp);
        Emv.setEmv("PDOL", pdol);
        String getProcessingOption = PerformCommand("80A80000" + pdol + "00", true);
        String parseResult = Emv.parseProcessingOption(getProcessingOption);
        if (parseResult.isEmpty())
        {
            StopTransaction(activity);
            //EventClass.OnTransactionCanceled("002");
            return false;
        };
        //Get data commands
        PerformCommand("80CA9F1700", true); //PIN Try Counter
        //PerformCommand("80CA9F3600"); //Generate ATC
        //PerformCommand("80CA9F1300"); //Last online ATC Register
        //PerformCommand("80CA9F4F00"); //Log Format

        List<String> allrecordCommand = Emv.getAPDURecordsCommands(Emv.getEmv("94")); //94 is the AFL tag
        for (String apduCommands : allrecordCommand)
        {
            PerformCommand(apduCommands, true);
        }

        //Checking to see if SDA or DDA is supported
        if ((Emv.appInterchangeProfile(7,1).equals("SDA")) || (Emv.appInterchangeProfile(6,1).equals("DDA")))
        {
            Emv.doOfflineDataAuthentication(context);
        }
        else
        {
            Emv.TVR = Keys.setBits(Emv.TVR, 1, 8);
        }

        Emv.doProcessingRestriction();
        return true;
    }

    public static String doFirstGenAc() {
        String pdol = Emv.getEmv("PDOL");
        String isCompleted;
        String termRisManIsSupported = Emv.appInterchangeProfile(4, 1); //4 means check if TRM is supported
        if (termRisManIsSupported.equals("TRM")) {
            Emv.doTerminalRiskManagement();
        }

        String cdolresp = Emv.getEmv("8C");
        if (!cdolresp.isEmpty()) {
            String cdol = "";
            Log.d("Result", "CDOL1: " + cdolresp);
            cdol = Emv.formulateCDOL(cdolresp);
            Emv.setEmv("cdol1", cdol);

            String cda = Emv.appInterchangeProfile(1, 1); //1 means check is cda is supported
            String iccPubKeyModulus = Emv.getEmv("ICCPUBKEY");
            String ac = Emv.doTerminalActionAnalysis();
            if ((cda.equals("CDA")) && (!iccPubKeyModulus.isEmpty()))
            {
                ac = Keys.setBits(ac, 1, 5);
                String cdaresp = PerformCommand("80AE" + ac + "00" + cdol + "00", false);
                if (!cdaresp.isEmpty()) cdaresp = Emv.parseFirstGenACResponse(cdaresp);
                boolean issuccess = Emv.doCombinedDynamicDataAuthentication(pdol, cdol, cdaresp);
                if (issuccess != true)
                {
                    Emv.TVR = Keys.setBits(Emv.TVR, 1, 3);
                    Emv.UpdateValues();
                }
            }
            else
            {
                String firstGenAc = PerformCommand("80AE" + ac + "00" + cdol + "00", false);
                if (!firstGenAc.isEmpty()) Emv.parseFirstGenACResponse(firstGenAc);
                else return "";
            }
            Log.d("Result","First GEN AC was successfully performed");
        }

        if (Emv.getCID().equals("AAC")) {
            return "DECLINED OFFLINE";
        } else if (Emv.getCID().equals("TC")) {
            return "APPROVED ONLINE";
        } else {
            isCompleted = "COMPLETED";
        }
        return isCompleted;
    }

    public static void completeTransaction() {

        //If entry mode is contactless.
        if(Emv.posEntryMode.equals("071")){
            Contactless.CompleteTransaction();
            return;
        }

        //Do issuer script processing
        String issuerscript1 = Emv.getEmv("71");
        if ((!issuerscript1.isEmpty()) && issuerscript1.contains("86")) {
            if (issuerscript1.contains("9F18")) {
                issuerscript1 = issuerscript1.substring(4 + 2 + 8);
            }
            String[] applications = issuerscript1.split("86");

            for (String data : applications) {
                Emv.parseResponse("86" + data, "");
                String isd = Emv.getEmv("86");
                if (isd.length() < 6) continue;
                PerformCommand(isd, false);
            }
            Emv.TSI = Keys.setBits(Emv.TSI, 1, 3);
            Emv.UpdateValues();
            Log.d("Result", "Issuer script on First GEN AC was successfully performed");
        }


        //Verify issuer authentication data
        String issAuthtData = Emv.getEmv("91");  //Example iad: B9D9F2105491832B3133   Emv tag 91
        if ((Emv.appInterchangeProfile(3, 1).equals("IAUTH")) && (!issAuthtData.isEmpty())) {
            Emv.setEmv("91", issAuthtData);
            String resp = PerformCommand("00 82 00 00 " + Keys.padLeft(Integer.toHexString(issAuthtData.length() / 2), 2, '0') + issAuthtData, false);
            if (resp.equals("9000")) {
                Emv.TSI = Keys.setBits(Emv.TSI, 1, 5);
                Emv.UpdateValues();
                Log.d("Result", "External authentication was performed successfully");
            }
        }

        //Do second gen ac;
        String cdolresp2 = Emv.getEmv("8D");
        if ((!cdolresp2.isEmpty()) && (Emv.posEntryMode.equals("051"))) {
            Log.d("Result", "CDOL2: " + cdolresp2);
            String cdol2 = Emv.formulateCDOL(cdolresp2);
            String cda = Emv.appInterchangeProfile(1, 1); //1 means check if cda is supported
            String iccPubKeyModulus = Emv.getEmv("ICCPUBKEY");
            if ((cda.equals("CDA")) && (!iccPubKeyModulus.isEmpty())) {
                String pdol = Emv.getEmv("PDOL");
                String cdol = Emv.getEmv("cdol1");
                String ac = "40";
                ac = Keys.setBits(ac, 1, 5);
                String cdaresp = PerformCommand("80AE" + ac + "00" + cdol2 + "00", false);
                if (!cdaresp.isEmpty()) cdaresp = Emv.parseFirstGenACResponse(cdaresp);
                boolean issuccess = Emv.doCombinedDynamicDataAuthentication(pdol, cdol + cdol2.substring(2), cdaresp);
                if (issuccess != true) {
                    Emv.TVR = Keys.setBits(Emv.TVR, 1, 3);
                    Emv.UpdateValues();
                } else {
                    Log.d("Result", "Second GEN AC was successfully performed");
                }
            } else {
                String secGenAc = PerformCommand("80AE4000" + cdol2 + "00", false);
                if (secGenAc.length() > 4) {
                    Emv.parseFirstGenACResponse(secGenAc);
                    Log.d("Result", "Second GEN AC was successfully performed");
                }
            }

            //Do issuer script processing
            String issuerscript2 = Emv.getEmv("72");
            if ((!issuerscript2.isEmpty()) && issuerscript2.contains("86")) {
                if (issuerscript2.contains("9F18")) {
                    issuerscript2 = issuerscript2.substring(4 + 2 + 8);
                }

                String[] applications = issuerscript2.split("86");

                for (String data : applications) {
                    Emv.parseResponse("86" + data, "");
                    String isd = Emv.getEmv("86");
                    if (isd.length() < 6) continue;
                    PerformCommand(isd, false);
                }
                Emv.TSI = Keys.setBits(Emv.TSI, 1, 3);
                Emv.UpdateValues();
                Log.d("Result", "Issuer script on Second GEN AC was successfully performed");
            }
        }
        Disconnect();
        return;
    }

    private static void Beep(final int sec) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //int beep = mBeeper.beep(BEEP_FREQUENCE, sec);
            }
        });
        t.start();
    }

    private static AidlCheckCardListener.Stub checkCardListener = new AidlCheckCardListener.Stub() {

        @Override
        public void onFindMagCard(TrackData data) throws RemoteException {
            String builder = "Card: " + data.getCardNo() +
                    "\nTk1: " + data.getTrack1Data() +
                    "\nTk2: " + data.getTrack2Data() +
                    "\nTk3: " + data.getTrack3Data() +
                    "\nExpiryDate: " + data.getExpiryDate() +
                    "\nServiceCode: " + data.getServiceCode() +
                    "\nHolder: " + data.getCardholderName() +
                    "\nIs IC card: " + (data.isIccCard() ? "Yes" : "No");
            Log.d("Result", builder.toString());
        }

        @Override
        public void onSwipeCardFail() throws RemoteException {
            Log.d("Result", "Swip card Failed");
        }

        @Override
        public void onFindICCard() throws RemoteException {
            Log.d("Result", "Find Contact IC Card");
            mCardReadMode = CardReadMode.CONTACT;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    posHandlerListener.onDetectICCard(mCardReadMode);
                    //This calls app selection.
                    Runnable runnable = ()-> runPSECommand(mCardReadMode);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(runnable, 10);
                    try {
                        cardreader.cancelSearchCard();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onFindRFCard(int ctlsCardType) throws RemoteException {
            Log.d("Result", "Find Contactless IC Card");
            mCardReadMode = CardReadMode.CONTACTLESS;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    posHandlerListener.onDetectContactlessCard(mCardReadMode);
                    //This calls app selection.
                    Runnable runnable = ()-> runPSECommand(mCardReadMode);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(runnable, 10);
                    try {
                        cardreader.cancelSearchCard();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onTimeout() throws RemoteException {
            posHandlerListener.onCardTimeount();
            Log.d("Result", "onTimeout");
        }

        @Override
        public void onCancelled() throws RemoteException {
            Log.d("Result", "onCancelled");
        }

        @Override
        public void onError(int errCode) throws RemoteException {
            Log.d("Result", "onError " + errCode);
        }
    };

    public static void runPSECommand(CardReadMode cardInfoEntity){
        List<AidClass> aidList = new ArrayList<>();
        //Check to see if the card is contact or contactless
        if(cardInfoEntity == CardReadMode.CONTACT){
            aidList = CardInfo.PseSupport(activity);
        }else if(cardInfoEntity == CardReadMode.CONTACTLESS){
            Contactless.initialize(activity);
            return;
        }else if(cardInfoEntity == CardReadMode.FALLBACK_SWIPE){
            Disconnect();
            return;
        }

        //If you get here, it means you have successfully done application selection
        if((aidList == null) || (CardInfo.isCardRemoved == true)){
            StopTransaction(activity);
            return;
        }else if(aidList.size() == 0){
            StopTransaction(activity);
            return;
        }

        if (aidList.get(0).Aid.equals("")) {
            Emv.startCVMProcessing(activity);
        } else if(aidList.size() > 0) {
            Collections.sort(aidList, AidClass.SortByAid);
            AppSelectionDialog dialog = new AppSelectionDialog(activity,aidList);
            dialog.show();
        }
    }

    public static String PerformCommand(String StringApdu, boolean parseit) {
        String result = "";
        StringApdu = StringApdu.replace(" ", "");
        try {
            String cmd = "", data = "";
            if (StringApdu.length() > 10) {
                cmd = StringApdu.substring(0, 8);
                data = StringApdu.substring(10, 10 + Integer.parseInt(StringApdu.substring(8, 10), 16) * 2);
            } else {
                cmd = StringApdu.substring(0, 8);
                data = "";
            }
            byte le = 0x00;
            if (cpucard == null) {
                return "96";
            }
            Log.d("Result", "Sent: " + cmd + Integer.toHexString(data.length() / 2) + data + "00");
            if (icCardReset != 0) {
                if (!cpucard.powerOn(mCardReadMode.ordinal(), 0, new byte[]{0x00, 0x00})) {
                    return "96";
                } else {
                    icCardReset = 0;
                }
            }
            byte[] cmdBytes = Keys.hexStringToByteArray(cmd);
            byte[] dataArray = Keys.hexStringToByteArray(data);
            byte[] tmp = new byte[256];

            if (dataArray != null) System.arraycopy(dataArray, 0, tmp, 0, dataArray.length);

            PApduEntity apduEntity = new PApduEntity();

            apduEntity.setCla(cmdBytes[0]);
            apduEntity.setIns(cmdBytes[1]);
            apduEntity.setP1(cmdBytes[2]);
            apduEntity.setP2(cmdBytes[3]);
            apduEntity.setLc(data.length() / 2);
            apduEntity.setLe(le);
            apduEntity.setDataIn(tmp);

            boolean ret = cpucard.exchangeApdu(apduEntity);
            if (ret == true) {
                if (apduEntity.getDataOutLen() > 0) {
                    result = Keys.byteToHexString(apduEntity.getDataOut()).substring(0, (apduEntity.getDataOutLen() * 2));
                } else {
                    result = "";
                }
            } else {
                return "";
            }
            Log.d("Result", "Response: " + result);
            if (result.length() > 4) {
                result = (result.endsWith("9000")) ? result = result.substring(0, result.length() - 4) : result;
                if (parseit) Emv.parseResponse(result, "");
            } else {
                return (parseit) ? "" : result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
