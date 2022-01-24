package Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.content.Context.BATTERY_SERVICE;

public class Emv {

    protected String tag;
    protected String value;

    public static final String REPRINTKEY = "reprint_data";
    public static final String PREFERENCEKEY = "MY_KEY";
    private static PosHandler posHandlerListener = null;
    public static List<Fragment> fragments;
    public static String AmountAuthorized = "9F02|000000000000"; //6 bytes
    public static String AmountOther = "9F03|000000000000"; //6 bytes
    public static String TransactionType = "9C|00"; //1 byte
    private static String TransactionDate = "9A|200619"; //3 bytes
    private static String UnpredictableNumber = "9F37|79BE08B2"; //4 bytes
    private static String TerminalCompatibilityIndicator = "9F52|01"; //1 bytes
    private static String CardVerificationResult = "9F34|000000"; //3 bytes
    private static String DataAuthCode = "9F45|0000"; //2 bytes
    protected static String ICCDynamicNumber = "9F4C|0000000000000000"; //8 bytes
    private static String TransactionTime = "9F21|041300"; //3 bytes
    private static String TerminalRiskManagementData = "9F1D|6C00800000000000"; //8 bytes
    public static String TSI = "9B|0000"; //2 bytes
    public static String TVR = "95|0000000000"; //5 bytes
    public static String staticDataToAuthenticate = "";
    public static String terminalPin = "1234";
    public static String pinType = "dukpt";
    public static String posEntryMode = "051";
    public static String serialNumber = "78452223";
    public static TransType transactionType = TransType.PURCHASE;



    private static String TacDefault; //5 bytes
    private static String TacDenial; //5 bytes
    private static String TACOnline; //5 bytes
    public static String TerminalappVersionNumber;
    private static int TerminalFloorLimit;
    public static int ContactlessFloorLimit;
    private static int Threshold;
    public static boolean pinbypass;
    public static String CurrencyLabel;
    public static String TerminalCapability; //2 bytes
    public static String TerminalTransQualifiers; //4 bytes
    private static String TerminalRiskManagement; //8 bytes
    private static String ExtendedTerminalCapability; //5 bytes - Contactless Card
    private static String ExtendedTerminalCapability2; //5 bytes - Contact Card
    public static String TerminalType; //1 byte -  22 for pax  34 for verifone
    public static String TerminalCountryCode; //2 bytes
    public static String TerminalCurrencyCode; //2 bytes
    public static String posGeoCode;
    public static String posDataCode;
    public static String merchantLocation;
    public static String ContactlessCVMLRequiredLimit;
    public static String readerContactlessTransactionLimit;
    public static String transUrl;
    public static String acctValidationUrl;
    public static String electricityValidationUrl;
    public static String electricityCardValidationUrl;
    public static String cableTvValidationUrl;
    public static String requeryUrl;
    public static String electricityCategoryUrl;
    public static String cableTvCategoryUrl;
    public static String airtimeCategoryURl;
    public static String notificationURL;
    public static String synchronizationURL;
    public static String upgradeURL;
    public static String accessTokenURL;
    public static String routeConfigURL;
    public static String transferCashUrl;
    public static String electricityCashUrl;
    public static String cableTVCashUrl;
    public static String airtimeCashUrl;
    public static String keyDownloadUrl = "https://kimono.interswitchng.com/kmw/keydownloadservice";
    public static String keysetid;
    public static String terminalId;
    public static String merchantId;
    public static String mcc;
    public static String nibssKey;
    public static String transip;
    public static String transport;
    public static String accountType = "Default";
    public static String processingCode = "000000";
    public static String deviceLocation = "0.0,0.0";
    public static boolean skipPinEntry;
    public static String pinKey;
    public static String sessionKey;
    public static String environment;
    public static String agentId;
    public static String agentLoc;
    public static String agentName;
    public static String transactionDate;
    public static String transactionTime;
    public static String responseCode;
    public static String transactionStan;
    public static String responseMessage;
    public static String accessToken;

    //private static readonly String Iksn = "000002DDDDE00000";
    //private static readonly String IpeK = "3F2216D8297BCE9C"; //Interswitch Nigeria
    //private static readonly String Iksn = "000002DDDDE00000";
    //private static readonly String IpeK = "33707E4927C4A0D5"; //Interswitch kenya
    //private static readonly String Iksn = "0000000006DDDDE00000";
    //private static readonly String IpeK = "9F8011E7E71E483B"; //Interswitch test environment


    //Emv tag 87 Application priority indicator

    protected static List<Emv> tags = null;
    private static List<Emv> Defaultemvs = null;
    protected static List<String> DefaultpdolCdol = null;
    public static List<AidClass> StoredAids = null;

    public static String parseResponse(String response, String skipTag) {
        // Tags added to this list will be read with full lenght
        int count = 0;
        String val = "";
        String readTagsAsSingle[] = {"9F4B", "9F20", "9F26", "9F0D", "9F36", "9F47", "9F48", "9F49", "9F42", "5F30", "9F08", "8C", "8D", "84", "50", "82", "9F17", "8F", "9F0E", "9F0F", "9F4A", "5F20", "9F1F", "9F5A", "9F5B", "4F", "5A", "8E", "9F38", "94", "9F4F", "90", "93", "72", "9F46", "57", "92", "9F32", "5F2F", "5F3F", "5F24", "5F34", "9F1A", "5F2D", "9F11", "9F12", "9F14", "9F23", "9F4D", "87", "86", "9D", "9F27", "9F10", "9F66", "5F50", "9F6E", "42", "5F56", "9F29", "9F2A", "9F0A", "9F6B", "9F69", "9F6C", "9F62", "56", "5F25", "5F28", "9F52", "DF4B", "9F60", "9F61", "9F5C", "DF8301", "DF4F", "9F44", "9F07", "9F3B", "9F43"};
        //List<string> issuerScriptTag = new List<string>() { "93", "9F46", "90"};
        String result = response.replace(" ", "") + Keys.padRight("", 1000, '#');
        for (int i = 0; i < response.length(); i += 2) {
            if (result.startsWith("#", 2)) {
                break;
            }
            String shortTag = result.substring(0, 2);
            String longTag = result.substring(0, 4);
            int skip = 2;
            for (Emv emv : Defaultemvs) {
                if (emv.tag.equals(shortTag) || emv.tag.equals(longTag)) {
                    int length = Integer.parseInt(result.substring(emv.tag.length(), (emv.tag.length() + 2)), 16);
                    if (length == 129) {
                        result = result.substring(emv.tag.length() + 2);
                        result = emv.tag + result;
                        length = Integer.parseInt(result.substring(emv.tag.length(), (emv.tag.length() + 2)), 16);
                    }
                    String finalStr = (length * 2 <= result.substring(4).length()) ? result.substring((emv.tag.length() + 2), ((emv.tag.length() + 2) + (length * 2))) : "";

                    if (!finalStr.isEmpty()) {
                        count++;
                        //if (count == 1) result = result.Substring(2);
                        if (count == 1) result = result.substring(emv.tag.length());
                        for (String exceptionTag : readTagsAsSingle) {
                            if (emv.tag.equals(exceptionTag)) {
                                skip = (length * 2) + (emv.tag.length() + 2);
                                break;
                            }
                        }
                        if (emv.tag.equals(skipTag)) {
                            setEmv("9F4B", finalStr);
                            break;
                        }
                        if (!finalStr.contains("#")) {
                            Log.d("Result", emv.tag + "\t" + emv.value + "\t" + Keys.padLeft(Integer.toHexString(length).toUpperCase(), 2, '0') + "\t" + finalStr);
                            if (count != 1)
                                val += emv.tag + Keys.padLeft(Integer.toHexString(length).toUpperCase(), 2, '0') + finalStr;
                            setEmv(emv.tag, finalStr);
                        }
                    }
                    break;
                }
            }
            result = result.substring(skip).toUpperCase();
        }
        return val;
    }

    public static String trimResponse(String response) {
        String result = response.replace(" ", "");
        for (int i = 0; i < response.length(); i += 2) {
            String shortTag = (result.length() > 0) ? result.substring(0, 2) : "";
            String longTag = (result.length() > 3) ? result.substring(0, 4) : "";
            for (Emv emv : Defaultemvs) {
                if (emv.tag.equals(shortTag) || emv.tag.equals(longTag)) {
                    if ((result.length() / 2) < 2) break;
                    int length = Integer.parseInt(result.substring(emv.tag.length(), emv.tag.length() + 2), 16);
                    if (result.length() == response.length())
                        result = result.substring(0, (length * 2) + (emv.tag.length() + 2));
                    return result;
                }
            }
            if (shortTag.isEmpty()) break;
            result = result.substring(2);
        }
        return response;
    }

