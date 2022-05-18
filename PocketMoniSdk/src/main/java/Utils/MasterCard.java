package Utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.util.Date;
import java.util.List;

public class MasterCard extends Emv {



    static double MaximumRelayResistanceGracePeriod = 0032f;
    static double MinimumRelayResistanceGracePeriod = 0014f;
    static double TerminalExpectedTransmissionTimeForRelayResistanceCAPDU = 0012f;
    static double TerminalExpectedTransmissionTimeForRelayResistanceRAPDU = 0018f;
    static double RelayResistanceTransmissionTimeMismatchThreshold = 32;

    private static Activity activity;
    public static void Initialize(Activity a, String aidSelected){
        activity = a;
        NormalSelection(aidSelected);
    }

    private static boolean NormalSelection(String aids)
    {
        Emv.setEmv("4F", aids);
        String selectAid = CardInfo.PerformCommand("00 A4 04 00 " + Keys.padLeft(Integer.toHexString(aids.length() / 2), 2, '0') + " " + aids + " 00", true);
        if (selectAid.isEmpty())
        {
            CardInfo.StopTransaction(activity);
            return false;
        }

        String pdolresp = Emv.getEmv("9F38");
        Log.d("Result", "PDOL: " + pdolresp);
        String pdol = Emv.formulatePDOL(pdolresp);
        Emv.setEmv("PDOL", pdol);
        String getProcessingOption = CardInfo.PerformCommand("80A80000" + pdol + "00", true);
        String parseResult = Emv.parseProcessingOption(getProcessingOption);
        if (parseResult.isEmpty())
        {
            CardInfo.StopTransaction(activity);
            return false;
        };
        //Get data commands
        //CardInfo.PerformCommand("80CA9F1700", true); //PIN Try Counter

        List<String> allrecordCommand = Emv.getAPDURecordsCommands(Emv.getEmv("94")); //94 is the AFL tag
        for (String apduCommands : allrecordCommand)
        {
            CardInfo.PerformCommand(apduCommands, true);
        }
        String rrpSupport = Emv.appInterchangeProfile(1, 2); //5 means check if cvm is supported
        if(rrpSupport.equals("RRP")){
            doRelayResistanceExchange();
        }else{
            Emv.TVR = Keys.setBits(Emv.TVR, 5, 1);
        }

        doCvmProcess();
        doProcessingRestriction();
        return true;
    }


