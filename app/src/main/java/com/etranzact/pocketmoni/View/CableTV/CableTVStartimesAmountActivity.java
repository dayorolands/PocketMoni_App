package com.etranzact.pocketmoni.View.CableTV;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.Dialogs.LoadingProgressDialog;
import com.etranzact.pocketmoni.Model.CableTVModel;
import com.etranzact.pocketmoni.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Utils.Emv;
import Utils.Keys;
import Utils.PosHandler;

public class CableTVStartimesAmountActivity extends AppCompatActivity implements PosHandler, View.OnClickListener {

    CableTVModel model;
    EditText amount, meterNo, meterNo2;
    ImageView backBtn;
    Button nextBtn;
    TextView title, meterNoLabel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cable_tv_startimes_amount_activity);
        model = new CableTVModel();
        meterNoLabel = findViewById(R.id.meter_no_label);
        title = findViewById(R.id.title_label_id);
        amount = findViewById(R.id.amount_id);
        meterNo = findViewById(R.id.meter_no_id);
        meterNo2 = findViewById(R.id.meter_no_id2);
        nextBtn = findViewById(R.id.next_btn_id);
        nextBtn.setOnClickListener(this);
        title.setText(model.getBillsName().toUpperCase());
        amount.addTextChangedListener(OnAmountTextChanged);
        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((view)-> onBackPressed());

        Emv.initializeEmv(CableTVStartimesAmountActivity.this);
    }

    TextWatcher OnAmountTextChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        private String current = "";
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().equals(current)){
                amount.removeTextChangedListener(this);
                String cleanString = s.toString().replaceAll("[#,.]", "");
                String formatted = formatAmount(cleanString);
                current = formatted;
                amount.setText(formatted);
                amount.setSelection(formatted.length());
                amount.addTextChangedListener(this);
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
            Toast.makeText(CableTVStartimesAmountActivity.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if(meterNo.getText().toString().isEmpty() || meterNo.getText().length() < 10){
            Toast.makeText(CableTVStartimesAmountActivity.this, "Please enter a valid Smartcard Number", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!meterNo.getText().toString().equals(meterNo2.getText().toString())){
            Toast.makeText(CableTVStartimesAmountActivity.this, "Smartcard number mismatched!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Keys.isMinimumAmount(CableTVStartimesAmountActivity.this, amt)){
            Toast.makeText(CableTVStartimesAmountActivity.this, "Amount cannot be less than minimum amount", Toast.LENGTH_SHORT).show();
            return;
        }

        CableTVModel.Plan plan = model.getPlanDetails(model.getBillsName());
        model.setAmount(amt);
        model.setCustomerId(meterNo2.getText().toString());
        model.setCustomerName("N/A");
        model.setBillsName(plan.getBillsName());
        model.setBillsCode(plan.getBillsCode());
        model.setProductName(plan.getBillsName());

        //Get card transaction amount
        double originalAmount = Double.parseDouble(amt);
        String amtF = String.valueOf(originalAmount * 100);
        DecimalFormat df = new DecimalFormat("###.#");
        String amount = "9F02|" + Keys.padLeft(df.format(Double.parseDouble(amtF)),12,'0');

        //Set the transaction route to use for transaction
        Emv.AmountAuthorized = amount;

        startActivity(new Intent(CableTVStartimesAmountActivity.this, CableTVPaymentMethodActivity.class));
        finish();
    }
}