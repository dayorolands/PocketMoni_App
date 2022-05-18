package com.etranzact.pocketmoni.View.CableTV.Cash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.etranzact.pocketmoni.Model.CableTVModel;
import com.etranzact.pocketmoni.R;
import java.util.Locale;
import Utils.Emv;
import Utils.Keys;
import Utils.TransDB;

public class CableTVTransactionActivity extends AppCompatActivity {

    CableTVModel model;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deposite_transaction_activity);
        model = new CableTVModel();
        startTransaction();
    }

    void startTransaction() {
        Thread t = new Thread(()->{
            String response =  model.processDeposit(CableTVTransactionActivity.this);
            Bundle bundle = new Bundle();
            Message msg = new Message();
            bundle.putString("RESPONSE", response);
            msg.setData(bundle);
            OnTransactionResponse.sendMessage(msg);
        });
        t.start();
    }

    Handler OnTransactionResponse = new Handler(Looper.getMainLooper(), (msg)->{
        Bundle bundle = msg.getData();
        String response = bundle.getString("RESPONSE");

        if(response == null){
            Toast.makeText(CableTVTransactionActivity.this, "TRANSACTION ROUTE NOT FOUND", Toast.LENGTH_LONG).show();
            return false;
        }

        String respCode = Keys.parseJson(response, "responseCode");
        String respMessage = Keys.parseJson(response, "responseMessage");
        String ref = Keys.parseJson(response, "data");
        model.setPaymentRef(ref);

        //PROCESS TMS RESPONSE
        if(respCode.equals("")){
            Toast.makeText(CableTVTransactionActivity.this,"No response from server, Try again", Toast.LENGTH_LONG).show();
            finish();
        } else if (respCode.equals("00")) {
            //Set the description to display on receipt
            model.setDescription(respMessage);
            Emv.responseCode = respCode;
            Emv.responseMessage = "TRANSACTION APPROVED";
            ShowApproved();
        } else {
            Emv.responseCode = respCode;
            Emv.responseMessage = respMessage;
            ShowDeclined();
        }
        return true;
    });

    private void ShowApproved() {
        TransDB eod = new TransDB(this);
        try {
            String amt = String.format(Locale.getDefault(),"%,.2f", Double.parseDouble(model.getAmount()));
            eod.open();
            eod.insert(Emv.getTransactionDatTime(), Emv.transactionType, amt, Emv.responseCode, "000000*******0000", model.getTransactionData(this));
            eod.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            eod.close();
        }
        startActivity(new Intent(this, CableTVApprovedActivity.class));
        finish();
    }

    void ShowDeclined() {
        TransDB eod = new TransDB(this);
        try {
            String amt = String.format(Locale.getDefault(),"%,.2f", Double.parseDouble(model.getAmount()));
            eod.open();
            eod.insert(Emv.getTransactionDatTime(), Emv.transactionType, amt, Emv.responseCode, "000000*******0000", model.getTransactionData(this));
            eod.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            eod.close();
        }
        startActivity(new Intent(this, CableTVFailedActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() { }
}