    static double deviceRelayResistanceEntropy = 0;
    static double minTimeForProcessingRelayResistanceAPDU = 0;
    static double maxTimeForProcessingRelayResistanceAPDU = 0;
    static double deviceEstimatedTransmissionTimeForRelayResistanceAPDU = 0;
    private static void doRelayResistanceExchange(){
        for(int i = 0; i< 2; i++){
            String terminalRelayResistanceEntropy = Keys.genUnpredictableNum();
            setEmv("9F37", terminalRelayResistanceEntropy);
            long start = System.currentTimeMillis();
            String relayResistanceData = CardInfo.PerformCommand("80EA000004" + terminalRelayResistanceEntropy + "00", false);
            long end = System.currentTimeMillis();

            deviceRelayResistanceEntropy = Double.parseDouble(relayResistanceData.substring(4,4+8));
            minTimeForProcessingRelayResistanceAPDU = Double.parseDouble(relayResistanceData.substring(12,12+4));
            maxTimeForProcessingRelayResistanceAPDU = Double.parseDouble(relayResistanceData.substring(16,16+4));
            deviceEstimatedTransmissionTimeForRelayResistanceAPDU = Double.parseDouble(relayResistanceData.substring(20,20+4));

            if(!relayResistanceData.isEmpty()){
                double MeasuredRelayResistanceProcessingTime =
                        ((end-start)/100f) - TerminalExpectedTransmissionTimeForRelayResistanceCAPDU -
                                Math.min(deviceEstimatedTransmissionTimeForRelayResistanceAPDU,
                                        TerminalExpectedTransmissionTimeForRelayResistanceRAPDU);
                if (MeasuredRelayResistanceProcessingTime < (minTimeForProcessingRelayResistanceAPDU - MinimumRelayResistanceGracePeriod)) {
                    continue;
                }
                if(MeasuredRelayResistanceProcessingTime >
                        (maxTimeForProcessingRelayResistanceAPDU + MaximumRelayResistanceGracePeriod)){
                    //Relay resistance time limits exceeded' in Terminal Verification Results
                    Emv.TVR = Keys.setBits(Emv.TVR,5,3);
                    Log.d("Result", "1");
                }else if((((TerminalExpectedTransmissionTimeForRelayResistanceRAPDU / deviceEstimatedTransmissionTimeForRelayResistanceAPDU)*100) <
                        RelayResistanceTransmissionTimeMismatchThreshold) || ((MeasuredRelayResistanceProcessingTime - minTimeForProcessingRelayResistanceAPDU) >
                        RelayResistanceTransmissionTimeMismatchThreshold)){
                    //Relay resistance time limits exceeded' in Terminal Verification Results
                    Emv.TVR = Keys.setBits(Emv.TVR,5,3);
                    Log.d("Result", "2");
                }else{
                    //RRP performed
                    Emv.TVR = Keys.setBits(Emv.TVR,5,2);
                    Log.d("Result", "3");
                }

                //Emv.TVR = Keys.setBits(Emv.TVR,5,2);

                Log.d("Result", "MeasuredRelayResistanceProcessingTime: " + MeasuredRelayResistanceProcessingTime);
                Log.d("Result", "deviceRelayResistanceEntropy: " + deviceRelayResistanceEntropy);
                Log.d("Result", "minTimeForProcessingRelayResistanceAPDU: " + minTimeForProcessingRelayResistanceAPDU);
                Log.d("Result", "maxTimeForProcessingRelayResistanceAPDU: " + maxTimeForProcessingRelayResistanceAPDU);
                Log.d("Result", "deviceEstimatedTransmissionTimeForRelayResistanceAPDU: " + deviceEstimatedTransmissionTimeForRelayResistanceAPDU);
                Log.d("Result", "end");
                break;
            }
        }
    }

