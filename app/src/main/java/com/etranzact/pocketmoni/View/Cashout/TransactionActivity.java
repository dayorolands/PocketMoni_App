package com.etranzact.pocketmoni.View.Cashout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.etranzact.pocketmoni.Model.CardModel;
import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.TransEnvironment.TMS;
import com.sdk.pocketmonisdk.TransEnvironment.Nibss;
import java.util.Locale;
import Utils.CardInfo;
import Utils.Emv;
import Utils.Keys;
import Utils.NotificationService;
import Utils.TransDB;
import Utils.TransRoute;
import Utils.TransType;

public class TransactionActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trans_processing_activity);
        startTransaction();
    }

    void startTransaction() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String response = "";
                switch (Emv.environment) {
                    case "NIBSS":
                        response = Nibss.processTransaction(TransactionActivity.this);
                        break;
                    case "TMS":
                        response = TMS.processTransaction(TransactionActivity.this);
                        break;
                    case "OTHERS":
                        // Do nothing
                        break;
                    default:
                        break;
                }
                Bundle bundle = new Bundle();
                Message msg = new Message();
                bundle.putString("RESPONSE", response);
                msg.setData(bundle);
                OnTransactionResponse.sendMessage(msg);
            }
        });
        t.start();
    }

    Handler OnTransactionResponse = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String response = bundle.getString("RESPONSE");

            if(response == null){
                Toast.makeText(TransactionActivity.this, "TRANSACTION ROUTE NOT FOUND", Toast.LENGTH_LONG).show();
                CardInfo.StopTransaction(TransactionActivity.this);
                return false;
            }

            //PROCESS NIBSS RESPONSE
            if (Emv.environment.equals("NIBSS")) {
                if (response.equals("REVERSAL_OK")) {
                    Toast.makeText(TransactionActivity.this, "TRANSACTION REVERSED", Toast.LENGTH_SHORT).show();
                    CardInfo.StopTransaction(TransactionActivity.this);
                    return false;
                } else if (response.equals("REVERSAL_FAIL")) {
                    Emv.responseCode = "404";
                    Emv.responseMessage = "Network Timeout";
                    Emv.setEmv("8A", Keys.asciiToHex("91"));
                    notifyEtz(response);
                    ShowDeclined();
                    return false;
                }else if(response.equals("12001")){
                    Toast.makeText(TransactionActivity.this,"Kindly perform key exchange", Toast.LENGTH_LONG).show();
                    CardInfo.StopTransaction(TransactionActivity.this);
                    return false;
                }

                if (!response.isEmpty()) {
                    String respCode = Keys.parseISO(response, "39");
                    String field55 = Keys.parseISO(response, "55");
                    if (!field55.isEmpty())
                        Emv.parseResponse(field55, ""); //Parse field 55 and get EMV data;

                    //Route transaction to TMS when response code is 91 3 times consecutively
                    TransRoute.setRouteRespCode(TransactionActivity.this, respCode);

                    //Response code 00 signifies approved
                    if (respCode.equals("00")) {
                        Emv.responseCode = respCode;
                        Emv.responseMessage = "TRANSACTION APPROVED";
                        String st1 = Emv.getEmv("71");
                        String st2 = Emv.getEmv("72");
                        Emv.setEmv("8A", Keys.asciiToHex(respCode));
                        notifyEtz(response);
                        ShowApproved();
                    } else {
                        String respMessage = Keys.getIsoMessage(respCode);
                        Emv.responseCode = respCode;
                        Emv.responseMessage = respMessage;
                        Emv.setEmv("8A", Keys.asciiToHex(respCode));
                        notifyEtz(response);
                        ShowDeclined();
                    }
                }else{
                    Toast.makeText(TransactionActivity.this, "NETWORK TIMEOUT", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            //PROCESS TMS RESPONSE
            else if (Emv.environment.equals("TMS")) {

                //response = "{\"responseCode\":\"00\",\"responseMessage\":\"Transaction Approved\",\"data\":{\"description\":\"Issuer or switch inoperative\",\"field39\":\"91\",\"stan\":\"24\",\"hostEmvData\":{\"atc\":\"0C49\",\"iad\":\"72D5E929008000000000000000000000\",\"rc\":\"00\",\"terminalType\":null,\"unpredictableNumber\":null,\"dedicatedFileName\":null,\"transactionDate\":null,\"transactionType\":null,\"terminalCountryCode\":null,\"terminalCapabilities\":null,\"cryptogram\":null,\"cvmResults\":null,\"amountAuthorized\":\"0\",\"cryptogramInformationData\":null,\"amountOther\":\"0\",\"applicationInterchangeProfile\":null,\"transactionCurrencyCode\":null,\"terminalVerificationResult\":null},\"transactionChannelName\":\"ASPFEP\",\"wasReceive\":\"true\",\"wasSend\":\"true\",\"referenceNumber\":null},\"timeStamp\":\"2020-12-27T15:31:41.664+00:00\"}";

                if (!response.isEmpty()) {
                    String respCode = Keys.parseJson(response, "field39");
                    if(respCode.isEmpty()) respCode = Keys.parseJson(response, "responseCode");

                    String field55 = Keys.parseISO(response, "55");
                    if (!field55.isEmpty())
                        Emv.parseResponse(field55, ""); //Parse field 55 and get EMV data;

                    //Route transaction to NIBSS when response code is 91 3 times consecutively
                    TransRoute.setRouteRespCode(TransactionActivity.this, respCode);
                    //Response code 00 signifies approved
                    if (respCode.equals("00")) {
                        Emv.responseCode = respCode;
                        Emv.responseMessage = "TRANSACTION APPROVED";
                        String iad = Keys.parseJson(response, "iad");
                        String st1 = Keys.parseJson(response, "st1");
                        String st2 = Keys.parseJson(response, "st2");
                        if (!iad.isEmpty()) Emv.setEmv("91", iad);
                        if (!st1.isEmpty()) Emv.setEmv("71", st1);
                        if (!st2.isEmpty()) Emv.setEmv("72", st2);
                        if (Emv.transactionType == TransType.ELECTRICITY){
                        String token = Keys.parseJson(response, "rechargeToken");
                        ElectricityModel model = new ElectricityModel();
                        model.setToken(token);
                        }
                        Emv.setEmv("8A", Keys.asciiToHex(respCode));
                        ShowApproved();
                    } else {
                        String respMessage = Keys.parseJson(response, "description");
                        if(respMessage.isEmpty()) respMessage = Keys.parseJson(response, "responseMessage");
                        Emv.responseCode = respCode;
                        Emv.responseMessage = respMessage;
                        String iad = Keys.parseJson(response, "iad");
                        if (!iad.isEmpty()) Emv.setEmv("91", iad);
                        Emv.setEmv("8A", Keys.asciiToHex(respCode));
                        ShowDeclined();
                    }
                } else {
                    Emv.responseCode = "";
                    Emv.responseMessage = "";
                    ShowDeclined();
                }
            }
            return true;
        }
    });

    private void notifyEtz(String response) {
        new Thread(()->{
            NotificationService.notifyEtzTms(TransactionActivity.this, response);
        }).start();
    }

    private void ShowApproved() {
        TransDB eod = new TransDB(TransactionActivity.this);
        try {
            String amt = String.format(Locale.getDefault(),"%,.2f", Double.parseDouble(Emv.getMinorAmount()) / 100);
            eod.open();
            eod.insert(Emv.getTransactionDatTime(), Emv.transactionType, amt, Emv.responseCode,
                    Emv.getMaskedPan(), CardModel.getTransactionData(TransactionActivity.this));
            eod.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        startActivity(new Intent(this,ApprovedActivity.class));
        finish();
    }

    void ShowDeclined() {
        TransDB eod = new TransDB(TransactionActivity.this);
        try {
            String amt = String.format(Locale.getDefault(),"%,.2f", Double.parseDouble(Emv.getMinorAmount()) / 100);
            eod.open();
            if(Emv.environment.equals("TMS") && Emv.responseMessage.isEmpty()){
                eod.insert(Emv.getTransactionDatTime(), Emv.transactionType, amt, Emv.responseCode, Emv.getMaskedPan(),
                        CardModel.getTransactionData(TransactionActivity.this) + "|" + CardModel.getRequeryPayload());
            }else{
                eod.insert(Emv.getTransactionDatTime(), Emv.transactionType, amt, Emv.responseCode, Emv.getMaskedPan(),
                        CardModel.getTransactionData(TransactionActivity.this));
            }
            eod.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        startActivity(new Intent(this,FailedActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() { }
}