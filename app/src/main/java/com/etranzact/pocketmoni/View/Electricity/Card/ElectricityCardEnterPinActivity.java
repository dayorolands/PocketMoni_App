package com.etranzact.pocketmoni.View.Electricity.Card;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DoNotInline;
import androidx.appcompat.app.AppCompatActivity;
import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.Cashout.TransactionActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import Utils.CardInfo;
import Utils.Emv;
import Utils.PosHandler;

public class ElectricityCardEnterPinActivity extends AppCompatActivity implements View.OnClickListener, PosHandler {

    TextView pinText, pinMessage, name, subscriptionPlan, amount, fee, total, outstandingBal;
    Button payButton;
    ImageView btnBack;
    ElectricityModel model;
    double originalAmount = 0, convenientAmount = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.electricity_card_enterpin_activity);
        model = new ElectricityModel();
        pin = "";
        String[] btn1 = {"2","9","8","3","0","6","4","1","5","7"};
        String[] btn2 = {"3","1","4","6","2","5","7","9","0","8"};
        String[] btn3 = {"1","0","2","7","8","5","3","9","4","6"};
        String[] btn4 = {"6","4","3","5","1","8","0","2","7","9"};
        List<String[]> btnArrays = new ArrayList<>();
        btnArrays.add(btn1); btnArrays.add(btn2); btnArrays.add(btn3); btnArrays.add(btn4);
        Random r = new Random();
        int btnArrayIndex = r.nextInt(4);
        pinText = findViewById(R.id.pin_label);
        pinMessage = findViewById(R.id.pin_message);
        name = findViewById(R.id.name_id);
        subscriptionPlan = findViewById(R.id.subscription_plan_id);
        outstandingBal = findViewById(R.id.outstanding_bal_id);
        amount = findViewById(R.id.amount_id);
        fee = findViewById(R.id.fee_id);
        total = findViewById(R.id.total_id);
        payButton = findViewById(R.id.pay_btn);
        btnBack = findViewById(R.id.back_btn_id);
        name.setText(model.getBillerName());
        subscriptionPlan.setText(model.getMeterType());
        outstandingBal.setText(model.getOutstandingBal());
        amount.setText(getAmount());
        fee.setText(getConvenientAmount());
        originalAmount = Double.parseDouble(model.getAmount());
        convenientAmount = Double.parseDouble(model.getConvenientFee());
        Log.d("The_Convenient_Amount", "The convenient Amount is " + convenientAmount);
        //double totalValue = Double.parseDouble(String.format(Locale.getDefault(), "%,.2f",model.getAmount() + model.getConvenientFee()));
        total.setText("TOTAL: ₦" + String.format("%,.2f", (originalAmount + convenientAmount)));

        payButton.setOnClickListener(this);
        btnBack.setOnClickListener((view)->{
            CardInfo.StopTransaction(this);
        });
        //Loop through all the buttons in the control and set onclick listener for them
        LinearLayout linearLayout = findViewById(R.id.pin_buttons);
        int btnNo = 0;
        int i = linearLayout.getChildCount();
        for(int u=0; u<i; u++){
            LinearLayout btnLayout = (LinearLayout) linearLayout.getChildAt(u);
            int j = btnLayout.getChildCount();
            for(int b=0; b<j; b++){
                if(btnLayout.getChildAt(b).getTag().equals("CLR")){
                    Button cb = (Button)btnLayout.getChildAt(b);
                    cb.setOnClickListener(this);
                }else if(btnLayout.getChildAt(b).getTag().equals("DEL")){
                    Button eb = (Button)btnLayout.getChildAt(b);
                    eb.setOnClickListener(this);
                }else{
                    String btnText = btnArrays.get(btnArrayIndex)[btnNo];
                    Button btn = (Button)btnLayout.getChildAt(b);
                    btn.setText(btnText);
                    btnNo++;
                    btn.setOnClickListener(this);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        CardInfo.StopTransaction(this);
    }

    String pin = "";
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_del){
            if(pinText.getText().toString().length() == 0) return;
            pin = "";
            pinText.setText(pin);
        }
        else if(v.getId() == R.id.btn_clear){
            if(pinText.getText().toString().length() == 0) return;
            pin = pin.substring(0,pin.length()-1);
            pinText.setText(pin);
        }
        else if(v.getId() == R.id.pay_btn){
            //Verify pin and start transaction
            btnProceed(pinText.getText().toString());
        }
        else{
            if(pin.length() > 11) return;
            Button b = (Button)v;
            pin += b.getText().toString();
            pinText.setText(pin);
        }
    }

    private String getAmount(){
        return String.format(Locale.getDefault(),"%,.2f",Double.parseDouble(model.getAmount()));
    }
    private String getConvenientAmount(){
        String amount = model.getConvenientFee();
        String amt = String.format(Locale.getDefault(),"%,.2f",Double.parseDouble(amount));
        return amt;
    }

    private void btnProceed(String pinValue)
    {
        if (pinValue.isEmpty())
        {
            Toast.makeText(this, "ENTER YOUR PIN", Toast.LENGTH_SHORT).show();
            return;
        }
        pin = "";
        pinText.setText("");
        pinMessage.setText("");
        Emv.doEnterPinLogic(this,pinValue);
    }

    @Override
    public void onCVMProcessFinished(Activity activity) {
        startActivity(new Intent(activity, TransactionActivity.class));
        finish();
    }

    @Override
    public void onPinVerificationResult(boolean isSuccess, String message) {
        if(!isSuccess){
            pinMessage.setText(message);
            Log.d( "Result", message);
        }
    }
}