    public static void doProcessingRestriction() {
        Emv.setEmv("9F02", Emv.AmountAuthorized.split("\\|")[1]);
        Emv.setEmv("9F03", Emv.AmountOther.split("\\|")[1]);
        Emv.setEmv("5F2A", Emv.TerminalCurrencyCode.split("\\|")[1]);
        Emv.setEmv("9F1A", Emv.TerminalCountryCode.split("\\|")[1]);
        Emv.setEmv("9C", Emv.TransactionType.split("\\|")[1]);
        Emv.setEmv("9F35", Emv.TerminalType.split("\\|")[1]);

        String appVersionNum = Emv.getEmv("9F09");
        if (!appVersionNum.isEmpty()) {
            if (!appVersionNum.equals(Emv.TerminalappVersionNumber)) {
                Emv.TVR = Keys.setBits(Emv.TVR, 2, 8);
            }
        }

        String appEffectiveDate = Emv.getEmv("5F25");
        if (!appEffectiveDate.isEmpty()) {
            int terminalMonth = DateTime.Now.Month();
            int terminalYear = DateTime.Now.Year();
            int terminalDay = DateTime.Now.Day();
            int year = Integer.parseInt(String.valueOf(terminalYear).substring(0, 2) + appEffectiveDate.substring(0, 2));
            int month = Integer.parseInt(appEffectiveDate.substring(2, 2 + 2));
            int day = Integer.parseInt(appEffectiveDate.substring(4, 4 + 2));
            Date appeffectiveDate = DateTime.CustomDate(year, month, day, 00, 1, 1);
            Date transactionDate = DateTime.CustomDate(terminalYear, terminalMonth, terminalDay, 00, 1, 1);
            int datecount = DateTime.Compare(transactionDate, appeffectiveDate);
            if(datecount < 0){
                Emv.TVR = Keys.setBits(Emv.TVR, 2, 6);
            }
        }

        String appExpirationDate = Emv.getEmv("5F24");
        if (!appExpirationDate.isEmpty()) {
            int terminalMonth = DateTime.Now.Month();
            int terminalYear = DateTime.Now.Year();
            int terminalDay = DateTime.Now.Day();
            int year = Integer.parseInt(String.valueOf(terminalYear).substring(0, 2) + appExpirationDate.substring(0, 2));
            int month = Integer.parseInt(appExpirationDate.substring(2, 2 + 2));
            int day = Integer.parseInt(appExpirationDate.substring(4, 4 + 2));
            Date appExpiryDate = DateTime.CustomDate(year, month, day, 00, 1, 1);
            Date transactionDate = DateTime.CustomDate(terminalYear, terminalMonth, terminalDay, 00, 1, 1);
            int datecount = DateTime.Compare(transactionDate, appExpiryDate);
            if (datecount > 0) {
                Emv.TVR = Keys.setBits(Emv.TVR, 2, 7);
            }
        }

        String applicationUsageControl = Emv.getEmv("9F07");
        if(!applicationUsageControl.isEmpty()){
            if(Keys.checkIfBitIsSet(applicationUsageControl, 1, 2) || Keys.checkIfBitIsSet(applicationUsageControl, 1, 1)){
                //step 14
                String issuerCountryCode = Emv.getEmv("5F28");
                if(!issuerCountryCode.isEmpty()){
                    if(Emv.TerminalCountryCode.split("\\|")[0].equals(issuerCountryCode)){
                        //do 22
                        if(Keys.checkIfBitIsSet(applicationUsageControl, 1, 6) || Keys.checkIfBitIsSet(applicationUsageControl, 1, 4)){

                        }else{
                            //Requested service not allowed
                            Emv.TVR = Keys.setBits(Emv.TVR, 2, 5);
                        }
                    }else{
                        //do 23
                        if(Keys.checkIfBitIsSet(applicationUsageControl, 1, 5) || Keys.checkIfBitIsSet(applicationUsageControl, 1, 3)){

                        }else{
                            //Requested service not allowed
                            Emv.TVR = Keys.setBits(Emv.TVR, 2, 5);
                        }
                    }
                }
            }else{
                //Requested service not allowed
                Emv.TVR = Keys.setBits(Emv.TVR, 2, 5);
            }
        }

        Emv.UpdateValues();

        String terminalRiskMan = Emv.appInterchangeProfile(4,1); //5 means check if cvm is supported
        if(terminalRiskMan.equals("TRM")){
            doTerminalRiskManagement();
        }

        //Do first gen ac
        String result = doFirstGenAc();
        if (result.equals("COMPLETED")) {
            Emv.doOncvmProcessingResult(activity,pinType);
        }else{
            activity.runOnUiThread(()->{
                Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
            });
            CardInfo.StopTransaction(activity);
        }

    }


    public static void doTerminalRiskManagement()
    {
        String cda = Emv.appInterchangeProfile(1, 1);
        if(!cda.equals("CDA")){
            Emv.TVR = Keys.setBits(Emv.TVR, 1, 8);
        }

        Emv.TSI = Keys.setBits(Emv.TSI, 1, 4);
        Emv.UpdateValues();
        Log.d("Result", "TVR: " + Emv.TVR);
        Log.d("Result", "CVR: " + Emv.getEmv("9F34"));
        Log.d("Result", "TSI: " + Emv.getEmv("9B"));
    }


