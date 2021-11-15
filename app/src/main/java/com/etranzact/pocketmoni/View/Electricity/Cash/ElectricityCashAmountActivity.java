package com.etranzact.pocketmoni.View.Electricity.Cash;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.Dialogs.LoadingProgressDialog;
import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.R;

import java.text.DecimalFormat;
import java.util.Locale;

import Utils.Emv;
import Utils.Keys;
import Utils.PosHandler;

public class ElectricityCashAmountActivity extends AppCompatActivity implements PosHandler, View.OnClickListener {


    private LoadingProgressDialog loadingDialog;
    ElectricityModel model;

    EditText amount, meterNo;
    LinearLayout amountAndMeterNumber;
    ImageView backBtn;
    Button nextBtn;
    Spinner plans;
    TextView title;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.electricity_amount_activity);
        model = new ElectricityModel();
        title = findViewById(R.id.title_label_id);
        plans = findViewById(R.id.subscription_plan_id);
        amountAndMeterNumber = findViewById(R.id.amount_layout);
        amount = findViewById(R.id.amount_id);
        meterNo = findViewById(R.id.meter_no_id);
        nextBtn = findViewById(R.id.next_btn_id);
        amountAndMeterNumber.setVisibility(View.INVISIBLE);
        nextBtn.setOnClickListener(this);
        title.setText(model.getSessionCategory().toUpperCase());
        amount.addTextChangedListener(OnAmountTextChanged);
        plans.setOnItemSelectedListener(onPlanSelected);
        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((view)->{
            onBackPressed();
        });

        Emv.initializeEmv(ElectricityCashAmountActivity.this);
    }


    AdapterView.OnItemSelectedListener onPlanSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(!plans.getSelectedItem().toString().equals("Select Plan")){
                amountAndMeterNumber.setVisibility(View.VISIBLE);
                ElectricityModel.Plan details = model.getPlan(model.getSessionCategory(),plans.getSelectedItem().toString());
                model.setBillerCode(details.getBillerCode());
                model.setBillerName(details.getBillerName());
                model.setDescription(details.getDescription());
                model.setMeterType(details.getMeterType());
                model.setMtype(details.getMtype());
                model.setZone(details.getZone());
            }else{
                amountAndMeterNumber.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

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
            Toast.makeText(ElectricityCashAmountActivity.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if(meterNo.getText().toString().isEmpty()){
            Toast.makeText(ElectricityCashAmountActivity.this, "Please enter a Meter Number", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Keys.isMinimumAmount(ElectricityCashAmountActivity.this, amt)){
            Toast.makeText(ElectricityCashAmountActivity.this, "Amount cannot be less than minimum amount", Toast.LENGTH_SHORT).show();
            return;
        }

        //Get card transaction amount
        double originalAmount = Double.parseDouble(amt);
        String amtF = String.valueOf(originalAmount * 100);
        DecimalFormat df = new DecimalFormat("###.#");
        String amount = "9F02|" + Keys.padLeft(df.format(Double.parseDouble(amtF)),12,'0');

        //Set the transaction route to use for transaction
        Emv.AmountAuthorized = amount;

        model.setAmount(amt);
        model.setCustomerId(meterNo.getText().toString());
        loadingDialog = new LoadingProgressDialog(this);
        loadingDialog.show();
        loadingDialog.setProgressLabel("Validating meter number", true);
        model.doAccountValidation(this,(response)->{
            Log.d("Result", "Response: " + response);
            String respCode = Keys.parseJson(response,"responseCode");
            String respMessage = Keys.parseJson(response,"responseMessage");
            if(!response.isEmpty() && respCode.equals("00")){
                String reference = Keys.parseJson(response,"reference");
                String customerName = Keys.parseJson(response,"customerName");
                String address = Keys.parseJson(response,"address");
                model.setPaymentRef(reference);
                model.setCustomerName(customerName);
                model.setAddress(address);
                startActivity(new Intent(ElectricityCashAmountActivity.this, ElectricityEnterPinActivity.class));
                finish();
            }else if(!respMessage.isEmpty()) {
                Toast.makeText(ElectricityCashAmountActivity.this,respMessage, Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(ElectricityCashAmountActivity.this,"Network error. Try again", Toast.LENGTH_SHORT).show();
            }
            loadingDialog.dismiss();
        });
    }
}