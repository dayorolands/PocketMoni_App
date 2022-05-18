package com.etranzact.pocketmoni.View.Cashout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.R;

import java.text.DecimalFormat;

import Utils.CardInfo;
import Utils.CardReadMode;
import Utils.Emv;
import Utils.Keys;
import Utils.MyPrinter;
import Utils.PosHandler;
import Utils.TransRoute;

public class AmountActivity extends AppCompatActivity implements View.OnClickListener, PosHandler {

    public static ProgressBar progressBar;
    LinearLayout linearLayout;
    TextView amountText, insertCardLabel;
    ImageView backBtn;
    String amtText = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.amount_activity);
        progressBar = findViewById(R.id.progress_bar);
        insertCardLabel = findViewById(R.id.insert_card_label);
        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((view)->{
            Disconnect();
        });
        Emv.initializeEmv(this);
        //Checks if there is any restriction for the agent
//        if(Keys.isAgentRestricted(this)){
//            Disconnect();
//            return null;
//        }

        amtText = "";
        amountText = findViewById(R.id.amt_label);
        //Loop through all the buttons in the control and set onclick listener for them
        linearLayout = findViewById(R.id.btn_layout);
        int i = linearLayout.getChildCount();
        for(int u=0; u<i; u++){
            RelativeLayout btnLayout = (RelativeLayout) ((LinearLayout) linearLayout.getChildAt(u)).getChildAt(0);
            int j = btnLayout.getChildCount();
            for(int b=0; b<j; b++){
                if(btnLayout.getChildAt(b).getTag().equals("button")){
                    Button btn = (Button)btnLayout.getChildAt(b);
                    btn.setOnClickListener(this);
                }
                else if(btnLayout.getChildAt(b).getTag().equals("clear_button")){
                    Button cb = (Button)btnLayout.getChildAt(b);
                    cb.setOnClickListener(this);
                }else if(btnLayout.getChildAt(b).getTag().equals("enter_button")){
                    Button eb = (Button)btnLayout.getChildAt(b);
                    eb.setOnClickListener(this);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_enter){
            String newAmt = amountText.getText().toString();
            if(newAmt.equals("") || newAmt.equals("0.00")){
                Toast.makeText(AmountActivity.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if(Keys.isMinimumAmount(AmountActivity.this, newAmt)){
                Toast.makeText(AmountActivity.this, "Amount cannot be less that minimum amount", Toast.LENGTH_SHORT).show();
                return;
            }

            String amtF = String.valueOf(Double.parseDouble(newAmt.replace(",", "")) * 100);
            DecimalFormat df = new DecimalFormat("###.#");
            String amt = "9F02|" + Keys.padLeft(df.format(Double.parseDouble(amtF)),12,'0');
            Emv.AmountAuthorized = amt;

            //Set the transaction route to use for transaction
            TransRoute.setTransactionRoute(AmountActivity.this, amt.split("\\|")[1]);

            //Enable account selection
            //new AccountSelectionDialog(getActivity()).show();
            
            DoInsertCardLogic();
        }
        else if(v.getId() == R.id.btn_clear){
            double txtAmt = Double.parseDouble(amountText.getText().toString().replace(",",""));
            if(txtAmt == 0){
                amountText.setText("0.00");
            }else{
                amtText = amtText.substring(0,amtText.length()-1);
                if(amtText.isEmpty()) amtText = "0";
                String fAmt = formatAmount(amtText);
                amountText.setText(fAmt);
            }
        }
        else{
            if(amtText.length() > 11) return;
            Button b = (Button)v;
            amtText += b.getText().toString();
            String fAmt = formatAmount(amtText);
            amountText.setText(fAmt);
        }
    }

    private String formatAmount(String amtString){
        Long amt = Long.parseLong(amtString);
        amtText = amt.toString();
        double amtD = (double)amt/100;
        return String.format("%,.2f",amtD);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        CardInfo.CancelCardSearch();
        super.onPause();
    }

    private void DoInsertCardLogic(){
        insertCardLabel.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);
        CardInfo.initialize(this);
    }

    private void Disconnect(){
        CardInfo.StopTransaction(this);
    }

    @Override
    public void onDetectICCard(CardReadMode cardType) {
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDetectContactlessCard(CardReadMode cardType) {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCardTimeount() {
        Toast.makeText(AmountActivity.this, "CARD READ TIMEOUT", Toast.LENGTH_LONG).show();
        Disconnect();
    }

    @Override
    public void onCVMProcessFinished(Activity activity) {
        startActivity(new Intent(activity, TransactionActivity.class));
        activity.finish();
    }

    @Override
    public void onEnterPinRequested(Activity activity) {
        startActivity(new Intent(activity, EnterPinActivity.class));
        activity.finish();
    }

    @Override
    public void onBackPressed() {
        Disconnect();
    }
}