    public static String doFirstGenAc() {
        String isCompleted;

        String pdol = Emv.getEmv("PDOL");
        String cdolresp = Emv.getEmv("8C");
        if (!cdolresp.isEmpty()) {
            String cdol = "";
            Log.d("Result", "CDOL1: " + cdolresp);
            cdol = Emv.formulateCDOL(cdolresp);
            Emv.setEmv("cdol1", cdol);

            String cda = Emv.appInterchangeProfile(1, 1); //1 means check is cda is supported
            String ac = Emv.doTerminalActionAnalysis();
            if (cda.equals("CDA")) {
                ac = Keys.setBits(ac, 1, 5);
                String cdaresp = CardInfo.PerformCommand("80AE" + ac + "00" + cdol + "00", false);
                if (!cdaresp.isEmpty()) cdaresp = Emv.parseFirstGenACResponse(cdaresp);

                retrieveICCPubKey(activity);
                boolean issuccess = doCombinedDynamicDataAuthentication(pdol, cdol, cdaresp);
                if (issuccess != true) {
                    Emv.TVR = Keys.setBits(Emv.TVR, 1, 3);
                    Emv.UpdateValues();
                }
            } else {
                String firstGenAc = CardInfo.PerformCommand("80AE" + ac + "00" + cdol + "00", false);
                if (!firstGenAc.isEmpty()){
                    Emv.parseFirstGenACResponse(firstGenAc);
                    Log.d("Result", "First GEN AC was successfully performed");
                }
                else return "";
            }
        }

        if (Emv.getCID().equals("AAC")) {
            return "DECLINED OFFLINE";
        } else if (Emv.getCID().equals("TC")) {
            return "APPROVED OFFLINE";
        } else if(Emv.getCID().equals("ARQC")) {
            isCompleted = "COMPLETED";
        }else{
            return "CARD WAS REMOVED";
        }
        return isCompleted;
    }


    private static String pinType = "";
    private static void doCvmProcess() {
        long transAmount =(long)Double.parseDouble(Emv.getMinorAmount());
        long cvmReqLimit = (long)Double.parseDouble(Emv.ContactlessCVMLRequiredLimit);
        String onDeviceCvm = Emv.appInterchangeProfile(2,1);
        if(onDeviceCvm.equals("ONDEVICE")){
            if (transAmount < cvmReqLimit) {
                cvmResult = "3F0002";
                Emv.setEmv("9F34",cvmResult);
                pinType = "NO CVM";
            } else {
                cvmResult = "010002";
                Emv.setEmv("9F34", cvmResult);
                pinType = "ONLINE";
            }
            return;
        }
        String onNormCvm = Emv.appInterchangeProfile(5,1);
        if(onNormCvm.equals("CVM")){
            String cvmresult = Emv.getEmv("8E");
            if (cvmresult.isEmpty()){
                cvmResult = "3F0000";
                Emv.setEmv("9F34", cvmResult);
                pinType = "NO CVM";
                //ICC data missing
                Emv.TVR = Keys.setBits(Emv.TVR, 1,6);
            }
            //Loop through the cvm list
            for (int i = 16; i < cvmresult.length(); i += 4) {
                String byte1 = Keys.binaryStringToHexString(Keys.hexStringToBinary(cvmresult.substring(i, i + 2)).substring(2));
                byte1 = Keys.padLeft(byte1,2,'0');
                String byte2 = cvmresult.substring(i + 2, (i + 2) + 2);
                String byte3 = Keys.binaryStringToHexString(Keys.hexStringToBinary(cvmresult.substring(i, i + 2)).substring(0,2));
                byte3 = Keys.padLeft(byte3,2,'0');

                //byte1 00 Fail cvm processing, byte1 01 Offline plaintext pin verification, byte1 02 Enciphered pin verified online,
                //byte1 04 Enciphered pin verified offline, byte1 1F No cvm required, byte1 1E Signature
                //byte2 00 Always, byte2 01 If unattended cash, byte2 03 If terminal supports the cvm
                //byte3 01 Next, byte3 00 Fail.

                cvmResult = cvmresult.substring(i, i + 4);
                //setEmv("9F34", cvmresult.Substring(i, 4) + "00");

                if (byte2.equals("01")) continue; //If unattended cash
                if (byte1.equals("00") || byte1.equals("40")){
                    //Fail cvm processing
                    pinType = "NO CVM";
                    Emv.setEmv("9F34",cvmResult + "01");
                    return;
                } else if ((byte1.equals("02")) && (Keys.checkIfBitIsSet(TerminalCapability, 2, 7))) {
                    pinType = "ONLINE";
                    Emv.setEmv("9F34", cvmResult + "00");
                    Emv.TVR = Keys.setBits(Emv.TVR,3,3);
                    return;
                } else if ((byte1.equals("1E")) && (Keys.checkIfBitIsSet(TerminalCapability, 2, 6) == true)) {
                    pinType = "SIGNATURE";
                    Emv.setEmv("9F34",cvmResult + "00");
                    return;
                } else if ((byte1.equals("1F")) && (Keys.checkIfBitIsSet(TerminalCapability, 2, 4) == true)) {
                    pinType = "NO CVM";
                    Emv.setEmv("9F34",cvmResult + "02");
                    return;
                }
                Emv.TVR = Keys.setBits(Emv.TVR,3,7);
                if(byte3.equals("00")) break;
            }
            pinType = "NO CVM";
            cvmResult = "3F0001";
            Emv.setEmv("9F34", cvmResult);
            //Cardholder verification was not successful.
            Emv.TVR = Keys.setBits(Emv.TVR,3,8);
        }else{
            pinType = "NO CVM";
            cvmResult = "3F0000";
            Emv.setEmv("9F34", cvmResult);
            pinType = "NO CVM";
        }

    }

