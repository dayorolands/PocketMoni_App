package com.etranzact.pocketmoni.View.Electricity.Cash;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.R;

import Utils.CardInfo;

public class ElectricityFailedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.electricity_failed_activity);
        transDate = findViewById(R.id.terminal_date);
        transTime = findViewById(R.id.terminal_time);
        cardHolder = findViewById(R.id.card_holder);
        cardNo = findViewById(R.id.card_no);
        transType = findViewById(R.id.trans_type);
        cardType = findViewById(R.id.card_type);
        amount = findViewById(R.id.trans_amt);
        respCode = findViewById(R.id.response_code);
        respMessage = findViewById(R.id.resp_message);
        closeBtn = findViewById(R.id.close_btn_id);
        closeBtn.setOnClickListener((view)->{
            CardInfo.StopTransaction(ElectricityFailedActivity.this);
        });

        SetParameters();

        printButton = findViewById(R.id.print_receipt);
        printButton.setOnClickListener(printButtonClicked);
        i = 0;
    }

    Button printButton, closeBtn;
    TextView transDate, transTime, cardHolder, cardNo, respCode, respMessage,
            transType,cardType, amount;

    private void SetParameters(){
        ElectricityModel model = new ElectricityModel();
        String transactionData = model.getTransactionData(this);
        String[] result = transactionData.split("\\|");
        respMessage.setText(result[5]);
        respCode.setText(result[7]);
        transDate.setText(result[0]);
        transTime.setText(result[1]);
        cardHolder.setText(result[2]);
        cardNo.setText(result[3]);
        transType.setText(result[4]);
        cardType.setText(result[12]);
        amount.setText(result[6]);
    }

    private int i = 0;
    View.OnClickListener printButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            printButton.setEnabled(false);
            Toast.makeText(ElectricityFailedActivity.this, "PRINTING RECEIPT", Toast.LENGTH_SHORT).show();
            if(i == 0) {   //PRINT CUSTOMER COPY
                i++;
                ElectricityReceipt.setMerchantOrCustomerCopy("CUSTOMER COPY");
                ElectricityReceipt.Print(ElectricityFailedActivity.this);
            }
            else if(i == 1){ //PRINT MERCHANT COPY
                i++;
                ElectricityReceipt.setMerchantOrCustomerCopy("MERCHANT COPY");
                ElectricityReceipt.Print(ElectricityFailedActivity.this);
            }
            else{ //PRINT REPRINT COPY
                ElectricityReceipt.setMerchantOrCustomerCopy("REPRINT COPY");
                ElectricityReceipt.Print(ElectricityFailedActivity.this);
            }
            printButton.setEnabled(true);
        }
    };

    @Override
    public void onBackPressed() {
        CardInfo.StopTransaction(ElectricityFailedActivity.this);
    }
}