package com.etranzact.pocketmoni.Model;

import android.app.Activity;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import Utils.DateTime;
import Utils.Emv;
import Utils.HttpRequest;
import Utils.Keys;
import Utils.Sdk;
import Utils.SharedPref;

public class AirtimeModel {
    private static final List<Plan> planDetails = new ArrayList<>();
    private static final String AKEY = "airtime_cat_key";
    public final String PIN_KEY = "A8BF4C78F2EEEDB96FAF3A0655454345";
    public final String CUSTOM_PAN = "0000000000000000";

    public AirtimeModel(){ }

    private static String billsName, billsCode, description, image, pin, amount, customerId, paymentRef, customerName;
    Activity activity;
    public AirtimeModel(Activity activity, ICategoryResponse internetCallbacks){
        this.activity = activity;
        getBillsCategories(internetCallbacks);
    }

    String category,categoryImage;
    public AirtimeModel(String category, String categoryImage){
        this.category = category;
        this.categoryImage = categoryImage;
    }
    public String getCategory(){
        return category;
    }
    public String getCategoryImage(){
        return categoryImage;
    }


    public String getBillsName() {
        return billsName;
    }

    public void setBillsName(String billsName) {
        AirtimeModel.billsName = billsName;
    }

    public String getBillsCode() {
        return billsCode;
    }

    public void setBillsCode(String billsCode) {
        AirtimeModel.billsCode = billsCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) { AirtimeModel.description = description; }

    public String getImage() { return image; }

    public void setImage(String image) {
        AirtimeModel.image = image;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        AirtimeModel.pin = pin;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        AirtimeModel.amount = amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        AirtimeModel.customerId = customerId;
    }

    public String getPaymentRef() {
        return paymentRef;
    }

    public void setPaymentRef(String paymentRef) {
        AirtimeModel.paymentRef = paymentRef;
    }

    public String getCustomerName() { return customerName; }

    public void setCustomerName(String customerName) { AirtimeModel.customerName = customerName; }

    public List<Plan> getCategories(){
        return planDetails;
    }

    public Plan getPlanDetails(String category){
        for(Plan plan : planDetails){
            if(plan.getBillsName().toLowerCase().equals(category.toLowerCase())){
                return plan;
            }
        }
        return new Plan("", "", "");
    }

    private String depositData(Activity activity){
        String transDate = DateTime.Now.ToString("yyyy-MM-dd+HH:mm:ss").replace("+", "T");
        Emv.transactionDate = formatDate(transDate);
        Emv.transactionTime = formatTime(transDate);
        return "{\n" +
                "   \"pin\":\""+pin+"\",\n" +
                "   \"amount\":"+amount+",  \n" +
                "   \"billerName\":\""+billsName+"\",\n" +
                "   \"billerCode\" :\""+billsCode+"\",\n" +
                "   \"customerId\":\""+customerId+"\",\n" +
                "   \"originalTransmissionDateTime\":\""+transDate+"\",\n" +
                "   \"billPayTerminalInfoReq\": {\n" +
                "   \"batteryInformation\":\""+Emv.getBatteryLevel(activity)+"\",\n" +
                "   \"cellStationId\":\""+Emv.deviceLocation+"\",\n" +
                "   \"currencyCode\":\""+Integer.parseInt(Emv.TerminalCountryCode.split("\\|")[1])+"\",\n" +
                "   \"languageInfo\":\"EN\",\n" +
                "   \"merchantId\":\""+Emv.merchantId+"\",\n" +
                "   \"merhcantLocation\":\""+Emv.merchantLocation+"\",\n" +
                "   \"posConditionCode\":\"00\",\n" +
                "   \"posDataCode\":\""+Emv.posDataCode+"\",\n" +
                "   \"posEntryMode\":\""+Emv.posEntryMode+"\",\n" +
                "   \"posGeoCode\":\""+Emv.posGeoCode+"\",\n" +
                "   \"printerStatus\":\""+ Sdk.getPrinterState()+"\",\n" +
                "   \"terminalId\":\""+Emv.terminalId+"\",\n" +
                "   \"terminalType\":\"22\",\n" +
                "   \"transmissionDate\":\""+transDate+"\",\n" +
                "   \"uniqueId\":\""+Emv.serialNumber+"\",\n" +
                "   \"requestDate\":\""+transDate+"\"\n" +
                "   }\n" +
                "}";
    }

