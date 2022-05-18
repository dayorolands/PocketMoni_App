package Utils;

import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.Model.TransferModel;
import org.json.JSONObject;

public class ExceptionHandler {
    public static String SendNIBSSExceptionToTMS(String exception, String responseCode, String responseMessage, String rrn){
        try {
            JSONObject json = new JSONObject();
            json.put("mti",Emv.transactionType);
            json.put("masked_pan",Emv.getMaskedPan());
            json.put("processing_code","000000");
            json.put("transaction_amount",Emv.getMinorAmount());
            json.put("amount_settled","000000000000");
            json.put("stan",Emv.getTransactionStan());
            json.put("transaction_time",Emv.getTransactionTime());
            json.put("transaction_date",Emv.getTransactionDate());
            json.put("card_expiry_date","2210");
            json.put("mcc",Emv.mcc);
            json.put("pos_entry_mode",Emv.posEntryMode);
            json.put("card_sequence_number","001");
            json.put("pos_condition_code","00");
            json.put("amount_transaction_fee","C00000000");
            json.put("acquiring_institution_id","");
            json.put("forwarding_institution_id","628009");
            json.put("rrn",rrn);
            json.put("authorization_id","633715");
            json.put("response_code",responseCode);
            json.put("response_message",responseMessage);
            json.put("service_restriction_code","221");
            json.put("terminal_id",Emv.terminalId);
            json.put("merchant_id",Emv.merchantId);
            json.put("merchant_location",Emv.merchantLocation);
            json.put("currency_code","566");
            json.put("additional_amount","000000000000");
            json.put("icc_data","910A6BA0BECE67F478130012");
            json.put("from_account","0000000000");
            json.put("to_account","");
            json.put("pos_data_code","510101511344101");
            json.put("battery_level","72");
            json.put("paper_roll_status","0");
            json.put("geo_location",Emv.deviceLocation);
            json.put("agent_location",Emv.agentLoc);
            json.put("agent_id",Emv.agentId);
            json.put("serial",Emv.serialNumber);
            json.put("card_holder_name",exception);
            json.put("reference",Keys.generateReference());
            json.put("transaction_type",Emv.transactionType.toString());
            json.put("processor_name",Emv.environment);
            if(Emv.transactionType == TransType.TRANSFER){
                TransferModel model = new TransferModel();
                json.put("account_no",model.getAcctNo());
                json.put("bank_code",model.getBankCode(model.getBankName()));
            }else if(Emv.transactionType == TransType.ELECTRICITY){
                ElectricityModel model = new ElectricityModel();
                json.put("bill_id",model.getBillerCode());
                json.put("client_ref","");
                json.put("bill_query_ref","");
                json.put("customer_id","");
                json.put("mobile","");
            }
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
