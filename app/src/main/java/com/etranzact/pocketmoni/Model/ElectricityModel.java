package com.etranzact.pocketmoni.Model;

import android.app.Activity;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

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

public class ElectricityModel {
    private static final HashMap<String,List<Plan>> planDetails = new HashMap<>();
    private static final String KEY = "cat_key";
    public final String PIN_KEY = "A8BF4C78F2EEEDB96FAF3A0655454345";
    public final String CUSTOM_PAN = "0000000000000000";

    private static String billerName, minimumAmount, id, convenientFee, billerCode, description,meterType, zone, mtype, sessionCategory, customerId, customerName,outstandingBal,amount,pin,paymentRef,address,token;
    Activity activity;
    private String data = "";
    public ElectricityModel(Activity activity, ICategoryResponse internetCallbacks){
        this.activity = activity;
        getBillsCategories(internetCallbacks);
    }

    public ElectricityModel(){ }

    public ElectricityModel(Activity activity){
        this.activity = activity;
        data = SharedPref.get(activity, "routeResp", "");
        Log.d("Route_Response","The route response for the data " + data);
    }

    private String category, categoryUrl;
    public ElectricityModel(String category, String categoryUrl) {
        this.category = category;
        this.categoryUrl = categoryUrl;
    }

    public String getCategory() {
        return category;
    }
    public String getCategoryUrl() {
        return categoryUrl;
    }

    public double getMinCvAmount(){ return Double.parseDouble(Keys.parseJson(data, "minCvAmount")); }

    public double getMaxCvAmount(){ return Double.parseDouble(Keys.parseJson(data, "maxCvAmount")); }

    public void setConvenientFee(String convenientFee) {
        ElectricityModel.convenientFee = convenientFee;
    }

    public String getConvenientFee() { return convenientFee; }

    public String getId() { return id; }
    public String getBillerName() {
        return billerName;
    }
    public String getBillerCode() {
        return billerCode;
    }
    public String getDescription() {
        return description;
    }
    public String getZone() {
        return zone;
    }
    public String getMtype() {
        return mtype;
    }
    public String getMeterType(){return meterType;}
    public String getCustomerId(){return customerId;}
    public String getCustomerName() {return customerName;}
    public String getSessionCategory() {return sessionCategory;}
    public String getOutstandingBal() {return outstandingBal;}
    public String getAmount(){return amount;}
    public String getPaymentRef(){return paymentRef;}
    public String getAddress(){return address;}
    public String getToken(){return token;}
    public String getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(String minimumAmount) {
        ElectricityModel.minimumAmount = minimumAmount;
    }
    public void setId(String id) { ElectricityModel.id = id; }
    public void setBillerName(String billerName) { ElectricityModel.billerName = billerName; }
    public void setBillerCode(String billerCode) { ElectricityModel.billerCode = billerCode; }
    public void setDescription(String description) { ElectricityModel.description = description; }
    public void setZone(String zone) {
        ElectricityModel.zone = zone;
    }
    public void setMtype(String mtype) { ElectricityModel.mtype = mtype; }
    public void setMeterType(String meterType){ElectricityModel.meterType = meterType;}
    public void setSessionCategory(String sessionCategory) { ElectricityModel.sessionCategory = sessionCategory; }
    public void setCustomerId(String customerId) { ElectricityModel.customerId = customerId; }
    public void setCustomerName(String customerName){ElectricityModel.customerName = customerName;}
    public void setOutstandingBal(String outstandingBal){ElectricityModel.outstandingBal = outstandingBal;}
    public void setAmount(String amount){ElectricityModel.amount = amount;}
    public void setPin(String pin){ElectricityModel.pin = pin;}
    public void setPaymentRef(String paymentRef){ElectricityModel.paymentRef = paymentRef;}
    public void setAddress(String address){ElectricityModel.address = address;}
    public void setToken(String token){ElectricityModel.token = token;}

    public List<String> getCategories(){
        List<String> categories = new ArrayList<>();
        for(Map.Entry<String,List<Plan>> entry : planDetails.entrySet()){
            categories.add(entry.getKey());
        }
        return categories;
    }

