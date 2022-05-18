package com.etranzact.pocketmoni.View.Payout.Transfer.Card;

import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.etranzact.pocketmoni.Dialogs.LoadingProgressDialog;
import com.etranzact.pocketmoni.Model.BankAccountModel;
import com.etranzact.pocketmoni.Model.TransferModel;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.Cashout.TransactionActivity;

import java.text.DecimalFormat;
import java.util.Locale;
import Utils.CardInfo;
import Utils.CardReadMode;
import Utils.Emv;
import Utils.Keys;
import Utils.PosHandler;

public class CardTransferAmountActivity extends AppCompatActivity implements PosHandler {

    public ProgressBar progressBar;
    private LoadingProgressDialog loadingDialog;
    EditText convenientAmountText, amountText;
    TextView insertCardLabel, acctName;
    EditText acctNumber;
    ImageView backBtn;
    Spinner bankDisplay;
    Button btnProceed;
    TransferModel model;
    double minCvAmount = 0;
    double maxCvAmount = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_transfer_amount_activity);
        model = new TransferModel(this);
        loadingDialog = new LoadingProgressDialog(this);
        progressBar = findViewById(R.id.progress_bar);
        insertCardLabel = findViewById(R.id.insert_card_label);
        bankDisplay = findViewById(R.id.bank_display);
        acctNumber = findViewById(R.id.acct_box);
        btnProceed = findViewById(R.id.btn_continue_id);
        amountText = findViewById(R.id.amt_label);
        convenientAmountText = findViewById(R.id.amt_convenient);
        acctName = findViewById(R.id.acct_name);
        acctNumber.addTextChangedListener(onAccountNumberEntry);
        amountText.addTextChangedListener(OnAmountTextChanged);
        convenientAmountText.addTextChangedListener(OnConvenientAmountTextChanged);
        btnProceed.setOnClickListener(onContinueButtonClicked);
        acctName.setVisibility(View.GONE);
        bankDisplay.setOnItemSelectedListener(onBankSelected);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, model.getBanks());
        bankDisplay.setAdapter(adapter);

        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((view)->{
            Disconnect();
        });

        Emv.initializeEmv(CardTransferAmountActivity.this);

        minCvAmount = model.getMinCvAmount();
        maxCvAmount = model.getMaxCvAmount();
    }

    TextWatcher onAccountNumberEntry = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if(acctNumber.getText().toString().length() > 9){
                if(bankDisplay.getSelectedItem().toString().equals("Select Bank")) {
                    Toast.makeText(CardTransferAmountActivity.this, "Please select a bank", Toast.LENGTH_SHORT).show();
                    Keys.hideKeyboard(CardTransferAmountActivity.this);
                }else{
                    Keys.hideKeyboard(CardTransferAmountActivity.this);
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


    TextWatcher OnConvenientAmountTextChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        private String current = "";
        final DecimalFormat df = new DecimalFormat("###.#");
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().equals(current)){
                current = s.toString();
                if(current.equals("")) current = "0";
                double cvAmt = Double.parseDouble(current.replace(",",""));
                if(cvAmt > maxCvAmount){
                    Toast.makeText(CardTransferAmountActivity.this, "Convenient fee cannot be greater than " + df.format(maxCvAmount), Toast.LENGTH_SHORT).show();
                    convenientAmountText.setText(df.format(maxCvAmount));
                    convenientAmountText.setSelection(df.format(maxCvAmount).length());
                }
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
                    Toast.makeText(CardTransferAmountActivity.this, "Network error, Try again later.", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(CardTransferAmountActivity.this,responseMessage, Toast.LENGTH_LONG).show();
                }
            }

            loadingDialog.dismiss();
        });
    }


    View.OnClickListener onContinueButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String newAmt = amountText.getText().toString().replace(",", "");
            String cvAmt = convenientAmountText.getText().toString().replace(",", "");
            if(cvAmt.isEmpty() || cvAmt.equals("0.00")){
                cvAmt = "0";
            }

//            if(Double.parseDouble(newAmt) <= Double.parseDouble(cvAmt)){
//                Toast.makeText(CardTransferAmountActivity.this, "Transaction amount cannot be less than transaction charges", Toast.LENGTH_LONG).show();
//                amountText.requestFocus();
//                return;
//            }


            if(Double.parseDouble(cvAmt) < minCvAmount){
                final DecimalFormat df = new DecimalFormat("###.#");
                Toast.makeText(CardTransferAmountActivity.this, "Convenient fee cannot be less than " + df.format(minCvAmount), Toast.LENGTH_LONG).show();
                convenientAmountText.requestFocus();
                return;
            }

            if(newAmt.equals("") || newAmt.equals("0.00")){
                Toast.makeText(CardTransferAmountActivity.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if(acctName.getText().toString().isEmpty()){
                Toast.makeText(CardTransferAmountActivity.this, "Please fill all mandatory fields to proceed", Toast.LENGTH_SHORT).show();
                return;
            }

            if(Keys.isMinimumAmount(CardTransferAmountActivity.this, newAmt)){
                Toast.makeText(CardTransferAmountActivity.this, "Amount cannot be less than minimum amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double originalAmount = Double.parseDouble(newAmt);
            String amtF = String.valueOf(originalAmount * 100);
            DecimalFormat df = new DecimalFormat("###.#");
            String amt = "9F02|" + Keys.padLeft(df.format(Double.parseDouble(amtF)),12,'0');

            model.setConvenientFee(cvAmt);

            //Set the transaction route to use for transaction
            Emv.AmountAuthorized = amt;

            //TransRoute.setTransactionRoute(CardAmountActivity.this, amt.split("\\|")[1]);
            //Force the transaction to TMS only
            Emv.environment = "TMS";

            //Enable account selection
            //new AccountSelectionDialog(getActivity()).show();
            //Do card transaction
            DoInsertCardLogic();
        }
    };


    private String formatAmount(String amtString){
        long amt = Long.parseLong(amtString);
        double amtD = (double)amt/100;
        return String.format(Locale.getDefault(),"%,.2f",amtD);
    }

    private void DoInsertCardLogic(){
        loadingDialog.show();
        loadingDialog.setProgressLabel("Waiting for card", false);
        CardInfo.initialize(this);
    }

    @Override
    public void onEnterPinRequested(Activity activity) {
        loadingDialog.dismiss();
        startActivity(new Intent(activity, CardTransferEnterPinActivity.class));
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
        Toast.makeText(CardTransferAmountActivity.this, "CARD READ TIMEOUT", Toast.LENGTH_LONG).show();
        Disconnect();
    }

    @Override
    public void onBackPressed() {
        Disconnect();
    }
}