    private static String formatDate(String datetime){
        return  datetime.substring(0, datetime.indexOf("T"));
    }

    private static String formatTime(String datetime){
        return  datetime.substring(datetime.indexOf("T")+1);
    }

    public String getTransactionData(Activity activity){
        String resultRRN = Keys.genKimonoRRN(activity, Emv.transactionStan);
        return Emv.getTransactionDate() + "|" + //0 TRANSACTION DATE
                Emv.getTransactionTime() + "|" + //1 TRANSACTION TIME
                getCustomerName() + "|" + //2 CARD HOLDER NAME
                "000000*******0000" + "|" +  //3 MASKED PAN
                Emv.transactionType.toString() + "|" + //4 TRANSACTION TYPE
                Emv.responseMessage + "|" + //5 RESPONSE MESSAGE
                String.format(Locale.getDefault(),"%,.2f",Double.parseDouble(getAmount())) + "|" +  //6 TRANSACTION MINOR AMOUNT
                Emv.responseCode + "|" + //7 RESPONSE CODE
                resultRRN + "|" + //8 RRN
                Emv.transactionStan + "|" + //9 STAN
                "N/A" + "|" + //10 TVR
                "N/A" + "|" + //11 TSI
                "CASH" + "|" + //12 CARD TYPE
                getCustomerName() + "|" + //13 CableTV Customer Name
                getCustomerId() + "|" + //14 CableTV Customer ID
                getBillsName() + "|" + //15 CableTV Biller Name
                getPaymentRef() + "|" + //16 CableTV Payment Ref
                getDescription(); //17 Description
    }

    public String processDeposit(Activity activity) {
        Emv.transactionStan = Emv.getStan(activity);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("Accept", "*/*");
        hashMap.put("Content-Type", "application/json");
        hashMap.put("geo_location", Emv.deviceLocation);
        hashMap.put("Authorization", Emv.accessToken);
        for (Map.Entry<String, String> header : hashMap.entrySet()) {
            Log.d("Result", "Header> " + header.getKey() + " : " + header.getValue());
        }
        Log.d("Result", "Request: " + depositData(activity));
        String response = HttpRequest.reqHttp("POST", Emv.airtimeCashUrl, depositData(activity), hashMap);
        Log.d("Result", "Response: " + response);
        return response;
    }


    private void getBillsCategories(ICategoryResponse internetCallback){
        this.internetListener = internetCallback;
        new Thread(()->{
            try{
                String json = SharedPref.get(activity,AKEY,"");
                HashMap<String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization",Emv.accessToken);
                if(json.isEmpty()){
                    json = HttpRequest.reqHttp("GET",Emv.airtimeCategoryURl,"",headers);
                }
                List<String> catBillsName = Keys.parseJsonCnt(json, "biller");
                List<String> catBillsCode = Keys.parseJsonCnt(json, "billerCode");
                List<String> catImage = Keys.parseJsonCnt(json, "image");

                if(catBillsName.size()>0) planDetails.clear();

                for(int i=0; i<catBillsName.size(); i++) {
                    planDetails.add(new Plan(
                            catBillsName.get(i),
                            catBillsCode.get(i),
                            catImage.get(i)
                    ));
                }
                activity.runOnUiThread(()-> internetListener.requestResponse(""+planDetails.size()));

//                json = HttpRequest.reqHttp("GET",Emv.airtimeCategoryURl,"",headers);
//                String respCode = Keys.parseJson(json,"responseCode");
//                if(respCode.equals("00")){
//                    SharedPref.set(activity,KEY,json);
//                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private ICategoryResponse internetListener;
    public interface ICategoryResponse {
        void requestResponse(String message);
    }

    //The class that holds the plan structure
    public static class Plan{
        private String billsName = "", billsCode = "", image = "";
        public Plan(String billsName, String billsCode, String image) {
            this.billsName = billsName;
            this.billsCode = billsCode;
            this.image = image;
        }

        public String getBillsName() {
            return billsName;
        }

        public String getBillsCode() {
            return billsCode;
        }

        public String getImage() {
            return image;
        }
    }
}
