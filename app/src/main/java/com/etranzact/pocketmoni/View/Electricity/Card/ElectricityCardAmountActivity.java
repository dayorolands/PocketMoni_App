package com.etranzact.pocketmoni.View.Electricity.Card;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.etranzact.pocketmoni.View.Cashout.TransactionActivity;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import Utils.CardInfo;
import Utils.CardReadMode;
import Utils.Emv;
import Utils.Keys;
import Utils.PosHandler;

public class ElectricityCardAmountActivity extends AppCompatActivity implements PosHandler, View.OnClickListener {


    private LoadingProgressDialog loadingDialog;
    ElectricityModel model;

    EditText amount, meterNo, convenientFee;
    LinearLayout amountAndMeterNumber;
    ImageView backBtn;
    Button nextBtn;
    Spinner plans;
    Handler electricHandler;
    TextView title, acctName;
    double maxCvAmount = 0;
    double minCvAmount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.electricity_card_amount_activity);
        model = new ElectricityModel();
        title = findViewById(R.id.title_label_id);
        plans = findViewById(R.id.subscription_plan_id);
        amountAndMeterNumber = findViewById(R.id.amount_layout);
        amount = findViewById(R.id.amount_id);
        meterNo = findViewById(R.id.meter_no_id);
        nextBtn = findViewById(R.id.next_btn_id);
        convenientFee = findViewById(R.id.amt_convenient);
        amountAndMeterNumber.setVisibility(View.INVISIBLE);
        nextBtn.setOnClickListener(this);
        title.setText(model.getSessionCategory().toUpperCase());
        amount.addTextChangedListener(OnAmountTextChanged);
        acctName = findViewById(R.id.acct_name);
        acctName.setVisibility(View.GONE);
        convenientFee.addTextChangedListener(OnConvenientFeeChanged);
        plans.setOnItemSelectedListener(onPlanSelected);
        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((view)->{
            onBackPressed();
        });

        Emv.initializeEmv(ElectricityCardAmountActivity.this);

        ElectricityModel electricityModel = new ElectricityModel(ElectricityCardAmountActivity.this);
        maxCvAmount = electricityModel.getMaxCvAmount();
        minCvAmount = electricityModel.getMinCvAmount();
    }

    AdapterView.OnItemSelectedListener onPlanSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(!plans.getSelectedItem().toString().equals("Select Plan")){
                amountAndMeterNumber.setVisibility(View.VISIBLE);
                ElectricityModel.Plan details = model.getPlan(model.getSessionCategory(),plans.getSelectedItem().toString());
                model.setBillerCode(details.getBillerCode());
                model.setBillerName(details.getBillerName());
                model.setId(details.getId());
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

    TextWatcher OnConvenientFeeChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        private String current = "";
        final DecimalFormat df = new DecimalFormat("###.#");
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(!charSequence.toString().equals(current)){
                current = charSequence.toString();
                if(current.equals("")) current = "0";
                double cvAmt = Double.parseDouble(current.replace(",",""));
                if(cvAmt > maxCvAmount){
                    Toast.makeText(ElectricityCardAmountActivity.this, "Convenient fee cannot be greater than " + df.format(maxCvAmount), Toast.LENGTH_SHORT).show();
                    convenientFee.setText(df.format(maxCvAmount));
                    convenientFee.setSelection(df.format(maxCvAmount).length());
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

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
        String cvAmt = convenientFee.getText().toString().replace(",", "");
        if (cvAmt.equals("") || cvAmt.equals("0.00")){
            cvAmt = "0";
        }

        if(Double.parseDouble(cvAmt) < minCvAmount){
            final DecimalFormat df = new DecimalFormat("###.#");
            Toast.makeText(ElectricityCardAmountActivity.this, "Convenient fee cannot be less than " + df.format(minCvAmount), Toast.LENGTH_LONG).show();
            convenientFee.requestFocus();
            return;
        }

        if(amt.equals("") || amt.equals("0.00")){
            Toast.makeText(ElectricityCardAmountActivity.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if(meterNo.getText().toString().isEmpty()){
            Toast.makeText(ElectricityCardAmountActivity.this, "Please enter a Meter Number", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Keys.isMinimumAmount(ElectricityCardAmountActivity.this, amt)){
            Toast.makeText(ElectricityCardAmountActivity.this, "Amount cannot be less than minimum amount", Toast.LENGTH_SHORT).show();
            return;
        }

        //Get card transaction amount
        double originalAmount = Double.parseDouble(amt);
        String amtF = String.valueOf(originalAmount * 100);
        DecimalFormat df = new DecimalFormat("###.#");
        String amount = "9F02|" + Keys.padLeft(df.format(Double.parseDouble(amtF)),12,'0');
        model.setConvenientFee(cvAmt);

        //Set the transaction route to use for transaction 04175299546
        Emv.AmountAuthorized = amount;

        model.setAmount(amt);
        model.setCustomerId(meterNo.getText().toString());
        loadingDialog = new LoadingProgressDialog(this);
        loadingDialog.show();
        loadingDialog.setProgressLabel("Validating meter number", true);
        model.doCardAcctValidation(this,(response)->{
            Log.d("Result", "Response: " + response);
            String respCode = Keys.parseJson(response,"responseCode");
            String respMessage = Keys.parseJson(response,"responseMessage");
            if(!response.isEmpty() && respCode.equals("00")){
                String reference = Keys.parseJson(response,"paymentRef");
                String customerName = Keys.parseJson(response,"customerName");
                Log.d("Result:", "The customer name is " + customerName);
                String address = Keys.parseJson(response,"otherInfo1");
                Log.d("Result:", "The customer address is " + address);
                String billId = Keys.parseJson(response, "billId");
                String arrears = Keys.parseJson(response, "arrears");
                if(!arrears.isEmpty()){
                    model.setDescription("ARREARS: " + arrears);
                }else{
                    model.setDescription("ARREARS: 0.00");
                }
                //String arrears = customerDetails.split("\\|")[4];
                this.acctName.setVisibility(View.VISIBLE);
                this.acctName.setText(customerName);
                model.setOutstandingBal("");
                model.setPaymentRef(reference);
                model.setCustomerName(customerName);
                model.setAddress(address);
                model.setId(billId);
                //Force route to TMS only
                Emv.environment = "TMS";
                loadingDialog.dismiss();
                electricHandler = new Handler(getMainLooper());
                electricHandler.postDelayed(doInsertCardLogic, 1000);
            }else if(!respMessage.isEmpty()) {
                Toast.makeText(ElectricityCardAmountActivity.this,respMessage, Toast.LENGTH_LONG).show();
                loadingDialog.dismiss();
            }else{
                Toast.makeText(ElectricityCardAmountActivity.this,"Network error. Try again", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }

    Runnable doInsertCardLogic = new Runnable() {
        @Override
        public void run() {
            DoInsertCardLogic();
            electricHandler.removeCallbacks(doInsertCardLogic);
        }
    };

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
        Toast.makeText(ElectricityCardAmountActivity.this, "CARD READ TIMEOUT", Toast.LENGTH_LONG).show();
        Disconnect();
    }

    @Override
    public void onBackPressed() {
        Disconnect();
    }
}