    public Plan getPlan(String category,String selectedPlan){
        for(Map.Entry<String,List<Plan>> entry : planDetails.entrySet()){
            if(entry.getKey().split("\\|")[0].equals(category)){
                for(Plan planType : entry.getValue()){
                    if(planType.meterType.toLowerCase().equals(selectedPlan.toLowerCase())){
                        return planType;
                    }
                }
            }
        }
        //Index 0 for prepaid and Index 1 for prepaid
        return new Plan("","","","","","","", "" );
    }

    private String depositData(Activity activity){
        String transDate = DateTime.Now.ToString("yyyy-MM-dd+HH:mm:ss").replace("+", "T");
        Emv.transactionDate = formatDate(transDate);
        Emv.transactionTime = formatTime(transDate);
        return "{\n" +
        "   \"pin\":\""+pin+"\",\n" +
        "   \"amount\":"+amount+",  \n" +
        "   \"billerName\":\""+billerName+"\",\n" +
        "   \"billerCode\" :\""+billerCode+"\",\n" +
        "   \"description\":\""+description+"\",\n" +
        "   \"meterType\" :\""+meterType+"\",\n" +
        "   \"zone\":\""+zone+"\",\n" +
        "   \"mtype\":\""+mtype+"\",\n" +
        "   \"customerId\":\""+customerId+"\",\n" +
        "   \"mobile\":\"\",\n" +
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
            getCustomerName() + "|" + //13 Electricity Customer Name
            getCustomerId() + "|" + //14 Electricity Customer ID
            getBillerName() + "|" + //15 Electricity Biller Name
            getPaymentRef() + "|" + //16 Electricity Payment Ref
            getDescription() + "|" + //17 Electricity Description
            getAddress() + "|" + //18 Electricity Address
            getSessionCategory() + "|" + //19 Electricity SessionCategory
            getToken(); //20 Electricity Token
    }

    private String validationData(){
        return "{\n" +
        "    \"customerId\": \""+customerId+"\",\n" +
        "    \"billerCode\": \""+billerCode+"\",\n" +
        "    \"meterType\": \""+meterType+"\",\n" +
        "    \"serialNumber\": \""+Emv.serialNumber+"\",\n" +
        "    \"terminalId\": \""+Emv.terminalId+"\",\n" +
        "    \"msgType\": \""+mtype+"\"\n" +
        "}";
    }

    private String cardValidationData(){
        return "{\n" +
                "    \"serviceTypeBilId\": \""+id+"\",\n" +
                "    \"customerId\": \""+customerId+"\",\n" +
                "    \"billerCode\": \""+billerCode+"\",\n" +
                "    \"meterType\": \""+meterType+"\",\n" +
                "    \"serialNumber\": \""+Emv.serialNumber+"\",\n" +
                "    \"terminalId\": \""+Emv.terminalId+"\",\n" +
                "    \"msgType\": \""+mtype+"\"\n" +
                "}";
    }

    public void doCardAcctValidation(Activity activity, IValidationResponse interCallback){
        this.internetListener2 = interCallback;
        new Thread(()->{
            HashMap<String,String> headers = new HashMap<>();
            headers.put("Content-Type","application/json");
            headers.put("Authorization",Emv.accessToken);
            Log.d("Result", "Request: "+cardValidationData());
            String response = HttpRequest.reqHttp("POST",Emv.electricityCardValidationUrl,cardValidationData(),headers);
            activity.runOnUiThread(()->{
                internetListener2.requestResponse(response);
            });
        }).start();
    }