    public static String doOfflinePlainTextPinVerif(String pin) {
        String pintryCount = Emv.getEmv("9F17");
        Log.d("Result", String.valueOf(Integer.parseInt(pintryCount, 16)));
        String len = Keys.padLeft(Integer.toHexString(pin.length()), 1, '0');
        pin = Keys.padRight(pin, 14, 'F');
        String resp = CardInfo.PerformCommand("00 20 00 80 08 2" + (len + pin), false);
        if (!resp.equals("9000")) return resp;
        TSI = Keys.setBits(TSI, 1, 7);
        UpdateValues();
        Log.d("Result", "Offline plaintext pin verified successfully");
        return resp;
    }

    public static void doEnterPinLogic(Activity activity, String pinValue){
        if(activity instanceof PosHandler){
            posHandlerListener = (PosHandler) activity;
        }
        Emv.terminalPin = pinValue;
        String value = "";
        if ((Emv.pinbypass == true) && (pinValue.isEmpty()))
        {
            if (Emv.cvmResult.length() == 4) Emv.setEmv("9F34", Emv.cvmResult + "00");
            TVR = Keys.setBits(Emv.TVR, 3, 4);
            doOnlineRequest(activity);
            return;
        }else if (Emv.offlineType.equals("enciphered"))
            value = Emv.doOfflineEncipheredPinVerif(Emv.terminalPin);
        else if (Emv.offlineType.equals("plain"))
            value = Emv.doOfflinePlainTextPinVerif(Emv.terminalPin);
        else if (Emv.offlineType.equals("online"))
        {
            if (Emv.cvmResult.length() == 4) Emv.setEmv("9F34", Emv.cvmResult + "00");
            Emv.doOnlineEncipheredPin(activity);
            Emv.TVR = Keys.setBits(Emv.TVR, 3, 3);
            doOnlineRequest(activity);
            return;
        }

        if (!value.equals("9000"))
        {
            if (Emv.cvmResult.length() == 4) Emv.setEmv("9F34", Emv.cvmResult + "02");
            CardInfo.PerformCommand("80CA9F1700", true);
            String pintryCount = Emv.getEmv("9F17");
            if ((Integer.parseInt(pintryCount, 16) < 1) || value.equals("6983") || value.equals("6984"))
            {
                if (Emv.cvmResult.length() == 4) Emv.setEmv("9F34", "420300");
                Emv.doOnlineEncipheredPin(activity);
                Emv.TVR = Keys.setBits(Emv.TVR, 3, 6); //Pin try limit exceeded
                Emv.TVR = Keys.setBits(Emv.TVR, 3, 3);
                doOnlineRequest(activity);
                return;
            }
            posHandlerListener.onPinVerificationResult(false,Integer.parseInt(pintryCount, 16) + " PIN TRIAL REMAINING");
            Log.d( "Result", "PIN TRY COUNT " + Integer.parseInt(pintryCount, 16));
            //if (Emv.cvmResult.length() == 4) Emv.setEmv("9F34", Emv.cvmResult + "01");
            //Emv.TVR = Keys.setBits(Emv.TVR, 3, 8);
            return;
        };
        if (Emv.cvmResult.length() == 4) Emv.setEmv("9F34", Emv.cvmResult + "02");
        Emv.TSI = Keys.setBits(Emv.TSI, 1, 7); //Card holder verification was successful
        posHandlerListener.onPinVerificationResult(true, "Card holder verification was successful");
        doOnlineRequest(activity);
    }
    
    static void doOnlineRequest(Activity activity){
        //There is no need to call first gen ac for contactless. It has already
        //been called in the contactless class.
        if(posEntryMode.equals("071")){
            posHandlerListener.onCVMProcessFinished(activity);
        }else{
            String result = CardInfo.doFirstGenAc();
            if (result.equals("COMPLETED")) {
                posHandlerListener.onCVMProcessFinished(activity);
            } else {
                activity.runOnUiThread(()->{
                    Toast.makeText(activity, "CARD DECLINED", Toast.LENGTH_SHORT).show();
                    CardInfo.StopTransaction(activity);
                });
            }
        }
    }

    public static String doOfflineEncipheredPinVerif(String pin) {
        String pintryCount = Emv.getEmv("9F17");
        Log.d("Result", String.valueOf(Integer.parseInt(pintryCount, 16)));
        String iccpubkey = getEmv("ICCPUBKEY");
        String iccpubKeyExponent = Emv.getEmv("9F47");
        if(iccpubkey.isEmpty()) return "";
        String unpredictable = CardInfo.PerformCommand("00 84 00 00 00", false); //Get challenge command
        String len = Keys.padLeft(Integer.toHexString(pin.length()), 1, '0');
        pin = Keys.padRight(pin, 14, 'F');
        String pinblock = "2" + (len + pin);
        if (unpredictable.length() < 8) return "";
        String ranPadding = Keys.padRight("", iccpubkey.length() - 34, 'B');
        String data = "7F" + pinblock + unpredictable + ranPadding;
        if (data.length() != iccpubkey.length()) return "";
        String pinData = Keys.rsaDecrypt(data, iccpubkey, iccpubKeyExponent);
        String resp = CardInfo.PerformCommand("00 20 00 88 " + Keys.padLeft(Integer.toHexString(pinData.length() / 2), 2, '0') + pinData, false);
        if (!resp.equals("9000")) return resp;
        TSI = Keys.setBits(TSI, 1, 7);
        UpdateValues();
        Log.d("Result", "Offline Enciphered pin verified successfully");
        return resp;
    }

    public static void doOnlineEncipheredPin(Context context) {
        long pan = Long.parseLong(getEmv("5A").replace("F", ""));
        if (pinType.equals("dukpt")) {
            ksn = Keys.ksnIncrement(context, SharedPref.get(context, "Iksn", "0000000006DDDDE00000"));
            String workingKey = Keys.getSessionKey(SharedPref.get(context, "Ipek", "9F8011E7E71E483B"), ksn);
            pinblock = Keys.desEncryptDukpt(workingKey, String.valueOf(pan), terminalPin);
            ksn = Keys.generateTransKsn(ksn);
            Log.d("Result", "PINBLOCK " + pinblock + "  KSN: " + ksn);
        }
    }

    public static boolean startCVMProcessing(Activity activity) {
        String cvmIsSupported = Emv.appInterchangeProfile(5, 1); //5 means check if cvm is supported
        if (cvmIsSupported.equals("96")) return true;
        if (cvmIsSupported.equals("CVM")) {
            if (Emv.posEntryMode.equals("051")) {
                doCVMProcess(activity);
            }
        } else {
            Emv.setEmv("9F34", "3F0000");
            doOnlineRequest(activity);
            return true;
        }
        return true;
    }

