package com.etranzact.pocketmoni.TransEnvironment;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.Model.TransferModel;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import Utils.DateTime;
import Utils.Emv;
import Utils.HttpRequest;
import Utils.Keys;
import Utils.Net;
import Utils.Sdk;

public class TMS {

    private static FragmentActivity activity;
    public static String processTransaction(FragmentActivity a){
        activity = a;
        String response = "";
        switch (Emv.transactionType){
            case PURCHASE:
                response = purchase();
                break;
            case ELECTRICITY:
                response = payELectric();
                break;
            case TRANSFER:
                response = transferCashout();
                break;
            case CASHOUT:
                response = cashout();
                break;
            case WITHDRAWAL:
                response = null;
                break;
        }
        return response;
    }

    //ELECTRICITY CARD PAYMENT
    public static String payELectric() {
        String transDate = DateTime.Now.ToString("yyyy-MM-dd+HH:mm:ss").replace("+", "T");
        Emv.transactionDate = formatDate(transDate);
        Emv.transactionTime = formatTime(transDate);
        originalTransDate = transDate;
        String track2 = Emv.getTrack2();
        String pan = Emv.getPan();
        String year = track2.substring(track2.indexOf("D") + 1, (track2.indexOf("D")+1)+2);
        String month = track2.substring(track2.indexOf("D") + 3, (track2.indexOf("D")+3)+2);
        Emv.transactionStan = Emv.getStan(activity.getApplicationContext());

        String data =
                "<transferRequest>\n" +
                        "    <terminalInformation>\n" +
                        "        <batteryInformation>"+ Emv.getBatteryLevel(activity.getApplicationContext())+"</batteryInformation>\n" +
                        "        <currencyCode>" + Integer.parseInt(Emv.TerminalCurrencyCode.split("\\|")[1]) + "</currencyCode>\n" +
                        "        <languageInfo>EN</languageInfo>\n" +
                        "        <merchantId>" + Emv.merchantId + "</merchantId>\n" +
                        "        <merhcantLocation>" + Emv.merchantLocation + "</merhcantLocation>\n" +
                        "        <posConditionCode>00</posConditionCode>\n" +
                        "        <posDataCode>" + Emv.posDataCode + "</posDataCode>\n" +
                        "        <posEntryMode>" + Emv.posEntryMode + "</posEntryMode>\n" +
                        "        <posGeoCode>" + Emv.posGeoCode + "</posGeoCode>\n" +
                        "        <printerStatus>"+ Sdk.getPrinterState()+"</printerStatus>\n" +
                        "        <terminalId>" + Emv.terminalId + "</terminalId>\n" +
                        "        <terminalType>22</terminalType>\n" +
                        "        <transmissionDate></transmissionDate>\n" +
                        "		 <requestDate>" + transDate + "</requestDate>\n" +
                        "        <uniqueId>" + Emv.serialNumber + "</uniqueId>\n" +
                        "    </terminalInformation>\n" +
                        "    <cardData>\n" +
                        "        <cardSequenceNumber>" + Emv.getEmv("5F34") + "</cardSequenceNumber>\n" +
                        "        <emvData>\n" +
                        "           <AmountAuthorized>" + Emv.getEmv("9F02") + "</AmountAuthorized>\n" +
                        "            <AmountOther>" + Emv.getEmv("9F03") + "</AmountOther>\n" +
                        "            <ApplicationInterchangeProfile>" + Emv.getEmv("82") + "</ApplicationInterchangeProfile>\n" +
                        "            <atc>" + Emv.getEmv("9F36") + "</atc>\n" +
                        "            <Cryptogram>" + Emv.getEmv("9F26") + "</Cryptogram>\n" +
                        "            <CryptogramInformationData>" + Emv.getEmv("9F27") + "</CryptogramInformationData>\n" +
                        "            <CvmResults>" + Emv.getEmv("9F34") + "</CvmResults>\n" +
                        "            <iad>" + Emv.getEmv("9F10") + "</iad>\n" +
                        "            <TransactionCurrencyCode>" + Integer.parseInt(Emv.getEmv("5F2A")) + "</TransactionCurrencyCode>\n" +
                        "            <TerminalVerificationResult>" + Emv.getEmv("95") + "</TerminalVerificationResult>\n" +
                        "            <TerminalCountryCode>" + Integer.parseInt(Emv.getEmv("9F1A")) + "</TerminalCountryCode>\n" +
                        "            <TerminalType>" + Emv.getEmv("9F35") + "</TerminalType>\n" +
                        "            <TerminalCapabilities>" + Emv.getEmv("9F33") + "</TerminalCapabilities>\n" +
                        "            <TransactionDate>" + Emv.getEmv("9A") + "</TransactionDate>\n" +
                        "            <TransactionType>00</TransactionType>\n" +
                        "            <UnpredictableNumber>" + Emv.getEmv("9F37") + "</UnpredictableNumber>\n" +
                        "            <DedicatedFileName>" + Emv.getEmv("4F") + "</DedicatedFileName>\n" +
                        "        </emvData>\n" +
                        "        <track2>\n" +
                        "            <pan>" + pan + "</pan>\n" +
                        "            <expiryMonth>" + month + "</expiryMonth>\n" +
                        "            <expiryYear>" + year + "</expiryYear>\n" +
                        "            <track2>" + track2 + "</track2>\n" +
                        "        </track2>\n" +
                        "    </cardData>\n" +
                        "    <originalTransmissionDateTime>" + transDate + "</originalTransmissionDateTime>\n" +
                        "    <stan>" + Emv.transactionStan + "</stan>\n" +
                        "    <fromAccount>"+Emv.accountType+"</fromAccount>\n" +
                        "    <toAccount></toAccount>\n" +
                        "    <minorAmount>" + Emv.getMinorAmount() + "</minorAmount>\n" +
                        "    <receivingInstitutionId>627629</receivingInstitutionId>\n" +
                        "    <surcharge>10.75</surcharge>\n" +
                        "    <pinData>\n" +
                        "        <ksnd>605</ksnd>\n" +
                        "        <ksn>" + Emv.getKsn() + "</ksn>\n" +
                        "        <pinBlock>" + Emv.getPinBlock() + "</pinBlock>\n" +
                        "        <pinType>Dukpt</pinType>\n" +
                        "    </pinData>\n" +
                        "    <keyLabel>" + Emv.keysetid +"</keyLabel>\n" +
                        "    <destinationAccountNumber>2117044742</destinationAccountNumber>\n" +
                        "    <extendedTransactionType>6101</extendedTransactionType>\n" +
                        "</transferRequest>\n";
        Log.d("Result", "REQUEST " + data);

        HashMap<String,String> hashMap = new HashMap<>();
        try{
            hashMap.put("user-agent", "PocketMoni");
            hashMap.put("Accept", "*/*");
            hashMap.put("Content-Type", "application/xml");
            hashMap.put("transaction_type", "BILLPAYMENT");
            hashMap.put("card_holder_name", URLEncoder.encode(Keys.hexStringToASCII(Emv.getEmv("5F20")),"UTF-8"));
            hashMap.put("geo_location", Emv.deviceLocation);
            hashMap.put("rrn", Keys.genKimonoRRN(activity.getApplicationContext(),Emv.getTransactionStan()));
            hashMap.put("amount", Emv.getMinorAmount());
            //Electricity parameters
            ElectricityModel electricityModel = new ElectricityModel();
            hashMap.put("payment_ref", electricityModel.getPaymentRef());
            hashMap.put("convenient_fee", electricityModel.getConvenientFee());
            hashMap.put("bill_id", electricityModel.getId());
            hashMap.put("customer_id", electricityModel.getCustomerId());
            hashMap.put("Authorization", Emv.accessToken);
            for (Map.Entry<String, String> header : hashMap.entrySet()) {
                Log.d("Result", "Header> " + header.getKey() + " : " + header.getValue());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        String response = HttpRequest.reqHttp("POST", Emv.transUrl, data, hashMap, 120);
        Log.d("Result", "Response: " + response);
        return response;
    };

    //PURCHASE TO KIMONO
    public static String purchase() {
        String transDate = DateTime.Now.ToString("yyyy-MM-dd+HH:mm:ss").replace("+", "T");
        Emv.transactionDate = formatDate(transDate);
        Emv.transactionTime = formatTime(transDate);
        String track2 = Emv.getTrack2();
        String pan = Emv.getPan();
        String year = track2.substring(track2.indexOf("D") + 1, (track2.indexOf("D")+1)+2);
        String month = track2.substring(track2.indexOf("D") + 3, (track2.indexOf("D")+3)+2);
        Emv.transactionStan = Emv.getStan(activity.getApplicationContext());

        String data =
                "<purchaseRequest>\n" +
                        "    <cardPan>" + pan + "</cardPan>\n" +
                        "    <terminalId>" + Emv.terminalId + "</terminalId>\n" +
                        "    <requestType>Payment</requestType>\n" +
                        "    <terminalInformation>\n" +
                        "        <batteryInformation>100</batteryInformation>\n" +
                        "        <currencyCode>" + Integer.parseInt(Emv.TerminalCurrencyCode.split("\\|")[1]) + "</currencyCode>\n" +
                        "        <languageInfo>EN</languageInfo>\n" +
                        "        <merchantId>" + Emv.merchantId + "</merchantId>\n" +
                        "        <merhcantLocation>" + Emv.merchantLocation + "</merhcantLocation>\n" +
                        "        <posConditionCode>00</posConditionCode>\n" +
                        "        <posDataCode>" + Emv.posDataCode + "</posDataCode>\n" +
                        "        <posEntryMode>" + Emv.posEntryMode + "</posEntryMode>\n" +
                        "        <posGeoCode>" + Emv.posGeoCode + "</posGeoCode>\n" +
                        "        <printerStatus>1</printerStatus>\n" +
                        "        <terminalId>" + Emv.terminalId + "</terminalId>\n" +
                        "        <terminalType>22</terminalType>\n" +
                        "        <transmissionDate></transmissionDate>\n" +
                        "		 <requestDate>" + transDate + "</requestDate>\n" +
                        "        <uniqueId>" + Emv.serialNumber + "</uniqueId>\n" +
                        "    </terminalInformation>\n" +
                        "    <cardData>\n" +
                        "        <cardSequenceNumber>" + Emv.getEmv("5F34") + "</cardSequenceNumber>\n" +
                        "        <emvData>\n" +
                        "           <AmountAuthorized>" + Emv.getEmv("9F02") + "</AmountAuthorized>\n" +
                        "            <AmountOther>" + Emv.getEmv("9F03") + "</AmountOther>\n" +
                        "            <ApplicationInterchangeProfile>" + Emv.getEmv("82") + "</ApplicationInterchangeProfile>\n" +
                        "            <atc>" + Emv.getEmv("9F36") + "</atc>\n" +
                        "            <Cryptogram>" + Emv.getEmv("9F26") + "</Cryptogram>\n" +
                        "            <CryptogramInformationData>" + Emv.getEmv("9F27") + "</CryptogramInformationData>\n" +
                        "            <CvmResults>" + Emv.getEmv("9F34") + "</CvmResults>\n" +
                        "            <iad>" + Emv.getEmv("9F10") + "</iad>\n" +
                        "            <TransactionCurrencyCode>" + Integer.parseInt(Emv.getEmv("5F2A")) + "</TransactionCurrencyCode>\n" +
                        "            <TerminalVerificationResult>" + Emv.getEmv("95") + "</TerminalVerificationResult>\n" +
                        "            <TerminalCountryCode>" + Integer.parseInt(Emv.getEmv("9F1A")) + "</TerminalCountryCode>\n" +
                        "            <TerminalType>" + Emv.getEmv("9F35") + "</TerminalType>\n" +
                        "            <TerminalCapabilities>" + Emv.getEmv("9F33") + "</TerminalCapabilities>\n" +
                        "            <TransactionDate>" + Emv.getEmv("9A") + "</TransactionDate>\n" +
                        "            <TransactionType>00</TransactionType>\n" +
                        "            <UnpredictableNumber>" + Emv.getEmv("9F37") + "</UnpredictableNumber>\n" +
                        "            <DedicatedFileName>" + Emv.getEmv("4F") + "</DedicatedFileName>\n" +
                        "        </emvData>\n" +
                        "        <track2>\n" +
                        "            <pan>" + pan + "</pan>\n" +
                        "            <expiryMonth>" + month + "</expiryMonth>\n" +
                        "            <expiryYear>" + year + "</expiryYear>\n" +
                        "            <track2>" + track2 + "</track2>\n" +
                        "        </track2>\n" +
                        "    </cardData>\n" +
                        "    <fromAccount>Default</fromAccount>\n" +
                        "    <stan>" + Emv.transactionStan + "</stan>\n" +
                        "    <minorAmount>" + Emv.getMinorAmount() + "</minorAmount>\n" +
                        "    <pinData>\n" +
                        "        <ksnd>605</ksnd>\n" +
                        "        <ksn>" + Emv.getKsn() + "</ksn>\n" +
                        "        <pinBlock>" + Emv.getPinBlock() + "</pinBlock>\n" +
                        "        <pinType>Dukpt</pinType>\n" +
                        "    </pinData>\n" +
                        "    <keyLabel>" + Emv.keysetid +"</keyLabel>\n" +
                        "</purchaseRequest>\n";
        Log.d("Result", "REQUEST " + data);
        String response = Net.httpRequest(data, "POST", Emv.transUrl);
        return response;
    }

    //CASHOUT TO KIMONO
    public static String originalTransDate;
    public static String cashout() {
        String transDate = DateTime.Now.ToString("yyyy-MM-dd+HH:mm:ss").replace("+", "T");
        Emv.transactionDate = formatDate(transDate);
        Emv.transactionTime = formatTime(transDate);
        originalTransDate = transDate;
        String track2 = Emv.getTrack2();
        String pan = Emv.getPan();
        String year = track2.substring(track2.indexOf("D") + 1, (track2.indexOf("D")+1)+2);
        String month = track2.substring(track2.indexOf("D") + 3, (track2.indexOf("D")+3)+2);
        Emv.transactionStan = Emv.getStan(activity.getApplicationContext());
        String data =
                "<transferRequest>\n" +
                        "    <terminalInformation>\n" +
                        "        <batteryInformation>"+ Emv.getBatteryLevel(activity.getApplicationContext())+"</batteryInformation>\n" +
                        "        <currencyCode>" + Integer.parseInt(Emv.TerminalCurrencyCode.split("\\|")[1]) + "</currencyCode>\n" +
                        "        <languageInfo>EN</languageInfo>\n" +
                        "        <merchantId>" + Emv.merchantId + "</merchantId>\n" +
                        "        <merhcantLocation>" + Emv.merchantLocation + "</merhcantLocation>\n" +
                        "        <posConditionCode>00</posConditionCode>\n" +
                        "        <posDataCode>" + Emv.posDataCode + "</posDataCode>\n" +
                        "        <posEntryMode>" + Emv.posEntryMode + "</posEntryMode>\n" +
                        "        <posGeoCode>" + Emv.posGeoCode + "</posGeoCode>\n" +
                        "        <printerStatus>"+ Sdk.getPrinterState()+"</printerStatus>\n" +
                        "        <terminalId>" + Emv.terminalId + "</terminalId>\n" +
                        "        <terminalType>22</terminalType>\n" +
                        "        <transmissionDate></transmissionDate>\n" +
                        "		 <requestDate>" + transDate + "</requestDate>\n" +
                        "        <uniqueId>" + Emv.serialNumber + "</uniqueId>\n" +
                        "    </terminalInformation>\n" +
                        "    <cardData>\n" +
                        "        <cardSequenceNumber>" + Emv.getEmv("5F34") + "</cardSequenceNumber>\n" +
                        "        <emvData>\n" +
                        "           <AmountAuthorized>" + Emv.getEmv("9F02") + "</AmountAuthorized>\n" +
                        "            <AmountOther>" + Emv.getEmv("9F03") + "</AmountOther>\n" +
                        "            <ApplicationInterchangeProfile>" + Emv.getEmv("82") + "</ApplicationInterchangeProfile>\n" +
                        "            <atc>" + Emv.getEmv("9F36") + "</atc>\n" +
                        "            <Cryptogram>" + Emv.getEmv("9F26") + "</Cryptogram>\n" +
                        "            <CryptogramInformationData>" + Emv.getEmv("9F27") + "</CryptogramInformationData>\n" +
                        "            <CvmResults>" + Emv.getEmv("9F34") + "</CvmResults>\n" +
                        "            <iad>" + Emv.getEmv("9F10") + "</iad>\n" +
                        "            <TransactionCurrencyCode>" + Integer.parseInt(Emv.getEmv("5F2A")) + "</TransactionCurrencyCode>\n" +
                        "            <TerminalVerificationResult>" + Emv.getEmv("95") + "</TerminalVerificationResult>\n" +
                        "            <TerminalCountryCode>" + Integer.parseInt(Emv.getEmv("9F1A")) + "</TerminalCountryCode>\n" +
                        "            <TerminalType>" + Emv.getEmv("9F35") + "</TerminalType>\n" +
                        "            <TerminalCapabilities>" + Emv.getEmv("9F33") + "</TerminalCapabilities>\n" +
                        "            <TransactionDate>" + Emv.getEmv("9A") + "</TransactionDate>\n" +
                        "            <TransactionType>00</TransactionType>\n" +
                        "            <UnpredictableNumber>" + Emv.getEmv("9F37") + "</UnpredictableNumber>\n" +
                        "            <DedicatedFileName>" + Emv.getEmv("4F") + "</DedicatedFileName>\n" +
                        "        </emvData>\n" +
                        "        <track2>\n" +
                        "            <pan>" + pan + "</pan>\n" +
                        "            <expiryMonth>" + month + "</expiryMonth>\n" +
                        "            <expiryYear>" + year + "</expiryYear>\n" +
                        "            <track2>" + track2 + "</track2>\n" +
                        "        </track2>\n" +
                        "    </cardData>\n" +
                        "    <originalTransmissionDateTime>" + transDate + "</originalTransmissionDateTime>\n" +
                        "    <stan>" + Emv.transactionStan + "</stan>\n" +
                        "    <fromAccount>"+Emv.accountType+"</fromAccount>\n" +
                        "    <toAccount></toAccount>\n" +
                        "    <minorAmount>" + Emv.getMinorAmount() + "</minorAmount>\n" +
                        "    <receivingInstitutionId>627629</receivingInstitutionId>\n" +
                        "    <surcharge>10.75</surcharge>\n" +
                        "    <pinData>\n" +
                        "        <ksnd>605</ksnd>\n" +
                        "        <ksn>" + Emv.getKsn() + "</ksn>\n" +
                        "        <pinBlock>" + Emv.getPinBlock() + "</pinBlock>\n" +
                        "        <pinType>Dukpt</pinType>\n" +
                        "    </pinData>\n" +
                        "    <keyLabel>" + Emv.keysetid +"</keyLabel>\n" +
                        "    <destinationAccountNumber>2117044742</destinationAccountNumber>\n" +
                        "    <extendedTransactionType>6101</extendedTransactionType>\n" +
                        "</transferRequest>\n";
        Log.d("Result", "REQUEST " + data);


        HashMap<String,String> hashMap = new HashMap<>();
        try{
            hashMap.put("user-agent", "PocketMoni");
            hashMap.put("Accept", "*/*");
            hashMap.put("Content-Type", "application/xml");
            hashMap.put("transaction_type", Emv.transactionType.toString());
            hashMap.put("card_holder_name", URLEncoder.encode(Keys.hexStringToASCII(Emv.getEmv("5F20")),"UTF-8"));
            hashMap.put("geo_location", Emv.deviceLocation);
            hashMap.put("rrn", Keys.genKimonoRRN(activity.getApplicationContext(),Emv.getTransactionStan()));
            hashMap.put("amount", Emv.getMinorAmount());
            hashMap.put("Authorization", Emv.accessToken);
        }catch (Exception e){
            e.printStackTrace();
        }
        String response = HttpRequest.reqHttp("POST", Emv.transUrl, data, hashMap, 120);
        Log.d("Result", "Response: " + response);
        return response;
    }

    public static String transferCashout() {
        String transDate = DateTime.Now.ToString("yyyy-MM-dd+HH:mm:ss").replace("+", "T");
        Emv.transactionDate = formatDate(transDate);
        Emv.transactionTime = formatTime(transDate);
        originalTransDate = transDate;
        String track2 = Emv.getTrack2();
        String pan = Emv.getPan();
        String year = track2.substring(track2.indexOf("D") + 1, (track2.indexOf("D")+1)+2);
        String month = track2.substring(track2.indexOf("D") + 3, (track2.indexOf("D")+3)+2);
        Emv.transactionStan = Emv.getStan(activity.getApplicationContext());

        String data =
                "<transferRequest>\n" +
                        "    <terminalInformation>\n" +
                        "        <batteryInformation>"+ Emv.getBatteryLevel(activity.getApplicationContext())+"</batteryInformation>\n" +
                        "        <currencyCode>" + Integer.parseInt(Emv.TerminalCurrencyCode.split("\\|")[1]) + "</currencyCode>\n" +
                        "        <languageInfo>EN</languageInfo>\n" +
                        "        <merchantId>" + Emv.merchantId + "</merchantId>\n" +
                        "        <merhcantLocation>" + Emv.merchantLocation + "</merhcantLocation>\n" +
                        "        <posConditionCode>00</posConditionCode>\n" +
                        "        <posDataCode>" + Emv.posDataCode + "</posDataCode>\n" +
                        "        <posEntryMode>" + Emv.posEntryMode + "</posEntryMode>\n" +
                        "        <posGeoCode>" + Emv.posGeoCode + "</posGeoCode>\n" +
                        "        <printerStatus>"+ Sdk.getPrinterState()+"</printerStatus>\n" +
                        "        <terminalId>" + Emv.terminalId + "</terminalId>\n" +
                        "        <terminalType>22</terminalType>\n" +
                        "        <transmissionDate></transmissionDate>\n" +
                        "		 <requestDate>" + transDate + "</requestDate>\n" +
                        "        <uniqueId>" + Emv.serialNumber + "</uniqueId>\n" +
                        "    </terminalInformation>\n" +
                        "    <cardData>\n" +
                        "        <cardSequenceNumber>" + Emv.getEmv("5F34") + "</cardSequenceNumber>\n" +
                        "        <emvData>\n" +
                        "           <AmountAuthorized>" + Emv.getEmv("9F02") + "</AmountAuthorized>\n" +
                        "            <AmountOther>" + Emv.getEmv("9F03") + "</AmountOther>\n" +
                        "            <ApplicationInterchangeProfile>" + Emv.getEmv("82") + "</ApplicationInterchangeProfile>\n" +
                        "            <atc>" + Emv.getEmv("9F36") + "</atc>\n" +
                        "            <Cryptogram>" + Emv.getEmv("9F26") + "</Cryptogram>\n" +
                        "            <CryptogramInformationData>" + Emv.getEmv("9F27") + "</CryptogramInformationData>\n" +
                        "            <CvmResults>" + Emv.getEmv("9F34") + "</CvmResults>\n" +
                        "            <iad>" + Emv.getEmv("9F10") + "</iad>\n" +
                        "            <TransactionCurrencyCode>" + Integer.parseInt(Emv.getEmv("5F2A")) + "</TransactionCurrencyCode>\n" +
                        "            <TerminalVerificationResult>" + Emv.getEmv("95") + "</TerminalVerificationResult>\n" +
                        "            <TerminalCountryCode>" + Integer.parseInt(Emv.getEmv("9F1A")) + "</TerminalCountryCode>\n" +
                        "            <TerminalType>" + Emv.getEmv("9F35") + "</TerminalType>\n" +
                        "            <TerminalCapabilities>" + Emv.getEmv("9F33") + "</TerminalCapabilities>\n" +
                        "            <TransactionDate>" + Emv.getEmv("9A") + "</TransactionDate>\n" +
                        "            <TransactionType>00</TransactionType>\n" +
                        "            <UnpredictableNumber>" + Emv.getEmv("9F37") + "</UnpredictableNumber>\n" +
                        "            <DedicatedFileName>" + Emv.getEmv("4F") + "</DedicatedFileName>\n" +
                        "        </emvData>\n" +
                        "        <track2>\n" +
                        "            <pan>" + pan + "</pan>\n" +
                        "            <expiryMonth>" + month + "</expiryMonth>\n" +
                        "            <expiryYear>" + year + "</expiryYear>\n" +
                        "            <track2>" + track2 + "</track2>\n" +
                        "        </track2>\n" +
                        "    </cardData>\n" +
                        "    <originalTransmissionDateTime>" + transDate + "</originalTransmissionDateTime>\n" +
                        "    <stan>" + Emv.transactionStan + "</stan>\n" +
                        "    <fromAccount>"+Emv.accountType+"</fromAccount>\n" +
                        "    <toAccount></toAccount>\n" +
                        "    <minorAmount>" + Emv.getMinorAmount() + "</minorAmount>\n" +
                        "    <receivingInstitutionId>627629</receivingInstitutionId>\n" +
                        "    <surcharge>10.75</surcharge>\n" +
                        "    <pinData>\n" +
                        "        <ksnd>605</ksnd>\n" +
                        "        <ksn>" + Emv.getKsn() + "</ksn>\n" +
                        "        <pinBlock>" + Emv.getPinBlock() + "</pinBlock>\n" +
                        "        <pinType>Dukpt</pinType>\n" +
                        "    </pinData>\n" +
                        "    <keyLabel>" + Emv.keysetid +"</keyLabel>\n" +
                        "    <destinationAccountNumber>2117044742</destinationAccountNumber>\n" +
                        "    <extendedTransactionType>6101</extendedTransactionType>\n" +
                        "</transferRequest>\n";
        Log.d("Result", "REQUEST " + data);

        HashMap<String,String> hashMap = new HashMap<>();
        try{
            hashMap.put("user-agent", "PocketMoni");
            hashMap.put("Accept", "*/*");
            hashMap.put("Content-Type", "application/xml");
            hashMap.put("transaction_type", Emv.transactionType.toString());
            hashMap.put("card_holder_name", URLEncoder.encode(Keys.hexStringToASCII(Emv.getEmv("5F20")),"UTF-8"));
            hashMap.put("geo_location", Emv.deviceLocation);
            hashMap.put("rrn", Keys.genKimonoRRN(activity.getApplicationContext(),Emv.getTransactionStan()));
            hashMap.put("amount", Emv.getMinorAmount());
            //Transfer parameters
            TransferModel transModel = new TransferModel();
            hashMap.put("account_no", transModel.getAcctNo());
            hashMap.put("bank_code", transModel.getBankCode(transModel.getBankName()));
            hashMap.put("transfer_reference", transModel.getTransferRef());
            hashMap.put("convenient_fee", transModel.getConvenientFee());
            hashMap.put("Authorization", Emv.accessToken);
            for (Map.Entry<String, String> header : hashMap.entrySet()) {
                Log.d("Result", "Header> " + header.getKey() + " : " + header.getValue());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        String response = HttpRequest.reqHttp("POST", Emv.transUrl, data, hashMap, 120);
        Log.d("Result", "Response: " + response);
        return response;
    }

    public static String reQuery(String transDate){
       String data = "<transactionRequeryRequest>\n" +
               "    <applicationType>gTransfer</applicationType>\n" +
               "    <originalTransStan>"+Emv.getTransactionStan()+"</originalTransStan>\n" +
               "    <originalMinorAmount>"+Emv.getMinorAmount()+"</originalMinorAmount>\n" +
               "    <terminalInformation>\n" +
               "        <terminalId>"+Emv.terminalId+"</terminalId>\n" +
               "        <merchantId>"+Emv.merchantId+"</merchantId>\n" +
               "        <transmissionDate>"+transDate+"</transmissionDate>\n" +
               "    </terminalInformation>\n" +
               "</transactionRequeryRequest>";
        Log.d("Result", "Request: " + data);

        String response = Net.httpRequest(data, "POST", Emv.requeryUrl);
        Log.d("Result", "Response: " + response);
        return response;
    }

    private static String formatDate(String datetime){
        return  datetime.substring(0, datetime.indexOf("T"));
    }

    private static String formatTime(String datetime){
        return  datetime.substring(datetime.indexOf("T")+1);
    }
}
