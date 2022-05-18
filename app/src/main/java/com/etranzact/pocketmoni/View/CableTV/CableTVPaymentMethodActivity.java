package com.etranzact.pocketmoni.View.CableTV;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.etranzact.pocketmoni.Dialogs.LoadingProgressDialog;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.CableTV.Card.CableTVCardEnterPinActivity;
import com.etranzact.pocketmoni.View.CableTV.Cash.CableTVEnterPinActivity;
import com.etranzact.pocketmoni.View.Cashout.TransactionActivity;
import Utils.CardInfo;
import Utils.CardReadMode;
import Utils.PosHandler;

public class CableTVPaymentMethodActivity extends AppCompatActivity implements PosHandler, View.OnClickListener {

    LoadingProgressDialog loadingDialog;
    CardView debitCardBtn, depositBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingProgressDialog(this);
        setContentView(R.layout.cable_tv_payment_method_activity);
        debitCardBtn = findViewById(R.id.debit_card_id);
        depositBtn = findViewById(R.id.deposit_id);
        debitCardBtn.setOnClickListener(this);
        depositBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.debit_card_id){
            //DoInsertCardLogic();
        } else if(v.getId() == R.id.deposit_id){
            startActivity(new Intent(this, CableTVEnterPinActivity.class));
            finish();
        }
    }

    private void DoInsertCardLogic(){
        loadingDialog.show();
        loadingDialog.setProgressLabel("Waiting for card", false);
        CardInfo.initialize(this);
    }

    @Override
    public void onEnterPinRequested(Activity activity) {
        loadingDialog.dismiss();
        startActivity(new Intent(activity, CableTVCardEnterPinActivity.class));
        finish();
    }

    @Override
    public void onCVMProcessFinished(Activity activity) {
        startActivity(new Intent(activity, TransactionActivity.class));
        finish();
    }

    public void Disconnect(){
        CardInfo.StopTransaction(this);
    }

    @Override
    public void onDetectICCard(CardReadMode cardType) {
        loadingDialog.setProgressLabel("Reading Card", false);
    }

    @Override
    public void onDetectContactlessCard(CardReadMode cardType) {
        loadingDialog.setProgressLabel("Reading Card", false);
    }

    @Override
    public void onCardTimeount() {
        Toast.makeText(CableTVPaymentMethodActivity.this, "CARD READ TIMEOUT", Toast.LENGTH_LONG).show();
        Disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CardInfo.StopTransaction(this);
    }
}