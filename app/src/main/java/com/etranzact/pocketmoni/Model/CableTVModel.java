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

public class CableTVModel {
    private static final List<Plan> planDetails = new ArrayList<>();
    private static final String KEY = "cable_cat_key";
    public final String PIN_KEY = "A8BF4C78F2EEEDB96FAF3A0655454345";
    public final String CUSTOM_PAN = "0000000000000000";

    public CableTVModel(){ }

    private static String id, billsName, billsCode, description, image, pin, address, amount, customerId, paymentRef, customerName, productKey, productName;
    Activity activity;
    public CableTVModel(Activity activity, ICategoryResponse internetCallbacks){
        this.activity = activity;
        getBillsCategories(internetCallbacks);
    }

    String category,categoryImage;
    public CableTVModel(String category, String categoryImage){
        this.category = category;
        this.categoryImage = categoryImage;
    }
    public String getCategory(){
        return category;
    }
    public String getCategoryImage(){
        return categoryImage;
    }



    public static String getId() {
        return id;
    }

    public void setId(String id) {
        CableTVModel.id = id;
    }

    public String getBillsName() {
        return billsName;
    }

    public void setBillsName(String billsName) {
        CableTVModel.billsName = billsName;
    }

    public String getBillsCode() {
        return billsCode;
    }

    public void setBillsCode(String billsCode) {
        CableTVModel.billsCode = billsCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) { CableTVModel.description = description; }

    public String getImage() { return image; }

    public void setImage(String image) {
        CableTVModel.image = image;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        CableTVModel.pin = pin;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        CableTVModel.address = address;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        CableTVModel.amount = amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        CableTVModel.customerId = customerId;
    }

    public String getPaymentRef() {
        return paymentRef;
    }

    public void setPaymentRef(String paymentRef) {
        CableTVModel.paymentRef = paymentRef;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        CableTVModel.customerName = customerName;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        CableTVModel.productKey = productKey;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        CableTVModel.productName = productName;
    }

    public List<Plan> getCategories(){
        return planDetails;
    }

    public Plan getPlanDetails(String category){
        for(Plan plan : planDetails){
            if(plan.getBillsName().toLowerCase().equals(category.toLowerCase())){
                return plan;
            }
        }
        return new Plan("", "", "", "", "");
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
        "   \"productKey\": \""+productKey+"\",\n" +
        "   \"productName\":\""+productName+"\",\n" +
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

    private String depositStartimes(Activity activity){
        String transDate = DateTime.Now.ToString("yyyy-MM-dd+HH:mm:ss").replace("+", "T");
        Emv.transactionDate = formatDate(transDate);
        Emv.transactionTime = formatTime(transDate);
        return "{\n" +
        "   \"pin\":\""+pin+"\",\n" +
        "   \"amount\":"+amount+",  \n" +
        "   \"billerName\":\""+billsName+"\",\n" +
        "   \"billerCode\" :\""+billsCode+"\",\n" +
        "   \"mtype\":\"3\",\n" +
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
    public String getTransactionData(Activity context){
        String resultRRN = Keys.genKimonoRRN(context, Emv.transactionStan);
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
            getDescription() + "|" + //17 CableTV Description
            getProductName(); //18 CabletTV ProductName
    }

    private String validationData(){
        return "{\n" +
        "    \"customerId\": \""+customerId+"\",\n" +
        "    \"billerCode\": \""+billsCode+"\",\n" +
        "    \"serialNumber\": \""+Emv.serialNumber+"\",\n" +
        "    \"terminalId\": \""+Emv.terminalId+"\"\n" +
        "}";
    }

    public void doAccountValidation(Activity activity, IValidationResponse interCallback){
        this.internetListener2 = interCallback;
        new Thread(()->{
            HashMap<String,String> headers = new HashMap<>();
            headers.put("Content-Type","application/json");
            headers.put("Authorization",Emv.accessToken);
            Log.d("Result", "Request: "+validationData());
            String response = HttpRequest.reqHttp("POST",Emv.cableTvValidationUrl,validationData(),headers);
            activity.runOnUiThread(()->{
                internetListener2.requestResponse(response);
            });
        }).start();
    }

    public String processDeposit(Activity activity) {
        Emv.transactionStan = Emv.getStan(activity);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("Accept", "*/*");
        hashMap.put("Content-Type", "application/json");
        hashMap.put("geo_location", Emv.deviceLocation);
        hashMap.put("Authorization", Emv.accessToken);

        String payload = (billsName.toLowerCase().contains("startimes")) ? depositStartimes(activity) : depositData(activity);

        for (Map.Entry<String, String> header : hashMap.entrySet()) {
            Log.d("Result", "Header> " + header.getKey() + " : " + header.getValue());
        }
        Log.d("Result", "Request: " + payload);
        String response = HttpRequest.reqHttp("POST", Emv.cableTVCashUrl, payload, hashMap);
        Log.d("Result", "Response: " + response);
        return response;
    }


    private void getBillsCategories(ICategoryResponse internetCallback){
        this.internetListener = internetCallback;
        new Thread(()->{
            try{
                String json = SharedPref.get(activity,KEY,"");
                HashMap<String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization",Emv.accessToken);
                if(json.isEmpty()){
                    json = HttpRequest.reqHttp("GET",Emv.cableTvCategoryUrl,"",headers);
                }
                List<String> catId = Keys.parseJsonCnt(json, "id");
                List<String> catBillsName = Keys.parseJsonCnt(json, "billerName");
                List<String> catBillsCode = Keys.parseJsonCnt(json, "billerCode");
                List<String> catDesc = Keys.parseJsonCnt(json, "description");
                List<String> catImage = Keys.parseJsonCnt(json, "image");

                if(catId.size()>0) planDetails.clear();

                for(int i=0; i<catId.size(); i++) {
                    planDetails.add(new Plan(
                            catId.get(i),
                            catBillsName.get(i),
                            catBillsCode.get(i),
                            catDesc.get(i),
                            catImage.get(i)
                    ));
                }
                activity.runOnUiThread(()-> internetListener.requestResponse(""+planDetails.size()));

//                json = HttpRequest.reqHttp("GET",Emv.cableTvCategoryUrl,"",headers);
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
    private IValidationResponse internetListener2;
    public interface ICategoryResponse {
        void requestResponse(String message);
    }
    public interface IValidationResponse {
        void requestResponse(String message);
    }

    //The class that holds the plan structure
    public static class Plan{
        private String id = "", billsName = "", billsCode = "", description = "", image = "";
        public Plan(String id, String billsName, String billsCode, String description, String image) {
            this.id = id;
            this.billsName = billsName;
            this.billsCode = billsCode;
            this.description = description;
            this.image = image;
        }

        public String getId() {
            return id;
        }

        public String getBillsName() {
            return billsName;
        }

        public String getBillsCode() {
            return billsCode;
        }

        public String getDescription() {
            return description;
        }

        public String getImage() {
            return image;
        }
    }
}
