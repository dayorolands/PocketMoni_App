package com.etranzact.pocketmoni.Model;

import android.app.Activity;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final String EKEY = "cat_key";
    public final String PIN_KEY = "A8BF4C78F2EEEDB96FAF3A0655454345";
    public final String CUSTOM_PAN = "0000000000000000";

    private static String billerName, id, convenientFee, billerCode, description,meterType, zone, mtype, sessionCategory, customerId, customerName,outstandingBal,amount,pin,paymentRef,address,token;
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
        return new Plan("","","","","","","");
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
                String json = SharedPref.get(activity,EKEY,"");
                HashMap<String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization",Emv.accessToken);
                if(json.isEmpty() || json.contains("responseCode") || json.contains("code")){
                    json = HttpRequest.reqHttp("GET",Emv.electricityCategoryUrl,"",headers);
                }
                //String json = "{\"PortHarcourtElectricity\":[{\"billerName\":\"PortHarcourtPrepaid\",\"billerCode\":\"phcnphe\",\"description\":\"ThisisPortHarcourtprepaidbiller\",\"meterType\":\"prepaid\",\"zone\":\"phcnphe\",\"mtype\":\"6\"},{\"billerName\":\"PortHarcourtPostpaid\",\"billerCode\":\"phcnppphe\",\"description\":\"ThisisPortHarcourtpostpaidbiller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppphe\",\"mtype\":\"3\"}],\"EkoElectricity\":[{\"billerName\":\"EkoPrepaid\",\"billerCode\":\"phcneko\",\"description\":\"Thisisekoprepaidbiller\",\"meterType\":\"prepaid\",\"zone\":\"phcneko\",\"mtype\":\"6\"},{\"billerName\":\"EkoPostpaid\",\"billerCode\":\"phcnppeko\",\"description\":\"Thisisekopostpaidbiller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppeko\",\"mtype\":\"3\"}],\"IbadanElectricity\":[{\"billerName\":\"IbadanPrepaid\",\"billerCode\":\"phcnibd\",\"description\":\"Thisisibadanprepaidbiller\",\"meterType\":\"prepaid\",\"zone\":\"phcnibd\",\"mtype\":\"6\"},{\"billerName\":\"IbadanPostpaid\",\"billerCode\":\"phcnppibd\",\"description\":\"ThisisIbadanpostpaidbiller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppibd\",\"mtype\":\"3\"}],\"AbujaElectricity\":[{\"billerName\":\"AbujaPrepaid\",\"billerCode\":\"phcnabj\",\"description\":\"Thisisabujaprepaidbiller\",\"meterType\":\"prepaid\",\"zone\":\"phcnabj\",\"mtype\":\"6\"},{\"billerName\":\"AbujaPostpaid\",\"billerCode\":\"phcnppabj\",\"description\":\"Thisisabujapostpaidbiller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppabj\",\"mtype\":\"3\"}],\"KadunaElectricity\":[{\"billerName\":\"KadunaPrePaid\",\"billerCode\":\"phcnkad\",\"description\":\"Thisiskadunaprepaidbiller\",\"meterType\":\"prepaid\",\"zone\":\"phcnkad\",\"mtype\":\"6\"},{\"billerName\":\"KadunaPostpaid\",\"billerCode\":\"phcnppkad\",\"description\":\"ThisisKadunapostpaidbiller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppkad\",\"mtype\":\"3\"}],\"KanoElectricity\":[{\"billerName\":\"KanoPrepaid\",\"billerCode\":\"phcnkan\",\"description\":\"Thisiskanoprepaidbiller\",\"meterType\":\"prepaid\",\"zone\":\"phcnkan\",\"mtype\":\"6\"},{\"billerName\":\"KanoPostpaid\",\"billerCode\":\"phcnppkan\",\"description\":\"Thisiskanopostpaidbiller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppkan\",\"mtype\":\"3\"}],\"EnuguElectricity\":[{\"billerName\":\"EnuguPrepaid\",\"billerCode\":\"phcnenu\",\"description\":\"Thisisenuguprepaidbiller\",\"meterType\":\"prepaid\",\"zone\":\"phcnenu\",\"mtype\":\"6\"},{\"billerName\":\"EnuguPostpaid\",\"billerCode\":\"phcnppenu\",\"description\":\"Thisisenugupostpaidbiller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppenu\",\"mtype\":\"3\"}],\"JosElectricity\":[{\"billerName\":\"JosPrepaid\",\"billerCode\":\"phcnjos\",\"description\":\"Thisisjosprepaidbiller\",\"meterType\":\"prepaid\",\"zone\":\"phcnjos\",\"mtype\":\"6\"},{\"billerName\":\"JosPostpaid\",\"billerCode\":\"phcnppjos\",\"description\":\"Thisisjospostpaidbiller\",\"meterType\":\"postpaid\",\"zone\":\"phcnppjos\",\"mtype\":\"3\"}]}";
                //Convert Json to Map
                HashMap<String, Object> map = new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>(){});
                if(map.size()>0) planDetails.clear();
                for(Map.Entry<String,Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    ArrayList arrayList = ((ArrayList) value);
                    List<Plan> eModel = new ArrayList<>();
                    for (int i = 0; i < arrayList.size(); i++) {
                        String id = ((HashMap<String, String>) arrayList.get(i)).get("id");
                        String billerName = ((HashMap<String, String>) arrayList.get(i)).get("billerName");
                        String billerCode = ((HashMap<String, String>) arrayList.get(i)).get("billerCode");
                        String description = ((HashMap<String, String>) arrayList.get(i)).get("description");
                        String meterType = ((HashMap<String, String>) arrayList.get(i)).get("meterType");
                        String zone = ((HashMap<String, String>) arrayList.get(i)).get("zone");
                        String mtype = ((HashMap<String, String>) arrayList.get(i)).get("mtype");
                        eModel.add(new Plan(id, billerName, billerCode, description, meterType, zone, mtype));
                    }
                    planDetails.put(key,eModel);
                }
                activity.runOnUiThread(()->{
                    internetListener.requestResponse(""+planDetails.size());
                });

//                json = HttpRequest.reqHttp("GET",Emv.electricityCategoryUrl,"",headers);
//                if((!json.isEmpty()) && (!json.contains("responseCode"))){
//                    SharedPref.set(activity,EKEY,json);
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
        String billerName, billerCode, id, description, meterType, zone, mtype;
        public Plan(String id, String billerName, String billerCode, String description, String meterType, String zone, String mtype) {
            this.id = id;
            this.billerName = billerName;
            this.billerCode = billerCode;
            this.description = description;
            this.meterType = meterType;
            this.zone = zone;
            this.mtype = mtype;
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
    }
}
