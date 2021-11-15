package com.etranzact.pocketmoni.View.Payout.Transfer.Cash;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.Model.TransferModel;
import com.etranzact.pocketmoni.R;
import Utils.CardInfo;

public class DepositApprovedActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deposit_approved_activity);
        transDate = findViewById(R.id.terminal_date);
        transTime = findViewById(R.id.terminal_time);
        cardHolder = findViewById(R.id.card_holder);
        cardNo = findViewById(R.id.card_no);
        transType = findViewById(R.id.trans_type);
        cardType = findViewById(R.id.card_type);
        amount = findViewById(R.id.trans_amt);
        closeBtn = findViewById(R.id.close_btn_id);
        closeBtn.setOnClickListener((view)->{
            CardInfo.StopTransaction(DepositApprovedActivity.this);
        });
        SetParameters();

        printButton = findViewById(R.id.print_receipt);
        printButton.setOnClickListener(printButtonClicked);
        i = 0;
    }

    Button printButton, closeBtn;
    TextView transDate, transTime, cardHolder, cardNo,
            transType,cardType, amount;

    private int i = 0;
    View.OnClickListener printButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            printButton.setEnabled(false);
            Toast.makeText(DepositApprovedActivity.this, "PRINTING RECEIPT", Toast.LENGTH_SHORT).show();
            if(i == 0) {   //PRINT CUSTOMER COPY
                i++;
                DepositReceipt.setMerchantOrCustomerCopy("CUSTOMER COPY");
                DepositReceipt.Print(DepositApprovedActivity.this);
            }
            else if(i == 1){ //PRINT MERCHANT COPY
                i++;
                DepositReceipt.setMerchantOrCustomerCopy("MERCHANT COPY");
                DepositReceipt.Print(DepositApprovedActivity.this);
            }
            else{ //PRINT REPRINT COPY
                DepositReceipt.setMerchantOrCustomerCopy("REPRINT COPY");
                DepositReceipt.Print(DepositApprovedActivity.this);
            }
            printButton.setEnabled(true);
        }
    };

    private void SetParameters(){
        TransferModel model = new TransferModel();
        String transactionData = model.getTransactionData(this);
        String[] result = transactionData.split("\\|");
        transDate.setText(result[0]);
        transTime.setText(result[1]);
        cardHolder.setText(result[2]);
        cardNo.setText(result[3]);
        transType.setText(result[4]);
        cardType.setText(result[12]);
        amount.setText(result[6]);
    }

    @Override
    public void onBackPressed() {
        CardInfo.StopTransaction(this);
    }
}