package Utils;

import android.util.Log;
import androidx.fragment.app.FragmentActivity;
import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.Model.TransferModel;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.HashMap;

public class NotificationService {
    public static void notifyEtzTms(FragmentActivity activity, String response) {
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("Accept", "*/*");
        hashMap.put("Content-Type", "application/json");
        hashMap.put("Authorization", Emv.accessToken);

        TransDB db = new TransDB(activity);
        db.open();
        try{
            JSONObject json = new JSONObject();
            json.put("mti",Emv.transactionType);
            json.put("masked_pan",Emv.getMaskedPan());
            json.put("processing_code",Emv.processingCode);
            json.put("transaction_amount",Emv.getMinorAmount());
            json.put("amount_settled",Keys.parseISO(response, "5"));
            json.put("stan",Emv.getTransactionStan());
            json.put("transaction_time",Emv.getTransactionTime());
            json.put("transaction_date",Emv.getTransactionDate());
            json.put("card_expiry_date",Keys.parseISO(response, "14"));
            json.put("mcc",Emv.mcc);
            json.put("pos_entry_mode",Emv.posEntryMode);
            json.put("card_sequence_number",Keys.parseISO(response, "23"));
            json.put("pos_condition_code","00");
            json.put("amount_transaction_fee",Keys.parseISO(response, "28"));
            json.put("acquiring_institution_id",Keys.parseISO(response, "32"));
            json.put("forwarding_institution_id",Keys.parseISO(response, "33"));
            json.put("rrn", Keys.genNibssRRN(activity,Emv.transactionStan) );
            json.put("authorization_id",Keys.parseISO(response, "38"));
            json.put("response_code",Emv.responseCode);
            json.put("response_message",Emv.responseMessage);
            json.put("service_restriction_code",Keys.parseISO(response, "40"));
            json.put("terminal_id",Emv.terminalId);
            json.put("merchant_id",Emv.merchantId);
            json.put("merchant_location",Emv.merchantLocation);
            json.put("currency_code",Integer.parseInt(Emv.getEmv("5F2A")));
            json.put("additional_amount","000000000000");
            json.put("icc_data",Keys.parseISO(response, "55"));
            json.put("from_account",Keys.parseISO(response, "102"));
            json.put("to_account",Keys.parseISO(response, "103"));
            json.put("pos_data_code",Emv.posDataCode);
            json.put("battery_level",Emv.getBatteryLevel(activity));
            json.put("paper_roll_status",Sdk.getPrinterState());
            json.put("geo_location",Emv.deviceLocation);
            json.put("agent_location",Emv.agentLoc);
            json.put("agent_id",Emv.agentId);
            json.put("serial",Emv.serialNumber);
            json.put("card_holder_name",URLEncoder.encode(Keys.hexStringToASCII(Emv.getEmv("5F20")),"UTF-8"));
            json.put("reference",Keys.generateReference());
            json.put("transaction_type",Emv.transactionType.toString());
            json.put("processor_name",Emv.environment);
            if(Emv.transactionType == TransType.TRANSFER){
                TransferModel model = new TransferModel();
                json.put("account_no",model.getAcctNo());
                json.put("bank_code",model.getBankCode(model.getBankName()));
            }else if(Emv.transactionType == TransType.ELECTRICITY){
                ElectricityModel model = new ElectricityModel();
                json.put("bill_id","");
                json.put("client_ref","");
                json.put("bill_query_ref","");
                json.put("customer_id","");
                json.put("mobile","");
            }

            String data = Keys.removeSpecialCharacters(json.toString());
            db.insert(Emv.getTransactionDatTime(), data);
            Log.d("Result", "Request: " + data);
            String result = HttpRequest.reqHttp("POST",Emv.notificationURL,data,hashMap);
            Log.d("Result", "Response: " + result);
            String respCode = Keys.parseJson(result, "responseCode");
            if(respCode.equals("00")){
                db.deleteData(Emv.getTransactionDatTime());
                Log.d("Result", "Notification was successful");
            }
        }catch (Exception e){
            String data = ExceptionHandler.SendNIBSSExceptionToTMS(e.toString(),Emv.responseCode,Emv.responseMessage,Keys.parseISO(response, "37"));
            String result = HttpRequest.reqHttp("POST",Emv.notificationURL,data,hashMap);
            String respCode = Keys.parseJson(result, "responseCode");
            if(respCode.equals("00")){
                db.deleteData(Emv.getTransactionDatTime());
                Log.d("Result", "Exception was logged");
            }
        }finally {
            db.close();
        }
    }
}
