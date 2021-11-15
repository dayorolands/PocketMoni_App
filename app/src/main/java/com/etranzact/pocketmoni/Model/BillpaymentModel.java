package com.etranzact.pocketmoni.Model;

import android.app.Activity;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import Utils.DateTime;
import Utils.Emv;
import Utils.Sdk;

public class BillpaymentModel {
//    public static String billId = "";
//    public static String customerId = "";
//    public static String billerId = "";
//    public static String customerId = "";
//    public static String clientRef = "";
//    public static String paymentRef = "";
//    public static String billerName = "";
//    public static String customerName = "";
//    public static String mobileNo = "";
//    public static String acctNo = "";
//    public static String bankCode = "";
//
//
//    private String cableTvReqData() {
//        return "{\n" +
//                "\t\"serialNumber\":\""+ Sdk.getSerialNo()+"\",\n" +
//                "\t\"billId\":\""+billId+"\",\n" +
//                "\t\"customerId\":\""+customerId+"\",\n" +
//                "\t\"terminalId\":\""+Emv.terminalId+"\"\n" +
//                "}";
//    }
//
//    private String getRData(){
//        return "{\n" +
//            "  \"serialNo\": \""+Emv.serialNumber+"\",\n" +
//            "  \"terminalId\": \""+Emv.terminalId+"\",\n" +
//            "  \"beneficiaryAccountNo\": \""+acctNo+"\",\n" +
//            "  \"amount\": \""+amount+"\",\n" +
//            "  \"bankcode\": \""+bankCode+"\",\n" +
//            "  \"destinationPhoneNo\": \""+phone+"\",\n" +
//            "  \"narration\": \""+narration+"\",\n" +
//            "  \"firstName\": \""+sendersName+"\",\n" +
//            "  \"lastName\": \""+lastName+"\",\n" +
//            "  \"reference\": \""+transferRef+"\",\n" +
//            "  \"pmPin\": \""+encryptedPin +"\"\n" +
//            "}";
//    }
//
//
//    private static Activity activity;
//
//    public static String processCashTransfer(Activity a) {
//        activity = a;
//        //Generate the transaction date and feed it to the variable for EOD
//        String transDate = DateTime.Now.ToString("yyyy-MM-dd+HH:mm:ss").replace("+", "T");
//        Emv.transactionDate = formatDate(transDate);
//        Emv.transactionTime = formatTime(transDate);
//
//        String data =
//
//                Log.d("Result", "Request: " + data);
//        String result = httpRequestCash(data, "POST", Emv.transferCashUrl);
//        Log.d("Result", "Response: " + result);
//        return result;
//    }
//
//    public void processTransfer(ICreateWallet internetCallbacks) {
//        internetListener = internetCallbacks;
//        new Thread(() -> {
//
//            HashMap<String, String> hashMap = new HashMap<>();
//            hashMap.put("Accept", "*/*");
//            hashMap.put("Content-Type", "application/json");
//            hashMap.put("geo_location", Emv.deviceLocation);
//            hashMap.put("Authorization", Emv.accessToken);
//            for (Map.Entry<String, String> header : hashMap.entrySet()) {
//                Log.d("Result", "Header> " + header.getKey() + " : " + header.getValue());
//            }
//            Log.d("Result", "Request " + getData());
//            String response = HttpRequest.reqHttp("POST", Emv.transferCashUrl, getData(), hashMap);
//            activity.runOnUiThread(() -> {
//                internetListener.requestResponse(response);
//            });
//        }).start();
//    }
//
//    private static ICreateWallet internetListener;
//
//    public interface ICreateWallet {
//        void requestResponse(String message);
//    }
//
//    private static String formatDate(String datetime) {
//        return datetime.substring(0, datetime.indexOf("T"));
//    }
//
//    private static String formatTime(String datetime) {
//        return datetime.substring(datetime.indexOf("T") + 1);
//    }
}
