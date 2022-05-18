package com.sdk.pocketmonisdk.TransEnvironment;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import Utils.DateTime;
import Utils.Emv;
import Utils.IsoCreator;
import Utils.Keys;
import Utils.Net;

public class Nibss {
    private static String stan = "";
    private static String mti = "";
    private static String dateTime = "";

    private static FragmentActivity activity;
    public static String processTransaction(FragmentActivity a){
        activity = a;
        String response = "";
        switch (Emv.transactionType){
            case CASHOUT:
            case TRANSFER:
            case ELECTRICITY:
            case PURCHASE:
                response = purchase();
                break;
            case WITHDRAWAL:
                response = null;
                break;
        }
        return response;
    }

    //PURCHASE TO NIBSS
    public static String purchase()
    {
        String track2 = Emv.getTrack2();
        String dateExpiration = track2.substring(track2.indexOf("D") + 1, (track2.indexOf("D")+1)+4);
        String serviceRestrictionCode = track2.substring(track2.indexOf("D")+5, (track2.indexOf("D")+5)+3);
        stan = Emv.getStan(activity.getApplicationContext());
        Emv.transactionStan = stan;
        dateTime = DateTime.Now.ToString("MMddHHmmss");
        Emv.transactionDate = formatDate(dateTime);
        Emv.transactionTime = formatTime(dateTime);
        mti = "0200";

        IsoCreator param = new IsoCreator();
        param.Field[0] = mti;
        param.Field[2] = Emv.getPan();
        param.Field[3] = Emv.processingCode;
        param.Field[4] = Emv.getEmv("9F02"); //Amount authorized
        param.Field[7] = dateTime;
        param.Field[11] = Keys.padLeft(stan,6,'0');
        param.Field[12] = dateTime.substring(4);
        param.Field[13] = dateTime.substring(0, 4);
        param.Field[14] = dateExpiration;
        param.Field[18] = Emv.mcc;
        param.Field[22] = Emv.posEntryMode;
        param.Field[23] = Keys.padLeft(Emv.getEmv("5F34"),3,'0'); //Card sequence number
        param.Field[25] = "00"; //Pos Condition Code
        param.Field[26] = "06"; //Pos pin capture code
        param.Field[28] = "C00000000";
        param.Field[35] = Emv.getTrack2();
        param.Field[37] = Keys.genNibssRRN(activity,stan); //Retrieval reference number
        param.Field[40] = serviceRestrictionCode;
        param.Field[41] = Emv.terminalId;
        param.Field[42] = Emv.merchantId;
        param.Field[43] = Emv.merchantLocation;
        param.Field[49] = String.valueOf(Integer.parseInt(Emv.getEmv("9F1A"))); // currency code
        param.Field[52] = (Emv.getPinBlock().equals("")) ? " " : Emv.getNibssPinblock();

        String icc = "";

        String cryptogram = Emv.getEmv("9F26");
        icc += "9F26" + Keys.padLeft(Integer.toHexString(cryptogram.length() / 2),2,'0') + cryptogram;

        String cid = Emv.getEmv("9F27");
        icc += "9F27" + Keys.padLeft(Integer.toHexString(cid.length() / 2),2,'0') + cid;

        String iad = Emv.getEmv("9F10");
        icc += "9F10" + Keys.padLeft(Integer.toHexString(iad.length() / 2),2,'0') + iad;

        String unpridictable = Emv.getEmv("9F37");
        icc += "9F37" + Keys.padLeft(Integer.toHexString(unpridictable.length() / 2),2,'0') + unpridictable;

        String atc = Emv.getEmv("9F36");
        icc += "9F36" + Keys.padLeft(Integer.toHexString(atc.length() / 2),2,'0') + atc;

        String tvr = Emv.getEmv("95");
        icc += "95" + Keys.padLeft(Integer.toHexString(tvr.length() / 2),2,'0') + tvr;

        String transactionDate = Emv.getEmv("9A");
        icc += "9A" + Keys.padLeft(Integer.toHexString(transactionDate.length() / 2),2,'0') + transactionDate;

        String transactionType = Emv.getEmv("9C");
        icc += "9C" + Keys.padLeft(Integer.toHexString(transactionType.length() / 2),2,'0') + transactionType;

        String amountAuthorized = Emv.getEmv("9F02");
        icc += "9F02" + Keys.padLeft(Integer.toHexString(amountAuthorized.length() / 2),2,'0') + amountAuthorized;

        String transCurrencyCode = Emv.getEmv("5F2A");
        icc += "5F2A" + Keys.padLeft(Integer.toHexString(transCurrencyCode.length() / 2),2,'0') + transCurrencyCode;

        String aip = Emv.getEmv("82");
        icc += "82" + Keys.padLeft(Integer.toHexString(aip.length() / 2),2,'0') + aip;

        String transCountryCode = Emv.getEmv("9F1A");
        icc += "9F1A" + Keys.padLeft(Integer.toHexString(transCountryCode.length() / 2),2,'0') + transCountryCode;

        String cvmResult = Emv.getEmv("9F34");
        icc += "9F34" + Keys.padLeft(Integer.toHexString(cvmResult.length() / 2),2,'0') + cvmResult;

        String terminalCapability = Emv.getEmv("9F33");
        icc += "9F33" + Keys.padLeft(Integer.toHexString(terminalCapability.length() / 2),2,'0') + terminalCapability;

        String termianlType = Emv.getEmv("9F35");
        icc += "9F35" + Keys.padLeft(Integer.toHexString(termianlType.length() / 2),2,'0') + termianlType;

        String amountOther = Emv.getEmv("9F03");
        icc += "9F03" + Keys.padLeft(Integer.toHexString(amountOther.length() / 2),2,'0') + amountOther;

        param.Field[55] = icc;
        param.Field[123] = Emv.posDataCode;
        param.Field[128] = "";
        String packedhex = param.getPackedISO(32);
        Log.d("Result", "Request hex: " + packedhex);
        String response = Net.sslTcpClient(packedhex);
        Log.d("Result", "Response hex: " + response);
        //Initiate reversal if response code is empty
        String respCode = Keys.parseISO(response,"39");
        if(respCode.isEmpty()){
            if(response.length() > 0){
                reversal();
                return "12001";
            }
            return reversal();
        }
        else if(respCode.equals("06")) return reversal();
        return response;
    }

