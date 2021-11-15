package com.etranzact.pocketmoni.Model;

import android.app.Activity;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import Utils.DateTime;
import Utils.Emv;
import Utils.HttpRequest;
import Utils.Keys;
import Utils.SharedPref;

public class TransferModel {
    private final static HashMap<String,String> bankListAndCode = new HashMap<>();
    private static String bankName,convenientFee,encryptedPin,transferRef,sendersName,phone,acctNo,amount,narration,bankCode,lastName,transactionData;

    Activity activity;
    private String data = "";
    public TransferModel(Activity activity) {
        this.activity = activity;
        initializeParameters();
        data = SharedPref.get(activity, "routeResp", "");
        Log.d("Route_Response","The route response for the data " + data);
        getBanksAndCode();
    }

    public TransferModel(){

    }

    private String getData() {
        String transDate = DateTime.Now.ToString("yyyy-MM-dd+HH:mm:ss").replace("+", "T");
        Emv.transactionDate = formatDate(transDate);
        Emv.transactionTime = formatTime(transDate);
        return "{\n" +
            "  \"serialNo\": \"" + Emv.serialNumber + "\",\n" +
            "  \"terminalId\": \"" + Emv.terminalId + "\",\n" +
            "  \"beneficiaryAccountNo\": \"" + acctNo + "\",\n" +
            "  \"amount\": \"" + amount + "\",\n" +
            "  \"bankcode\": \"" + bankCode + "\",\n" +
            "  \"destinationPhoneNo\": \"" + phone + "\",\n" +
            "  \"narration\": \"" + narration + "\",\n" +
            "  \"firstName\": \"" + sendersName + "\",\n" +
            "  \"lastName\": \"" + lastName + "\",\n" +
            "  \"reference\": \"" + transferRef + "\",\n" +
            "  \"pmPin\": \"" + encryptedPin + "\"\n" +
            "}";
    }