    public static void retrieveICCPubKey(Context context) {
        ///Todo Remember to add production keys in emv.xml
        String aid = Emv.getEmv("4F");
        String issPubKCertificate = Emv.getEmv("90");
        String issPubKRemainder = Emv.getEmv("92");
        String issPubKExponent = Emv.getEmv("9F32");
        String issPubKeyIndex = Emv.getEmv("8F");
        Emv.retrieveCAPublicKeys(context, aid, issPubKeyIndex);
        String CAModulus = Emv.getEmv("modulus");
        if (CAModulus.isEmpty()) {
            Emv.TVR = Keys.setBits(Emv.TVR, 1, 5); //ICC data missing
            Emv.TVR = Keys.setBits(Emv.TVR, 1, 3); //CDA failed
        } else if (CAModulus.length() != issPubKCertificate.length()) {
            Emv.TVR = Keys.setBits(Emv.TVR, 1, 5); //ICC data missing
            Emv.TVR = Keys.setBits(Emv.TVR, 1, 3); //CDA failed
        } else {
            String recoveredData = Keys.rsaDecrypt(issPubKCertificate, CAModulus, issPubKExponent);
            boolean issuccess = retrieveIssuerPubKey(recoveredData, issPubKRemainder, issPubKExponent);
            if (!issuccess) {
                //TVR Bit 7 is for SDA, Bit 4 is for DDA, Bit 3 is for CDA
                Emv.TVR = Keys.setBits(Emv.TVR, 1, 3);
                Emv.UpdateValues();
            }
        }
    }

