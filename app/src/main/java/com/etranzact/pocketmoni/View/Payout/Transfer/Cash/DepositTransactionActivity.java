package com.etranzact.pocketmoni.View.Payout.Transfer.Cash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.etranzact.pocketmoni.Model.TransferModel;
import com.etranzact.pocketmoni.R;
import java.util.Locale;
import Utils.Emv;
import Utils.Keys;
import Utils.TransDB;

public class DepositTransactionActivity extends AppCompatActivity {

    TransferModel model;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deposite_transaction_activity);
        model = new TransferModel();
        startTransaction();
    }

    void startTransaction() {
        Thread t = new Thread(()->{
            String response =  model.processDeposit(DepositTransactionActivity.this);
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
            Toast.makeText(DepositTransactionActivity.this, "TRANSACTION ROUTE NOT FOUND", Toast.LENGTH_LONG).show();
            return false;
        }

        String respCode = Keys.parseJson(response, "responseCode");

        //PROCESS TMS RESPONSE
        if(respCode.equals("")){
            Toast.makeText(DepositTransactionActivity.this,"No response from server, Try again", Toast.LENGTH_LONG).show();
            finish();
        } else if (respCode.equals("00")) {
            Emv.responseCode = respCode;
            Emv.responseMessage = "TRANSACTION APPROVED";
            ShowApproved();
        } else {
            String respMessage = Keys.parseJson(response, "description");
            if(respMessage.isEmpty()) respMessage = Keys.parseJson(response, "responseMessage");
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
        startActivity(new Intent(this, DepositApprovedActivity.class));
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
        startActivity(new Intent(this, DepositFailedActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() { }
}