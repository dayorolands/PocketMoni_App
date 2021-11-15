package com.etranzact.pocketmoni.View.Payout.Transfer.Cash;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.etranzact.pocketmoni.Dialogs.LoadingProgressDialog;
import com.etranzact.pocketmoni.Model.BankAccountModel;
import com.etranzact.pocketmoni.Model.TransferModel;
import com.etranzact.pocketmoni.R;
import java.util.Locale;
import Utils.Emv;
import Utils.Keys;
import Utils.PosHandler;

public class DepositAmountActivity extends AppCompatActivity implements PosHandler {

    public ProgressBar progressBar;

    private LoadingProgressDialog loadingDialog;
    EditText amountText, narration, phone;
    TextView insertCardLabel, acctName;
    EditText acctNumber;
    ImageView backBtn;
    Spinner bankDisplay;
    Button btnProceed;
    LinearLayout phoneAndNarration;
    TransferModel model;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new TransferModel(this);
        setContentView(R.layout.deposite_amount_activity);
        loadingDialog = new LoadingProgressDialog(this);
        phoneAndNarration = findViewById(R.id.phone_n_narration);
        progressBar = findViewById(R.id.progress_bar);
        insertCardLabel = findViewById(R.id.insert_card_label);
        bankDisplay = findViewById(R.id.bank_display);
        acctNumber = findViewById(R.id.acct_box);
        btnProceed = findViewById(R.id.btn_continue_id);
        amountText = findViewById(R.id.amt_label);
        narration = findViewById(R.id.narration_id);
        phone = findViewById(R.id.phone_id);
        acctName = findViewById(R.id.acct_name);
        acctNumber.addTextChangedListener(onAccountNumberEntry);
        amountText.addTextChangedListener(OnAmountTextChanged);
        btnProceed.setOnClickListener(onContinueButtonClicked);
        phoneAndNarration.setVisibility(View.VISIBLE);
        acctName.setVisibility(View.GONE);

        bankDisplay.setOnItemSelectedListener(onBankSelected);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, model.getBanks());
        bankDisplay.setAdapter(adapter);

        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((view)->{
            onBackPressed();
        });

        Emv.initializeEmv(DepositAmountActivity.this);
    }

    TextWatcher onAccountNumberEntry = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if(acctNumber.getText().toString().length() > 9){
                if(bankDisplay.getSelectedItem().toString().equals("Select Bank")) {
                    Toast.makeText(DepositAmountActivity.this, "Please select a bank", Toast.LENGTH_SHORT).show();
                    Keys.hideKeyboard(DepositAmountActivity.this);
                }else{
                    Keys.hideKeyboard(DepositAmountActivity.this);
                    retrieveAccountName();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    AdapterView.OnItemSelectedListener onBankSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(acctNumber.getText().toString().length() > 9){
                retrieveAccountName();
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
                amountText.removeTextChangedListener(this);
                String cleanString = s.toString().replaceAll("[#,.]", "");
                String formatted = formatAmount(cleanString);
                current = formatted;
                amountText.setText(formatted);
                amountText.setSelection(formatted.length());
                amountText.addTextChangedListener(this);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void retrieveAccountName(){
        acctName.setText("");

        BankAccountModel bankModel = new BankAccountModel(this);
        String selectedBankName = bankDisplay.getSelectedItem().toString();
        bankModel.setAcctNo(acctNumber.getText().toString());
        bankModel.setBankCode(model.getBankCode(selectedBankName));

        loadingDialog.show();
        loadingDialog.setProgressLabel("Getting account details",true);

        bankModel.accountNoValidation((response)->{
            String resp = Keys.parseJson(response, "responseCode");
            if(resp.equals("00")){
                String transRef = Keys.parseJson(response, "data");
                String acctName = Keys.parseJson(response, "responseMessage").replace("~", "");;
                model.setTransferRef(transRef);
                model.setSendersName(acctName);
                model.setBankCode(model.getBankCode(selectedBankName));
                model.setBankName(selectedBankName);
                model.setAcctNo(acctNumber.getText().toString());
                model.setLastName("");
                this.acctName.setVisibility(View.VISIBLE);
                this.acctName.setText(acctName);
            }else{
                acctNumber.setText("");
                String responseMessage = Keys.parseJson(response, "responseMessage");
                if(responseMessage.isEmpty()){
                    Toast.makeText(DepositAmountActivity.this, "Network error, Try again later.", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(DepositAmountActivity.this,responseMessage, Toast.LENGTH_LONG).show();
                }
            }

            loadingDialog.dismiss();
        });
    }


    View.OnClickListener onContinueButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String newAmt = amountText.getText().toString();

            if(newAmt.equals("") || newAmt.equals("0.00")){
                Toast.makeText(DepositAmountActivity.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if(acctName.getText().toString().isEmpty()){
                Toast.makeText(DepositAmountActivity.this, "Please fill all mandatory fields to proceed", Toast.LENGTH_SHORT).show();
                return;
            }

            if(Keys.isMinimumAmount(DepositAmountActivity.this, newAmt)){
                Toast.makeText(DepositAmountActivity.this, "Amount cannot be less than minimum amount", Toast.LENGTH_SHORT).show();
                return;
            }
            model.setAmount(newAmt.replace(",", ""));
            model.setNarration(narration.getText().toString());
            model.setPhone(phone.getText().toString());
            //Do cash transaction
            startActivity(new Intent(DepositAmountActivity.this, DepositEnterPinActivity.class));
            finish();
        }
    };

    private String formatAmount(String amtString){
        Long amt = Long.parseLong(amtString);
        double amtD = (double)amt/100;
        return String.format(Locale.getDefault(),"%,.2f",amtD);
    }
}