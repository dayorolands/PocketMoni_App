package com.etranzact.pocketmoni.View.Cashout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.Model.CardModel;
import com.etranzact.pocketmoni.R;

import Utils.CardInfo;
import Utils.Emv;
import Utils.MyPrinter;
import Utils.TransType;

public class ApprovedActivity extends AppCompatActivity {

    Button printButton, closeBtn;
    TextView transDate, transTime, cardHolder, cardNo, transType, cardType, amount, meterNo, billerName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.approved_activity);
        transDate = findViewById(R.id.terminal_date);
        transTime = findViewById(R.id.terminal_time);
        cardHolder = findViewById(R.id.card_holder);
        cardNo = findViewById(R.id.card_no);
        transType = findViewById(R.id.trans_type);
        cardType = findViewById(R.id.card_type);
        amount = findViewById(R.id.trans_amt);
        meterNo = findViewById(R.id.trans_customer_id);
        billerName = findViewById(R.id.trans_biller_name);
        closeBtn = findViewById(R.id.close_btn_id);
        closeBtn.setOnClickListener((view) -> {
            CardInfo.StopTransaction(ApprovedActivity.this);
        });
        SetParameters();

        printButton = findViewById(R.id.print_receipt);
        printButton.setOnClickListener(printButtonClicked);
        i = 0;
        Thread t = new Thread(CardInfo::completeTransaction);
        t.start();
    }

    @Override
    public void onBackPressed() {
        CardInfo.StopTransaction(this);
    }

    private int i = 0;
    View.OnClickListener printButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            printButton.setEnabled(false);
            Toast.makeText(ApprovedActivity.this, "PRINTING RECEIPT", Toast.LENGTH_SHORT).show();
            if(i == 0) {   //PRINT CUSTOMER COPY
                i++;
                MyPrinter.setMerchantOrCustomerCopy("CUSTOMER COPY");
                MyPrinter.Print(ApprovedActivity.this);
            }
            else if(i == 1){ //PRINT MERCHANT COPY
                i++;
                MyPrinter.setMerchantOrCustomerCopy("MERCHANT COPY");
                MyPrinter.Print(ApprovedActivity.this);
            }
            else{ //PRINT REPRINT COPY
                MyPrinter.setMerchantOrCustomerCopy("REPRINT COPY");
                MyPrinter.Print(ApprovedActivity.this);
            }
            printButton.setEnabled(true);
        }
    };

    private void SetParameters(){
        String[] result = CardModel.getTransactionData(this).split("\\|");
        transDate.setText(result[0]);
        transTime.setText(result[1]);
        cardHolder.setText(result[2]);
        cardNo.setText(result[3]);
        transType.setText(result[4]);
        cardType.setText(result[12]);
        amount.setText(result[6]);
        billerName.setText(result[15]);
        meterNo.setText(result[14]);
    }
}