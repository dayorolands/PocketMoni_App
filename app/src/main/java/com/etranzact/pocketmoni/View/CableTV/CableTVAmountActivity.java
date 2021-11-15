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

public class CableTVAmountActivity extends AppCompatActivity implements PosHandler, View.OnClickListener {


    private LoadingProgressDialog loadingDialog;
    CableTVModel model;
    List<String> bouquetList = new ArrayList<>();
    EditText amount, meterNo;
    ImageView backBtn;
    Button nextBtn;
    Spinner plans;
    TextView title, meterNoLabel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cable_tv_amount_activity);
        model = new CableTVModel();
        meterNoLabel = findViewById(R.id.meter_no_label);
        title = findViewById(R.id.title_label_id);
        plans = findViewById(R.id.subscription_plan_id);
        amount = findViewById(R.id.amount_id);
        meterNo = findViewById(R.id.meter_no_id);
        nextBtn = findViewById(R.id.next_btn_id);
        nextBtn.setOnClickListener(this);
        title.setText(model.getBillsName().toUpperCase());
        meterNo.addTextChangedListener(OnMeterNoTextChanged);
        amount.addTextChangedListener(OnAmountTextChanged);
        plans.setOnItemSelectedListener(onPlanSelected);
        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((view)-> onBackPressed());

        Emv.initializeEmv(CableTVAmountActivity.this);

        setMeterNoLabel();
    }

    private void setMeterNoLabel(){
        if(model.getBillsName().toLowerCase().contains("gotv")){
            meterNoLabel.setText(R.string.iuc_number);
        }else if(model.getBillsName().toLowerCase().contains("dstv")){
            meterNoLabel.setText(R.string.enter_decoder_number);
        }else{
            meterNoLabel.setText(R.string.customer_id);
        }
    }

    AdapterView.OnItemSelectedListener onPlanSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(!plans.getSelectedItem().toString().equals("Select a plan")){
                for(String s : bouquetList){
                    String[] result = s.split("\\|");
                    if(result[2].equals(plans.getSelectedItem().toString())){
                        model.setAmount(result[0]);
                        model.setProductKey(result[1]);
                        model.setProductName(result[2]);
                        String formattedAmt = String.format(Locale.getDefault(),"%,.2f",Double.parseDouble(model.getAmount().replace(",","")));
                        amount.setText(formattedAmt);
                        break;
                    }
                }
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    TextWatcher OnMeterNoTextChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(meterNo.getText().length() > 9){
                loadingDialog = new LoadingProgressDialog(CableTVAmountActivity.this);
                loadingDialog.show();
                loadingDialog.setProgressLabel("Loading bouquet", true);
                model.setCustomerId(meterNo.getText().toString());
                CableTVModel.Plan details = model.getPlanDetails(model.getBillsName());
                model.setBillsCode(details.getBillsCode());
                model.setBillsName(details.getBillsName());
                model.setDescription(details.getDescription());
                model.setId(details.getId());
                model.setImage(details.getImage());
                model.doAccountValidation(CableTVAmountActivity.this, (response)->{
                    Log.d("Result", "Response: " + response);
                    String respCode = Keys.parseJson(response, "responseCode");
                    if(respCode.equals("00")){
                        bouquetList.clear();
                        String fullName = Keys.parseJson(response, "fullname");
                        model.setCustomerName(fullName);
                        List<String> price = Keys.parseJsonCnt(response, "price");
                        List<String> productKey = Keys.parseJsonCnt(response, "productKey");
                        List<String> productName = Keys.parseJsonCnt(response, "productName");
                        for(int i=0; i<price.size(); i++){
                            bouquetList.add(price.get(i) + "|" + productKey.get(i) + "|" + productName.get(i));
                        }
                        productName.add(0,"Select a plan");
                        plans.setAdapter(new ArrayAdapter<>(CableTVAmountActivity.this,R.layout.spinner_custom_list,productName));
                    }
                    loadingDialog.dismiss();
                });
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
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
            Toast.makeText(CableTVAmountActivity.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if(plans.getSelectedItem().toString().equals("Select a plan")){
            Toast.makeText(CableTVAmountActivity.this, "Please select a plan", Toast.LENGTH_SHORT).show();
            return;
        }

        if(meterNo.getText().toString().isEmpty()){
            Toast.makeText(CableTVAmountActivity.this, "Please enter a valid customer Id", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Keys.isMinimumAmount(CableTVAmountActivity.this, amt)){
            Toast.makeText(CableTVAmountActivity.this, "Amount cannot be less than minimum amount", Toast.LENGTH_SHORT).show();
            return;
        }

        //Get card transaction amount
        double originalAmount = Double.parseDouble(amt);
        String amtF = String.valueOf(originalAmount * 100);
        DecimalFormat df = new DecimalFormat("###.#");
        String amount = "9F02|" + Keys.padLeft(df.format(Double.parseDouble(amtF)),12,'0');

        //Set the transaction route to use for transaction
        Emv.AmountAuthorized = amount;

        startActivity(new Intent(CableTVAmountActivity.this, CableTVPaymentMethodActivity.class));
        finish();
    }
}