    public static boolean retrieveIssuerPubKey(String response, String issPubRemainder, String IssPubExponent) {
        String recoveredDataHeader = response.substring(0, 2);
        String certificateFormat = response.substring(2, 2 + 2);
        String issuerIdentifier = response.substring(4, 4 + 8);
        String certifExpiryDate = response.substring(12, 12 + 4);
        String certifSerialNumb = response.substring(16, 16 + 6);
        String hashAlgorithmIndic = response.substring(22, 22 + 2);
        String issPubKeyAlgoriInd = response.substring(24, 24 + 2);
        String issPubKeyLength = response.substring(26, 26 + 2);
        String issPubKeyExpLength = response.substring(28, 28 + 2);
        String recovDataTrailer = response.substring((response.length() - 2), (response.length() - 2) + 2);
        String hashResult = response.substring((response.length() - 42), (response.length() - 42) + 40);
        response = response.substring(0, response.length() - 42);
        String issPubKeyLeftmost = response.substring(30);
        if (!recoveredDataHeader.equals("6A")) return false;
        if (!certificateFormat.equals("02")) return false;
        if (!recovDataTrailer.equals("BC")) return false;
        if (!issPubKeyAlgoriInd.equals("01")) return false;
        String checkHash = certificateFormat + issuerIdentifier + certifExpiryDate + certifSerialNumb + hashAlgorithmIndic + issPubKeyAlgoriInd + issPubKeyLength + issPubKeyExpLength + issPubKeyLeftmost + issPubRemainder + IssPubExponent;
        checkHash = Keys.SHA1Encrypt(checkHash, hashAlgorithmIndic);
        if (!checkHash.equals(hashResult)) return false;
        String pan = Emv.getEmv("5A");
        if (!issuerIdentifier.substring(0, 6).equals(pan.substring(0, 6))) return false;
        int terminalMonth = DateTime.Now.Month();
        int terminalYear = DateTime.Now.Year();
        int certifMonth = Integer.parseInt(certifExpiryDate.substring(0, 2));
        int certifYear = Integer.parseInt(String.valueOf(terminalYear).substring(0, 2) + String.valueOf(certifExpiryDate).substring(2, 2 + 2));
        Date certifDate = DateTime.CustomDate(certifYear, certifMonth, 1, 00, 1, 1);
        Date terminalDate = DateTime.CustomDate(terminalYear, terminalMonth, 1, 00, 1, 1);
        int datecount = DateTime.Compare(certifDate, terminalDate);
        if (datecount <= 0) return false;
        String issPubKeyModulus = issPubKeyLeftmost + issPubRemainder;
        issPubKeyModulus = issPubKeyModulus.substring(0, Integer.parseInt(issPubKeyLength, 16) * 2);
        return retrieveICCPubKey(issPubKeyModulus);
    }

