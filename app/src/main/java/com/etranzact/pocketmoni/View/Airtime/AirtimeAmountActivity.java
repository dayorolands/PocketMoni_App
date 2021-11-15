package com.etranzact.pocketmoni.View.Airtime;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.etranzact.pocketmoni.Model.AirtimeModel;
import com.etranzact.pocketmoni.R;
import java.text.DecimalFormat;
import java.util.Locale;
import Utils.Emv;
import Utils.Keys;
import Utils.PosHandler;

public class AirtimeAmountActivity extends AppCompatActivity implements PosHandler, View.OnClickListener {

    AirtimeModel model;
    EditText amount, phone;
    LinearLayout amountLayout;
    ImageView backBtn;
    Button nextBtn;
    TextView title;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airtime_amount_activity);
        model = new AirtimeModel();
        title = findViewById(R.id.title_label_id);
        amountLayout = findViewById(R.id.select_amount_layout);
        amount = findViewById(R.id.amount_id);
        phone = findViewById(R.id.phone_no_id);
        nextBtn = findViewById(R.id.next_btn_id);
        nextBtn.setOnClickListener(this);
        title.setText(model.getBillsName().toUpperCase());
        amount.addTextChangedListener(OnAmountTextChanged);
        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((view)->{
            onBackPressed();
        });

        int count = amountLayout.getChildCount();
        for(int i=0; i<count; i++){
            TextView tv = (TextView)amountLayout.getChildAt(i);
            tv.setOnClickListener((v)->{
                String amt = ((TextView)v).getText().toString().replace("₦","");
                String minorAmt = String.valueOf(Double.parseDouble(amt) * 10);
                amount.setText(minorAmt);
            });
        }
        Emv.initializeEmv(AirtimeAmountActivity.this);
    }

    TextWatcher OnAmountTextChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        private String current = "";
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().equals(current) && amount.getText().length() > 1){
                amount.removeTextChangedListener(this);
                String cleanString = s.toString().replaceAll("[#,.]", "");
                String formatted = formatAmount(cleanString);
                current = formatted;
                amount.setText(formatted);
                amount.setSelection(formatted.length());
                amount.addTextChangedListener(this);

                nextBtn.setVisibility(View.VISIBLE);
            }else{
                nextBtn.setVisibility(View.INVISIBLE);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private String formatAmount(String amtString){
        long amt = Long.parseLong(amtString);
        double amtD = (double)amt/100;
        return String.format(Locale.getDefault(),"%,.2f",amtD);
    }

    @Override
    public void onClick(View v) {
        String amt = amount.getText().toString().replace(",","");

        if(amt.equals("") || amt.equals("0.00")){
            Toast.makeText(AirtimeAmountActivity.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if(phone.getText().toString().isEmpty()){
            Toast.makeText(AirtimeAmountActivity.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Keys.isMinimumAmount(AirtimeAmountActivity.this, amt)){
            Toast.makeText(AirtimeAmountActivity.this, "Amount cannot be less than minimum amount", Toast.LENGTH_SHORT).show();
            return;
        }

        AirtimeModel.Plan details = model.getPlanDetails(model.getBillsName());
        model.setBillsCode(details.getBillsCode());
        model.setBillsName(details.getBillsName());
        model.setImage(details.getImage());

        model.setAmount(amt);
        model.setCustomerName("N/A");
        model.setCustomerId(phone.getText().toString());

        //Get card transaction amount
        double originalAmount = Double.parseDouble(amt);
        String amtF = String.valueOf(originalAmount * 100);
        DecimalFormat df = new DecimalFormat("###.#");
        String amount = "9F02|" + Keys.padLeft(df.format(Double.parseDouble(amtF)),12,'0');

        //Set the transaction route to use for transaction
        Emv.AmountAuthorized = amount;

        startActivity(new Intent(AirtimeAmountActivity.this, AirtimePaymentMethodActivity.class));
        finish();
    }
}