    public static String cvmResult = "";
    public static void doCVMProcess(Activity activity) {
        String cvmresult = Emv.getEmv("8E");
        if (cvmresult.isEmpty()) return;
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
            if (byte1.equals("00")) return; //Fail cvm processing

            if ((byte1.equals("01")) && (Keys.checkIfBitIsSet(TerminalCapability, 2, 8) == true)) {
                doOncvmProcessingResult(activity, "OFFLINE PLAIN");
                //EventClass.OnCVMProcessEvent("OFFLINE PLAIN");
                return;
            } else if ((byte1.equals("02")) && (Keys.checkIfBitIsSet(TerminalCapability, 2, 7))) {
                doOncvmProcessingResult(activity, "ONLINE");
                //EventClass.OnCVMProcessEvent("ONLINE");
                return;
            } else if ((byte1.equals("1E")) && (Keys.checkIfBitIsSet(TerminalCapability, 2, 6) == true)) {
                doOncvmProcessingResult(activity, "SIGNATURE");
                //EventClass.OnCVMProcessEvent("SIGNATURE");
                return;
            } else if ((byte1.equals("04")) && (Keys.checkIfBitIsSet(TerminalCapability, 2, 5) == true)) {
                String pubKey = getEmv("ICCPUBKEY");
                if(pubKey.isEmpty() && byte3.equals("01")) continue;
                doOncvmProcessingResult(activity, "OFFLINE ENCHIPHERED");
                //EventClass.OnCVMProcessEvent("OFFLINE ENCHIPHERED");
                return;
            } else if ((byte1.equals("1F")) && (Keys.checkIfBitIsSet(TerminalCapability, 2, 4) == true)) {
                doOncvmProcessingResult(activity, "NO CVM");
                //EventClass.OnCVMProcessEvent("NO CVM");
                return;
            } else {
                //Emv.TVR = Keys.setBits(Emv.TVR, 3, 7);
                //Main.enterpinCard.doOncvmProcessingResult("UNKNOWN");
                //EventClass.OnCVMProcessEvent("UNKNOWN");
                continue;
            }
        }
        doOncvmProcessingResult(activity, "UNKNOWN");
        return;
    }


    public static String offlineType = "";
    //Call this event based on the offline processing
    public static void doOncvmProcessingResult(Activity activity, String sender) {

        switch (sender) {
            case "OFFLINE ENCHIPHERED":
                offlineType = "enciphered";
                DisplayEnterPin(activity);
                break;
            case "OFFLINE PLAIN":
                offlineType = "plain";
                DisplayEnterPin(activity);
                break;
            case "ONLINE":
                offlineType = "online";
                DisplayEnterPin(activity);
                break;
            case "PIN BYPASSED":
                if (cvmResult.length() == 4) Emv.setEmv("9F34", cvmResult + "00");
                Emv.TSI = Keys.setBits(Emv.TSI, 1, 7); //Card holder verification was successful signature
                doOnlineRequest(activity);
                break;
            case "UNKNOWN":
                if (cvmResult.length() == 4) Emv.setEmv("9F34", "3F0001");
                Emv.TSI = Keys.setBits(Emv.TSI, 1, 7); //Card holder verification was successful
                doOnlineRequest(activity);
                break;
            case "SIGNATURE":
            case "NO CVM":
            default:
                doOnlineRequest(activity);
                break;
        }
    }

    private static void DisplayEnterPin(Activity activity) {
        posHandlerListener.onEnterPinRequested(activity);
    }

    public static String getCID() {
        String cid = getEmv("9F27");
        if (cid.equals("00")) return "AAC";
        else if (cid.equals("40")) return "TC";
        else if (cid.equals("80")) return "ARQC";
        else if (cid.equals("C0")) return "AAR";
        else return "";
    }

    public static String appInterchangeProfile(int bit, int bytes) {
        try {
            String aip = Emv.getEmv("82");
            if (aip.isEmpty()) {
                return "96";
            }
            String[] data1 = {"MAG", "SDA", "DDA", "CVM", "TRM", "IAUTH", "ONDEVICE", "CDA"};
            String[] data2 = {"EMVMODE", "", "", "", "", "", "", "RRP"};
            if (bytes == 1) {
                String resp = Keys.hexStringToBinary(aip.substring(0,2));
                final String aidBit = resp.substring(8 - bit, ((8 - bit) + 1));
                System.out.println(aidBit);
                if ((aidBit.equals("1"))) return data1[8 - bit];
            } else if(bytes == 2){
                String resp = Keys.hexStringToBinary(aip.substring(2,4));
                final String aidBit = resp.substring(8 - bit, ((8 - bit) + 1));
                if ((aidBit.equals("1"))) return data2[8 - bit];
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static String getEmv(String emvtag) {
        for (Emv emv : tags) {
            if (emv.tag.toUpperCase().equals(emvtag.toUpperCase())) {
                return emv.value;
            }
        }
        return "";
    }

    public static void setEmv(String emvtag, String value) {
        Emv emv = new Emv();
        emv.tag = emvtag;
        emv.value = value;
        for (Emv ev : tags) {
            if (ev.tag.equals(emvtag)) {
                ev.value = value;
                return;
            }
        }
        tags.add(emv);
    }

    public static String parseProcessingOption(String response) {
        if (response.isEmpty()) {
            Log.d("Result", "Card not valid");
            return "";
        }
        //800E7900080101001001050018010201
        if (response.substring(0, 2).equals("80")) {
            String aip = "8202" + response.substring(4, (4 + 4)); //Application Interchange Profile (2 byte).
            String afl = ""; //Applicaiton file locator (var).
            if (response.length() > 8) {
                afl = "94" + Keys.padLeft(Integer.toHexString(response.substring(8).length() / 2), 2, '0') + response.substring(8);
            }
            String finalStr = "80" + Keys.padLeft(Integer.toHexString((aip + afl).length() / 2), 2, '0') + (aip + afl);
            parseResponse(finalStr, "");
            return finalStr;
        }
        return response;
    }

    public static String formulatePDOL(String response) {
        String result = response.replace(" ", "");
        String resultval = "";
        for (int i = 0; i < response.length(); i += 2) {
            String shortTag = (result.length() > 0) ? result.substring(0, 2) : "";
            String longTag = (result.length() > 3) ? result.substring(0, 4) : "";
            for (Emv emv : Defaultemvs) {
                if (emv.tag.equals(shortTag) || emv.tag.equals(longTag)) {
                    String val = "";
                    int length = Integer.parseInt(result.substring(emv.tag.length(), (emv.tag.length() + 2)), 16);
                    val = Keys.padRight("", length * 2, '0');

                    for (String p : DefaultpdolCdol) {
                        val = (p.split("\\|")[0].equals(emv.tag)) ? ((p.split("\\|")[1].length() < length) ? val : p.split("\\|")[1]) : val;
                    }
                    Log.d("Result", "PDOL/CDOL: " + emv.tag + "\t" + emv.value + "\t" + length + "\t" + val);
                    resultval += val;
                    break;
                }
            }
            if (shortTag.isEmpty()) break;
            result = result.substring(2);
        }
        return Keys.padLeft(Integer.toHexString((resultval.length() + 4) / 2), 2, '0') + "83" + Keys.padLeft(Integer.toHexString(resultval.length() / 2), 2, '0') + resultval;
    }

    public static List<String> getAPDURecordsCommands(String sfivalue) {
        //080101001001050018010201
        List<String> commandList = new ArrayList<String>();
        for (int i = 0; i < sfivalue.length(); i += 8) {
            // string sfitoBinary = Convert.ToString(Convert.ToInt32(sfivalue.Substring(i, 2).Substring(0, 2), 16), 2).ToString();


            String sfitoBinary = Keys.hexStringToBinary(sfivalue.substring(i, (i + 2)));
            sfitoBinary = sfitoBinary.substring(0, sfitoBinary.length() - 3);
            int sfilocation = Integer.parseInt(sfitoBinary, 2);
            String val = Keys.hexStringToBinary(sfivalue.substring((i + 2), (i + 2) + 2)); //new BigInteger(String.valueOf(Integer.parseInt(sfivalue.substring((i + 2), (i + 2)+2), 16)), 2).toString();
            int recStart = Integer.parseInt(val, 2);
            val = Keys.hexStringToBinary(sfivalue.substring((i + 4), (i + 4) + 2));
            int recEnd = Integer.parseInt(val, 2);
            val = Keys.hexStringToBinary(sfivalue.substring((i + 6), (i + 6) + 2));
            int recFour = Integer.parseInt(val, 2); //The fourth record
            Log.d("SFI ", sfilocation + " RECORD: " + recStart + " - " + recEnd);
            Log.d("SFI OFFLINE ", sfilocation + " RECORDS: " + recFour);
            boolean canchange = true;
            for (int u = recStart; u <= recEnd; u++) {
                if ((recFour > 0) && (canchange == true)) {
                    canchange = false;
                    doOfflineCommand(recFour, sfilocation, u);
                } else {
                    commandList.add("00 B2 " + Keys.padLeft(Integer.toHexString(u), 2, '0') + " " + Keys.padLeft(Integer.toHexString(((sfilocation - 1) * 8) + 12), 2, '0') + " 00");
                }
            }
        }
        return commandList;
    }

    public static void doOfflineCommand(int recFour, int sfilocation, int u) {
        String result = "";
        if (recFour > 10) {
            result = CardInfo.PerformCommand("00 B2 " + Keys.padLeft(Integer.toHexString(u), 2, '0') + " " + Keys.padLeft(Integer.toHexString(((sfilocation - 1) * 8) + 12), 2, '0') + " 00", true);
            staticDataToAuthenticate += result;
        } else {
            result = CardInfo.PerformCommand("00 B2 " + Keys.padLeft(Integer.toHexString(u), 2, '0') + " " + Keys.padLeft(Integer.toHexString(((sfilocation - 1) * 8) + 12), 2, '0') + " 00", false);
            if (result.startsWith("7081"))
                staticDataToAuthenticate += result.substring(6);
            else staticDataToAuthenticate += result.substring(4);
            parseResponse(result, ""); // Passing 4 will enable it skip the lenght of tag 70
        }
    }

    public static void retrieveCAPublicKeys(Context context, String aid, String keyIndex) {
        try {
            InputStream filePath = context.getResources().openRawResource(context.getResources().getIdentifier("@raw/emv", null, context.getPackageName()));
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(filePath);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getDocumentElement().getElementsByTagName("emvcard");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList nKey = nNode.getChildNodes().item(7).getChildNodes();
                    for (int temp2 = 1; temp2 < nKey.getLength(); temp2 += 2) {
                        String name = nNode.getChildNodes().item(1).getTextContent();
                        String aids = nNode.getChildNodes().item(3).getTextContent();
                        String keyidx = nKey.item(temp2).getChildNodes().item(1).getTextContent();
                        String expdate = nKey.item(temp2).getChildNodes().item(3).getTextContent();
                        String modulus = nKey.item(temp2).getChildNodes().item(5).getTextContent();
                        String exponent = nKey.item(temp2).getChildNodes().item(7).getTextContent();
                        String checksum = nKey.item(temp2).getChildNodes().item(9).getTextContent();
                        if (aid.contains(aids) && (keyIndex.equals(keyidx))) {
                            setEmv("aid", aids);
                            setEmv("keyidx", keyidx);
                            setEmv("name", name);
                            setEmv("expdate", expdate);
                            setEmv("modulus", modulus);
                            setEmv("exponent", exponent);
                            setEmv("checksum", checksum);
                            return;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void doProcessingRestriction() {
        setEmv("9F02", AmountAuthorized.split("\\|")[1]);
        setEmv("9F03", AmountOther.split("\\|")[1]);
        setEmv("5F2A", TerminalCurrencyCode.split("\\|")[1]);
        setEmv("9F1A", TerminalCountryCode.split("\\|")[1]);
        setEmv("9C", TransactionType.split("\\|")[1]);
        setEmv("9F35", TerminalType.split("\\|")[1]);

        String appVersionNum = getEmv("9F09");
        if (!appVersionNum.isEmpty()) {
            if (!appVersionNum.equals(TerminalappVersionNumber)) {
                TVR = Keys.setBits(TVR, 2, 8);
            }
        }

        if ((Double.parseDouble(AmountAuthorized.split("\\|")[1]) > (TerminalFloorLimit / 100))) {
            TVR = Keys.setBits(TVR, 4, 8);
        }

        if ((Double.parseDouble(AmountAuthorized.split("\\|")[1]) / 100) > (Threshold / 100)) {
            TVR = Keys.setBits(TVR, 4, 8);
        }

        String appEffectiveDate = getEmv("5F25");
        if (!appEffectiveDate.isEmpty()) {
            int terminalMonth = DateTime.Now.Month();
            int terminalYear = DateTime.Now.Year();
            int terminalDay = DateTime.Now.Day();
            int year = Integer.parseInt(String.valueOf(terminalYear).substring(0, 2) + appEffectiveDate.substring(0, 2));
            int month = Integer.parseInt(appEffectiveDate.substring(2, 2 + 2));
            int day = Integer.parseInt(appEffectiveDate.substring(4, 4 + 2));
            Date appeffectiveDate = DateTime.CustomDate(year, month, day, 00, 1, 1);
            Date terminalDate = DateTime.CustomDate(terminalYear, terminalMonth, terminalDay, 00, 1, 1);
            int datecount = DateTime.Compare(terminalDate, appeffectiveDate);
            if (datecount <= 0) {
                TVR = Keys.setBits(TVR, 2, 6);
            }
        }

        String appExpiryDate = getEmv("5F24");
        if (!appExpiryDate.isEmpty()) {
            int terminalMonth = DateTime.Now.Month();
            int terminalYear = DateTime.Now.Year();
            int terminalDay = DateTime.Now.Day();
            int year = Integer.parseInt(String.valueOf(terminalYear).substring(0, 2) + appExpiryDate.substring(0, 2));
            int month = Integer.parseInt(appExpiryDate.substring(2, 2 + 2));
            int day = Integer.parseInt(appExpiryDate.substring(4, 4 + 2));
            Date appexpirydate = DateTime.CustomDate(year, month, day);
            Date terminalDate = DateTime.CustomDate(terminalYear, terminalMonth, terminalDay);
            int datecount = DateTime.Compare(appexpirydate, terminalDate);
            if (datecount <= 0) {
                TVR = Keys.setBits(TVR, 2, 7);
            }
        }
        UpdateValues();
    }

    public static String formulateCDOL(String response) {
        setEmv("9F02", AmountAuthorized.split("\\|")[1]);
        setEmv("9F03", AmountOther.split("\\|")[1]);
        setEmv("5F2A", TerminalCurrencyCode.split("\\|")[1]);
        setEmv("9F1A", TerminalCountryCode.split("\\|")[1]);
        setEmv("9C", TransactionType.split("\\|")[1]);
        setEmv("9F35", TerminalType.split("\\|")[1]);

        String transdate = DateTime.Now.ToString("yMMdd");
        setEmv("9A", transdate);

        String transtime = DateTime.Now.ToString("HHmmss");
        setEmv("9F21", transtime);

        String result = response.replace(" ", "");
        String resultval = "";
        for (int i = 0; i < response.length(); i += 2) {
            String shortTag = (result.length() > 0) ? result.substring(0, 2) : "";
            String longTag = (result.length() > 3) ? result.substring(0, 4) : "";
            for (Emv emv : Defaultemvs) {
                if (emv.tag.equals(shortTag) || emv.tag.equals(longTag)) {
                    String val = "";
                    int length = Integer.parseInt(result.substring(emv.tag.length(), emv.tag.length() + 2), 16);
                    val = Keys.padLeft("", length * 2, '0');

                    for (Emv p : tags) {
                        val = (p.tag.equals(emv.tag)) ? ((p.value.length() < (length * 2)) ? Keys.padLeft(p.value, (length * 2), '0') : p.value.substring(0, length * 2)) : val;
                    }

                    Log.d("Result", "PDOL/CDOL: " + emv.tag + "\t" + emv.value + "\t" + length + "\t" + val);
                    resultval += val;
                    break;
                }
            }
            if (shortTag.isEmpty()) break;
            result = result.substring(2);
        }
        return Keys.padLeft(Integer.toHexString((resultval.length()) / 2), 2, '0') + resultval;
    }

    public static void doOfflineDataAuthentication(Context context) {
        //appInterchangeProfile(1) = CDA, appInterchangeProfile(6) = DDA,  appInterchangeProfile(7) = SDA
        String cardSecurityType = (!appInterchangeProfile(6, 1).isEmpty()) ? appInterchangeProfile(6, 1) : appInterchangeProfile(7, 1);

        String aid = getEmv("4F");
        String issPubKCertificate = getEmv("90");
        String issPubKRemainder = getEmv("92");
        String issPubKExponent = getEmv("9F32");
        String issPubKeyIndex = getEmv("8F");
        retrieveCAPublicKeys(context, aid, issPubKeyIndex);
        String CAModulus = getEmv("modulus");
        if (CAModulus.isEmpty()) {
            if(cardSecurityType.equals("SDA")) TVR = Keys.setBits(TVR, 1, 7);
            else if(cardSecurityType.equals("DDA")) TVR = Keys.setBits(TVR, 1, 4);
            else TVR = Keys.setBits(TVR, 1, 7);
        } else if (CAModulus.length() != issPubKCertificate.length()) {
            if(cardSecurityType.equals("SDA")) TVR = Keys.setBits(TVR, 1, 7);
            else if(cardSecurityType.equals("DDA")) TVR = Keys.setBits(TVR, 1, 4);
            else TVR = Keys.setBits(TVR, 1, 7);
        } else {
            if (cardSecurityType.equals("SDA")) TVR = Keys.setBits(TVR, 1, 2);
            String recoveredData = Keys.rsaDecrypt(issPubKCertificate, CAModulus, issPubKExponent);
            boolean issuccess = retrieveIssuerPubKey(recoveredData, issPubKRemainder, issPubKExponent, cardSecurityType);
            if (!issuccess) {
                //TVR Bit 7 is for SDA, Bit 4 is for DDA, Bit 3 is for CDA
                int bit = (cardSecurityType.equals("DDA")) ? 4 : 7;
                TVR = Keys.setBits(TVR, 1, bit);
                UpdateValues();
            }
        }
    }

    public static void UpdateValues() {
        setEmv("9B", TSI.split("\\|")[1]);
        setEmv("95", TVR.split("\\|")[1]);
    }

    public static boolean retrieveIssuerPubKey(String response, String issPubRemainder, String IssPubExponent, String verificationType) {
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
        String pan = getEmv("5A");
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
        if (verificationType.equals("SDA")) {
            if (!verifySDA(issPubKeyModulus)) return false;
        } else if (verificationType.equals("DDA")) {
            if (!retrieveICCPubKey(issPubKeyModulus)) return false;
        }
        return true;
    }

    public static boolean verifySDA(String issPubKeyModulus) {
        String signedStaticAppData = getEmv("93");
        if (issPubKeyModulus.length() != signedStaticAppData.length()) return false;
        String isspubKeyExponent = Emv.getEmv("9F32");
        String response = Keys.rsaDecrypt(signedStaticAppData, issPubKeyModulus, isspubKeyExponent);
        String recoveredDataHeader = response.substring(0, 2);
        String signedDataFormat = response.substring(2, 2 + 2);
        String hashAlgorithmIndic = response.substring(4, 4 + 2);
        String dataAuthCode = response.substring(6, 6 + 4);
        String recovDataTrailer = response.substring((response.length() - 2), (response.length() - 2) + 2);
        String hashResult = response.substring((response.length() - 42), (response.length() - 42) + 40);
        response = response.substring(0, response.length() - 42);
        String padPattern = response.substring(10);
        if (!recovDataTrailer.equals("BC")) return false;
        if (!recoveredDataHeader.equals("6A")) return false;
        if (!signedDataFormat.equals("03")) return false;
        String staticDataAuthTagList = getEmv("9F4A");
        staticDataAuthTagList = getEmv(staticDataAuthTagList);
        String checkHash = signedDataFormat + hashAlgorithmIndic + dataAuthCode + padPattern + staticDataToAuthenticate + staticDataAuthTagList;
        checkHash = Keys.SHA1Encrypt(checkHash, hashAlgorithmIndic);
        if (!checkHash.equals(hashResult)) return false;
        setEmv("9F45", dataAuthCode);
        TSI = Keys.setBits(TSI, 1, 8);
        UpdateValues();
        Log.d("Result", "SDA was successfully performed");
        return true;
    }

    public static boolean retrieveICCPubKey(String response) {
        //Retrieving ICC Public Key
        String modulus = response;
        String iccPubKeyCert = getEmv("9F46");
        String iccPubKExponent = getEmv("9F47");
        String iccPubKeyRem = getEmv("9F48");
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
        String staticDataAuthTagList = getEmv("9F4A");
        staticDataAuthTagList = getEmv(staticDataAuthTagList);
        String checkHash = certificateFormat + applicationPan + certifExpiryDate + certifSerialNumb + hashAlgorithmIndic + iccPubKeyAlgoriInd + iccPubKeyLength + iccPubKeyExpLength + iccPubKeyLeftmost + iccPubKeyRem + iccPubKExponent + staticDataToAuthenticate + staticDataAuthTagList;
        checkHash = Keys.SHA1Encrypt(checkHash, hashAlgorithmIndic);
        if (!checkHash.equals(hashResult)) return false;
        String pan = getEmv("5A");
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
        String ddolresp = Emv.getEmv("9F49");
        Log.d("Result", "DDOL: " + ddolresp);
        String ddol = formulateCDOL(ddolresp);
        String resp = CardInfo.PerformCommand("00880000" + ddol + "00", false);
        if (resp.substring(0, 4).equals("8081")) setEmv("9F4B", resp.substring(6));
        else if (resp.substring(0, 2).equals("80")) setEmv("9F4B", resp.substring(4));
        else if (resp.substring(0, 2).equals("77")) parseResponse(resp, "");
        String staticDynamicAppData = getEmv("9F4B");
        if (staticDynamicAppData.length() != iccPubKeyModulus.length()) return false;
        String iccpubKeyExponent = Emv.getEmv("9F47");
        String recoveredData = Keys.rsaDecrypt(staticDynamicAppData, iccPubKeyModulus, iccpubKeyExponent);
        setEmv("ICCPUBKEY", iccPubKeyModulus);
        if (!verifyDDA(recoveredData, ddol)) return false;
        return true;
    }

    public static boolean verifyDDA(String response, String ddol) {
        String recoveredDataHeader = response.substring(0, 2);
        String signedDataFormat = response.substring(2, 2 + 2);
        String hashAlgorithmIndic = response.substring(4, 4 + 2);
        String ICCDynamicDataLen = response.substring(6, 6 + 2);
        if (response.substring(8).length() < (Integer.parseInt(ICCDynamicDataLen, 16) * 2))
            return false;
        String ICCDynamicData = response.substring(8, 8 + Integer.parseInt(ICCDynamicDataLen, 16) * 2);
        String recovDataTrailer = response.substring((response.length() - 2), (response.length() - 2) + 2);
        String hashResult = response.substring((response.length() - 42), (response.length() - 42) + 40);
        response = response.substring(0, response.length() - 42);
        if ((ICCDynamicData.length() + 8) > response.length()) return false;
        String padPattern = response.substring(ICCDynamicData.length() + 8);
        if (!recovDataTrailer.equals("BC")) return false;
        if (!recoveredDataHeader.equals("6A")) return false;
        if (!signedDataFormat.equals("05")) return false;
        String checkHash = signedDataFormat + hashAlgorithmIndic + ICCDynamicDataLen + ICCDynamicData + padPattern + ddol.substring(2);
        checkHash = Keys.SHA1Encrypt(checkHash, hashAlgorithmIndic);
        if (!checkHash.equals(hashResult)) return false;
        setEmv("9F4C", Keys.padRight(ICCDynamicData, 18, '0').substring(2, 2 + 16));
        DefaultpdolCdol.add(ICCDynamicNumber);
        TSI = Keys.setBits(TSI, 1, 8);
        UpdateValues();
        Log.d("Result", "DDA was successfully performed");
        return true;
    }

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

    public static void doTerminalRiskManagement() {
        if ((Double.parseDouble(AmountAuthorized.split("\\|")[1])/100) > (TerminalFloorLimit/100))
        {
            TVR = Keys.setBits(TVR, 4, 8);
        }
        String lowCensecutiveOfflineLimt = getEmv("9F14");
        String upperCensecutivOfflineLimt = getEmv("9F23");
        if (!lowCensecutiveOfflineLimt.isEmpty())
        {
            if(!upperCensecutivOfflineLimt.isEmpty())
            {
                CardInfo.PerformCommand("80CA9F3600",true); //Generate ATC
                CardInfo.PerformCommand("80CA9F1300",true); //Last online ATC Register
                String lastOnlineATCReg = getEmv("9F13");
                String appTransCounter = getEmv("9F36");
                if (((lastOnlineATCReg.isEmpty()) || (appTransCounter.isEmpty())) || (Integer.parseInt(appTransCounter, 16) <= Integer.parseInt(lastOnlineATCReg, 16)))
                {
                    TVR = Keys.setBits(TVR, 4, 7); //Lower consecutive offline limit exceeded
                    TVR = Keys.setBits(TVR, 4, 6); //Upper consecutive offline limit exceeded
                    if (Integer.parseInt(lastOnlineATCReg, 16) == 0)
                    {
                        TVR = Keys.setBits(TVR, 2, 4); //New Card
                    }
                    TSI = Keys.setBits(TSI, 1, 4);
                    UpdateValues();
                    return;
                }

                if(Integer.parseInt(lastOnlineATCReg, 16) > 0)
                {
                    int compareAtc = Integer.parseInt(appTransCounter, 16) - Integer.parseInt(lastOnlineATCReg, 16);
                    if (compareAtc > Integer.parseInt(lowCensecutiveOfflineLimt, 16))
                    {
                        TVR = Keys.setBits(TVR, 4, 7); //Lower consecutive offline limit exceeded
                    }
                    if (compareAtc > Integer.parseInt(upperCensecutivOfflineLimt, 16))
                    {
                        TVR = Keys.setBits(TVR, 4, 6); //Upper consecutive offline limit exceeded
                    }
                    if (Integer.parseInt(lastOnlineATCReg, 16) == 0)
                    {
                        TVR = Keys.setBits(TVR, 2, 4); //New Card
                    }
                }
            }
        }
        TSI = Keys.setBits(TSI, 1, 4);
        UpdateValues();
    }

    public static String parseFirstGenACResponse(String response) {
        //801280002FE9C2AA84C354219406011203A08000
        if (response.substring(0, 2).equals("80"))
        {
            String cid = "9F2701" + response.substring(4, 4+2); //Cryptogram information Data (1 byte).
            String atc = "9F3602" + response.substring(6, 6+4); //Application Transaction Counter (2 bytes).
            String ac = "9F2608" + response.substring(10, 10+16); //Issuer Application Data (8 bytes).
            String iad = ""; //Applicaiton interchange profile (var).
            if (response.length() > 26)
            {
                iad = "9F10" + Keys.padLeft(Integer.toHexString (response.substring(26).length() / 2),2,'0') + response.substring(26);
            }
            String finalStr = "77" + Keys.padLeft(Integer.toHexString ((cid + atc + ac + iad).length() / 2),2,'0') + (cid + atc + ac + iad);
            return parseResponse(finalStr,"");
        }
        else
        {
            return Emv.parseResponse(response, "9F4B");
        }
    }

    public static String doTerminalActionAnalysis()
    {
        TSI = Keys.setBits(TSI, 1, 6);
        UpdateValues();
        String terminalType = Emv.getEmv("9F35");
        String tacStatic = "FFFFFFFFFC";
        String iacOnline = getEmv("9F0F");
        String iacDefault = getEmv("9F0D");
        String iacDenial = getEmv("9F0E");
        String tvrResult = TVR.split("\\|")[1];

        if(iacDenial.isEmpty()){
            for (int i = 0; i < Keys.hexStringToBinary(TacDenial).length(); i++)
            {
                String tvr = Keys.hexStringToBinary(tvrResult).substring(i,i+1);
                String tacdenial = Keys.hexStringToBinary(TacDenial).substring(i,i+1);
                if ((tacdenial.equals("1")) && ((tvr.equals("1"))))
                {
                    return "00";
                }
            }
        }else{
            for (int i = 0; i < Keys.hexStringToBinary(TacDenial).length(); i++)
            {
                String tvr = Keys.hexStringToBinary(tvrResult).substring(i,i+1);
                String tacdenial = Keys.hexStringToBinary(TacDenial).substring(i,i+1);
                String iacdenial = Keys.hexStringToBinary(iacDenial).substring(i,i+1);

                if ((tacdenial.equals("1") || iacdenial.equals("1")) && (tvr.equals("1")))
                {
                    return "00";
                }
            }
        }
        //Online only terminal
        if(terminalType.equals("21")){
            return "80";
        }
        //Offline only terminal
        if(terminalType.equals("23")){
            if(iacDefault.isEmpty()){
                for (int i = 0; i < Keys.hexStringToBinary(TacDefault).length(); i++)
                {
                    String tvr = Keys.hexStringToBinary(tvrResult).substring(i,i+1);
                    String tacdefault = Keys.hexStringToBinary(TacDefault).substring(i, i+1);
                    String staticTac = Keys.hexStringToBinary(tacStatic).substring(i, i+1);
                    if ((tacdefault.equals("1") || staticTac.equals("1")) && (tvr.equals("1")))
                    {
                        return "00";
                    }
                }
            }else{
                for (int i = 0; i < Keys.hexStringToBinary(TacDefault).length(); i++)
                {
                    String tvr = Keys.hexStringToBinary(tvrResult).substring(i,i+1);
                    String tacdefault = Keys.hexStringToBinary(TacDefault).substring(i, i+1);
                    String iacdefault = Keys.hexStringToBinary(iacDefault).substring(i, i+1);
                    if ((tacdefault.equals("1") || iacdefault.equals("1")) && (tvr.equals("1")))
                    {
                        return "00";
                    }
                }
            }
        }else{
            if(iacOnline.isEmpty()){
                for (int i = 0; i < Keys.hexStringToBinary(TACOnline).length(); i++)
                {
                    String tvr = Keys.hexStringToBinary(tvrResult).substring(i,i+1);
                    String taconline = Keys.hexStringToBinary(TACOnline).substring(i, i+1);
                    String staticTac = Keys.hexStringToBinary(tacStatic).substring(i, i+1);
                    if ((taconline.equals("1") || staticTac.equals("1")) && (tvr.equals("1")))
                    {
                        return "80";
                    }
                }
            }else{
                for (int i = 0; i < Keys.hexStringToBinary(TACOnline).length(); i++)
                {
                    String tvr = Keys.hexStringToBinary(TVR.split("\\|")[1]).substring(i,i+1);
                    String taconline = Keys.hexStringToBinary(TACOnline).substring(i, i+1);
                    String iaconline = Keys.hexStringToBinary(iacOnline).substring(i, i+1);
                    if ((taconline.equals("1") || iaconline.equals("1")) && (tvr.equals("1")))
                    {
                        return "80";
                    }
                }
            }
        }
        return "40";
    }

    public static String getBatteryLevel(Context context){
        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            return String.valueOf(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        } else {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);
            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
            double batteryPct = level / (double) scale;
            return String.valueOf((int) (batteryPct * 100));
        }
    }

    public static Address getTerminalLocation(Context context, double latitude, double longitude){
        try {
            while(true){
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude,1);
                if(addresses.size() == 0) continue;
                return addresses.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAppVersion(Activity activity) {
        try {
            return activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (Exception ex) {
            return "0.0.0";
        }
    }

    public static void getDeviceLocation(Activity activity){
        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, 0);
        }
        while(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED);

        LocationManager locationManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        while(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));

        Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location locationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        double longitude = 0, latitude = 0;
        if(locationGps != null){
            longitude = locationGps.getLongitude();
            latitude = locationGps.getLatitude();
        }
        else if(locationNetwork != null){
            longitude = locationNetwork.getLongitude();
            latitude = locationNetwork.getLatitude();
        }
        else if(locationPassive != null){
            longitude = locationPassive.getLongitude();
            latitude = locationPassive.getLatitude();
        }

        //Request location from device
        boolean isLocationRequested = Boolean.parseBoolean(SharedPref.get(activity, "location_req", "false"));
        if((longitude == 0.0D) && (isLocationRequested == false)){
            SharedPref.set(activity, "location_req", "true");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("Result", "Request location change");
                    deviceLocation = location.getLatitude() + "," + location.getLongitude();
                    locationManager.removeUpdates(this);
                }
            });
        }

        Log.d("Result",latitude+","+longitude);
        deviceLocation = latitude + "," + longitude;
    }


    private static String stan = "";
    public static String getStan(Context context) {
        stan = String.valueOf(Integer.parseInt(SharedPref.get(context, "stan", "0")) + 1);
        SharedPref.set(context, "stan", stan);
        if(Integer.parseInt(stan) > 999998 ){
            SharedPref.set(context,"stan", "0");
        }
        return Keys.padLeft(stan, 6, '0');
    }

    public static String getMinorAmount(){
        String newAmt = Emv.AmountAuthorized.split("\\|")[1];
        String amtF = String.valueOf(Double.parseDouble(newAmt.replace(",", "")));
        DecimalFormat df = new DecimalFormat("###.#");
        String minorAmt = df.format(Double.parseDouble(amtF));
        return minorAmt;
    }

    public static String getPan() {
        String pan = Emv.getEmv("5A").replace("F", "");
        if (pan.isEmpty())
        {
            String track2 = getTrack2();
            pan = track2.substring(0, track2.indexOf("D")).replace("F", "");
        }
        return pan;
    }

    public static String getMaskedPan() {
        String pan = getPan();
        String maskedpan = pan.substring(0,6) + "************" + pan.substring(pan.length()-4);
        return maskedpan;
    }

    public static String getTransactionStan(){
        return Emv.transactionStan;
    }

    public static String getTransactionDatTime(){
        try{
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
            SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dt = from.parse(Emv.getTransactionDate() + Emv.getTransactionTime());
            return to.format(dt);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static String getTrack2() {
        String track2 = Emv.getEmv("57").replace("F", "");
        if (track2.isEmpty())
        {
            track2 = Emv.getEmv("9F6B").replace("F", "");
        }
        return track2;
    }

    private static String ksn = "";

    public static String getKsn() {
        return ksn.toUpperCase();
    }

    private static String pinblock = "";

    public static String getPinBlock() {
        return pinblock;
    }

    public static String getNibssPinblock() {
        String pan = Emv.getEmv("5A").replace("F", "");
        String pinblock = Keys.encryptPinBlock(pan, terminalPin);
        return Keys.trippleDesEncrypt(pinblock, pinKey);
    }

    public static String getTransactionDate(){
        return Emv.transactionDate;
    }

    public static String getTransactionTime(){
        return Emv.transactionTime;
    }

    public static void initializeEmv(Activity activity) {
        if(activity instanceof PosHandler){
            posHandlerListener = (PosHandler)activity;
        }
        tags = new ArrayList<Emv>();

        Defaultemvs = new ArrayList<Emv>();
        DefaultpdolCdol = new ArrayList<String>();
        StoredAids = new ArrayList<AidClass>();
        staticDataToAuthenticate = "";
        skipPinEntry = false;

        //Loading emv tags
        List<String> emvtaglist = new ArrayList<String>();
        emvtaglist.add("9F01|Acquirer Identifier");
        emvtaglist.add("9F40|Additional Terminal Capabilities");
        //emvtaglist.add("81|Amount, Authorised (Binary)");
        emvtaglist.add("9F02|Amount, Authorised (Numeric)");
        emvtaglist.add("9F5C|Cumulative Total Transaction Amount Upper Limit");
        emvtaglist.add("9F04|Amount, Other (Binary)");
        emvtaglist.add("9F03|Amount, Other (Numeric)");
        emvtaglist.add("9F5A|Application Program Identifier (Program ID)");
        emvtaglist.add("9F5B|Issuer Script Results");
        emvtaglist.add("9F3A|Amount, Reference Currency");
        emvtaglist.add("9F26|Application Cryptogram");
        emvtaglist.add("9F42|Application Currency Code");
        emvtaglist.add("9F44|Application Currency Exponent");
        emvtaglist.add("9F05|Application Discretionary Data");
        emvtaglist.add("5F25|Application Effective Date");
        emvtaglist.add("5F24|Application Expiration Date");
        emvtaglist.add("94|Application File Locator (AFL)");
        emvtaglist.add("4F|Application Identifier (AID) – card");
        emvtaglist.add("9F06|Application Identifier (AID) – terminal");
        emvtaglist.add("82|Application Interchange Profile");
        emvtaglist.add("50|Application Label");
        emvtaglist.add("9F12|Application Preferred Name");
        emvtaglist.add("5A|Application Primary Account Number (PAN)");
        emvtaglist.add("5F34|Application Primary Account Number (PAN) Sequence Number");
        emvtaglist.add("87|Application Priority Indicator");
        emvtaglist.add("9F3B|Application Reference Currency");
        emvtaglist.add("9F43|Application Reference Currency Exponent");
        emvtaglist.add("—|Application Selection Indicator");
        emvtaglist.add("61|Application Template");
        emvtaglist.add("9F36|Application Transaction Counter (ATC)");
        emvtaglist.add("9F07|Application Usage Control");
        emvtaglist.add("9F08|Application Version Number");
        emvtaglist.add("9F09|Application Version Number");
        emvtaglist.add("89|Authorisation Code");
        emvtaglist.add("8A|Authorisation Response Code");
        emvtaglist.add("—|Authorisation Response Cryptogram (ARPC)");
        emvtaglist.add("5F54|Bank Identifier Code (BIC)");
        emvtaglist.add("9F53|Dynamic Terminal Interchange Profile");
        emvtaglist.add("9F5C|Magstripe Data Object List (MDOL)");
        emvtaglist.add("8C|Card Risk Management Data Object List 1 (CDOL1)");
        emvtaglist.add("8D|Card Risk Management Data Object List 2 (CDOL2)");
        emvtaglist.add("—|Card Status Update (CSU)");
        emvtaglist.add("5F20|Cardholder Name");
        emvtaglist.add("9F0B|Cardholder Name Extended");
        emvtaglist.add("DF4F|JIS 2 Equivalent Data");
        emvtaglist.add("8E|Cardholder Verification Method (CVM) List");
        emvtaglist.add("9F34|Cardholder Verification Method (CVM) Results");
        emvtaglist.add("—|Certification Authority Public Key Check Sum");
        emvtaglist.add("—|Certification Authority Public Key Exponent");
        emvtaglist.add("8F|Certification Authority Public Key Index");
        emvtaglist.add("9F22|Certification Authority Public Key Index");
        emvtaglist.add("—|Certification Authority Public Key Modulus");
        emvtaglist.add("83|Command Template");
        emvtaglist.add("9F27|Cryptogram Information Data");
        emvtaglist.add("9F45|Data Authentication Code");
        emvtaglist.add("84|Dedicated File (DF) Name");
        emvtaglist.add("—|Default Dynamic Data Authentication Data Object List (DDOL)");
        emvtaglist.add("—|Default Transaction Certificate Data Object List (TDOL)");
        emvtaglist.add("9D|Directory Definition File (DDF) Name");
        emvtaglist.add("73|Directory Discretionary Template");
        emvtaglist.add("9F49|Dynamic Data Authentication Data Object List (DDOL)");
        emvtaglist.add("70|EMV Proprietary Template");
        emvtaglist.add("—|Enciphered Personal Identification Number (PIN) Data");
        emvtaglist.add("BF0C|File Control Information (FCI) Issuer Discretionary Data");
        emvtaglist.add("A5|File Control Information (FCI) Proprietary Template");
        emvtaglist.add("6F|File Control Information (FCI) Template");
        emvtaglist.add("9F4C|ICC Dynamic Number");
        emvtaglist.add("9F2D|Integrated Circuit Card (ICC) PIN Encipherment Public Key Certificate ");
        emvtaglist.add("9F2E|Integrated Circuit Card (ICC) PIN Encipherment Public Key Exponent ");
        emvtaglist.add("9F2F|Integrated Circuit Card (ICC) PIN Encipherment Public Key Remainder ");
        emvtaglist.add("9F46|Integrated Circuit Card (ICC) Public Key Certificate");
        emvtaglist.add("9F47|Integrated Circuit Card (ICC) Public Key Exponent");
        emvtaglist.add("9F48|Integrated Circuit Card (ICC) Public Key Remainder");
        emvtaglist.add("9F1E|Interface Device (IFD) Serial Number");
        emvtaglist.add("5F53|International Bank Account Number (IBAN)");
        emvtaglist.add("9F0A|EEA Product Identification");
        emvtaglist.add("9F0D|Issuer Action Code – Default");
        emvtaglist.add("9F0E|Issuer Action Code – Denial");
        emvtaglist.add("9F0F|Issuer Action Code – Online");
        emvtaglist.add("9F10|Issuer Application Data");
        emvtaglist.add("91|Issuer Authentication Data");
        emvtaglist.add("9F11|Issuer Code Table Index");
        emvtaglist.add("5F28|Issuer Country Code");
        emvtaglist.add("5F55|Issuer Country Code (alpha2 format)");
        emvtaglist.add("5F56|Issuer Country Code (alpha3 format)");
        emvtaglist.add("42|Issuer Identification Number (IIN)");
        emvtaglist.add("90|Issuer Public Key Certificate");
        emvtaglist.add("9F32|Issuer Public Key Exponent");
        emvtaglist.add("92|Issuer Public Key Remainder");
        emvtaglist.add("86|Issuer Script Command");
        emvtaglist.add("9F6A|Unpredictable Number (Numeric)");
        emvtaglist.add("9F6C|Card Transaction Qualifiers (CTQ)");
        emvtaglist.add("9F62|PCVC3 (Track1)");
        emvtaglist.add("9F18|Issuer Script Identifier");
        emvtaglist.add("—|Issuer Script Results");
        emvtaglist.add("71|Issuer Script Template 1");
        emvtaglist.add("72|Issuer Script Template 2");
        emvtaglist.add("5F50|Issuer URL");
        emvtaglist.add("5F2D|Language Preference");
        emvtaglist.add("9F13|Last Online Application Transaction Counter (ATC) Register");
        emvtaglist.add("9F4D|Log Entry");
        emvtaglist.add("9F4F|Log Format");
        emvtaglist.add("9F14|Lower Consecutive Offline Limit");
        emvtaglist.add("—|Maximum Target Percentage to be used for Biased Random Selection");
        emvtaglist.add("9F15|Merchant Category Code");
        emvtaglist.add("9F2A|Kernel Identifier");
        emvtaglist.add("9F29|Extended Selection");
        emvtaglist.add("9F16|Merchant Identifier");
        emvtaglist.add("9F4E|Merchant Name and Location");
        emvtaglist.add("DF4B|POS Cardholder Interaction Information");
        emvtaglist.add("—|Message Type");
        emvtaglist.add("—|Personal Identification Number (PIN) Pad Secret Key");
        emvtaglist.add("9F17|Personal Identification Number (PIN) Try Counter");
        emvtaglist.add("9F39|Point-of-Service (POS) Entry Mode");
        emvtaglist.add("9F38|Processing Options Data Object List (PDOL)");
        emvtaglist.add("—|Proprietary Authentication Data");
        emvtaglist.add("80|Response Message Template Format 1");
        emvtaglist.add("77|Response Message Template Format 2");
        emvtaglist.add("5F30|Service Code");
        emvtaglist.add("9F7E|Mobile Support Indicator");
        emvtaglist.add("88|Short File Identifier (SFI)");
        emvtaglist.add("9F4B|Signed Dynamic Application Data");
        emvtaglist.add("93|Signed Static Application Data");
        emvtaglist.add("9F4A|Static Data Authentication Tag List");
        emvtaglist.add("—|Target Percentage to be Used for Random Selection");
        emvtaglist.add("—|Terminal Action Code – Default");
        emvtaglist.add("—|Terminal Action Code – Denial");
        emvtaglist.add("—|Terminal Action Code – Online");
        emvtaglist.add("9F33|Terminal Capabilities");
        emvtaglist.add("9F69|UDOL");
        emvtaglist.add("9F6B|Track2 data for Magstripe");
        emvtaglist.add("9F61|CVC3 (Track2)");
        emvtaglist.add("9F60|CVC3 (Track1)");
        emvtaglist.add("9F1A|Terminal Country Code");
        emvtaglist.add("9F1B|Terminal Floor Limit");
        emvtaglist.add("9F1C|Terminal Identification");
        emvtaglist.add("9F1D|Terminal Risk Management Data");
        emvtaglist.add("9F35|Terminal Type");
        emvtaglist.add("95|Terminal Verification Results");
        emvtaglist.add("—|Threshold Value for Biased Random Selection");
        emvtaglist.add("9F1F|Track 1 Discretionary Data");
        emvtaglist.add("9F20|Track 2 Discretionary Data");
        emvtaglist.add("57|Track 2 Equivalent Data|37");
        emvtaglist.add("—|Transaction Amount");
        emvtaglist.add("98|Transaction Certificate (TC) Hash Value");
        emvtaglist.add("97|Transaction Certificate Data Object List (TDOL)");
        emvtaglist.add("5F2A|Transaction Currency Code");
        emvtaglist.add("5F36|Transaction Currency Exponent");
        emvtaglist.add("9A|Transaction Date");
        emvtaglist.add("99|Transaction Personal Identification Number (PIN) Data");
        emvtaglist.add("9F3C|Transaction Reference Currency Code");
        emvtaglist.add("—|Transaction Reference Currency Conversion");
        emvtaglist.add("9F3D|Transaction Reference Currency Exponent");
        emvtaglist.add("9F41|Transaction Sequence Counter");
        emvtaglist.add("9B|Transaction Status Information");
        emvtaglist.add("9F21|Transaction Time");
        emvtaglist.add("DF8301|Terminal Relay Resistance Entropy");
        emvtaglist.add("9C|Transaction Type");
        emvtaglist.add("9F37|Unpredictable Number");
        emvtaglist.add("9F23|Upper Consecutive Offline Limit");
        emvtaglist.add("9F7C|Unprotected Data Envelope");
        emvtaglist.add("C7|Extended Terminal Capability");
        emvtaglist.add("C5|Contactless Cryptogram Information Data");
        emvtaglist.add("9F52|Terminal Compatibility Indicator");
        emvtaglist.add("VLP Terminal Support Indicator|Unknown");
        emvtaglist.add("DF69|Unknown");
        emvtaglist.add("9F66|Terminal Transaction Qualifiers (TTQ)");
        emvtaglist.add("9F6E|FormFactorIndicator");
        emvtaglist.add("9F7A|VLP Terminal Support Indicator");
        emvtaglist.add("DF69|Unknown");
        emvtaglist.add("9F4D|Unknown");
        emvtaglist.add("DF4D|Unknown");
        emvtaglist.add("D1|Currency conversion table");
        emvtaglist.add("C2|Unknown");
        emvtaglist.add("56|Track 1 Data");
        emvtaglist.add("9F63|Offline Counter Initial Value");

        //Load configs from emv.xml
        try {
            serialNumber = Sdk.getSerialNo();
            TACOnline = SharedPref.get(activity, "taconline", "");
            TacDefault = SharedPref.get(activity, "tacdefault", "");
            TacDenial = SharedPref.get(activity, "tacdenial", "");
            TerminalFloorLimit = Integer.parseInt(SharedPref.get(activity, "floorlimit", "0"));
            ContactlessFloorLimit = Integer.parseInt(SharedPref.get(activity, "contactlessfloorlimit", "0"));
            TerminalCurrencyCode = SharedPref.get(activity, "currencycode", "5F2A|0566");
            TerminalCountryCode = SharedPref.get(activity, "countrycode", "9F1A|0566");
            CurrencyLabel = SharedPref.get(activity, "currencylabel", "NGN");
            terminalId = SharedPref.get(activity, "terminalid", "");
            merchantId = SharedPref.get(activity, "merchantid", "");
            ///Todo remember to change to production url
            String baseUrl = "https://demo.etranzact.com";
            //String baseUrl = "https://www.etranzact.net";
            //SharedPref.get(activity, "baseurl", "https://www.etranzact.net");
            transUrl = baseUrl + "/tms-service/tms/transact";
            acctValidationUrl = baseUrl + "/tms-service/tms/account-validation";
            electricityCategoryUrl = baseUrl + "/tms-service/tms/bill/electricity/getCategories?serialNo="+serialNumber+"&terminalId="+terminalId+"";
            cableTvCategoryUrl = baseUrl + "/tms-service/tms/bill/cable/getCategories?serialNo="+serialNumber+"&terminalId="+terminalId+"";
            airtimeCategoryURl = baseUrl + "/tms-service/tms/bill/airtime/billers?serialNo="+serialNumber+"&terminalId="+terminalId+"";
            electricityValidationUrl = baseUrl + "/tms-service/tms/bill/verify/cash";
            electricityCardValidationUrl = baseUrl + "/tms-service/tms/bill/verify/card";
            cableTvValidationUrl = baseUrl + "/tms-service/tms/bill/bouquet";
            notificationURL = baseUrl + "/tms-service/tms/notification";
            requeryUrl = baseUrl + "/tms-service/tms/requery";
            synchronizationURL = baseUrl + "/tms-service/tms/synchronization";
            upgradeURL = baseUrl + "/tms-service/tms/downloadFile";
            accessTokenURL = baseUrl + "/auth-server/oauth/token";
            routeConfigURL = baseUrl + "/tms-service/tms/route";
            transferCashUrl = baseUrl + "/tms-service/tms/cash_transfer";
            electricityCashUrl = baseUrl + "/tms-service/tms/bill/cash/payment";
            cableTVCashUrl = baseUrl + "/tms-service/tms/bill/cash/payment";
            airtimeCashUrl = baseUrl + "/tms-service/tms/bill/topup/payment";
            keysetid = SharedPref.get(activity, "keysetid", "");
            posDataCode = SharedPref.get(activity, "posdatacode", "");
            merchantLocation = SharedPref.get(activity, "merchantlocation", "");
            Threshold = Integer.parseInt(SharedPref.get(activity, "threshold", "0"));
            pinbypass = Boolean.parseBoolean(SharedPref.get(activity, "pinbypass", "false"));
            TerminalCapability = SharedPref.get(activity, "terminalcapability", "9F33|E0F8C8");
            TerminalRiskManagement = SharedPref.get(activity, "terminalriskmanagement", "");
            ExtendedTerminalCapability = SharedPref.get(activity, "extendedterminalcapability", "");
            ExtendedTerminalCapability2 = SharedPref.get(activity, "extendedterminalcapability", "");
            TerminalappVersionNumber = SharedPref.get(activity, "version", "");
            TerminalTransQualifiers = SharedPref.get(activity, "terminalTransQualifiers", "");
            TerminalType = SharedPref.get(activity, "terminaltype", "");
            agentId = SharedPref.get(activity, "agentId", "");
            agentLoc = SharedPref.get(activity, "agentLoc", "");
            agentName = SharedPref.get(activity, "agentName", "");
            ContactlessCVMLRequiredLimit = SharedPref.get(activity, "contactlessCVMLRequiredLimit", "");
            readerContactlessTransactionLimit = SharedPref.get(activity, "readerContactlessTransactionLimit", "");
            posGeoCode = "0" + TerminalCountryCode.split("\\|")[1] + "00000000" + TerminalCurrencyCode.split("\\|")[1];
            mcc = SharedPref.get(activity, "mcc", "");
            transip = SharedPref.get(activity, "transIp", "");
            transport = SharedPref.get(activity, "transPort", "");
            nibssKey = SharedPref.get(activity, "nibssKey", "");
            environment = SharedPref.get(activity, "environment", "");
            accessToken = SharedPref.get(activity, "accesstoken", "");
            sessionKey = SharedPref.get(activity, "sessionkey", "");
            pinKey = SharedPref.get(activity, "pinkey", "");


            //Loading PDOL list (All items that may be required for PDOL, must be added here)
            DefaultpdolCdol.add(AmountAuthorized);
            DefaultpdolCdol.add(TerminalCountryCode);
            DefaultpdolCdol.add(TerminalCurrencyCode);
            DefaultpdolCdol.add(TerminalCapability);
            DefaultpdolCdol.add(ExtendedTerminalCapability);
            DefaultpdolCdol.add(TerminalRiskManagement);
            DefaultpdolCdol.add(TerminalType);
            DefaultpdolCdol.add(ExtendedTerminalCapability2);
            DefaultpdolCdol.add(TerminalTransQualifiers);
            DefaultpdolCdol.add(AmountOther);
            DefaultpdolCdol.add(TVR);
            DefaultpdolCdol.add(TerminalCompatibilityIndicator);
            DefaultpdolCdol.add(TransactionType);
            DefaultpdolCdol.add(TransactionDate);
            DefaultpdolCdol.add(UnpredictableNumber);
            DefaultpdolCdol.add(CardVerificationResult);
            DefaultpdolCdol.add(DataAuthCode);
            DefaultpdolCdol.add(TransactionTime);
            DefaultpdolCdol.add(TerminalRiskManagementData);

            //Get AIDS from emv.xml
            StoredAids = Keys.getStoredAid(activity);

            //Generate Unpredicatable number to use for the runtime;
            String unpredictNum = Keys.genUnpredictableNum();
            DefaultpdolCdol.add("9F37|" + unpredictNum);
            DefaultpdolCdol.add("9F6A|" + unpredictNum);
            DefaultpdolCdol.add("DF8301|" + unpredictNum);
            setEmv("DF8301", unpredictNum);
            setEmv("9F37", unpredictNum);
            setEmv("9F6A", unpredictNum);
            setEmv("9F33", TerminalCapability.split("\\|")[1]);
            TSI = "9B|0000";
            TVR = "95|0000000000";
            posEntryMode = "051";
            pinblock = "";
            ksn = "";
            terminalPin = "";

            for (String readtag : emvtaglist) {
                String[] TagVal = readtag.split("\\|");
                Emv emv = new Emv();
                emv.tag = TagVal[0];
                emv.value = TagVal[1];
                Defaultemvs.add(emv);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }


    }
}