    public static boolean retrieveICCPubKey(String response) {
        //Retrieving ICC Public Key
        String modulus = response;
        String iccPubKeyCert = Emv.getEmv("9F46");
        String iccPubKExponent = Emv.getEmv("9F47");
        String iccPubKeyRem = Emv.getEmv("9F48");
        if (response.length() != iccPubKeyCert.length()) return false;
        if (iccPubKeyCert.length() != modulus.length()) return false;
        String isspubKeyExponent = Emv.getEmv("9F32");
        String recoveryData = Keys.rsaDecrypt(iccPubKeyCert, modulus, isspubKeyExponent);
        String recoveredDataHeader = recoveryData.substring(0, 2);
        String certificateFormat = recoveryData.substring(2, 2 + 2);
        String applicationPan = recoveryData.substring(4, 4 + 20);
        String certifExpiryDate = recoveryData.substring(24, 24 + 4);
        String certifSerialNumb = recoveryData.substring(28, 28 + 6);
        String hashAlgorithmIndic = recoveryData.substring(34, 34 + 2);
        String iccPubKeyAlgoriInd = recoveryData.substring(36, 36 + 2);
        String iccPubKeyLength = recoveryData.substring(38, 38 + 2);
        String iccPubKeyExpLength = recoveryData.substring(40, 40 + 2);
        String recovDataTrailer = recoveryData.substring((recoveryData.length() - 2), (recoveryData.length() - 2) + 2);
        String hashResult = recoveryData.substring((recoveryData.length() - 42), (recoveryData.length() - 42) + 40);
        recoveryData = recoveryData.substring(0, recoveryData.length() - 42);
        String iccPubKeyLeftmost = recoveryData.substring(42);
        if (!recoveredDataHeader.equals("6A")) return false;
        if (!certificateFormat.equals("04")) return false;
        if (!recovDataTrailer.equals("BC")) return false;
        if (!iccPubKeyAlgoriInd.equals("01")) return false;
        String staticDataAuthTagList = Emv.getEmv("9F4A");
        staticDataAuthTagList = Emv.getEmv(staticDataAuthTagList);
        String checkHash = certificateFormat + applicationPan + certifExpiryDate + certifSerialNumb + hashAlgorithmIndic + iccPubKeyAlgoriInd + iccPubKeyLength + iccPubKeyExpLength + iccPubKeyLeftmost + iccPubKeyRem + iccPubKExponent + Emv.staticDataToAuthenticate + staticDataAuthTagList;
        checkHash = Keys.SHA1Encrypt(checkHash, hashAlgorithmIndic);
        if (!checkHash.equals(hashResult)) return false;
        String pan = Emv.getEmv("5A");
        if (!applicationPan.substring(0, 10).equals(pan.substring(0, 10))) return false;
        int terminalMonth = DateTime.Now.Month();
        int terminalYear = DateTime.Now.Year();
        int certifMonth = Integer.parseInt(certifExpiryDate.substring(0, 2));
        int certifYear = Integer.parseInt(String.valueOf(terminalYear).substring(0, 2) + String.valueOf(certifExpiryDate).substring(2, 2 + 2));
        Date certifDate = DateTime.CustomDate(certifYear, certifMonth, 1, 00, 1, 1);
        Date terminalDate = DateTime.CustomDate(terminalYear, terminalMonth, 1, 00, 1, 1);
        int datecount = DateTime.Compare(certifDate, terminalDate);
        if (datecount <= 0) return false;
        String iccPubKeyModulus = iccPubKeyLeftmost + iccPubKeyRem;
        iccPubKeyModulus = iccPubKeyModulus.substring(0, Integer.parseInt(iccPubKeyLength, 16) * 2);
//        String ddolresp = Emv.getEmv("9F49");
//        Log.d("Result", "DDOL: " + ddolresp);
//        String ddol = Emv.formulateCDOL(ddolresp);
//        String resp = CardInfo.PerformCommand("00880000" + ddol + "00", false);
//        if (resp.startsWith("8081")) Emv.setEmv("9F4B", resp.substring(6));
//        else if (resp.startsWith("80")) Emv.setEmv("9F4B", resp.substring(4));
//        else if (resp.startsWith("77")) Emv.parseResponse(resp, "");
//        String staticDynamicAppData = Emv.getEmv("9F4B");
//        if (staticDynamicAppData.length() != iccPubKeyModulus.length()) return false;
        //String iccpubKeyExponent = Emv.getEmv("9F47");
        //String recoveredData = Keys.rsaDecrypt(staticDynamicAppData, iccPubKeyModulus, iccpubKeyExponent);
        Emv.setEmv("ICCPUBKEY", iccPubKeyModulus);
        Log.d("Result", "ICC public key was retrieved successfully");
        //if(!retrieveICCDynamicNumber(recoveredData)) return false;
        return true;
    }

//    public static boolean retrieveICCDynamicNumber(String response) {
//        String ICCDynamicDataLen = response.substring(6, 6 + 2);
//        if (response.substring(8).length() < (Integer.parseInt(ICCDynamicDataLen, 16) * 2))
//            return false;
//        String ICCDynamicData = response.substring(8, 8 + Integer.parseInt(ICCDynamicDataLen, 16) * 2);
//        response = response.substring(0, response.length() - 42);
//        if ((ICCDynamicData.length() + 8) > response.length()) return false;
//        String storedDynamicData = ICCDynamicData;
//        int dyNamicNumLen = Integer.parseInt(ICCDynamicData.substring(0, 2), 16) * 2;
//        String ICCDynamicNum = storedDynamicData.substring(2, 2 + dyNamicNumLen);
//        Emv.DefaultpdolCdol.add("9F4C|"+ICCDynamicNum);
//        return true;
//    }