    public void doAccountValidation(Activity activity, IValidationResponse interCallback){
        this.internetListener2 = interCallback;
        new Thread(()->{
            HashMap<String,String> headers = new HashMap<>();
            headers.put("Content-Type","application/json");
            headers.put("Authorization",Emv.accessToken);
            Log.d("Result", "Request: "+validationData());
            String response = HttpRequest.reqHttp("POST",Emv.electricityValidationUrl,validationData(),headers);
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
        for (Map.Entry<String, String> header : hashMap.entrySet()) {
            Log.d("Result", "Header> " + header.getKey() + " : " + header.getValue());
        }
        Log.d("Result", "Request: " + depositData(activity));
        String response = HttpRequest.reqHttp("POST", Emv.electricityCashUrl, depositData(activity), hashMap);
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
                String responseCode = Keys.parseJson(json, "responseCode");
                if(!responseCode.equals("00")){
                    json = HttpRequest.reqHttp("GET",Emv.electricityCategoryUrl,"",headers);
                    Log.d("Result", "The response from the server is " + json);
                }
                //String json = "{\"responseCode\":\"00\",\"responseMessage\":\"Electricity List Fetched\",\"data\":{\"Enugu Electricity|https://demo.etranzact.com/apps/pocketmonibillerlist/phcn_enugu.png\":[{\"id\":\"12\",\"billerName\":\"PHCN Enugu Prepaid\",\"billerCode\":\"phcnenu\",\"description\":\"This is phcn enugu prepaid biller\",\"meterType\":\"prepaid\",\"zone\":\"phcnenu\",\"mtype\":\"3\"},{\"id\":\"13\",\"billerName\":\"PHCN Enugu Postpaid\",\"billerCode\":\"phcnppenu\",\"description\":\"This is phcn enugu postpaid biller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppenu\",\"mtype\":\"6\"}],\"Jos Electricity|https://demo.etranzact.com/apps/pocketmonibillerlist/phcn_jos.png\":[{\"id\":\"19\",\"billerName\":\"PHCN Jos Prepaid\",\"billerCode\":\"phcnjos\",\"description\":\"This is phcn jos prepaid biller\",\"meterType\":\"prepaid\",\"zone\":\"phcnjos\",\"mtype\":\"3\"},{\"id\":\"20\",\"billerName\":\"PHCN Jos Postpaid\",\"billerCode\":\"phcnppjos\",\"description\":\"This is phcn jos postpaid biller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppjos\",\"mtype\":\"6\"}],\"Eko Electricity|https://demo.etranzact.com/apps/pocketmonibillerlist/phcn_eko.png\":[{\"id\":\"15\",\"billerName\":\"PHCN Eko Prepaid\",\"billerCode\":\"phcneko\",\"description\":\"This is phcn eko prepaid biller\",\"meterType\":\"prepaid\",\"zone\":\"phcneko\",\"mtype\":\"3\"},{\"id\":\"16\",\"billerName\":\"PHCN Eko Postpaid\",\"billerCode\":\"phcnppeko\",\"description\":\"This is phcn eko postpaid biller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppeko\",\"mtype\":\"6\"}],\"Ibadan Electricity|https://demo.etranzact.com/apps/pocketmonibillerlist/phcn_ibadan.png\":[{\"id\":\"25\",\"billerName\":\"PHCN Ibadan Prepaid\",\"billerCode\":\"phcnibd\",\"description\":\"This is phcn ibadan prepaid biller\",\"meterType\":\"prepaid\",\"zone\":\"phcnibd\",\"mtype\":\"3\"},{\"id\":\"26\",\"billerName\":\"PHCN Ibadan Postpaid\",\"billerCode\":\"phcnppibd\",\"description\":\"This is phcn Ibadan postpaid biller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppibd\",\"mtype\":\"6\"}],\"Kano Electricity|https://demo.etranzact.com/apps/pocketmonibillerlist/phcn_kano.png\":[{\"id\":\"21\",\"billerName\":\"PHCN Kano Prepaid\",\"billerCode\":\"phcnkan\",\"description\":\"This is phcn kano prepaid biller\",\"meterType\":\"prepaid\",\"zone\":\"phcnkan\",\"mtype\":\"3\"},{\"id\":\"22\",\"billerName\":\"PHCN Kano Postpaid\",\"billerCode\":\"phcnppkan\",\"description\":\"This is phcn kano postpaid biller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppkan\",\"mtype\":\"6\"}],\"PortHarcourt Electricity|https://demo.etranzact.com/apps/pocketmonibillerlist/phcn_portharcourt.png\":[{\"id\":\"29\",\"billerName\":\"PHCN PortHarcourt Prepaid\",\"billerCode\":\"phcnphe\",\"description\":\"This is phcn PortHarcourt prepaid biller\",\"meterType\":\"prepaid\",\"zone\":\"phcnphe\",\"mtype\":\"3\"},{\"id\":\"30\",\"billerName\":\"PHCN PortHarcourt Postpaid\",\"billerCode\":\"phcnppphe\",\"description\":\"This is phcn PortHarcourt postpaid biller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppphe\",\"mtype\":\"6\"}],\"Abuja Electricity|https://demo.etranzact.com/apps/pocketmonibillerlist/phcn_abuja.png\":[{\"id\":\"23\",\"billerName\":\"PHCN Abuja Prepaid\",\"billerCode\":\"phcnabj\",\"description\":\"This is phcn abuja prepaid biller\",\"meterType\":\"prepaid\",\"zone\":\"phcnabj\",\"mtype\":\"3\"},{\"id\":\"24\",\"billerName\":\"PHCN Abuja Postpaid\",\"billerCode\":\"phcnppabj\",\"description\":\"This is phcn abuja postpaid biller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppabj\",\"mtype\":\"6\"}],\"Kaduna Electricity|https://demo.etranzact.com/apps/pocketmonibillerlist/phcn_kaduna.png\":[{\"id\":\"27\",\"billerName\":\"PHCN Kaduna PrePaid\",\"billerCode\":\"phcnkad\",\"description\":\"This is phcn kaduna prepaid biller\",\"meterType\":\"prepaid\",\"zone\":\"phcnkad\",\"mtype\":\"3\"},{\"id\":\"28\",\"billerName\":\"PHCN Kaduna Postpaid\",\"billerCode\":\"phcnppkad\",\"description\":\"This is phcn Kaduna postpaid biller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppkad\",\"mtype\":\"6\"}]}}";
                //Convert Json to Map
                JSONObject jsonObject = new JSONObject(json);
                responseCode = jsonObject.getString("responseCode");
                if (responseCode.equals("00")) {
                    //Convert Json to Map
                    String data = jsonObject.getJSONObject("data").toString();
                    HashMap<String, Object> map = new ObjectMapper().readValue(data, new TypeReference<Map<String, Object>>() {
                    });
                    if(map.size()>0) planDetails.clear();
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        ArrayList arrayList = ((ArrayList) value);
                        List<ElectricityModel.Plan> eModel = new ArrayList<>();
                        for (int i = 0; i < arrayList.size(); i++) {
                            String id = ((HashMap<String, String>) arrayList.get(i)).get("id");
                            String billerName = ((HashMap<String, String>) arrayList.get(i)).get("billerName");
                            String billerCode = ((HashMap<String, String>) arrayList.get(i)).get("billerCode");
                            String description = ((HashMap<String, String>) arrayList.get(i)).get("description");
                            String meterType = ((HashMap<String, String>) arrayList.get(i)).get("meterType");
                            String zone = ((HashMap<String, String>) arrayList.get(i)).get("zone");
                            String mtype = ((HashMap<String, String>) arrayList.get(i)).get("mtype");
                            Integer minimumAmount = ((HashMap<Integer, Integer>) arrayList.get(i)).get("minAmount");
                            Log.d("Result", "The minimum amount for this " +billerName + " transaction is " + minimumAmount);
                            eModel.add(new ElectricityModel.Plan(id, billerName, billerCode, description, meterType, zone, mtype, String.valueOf(minimumAmount)));
                        }
                        planDetails.put(key, eModel);
                    }
                }
                activity.runOnUiThread(()->{
                    internetListener.requestResponse(""+planDetails.size());
                });

                json = HttpRequest.reqHttp("GET",Emv.electricityCategoryUrl,"",headers);
                responseCode = Keys.parseJson(json, "responseCode");
                if(responseCode.equals("00")){
                    SharedPref.set(activity,KEY,json);
                }
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
        String billerName, billerCode, id, description, meterType, zone, mtype, minimumAmount;
        public Plan(String id, String billerName, String billerCode, String description, String meterType, String zone, String mtype, String minimumAmount) {
            this.id = id;
            this.billerName = billerName;
            this.billerCode = billerCode;
            this.description = description;
            this.meterType = meterType;
            this.zone = zone;
            this.mtype = mtype;
            this.minimumAmount = minimumAmount;
        }

        public String getId() {
            return id;
        }

        public String getBillerName() {
            return billerName;
        }

        public String getBillerCode() {
            return billerCode;
        }

        public String getDescription() {
            return description;
        }

        public String getMeterType() {
            return meterType;
        }

        public String getZone() {
            return zone;
        }

        public String getMtype() {
            return mtype;
        }

        public String getMinimumAmount() {
            return minimumAmount;
        }
    }
}