    //REVERSAL TO NIBSS
    public static String reversal()
    {
        String track2 = Emv.getTrack2();
        String dateExpiration = track2.substring(track2.indexOf("D") + 1, (track2.indexOf("D")+1)+4);
        String serviceRestrictionCode = track2.substring(track2.indexOf("D")+5, (track2.indexOf("D")+5)+3);

        IsoCreator param = new IsoCreator();
        param.Field[0] = "0420";
        param.Field[2] = Emv.getPan();
        param.Field[3] = Emv.processingCode;
        param.Field[4] = Emv.getEmv("9F02"); //Amount authorized
        param.Field[7] = dateTime;
        param.Field[11] = Keys.padLeft(stan,6,'0');
        param.Field[12] = dateTime.substring(4);
        param.Field[13] = dateTime.substring(0, 4);
        param.Field[14] = dateExpiration;
        param.Field[18] = Emv.mcc;
        param.Field[22] = Emv.posEntryMode;
        param.Field[23] = Keys.padLeft(Emv.getEmv("5F34"),3,'0'); //Card sequence number
        param.Field[25] = "00"; //Pos Condition Code
        param.Field[26] = "06"; //Pos pin capture code
        param.Field[28] = "C00000000";
        param.Field[35] = Emv.getTrack2();
        param.Field[37] = Keys.genNibssRRN(activity,stan);  //Retrieval refernce number
        param.Field[40] = serviceRestrictionCode;
        param.Field[41] = Emv.terminalId;
        param.Field[42] = Emv.merchantId;
        param.Field[43] = Emv.merchantLocation;
        param.Field[49] = String.valueOf(Integer.parseInt(Emv.getEmv("9F1A"))); // currency code
        param.Field[56] = "4000"; //040 is the length
        param.Field[90] = mti + stan + dateTime + "0000011112900000111129";
        param.Field[95] = param.Field[4] + param.Field[4] + "C00000000C00000000";
        param.Field[52] = (Emv.getPinBlock().equals("")) ? " " : Emv.getNibssPinblock();
        param.Field[123] = Emv.posDataCode;
        param.Field[128] = "";
        String packedhex = param.getPackedISO(32);
        Log.d("Result", "Reversal Request hex: " + packedhex);
        String transResp = Net.sslTcpClient(packedhex);
        Log.d("Result", "Reversal Response hex: " + transResp);
        String respCode = Keys.parseISO(transResp, "39");
        if(respCode.isEmpty()){
            transResp = reversalRepeat();
            respCode = Keys.parseISO(transResp, "39");
        }
        if(respCode.equals("00"))
            return "REVERSAL_OK";
        else
            return "REVERSAL_FAIL";
    }