    public static boolean doCombinedDynamicDataAuthentication(String pdol, String cdol, String genAcRespFormatData) {
        if (getCID().equals("AAC")) {
            setEmv("decline", "true");
            return false;
        }

        try{
            String checkHash = pdol.substring(6) + cdol.substring(2) + genAcRespFormatData;
            String transDataHashCode = Keys.SHA1Encrypt(checkHash, "01");
            String signedDynamicAppData = getEmv("9F4B");
            String iccPubKeyModulus = getEmv("ICCPUBKEY");
            if (signedDynamicAppData.length() != iccPubKeyModulus.length()) return false;
            String recoveryData = Keys.rsaDecrypt(signedDynamicAppData, iccPubKeyModulus, "03");
            String recoveredDataHeader = recoveryData.substring(0, 2);
            String signedDataFormat = recoveryData.substring(2, 2 + 2);
            String hashAlgorithmIndic = recoveryData.substring(4, 4 + 2);
            String ICCDynamicDataLen = recoveryData.substring(6, 6 + 2);
            if ((Integer.parseInt(ICCDynamicDataLen, 16) * 2) > recoveryData.length()) return false;
            String ICCDynamicData = recoveryData.substring(8, 8 + (Integer.parseInt(ICCDynamicDataLen, 16) * 2));
            String recovDataTrailer = recoveryData.substring((recoveryData.length() - 2), (recoveryData.length() - 2) + 2);
            String hashResult = recoveryData.substring((recoveryData.length() - 42), (recoveryData.length() - 42) + 40);
            recoveryData = recoveryData.substring(0, recoveryData.length() - 42);
            String padPattern = recoveryData.substring(ICCDynamicData.length() + 8);
            if (!recovDataTrailer.equals("BC")) return false;
            if (!recoveredDataHeader.equals("6A")) return false;
            if (!signedDataFormat.equals("05")) return false;
            int dyNamicNumLen = Integer.parseInt(ICCDynamicData.substring(0, 2), 16) * 2;
            String storedDynamicData = ICCDynamicData;
            String ICCDynamicNum = storedDynamicData.substring(2, 2 + dyNamicNumLen);
            storedDynamicData = storedDynamicData.substring(dyNamicNumLen + 2);
            String CrypTogramInfoData = storedDynamicData.substring(0, 2);
            String ApplicationCryptogram = storedDynamicData.substring(2, 2 + 16);
            String TransDataHashCode = storedDynamicData.substring(18, 18 + 40);

            if(Keys.checkIfBitIsSet(Emv.TVR, 5,2)){
                String TerminalRelayResistanceEntropyCDA = storedDynamicData.substring(58,58+8);
                double DeviceRelayResistanceEntropyCDA = Double.parseDouble(storedDynamicData.substring(66,66+8));
                double MinTimeForProcessingRelayResistanceAPDUCDA = Double.parseDouble(storedDynamicData.substring(74,74+4));
                double MaxTimeForProcessingRelayResistanceAPDUCDA = Double.parseDouble(storedDynamicData.substring(78,78+4));
                double DeviceEstimatedTransmissionTimeForRelayResistanceRAPDUCDA = Double.parseDouble(storedDynamicData.substring(82,82+4));
                if(!TerminalRelayResistanceEntropyCDA.equals(Emv.getEmv("9F37"))) return false;
                if(DeviceRelayResistanceEntropyCDA != deviceRelayResistanceEntropy) return false;
                if(MinTimeForProcessingRelayResistanceAPDUCDA != minTimeForProcessingRelayResistanceAPDU) return false;
                if(MaxTimeForProcessingRelayResistanceAPDUCDA != maxTimeForProcessingRelayResistanceAPDU) return false;
                if(DeviceEstimatedTransmissionTimeForRelayResistanceRAPDUCDA != deviceEstimatedTransmissionTimeForRelayResistanceAPDU) return false;
            }
            String cid = getEmv("9F27");
            if (!CrypTogramInfoData.equals(cid)) return false;
            String unpredictableNum = getEmv("9F37");
            String checkHash2 = signedDataFormat + hashAlgorithmIndic + ICCDynamicDataLen + ICCDynamicData + padPattern + unpredictableNum;
            checkHash2 = Keys.SHA1Encrypt(checkHash2, hashAlgorithmIndic);
            if (!checkHash2.equals(hashResult)) return false;
            if (!TransDataHashCode.equals(transDataHashCode)) return false;
            setEmv("9F4C", ICCDynamicNum);
            setEmv("9F26", ApplicationCryptogram);
            UpdateValues();
            Log.d("Result", "CDA was successful.");
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void CompleteTransaction(){
        // No second gen ac for mastercard
    }
}