    private void getBanksAndCode(){
        //String data = "{\"responseCode\":\"00\",\"responseMessage\":\"Successful\",\"data\":{\"routes\":[{\"min\":0,\"max\":3000,\"processor\":\"TMS\"},{\"min\":3001,\"max\":10000000,\"processor\":\"NIBSS\"}],\"version\":{\"latest_version\":\"0.0.6\",\"upgrade\":\"TRUE\",\"forceUpgrade\":\"TRUE\"},\"forceConfig\":true,\"active\":true,\"minimum_amount\":36.0,\"terminalConfiguration\":{\"agentLocation\":\"NO 15 ADO BALE EBUTE STREET, AJAH, LAGOS\",\"currencyCode\":\"566\",\"countryCode\":\"0566\",\"logo_url\":\"https://demo.etranzact.com/tms-service/logo\",\"terminalId\":\"2214I13M\",\"agentId\":\"1\",\"tmsBaseUrl\":\"https://demo.etranzact.com\",\"nibbsIp\":\"196.6.103.73\",\"nibbsPort\":\"5043\",\"nibbsEnv\":\"EPMS\",\"nibbsKey\":\"A050F63AFF366A4B0588D818D23C6C77\",\"posDataCode\":\"510101511344101\",\"terminalCapability\":\"E0F8C8\",\"contactlessCvmLimit\":999.0,\"contactlessTransLimit\":100000.0,\"nibbsMerchantId\":\"2214LA565107542\",\"processorMerchantLocation\":\"ETRANZACT INTERNATIO LA LANG\",\"agentName\":\"JOY NWANKWO\",\"minimumAmount\":36.0},\"billersCategories\":{\"status\":true,\"message\":\"Biller category retrieved sucessfully\",\"result\":[{\"id\":\"1\",\"categoryName\":\"Cable TV\",\"description\":\"This is cable TV service biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"1\",\"billerName\":\"GOTV\",\"billerCode\":\"gotv\",\"description\":\"This is gotv biller\"},{\"id\":\"2\",\"billerName\":\"DSTV\",\"billerCode\":\"dstv\",\"description\":\"This is dstv biller\"},{\"id\":\"3\",\"billerName\":\"Startimes\",\"billerCode\":\"startimes\",\"description\":\"This is startimes biller\"}]}},{\"id\":\"2\",\"categoryName\":\"Electricity\",\"description\":\"This is electricity biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"12\",\"billerName\":\"PHCN Enugu Prepaid\",\"billerCode\":\"phcnenu\",\"description\":\"This is phcn enugu prepaid biller\"},{\"id\":\"13\",\"billerName\":\"PHCN Enugu Postpaid\",\"billerCode\":\"phcnppenu\",\"description\":\"This is phcn enugu postpaid biller\"},{\"id\":\"15\",\"billerName\":\"PHCN Eko Prepaid\",\"billerCode\":\"phcneko\",\"description\":\"This is phcn eko prepaid biller\"},{\"id\":\"16\",\"billerName\":\"PHCN Eko Postpaid\",\"billerCode\":\"phcnppeko\",\"description\":\"This is phcn eko postpaid biller\"},{\"id\":\"19\",\"billerName\":\"PHCN Jos Prepaid\",\"billerCode\":\"phcnjos\",\"description\":\"This is phcn jos prepaid biller\"},{\"id\":\"20\",\"billerName\":\"PHCN Jos Postpaid\",\"billerCode\":\"phcnppjos\",\"description\":\"This is phcn jos postpaid biller\"},{\"id\":\"21\",\"billerName\":\"PHCN Kano Prepaid\",\"billerCode\":\"phcnkan\",\"description\":\"This is phcn kano prepaid biller\"},{\"id\":\"22\",\"billerName\":\"PHCN Kaduna Postpaid\",\"billerCode\":\"phcnppkad\",\"description\":\"This is phcn kaduna postpaid biller\"},{\"id\":\"23\",\"billerName\":\"PHCN Abuja Prepaid\",\"billerCode\":\"phcnabj\",\"description\":\"This is phcn abuja prepaid biller\"},{\"id\":\"24\",\"billerName\":\"PHCN Abuja Postpaid\",\"billerCode\":\"phcnppabj\",\"description\":\"This is phcn abuja postpaid biller\"},{\"id\":\"25\",\"billerName\":\"PHCN Ibadan Prepaid\",\"billerCode\":\"phcnibd\",\"description\":\"This is phcn ibadan prepaid biller\"},{\"id\":\"26\",\"billerName\":\"PHCN Kano PostPaid\",\"billerCode\":\"phcnppkan\",\"description\":\"This is phcn kano postpaid biller\"},{\"id\":\"27\",\"billerName\":\"PHCN Kaduna PrePaid\",\"billerCode\":\"phcnkad\",\"description\":\"This is phcn kaduna prepaid biller\"},{\"id\":\"28\",\"billerName\":\"PHCN PortHarcourt Prepaid\",\"billerCode\":\"phcnphe\",\"description\":\"This is phcn portharcourt prepaid biller\"},{\"id\":\"29\",\"billerName\":\"PHCN PortHarcourt Postpaid\",\"billerCode\":\"phcnppphe\",\"description\":\"This is phcn portharcourt postpaid biller\"},{\"id\":\"30\",\"billerName\":\"PHCN Ibadan Postpaid\",\"billerCode\":\"phcnppibd\",\"description\":\"This is phcn ibadan postpaid biller\"}]}},{\"id\":\"3\",\"categoryName\":\"Govt Tax\",\"description\":\"This is Government tax biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"7\",\"billerName\":\"Rev Pay\",\"billerCode\":\"revpay\",\"description\":\"This is rev pay biller\"}]}},{\"id\":\"4\",\"categoryName\":\"Internet Services\",\"description\":\"This is internet services biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"5\",\"billerName\":\"Smile\",\"billerCode\":\"smile\",\"description\":\"This is smile biller\"},{\"id\":\"6\",\"billerName\":\"Swift\",\"billerCode\":\"swift\",\"description\":\"This is swift biller\"}]}},{\"id\":\"5\",\"categoryName\":\"Toll Service\",\"description\":\"This is toll service biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"4\",\"billerName\":\"LCC\",\"billerCode\":\"lcc\",\"description\":\"This is lcc biller\"}]}}]},\"banks\":[{\"code\":\"070\",\"name\":\"Fidelity\"},{\"code\":\"076\",\"name\":\"Polaris\"},{\"code\":\"011\",\"name\":\"First Bank\"}]},\"timeStamp\":\"2021-03-26T09:47:50.910+00:00\"}";
        bankListAndCode.clear();
        try{
            JSONObject obj = new JSONObject(data);
            JSONArray banksArray = obj.getJSONObject("data").getJSONArray("banks");
            for (int i = 0; i < banksArray.length(); i++) {
                String bName = banksArray.getJSONObject(i).getString("name");
                String bCode = banksArray.getJSONObject(i).getString("code");
                bankListAndCode.put(bCode,bName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<String> getBanks(){
        List<String> banks = new ArrayList<>();
        for(Map.Entry<String,String> entry : bankListAndCode.entrySet()){
            banks.add(entry.getValue());
        }
        Collections.sort(banks, String::compareTo);
        banks.add(0,"Select Bank");
        return banks;
    }

    public String getBankCode(String bankName){
        for(Map.Entry<String,String> entry : bankListAndCode.entrySet()){
            if(bankName.equals(entry.getValue())){
                return entry.getKey();
            }
        }
        return "";
    }

    public String getTransferRef(){
        return transferRef;
    }

    public double getMinCvAmount(){
        return Double.parseDouble(Keys.parseJson(data, "minCvAmount"));
    }

    public double getMaxCvAmount(){
        return Double.parseDouble(Keys.parseJson(data, "maxCvAmount"));
    }

    public String getConvenientFee() {
        return convenientFee;
    }

    public String getSendersName(){
        return sendersName;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAmount() {
        return amount;
    }

    public String getAcctNo(){
        return acctNo;
    }

    public String getNarration(){
        return narration;
    }

    public String getPhone(){
        return phone;
    }

    public void setBankName(String bankName) {
        TransferModel.bankName = bankName;
    }

    public void setConvenientFee(String convenientFee) {
        TransferModel.convenientFee = convenientFee;
    }

    public void setAcctNo(String acctNo) {
        TransferModel.acctNo = acctNo;
    }

    public void setAmount(String amount) {
        TransferModel.amount = amount;
    }

    public void setBankCode(String bankCode) {
        TransferModel.bankCode = bankCode;
    }

    public void setPhone(String phone) {
        TransferModel.phone = phone;
    }

    public void setNarration(String narration) {
        TransferModel.narration = narration;
    }

    public void setSendersName(String sendersName) {
        TransferModel.sendersName = sendersName;
    }

    public void setLastName(String lastName) {
        TransferModel.lastName = lastName;
    }

    public void setTransferRef(String transferRef) {
        TransferModel.transferRef = transferRef;
    }

    public void setEncryptedPin(String encryptedPin) {
        TransferModel.encryptedPin = encryptedPin;
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
        Log.d("Result", "Request: " + getData());
        String response = HttpRequest.reqHttp("POST", Emv.transferCashUrl, getData(), hashMap);
        Log.d("Result", "Response: " + response);
        return response;
    }

    private static String formatDate(String datetime) {
        return datetime.substring(0, datetime.indexOf("T"));
    }
    private static String formatTime(String datetime) {
        return datetime.substring(datetime.indexOf("T") + 1);
    }

    public String getTransactionData(Activity activity){
        String resultRRN = Keys.genKimonoRRN(activity, Emv.transactionStan);
        return Emv.getTransactionDate() + "|" + //0 TRANSACTION DATE
                Emv.getTransactionTime() + "|" + //1 TRANSACTION TIME
                getSendersName() + "|" + //2 CARD HOLDER NAME
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
                getSendersName() + "|" + //13 Transfer Name
                getAcctNo() + "|" + //14 Transfer Account No
                getTransferRef(); //15 Transfer Ref
    }

    private void initializeParameters(){
        bankName = "";
        convenientFee = "";
        encryptedPin = "";
        transferRef = "";
        sendersName = "";
        phone = "";
        acctNo = "";
        amount = "";
        narration = "";
    }
}