    //REVERSAL TO NIBSS
    private static String reversalRepeat()
    {
        String track2 = Emv.getTrack2();
        String dateExpiration = track2.substring(track2.indexOf("D") + 1, (track2.indexOf("D")+1)+4);
        String serviceRestrictionCode = track2.substring(track2.indexOf("D")+5, (track2.indexOf("D")+5)+3);

        IsoCreator param = new IsoCreator();
        param.Field[0] = "0421";
        param.Field[2] = Emv.getPan();
        param.Field[3] = Emv.processingCode;
        param.Field[4] = Emv.getEmv("9F02"); //Amount authorized
        param.Field[7] = dateTime;
        param.Field[11] = Keys.padLeft(stan,6,'0');
        param.Field[12] = dateTime.substring(4);
        param.Field[13] = dateTime.substring(0, 4);
        param.Field[14] = dateExpiration;
        param.Field[18] = Emv.mcc;
        param.Field[22] = Emv.posEntryMode;
        param.Field[23] = Keys.padLeft(Emv.getEmv("5F34"),3,'0'); //Card sequence number
        param.Field[25] = "00"; //Pos Condition Code
        param.Field[26] = "06"; //Pos pin capture code
        param.Field[28] = "C00000000";
        param.Field[35] = Emv.getTrack2();
        param.Field[37] = Keys.genNibssRRN(activity,stan);  //Retrieval refernce number
        param.Field[40] = serviceRestrictionCode;
        param.Field[41] = Emv.terminalId;
        param.Field[42] = Emv.merchantId;
        param.Field[43] = Emv.merchantLocation;
        param.Field[49] = String.valueOf(Integer.parseInt(Emv.getEmv("9F1A"))); // currency code
        param.Field[56] = "4000"; //040 is the length
        param.Field[90] = mti + stan + dateTime + "0000011112900000111129";
        param.Field[95] = param.Field[4] + param.Field[4] + "C00000000C00000000";
        param.Field[52] = (Emv.getPinBlock().equals("")) ? " " : Emv.getNibssPinblock();
        param.Field[123] = Emv.posDataCode;
        param.Field[128] = "";
        String packedhex = param.getPackedISO(32);
        Log.d("Result", "Repeat Request hex: " + packedhex);
        String transResp = Net.sslTcpClient(packedhex);
        Log.d("Result", "Repeat Response hex: " + transResp);
        return transResp;
    }

    private static String formatDate(String datetime){
        return  DateTime.Now.Year() +
                "-" + datetime.substring(0,2) +
                "-" + datetime.substring(2,4);
    }

    private static String formatTime(String datetime){
        return  datetime.substring(4,6) +
                ":" + datetime.substring(6,8) +
                ":" + datetime.substring(8,10);
    }
}
