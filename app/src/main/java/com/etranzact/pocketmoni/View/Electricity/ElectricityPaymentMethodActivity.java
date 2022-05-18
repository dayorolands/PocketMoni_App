package com.etranzact.pocketmoni.View.Electricity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.etranzact.pocketmoni.Dialogs.LoadingProgressDialog;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.Cashout.TransactionActivity;
import com.etranzact.pocketmoni.View.Electricity.Card.ElectricityCardAmountActivity;
import com.etranzact.pocketmoni.View.Electricity.Card.ElectricityCardEnterPinActivity;
import com.etranzact.pocketmoni.View.Electricity.Cash.ElectricityCashAmountActivity;

import Utils.CardInfo;
import Utils.CardReadMode;
import Utils.PosHandler;

public class ElectricityPaymentMethodActivity extends AppCompatActivity implements PosHandler, View.OnClickListener {

    LoadingProgressDialog loadingDialog;
    CardView debitCardBtn, depositBtn;
    ImageView backBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingProgressDialog(this);
        setContentView(R.layout.electricity_payment_method_activity);
        debitCardBtn = findViewById(R.id.debit_card_id);
        depositBtn = findViewById(R.id.deposit_id);
        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((view)->{
            onBackPressed();
        });
        debitCardBtn.setOnClickListener(this);
        depositBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.debit_card_id){
            startActivity(new Intent(this, ElectricityCardAmountActivity.class));
            //DoInsertCardLogic();
        } else if(v.getId() == R.id.deposit_id){
            startActivity(new Intent(this, ElectricityCashAmountActivity.class));
            //startActivity(new Intent(this, ElectricityEnterPinActivity.class));
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
        startActivity(new Intent(activity, ElectricityCardEnterPinActivity.class));
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
        Toast.makeText(ElectricityPaymentMethodActivity.this, "CARD READ TIMEOUT", Toast.LENGTH_LONG).show();
        Disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CardInfo.StopTransaction(this);
    }
}