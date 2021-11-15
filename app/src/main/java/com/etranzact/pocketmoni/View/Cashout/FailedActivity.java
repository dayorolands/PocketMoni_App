package com.etranzact.pocketmoni.View.Cashout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.etranzact.pocketmoni.Dialogs.LoadingProgressDialog;
import com.etranzact.pocketmoni.Model.CardModel;
import com.etranzact.pocketmoni.R;
import com.sdk.pocketmonisdk.Model.RecyclerModel;
import Utils.CardInfo;
import Utils.Emv;
import Utils.Keys;
import Utils.MyPrinter;
import Utils.Net;
import Utils.TransDB;
import Utils.TransRoute;

public class FailedActivity extends AppCompatActivity {

    private static boolean isRefresh = true;
    private final static String REFRESH = "REFRESH";
    private final static String PRINT = "PRINT RECEIPT";
    private final static String TIMEOUT_MSG = "Network timeout, please click the refresh button";
    Button printButton, closeBtn;
    TextView transDate, transTime, cardHolder, cardNo, respCode, respMessage,
            transType,cardType, amount, paymentFailedId;
    ImageView passOrFailedLogo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.failed_activity);
        transDate = findViewById(R.id.terminal_date);
        transTime = findViewById(R.id.terminal_time);
        cardHolder = findViewById(R.id.card_holder);
        cardNo = findViewById(R.id.card_no);
        transType = findViewById(R.id.trans_type);
        cardType = findViewById(R.id.card_type);
        amount = findViewById(R.id.trans_amt);
        passOrFailedLogo = findViewById(R.id.pass_or_fail_logo);
        paymentFailedId = findViewById(R.id.payment_failed_id);
        respCode = findViewById(R.id.response_code);
        respMessage = findViewById(R.id.resp_message);
        printButton = findViewById(R.id.print_receipt);
        closeBtn = findViewById(R.id.close_btn_id);
        closeBtn.setOnClickListener((view)->{
            CardInfo.StopTransaction(FailedActivity.this);
        });
        printButton.setOnClickListener(printButtonClicked);

        SetParameters();
        doIsRefreshLogic();

        i = 0;
        Thread t = new Thread(CardInfo::completeTransaction);
        t.start();
    }

    private void doIsRefreshLogic() {
        String resp = respCode.getText().toString();
        if(resp.isEmpty()){
            respMessage.setText(TIMEOUT_MSG);
            printButton.setText(REFRESH);
            isRefresh = true;
        }else{
            printButton.setText(PRINT);
            isRefresh = false;
        }
    }

    @Override
    public void onBackPressed() {
        CardInfo.StopTransaction(FailedActivity.this);
    }

    private void SetParameters(){
        String[] result = CardModel.getTransactionData(this).split("\\|");
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
            if (isRefresh){
                doRefresh();
            }else{
                doPrint();
            }
        }
    };

    public void doPrint(){
        printButton.setEnabled(false);
        Toast.makeText(FailedActivity.this, "PRINTING RECEIPT", Toast.LENGTH_SHORT).show();
        if(i == 0) {   //PRINT CUSTOMER COPY
            i++;
            MyPrinter.setMerchantOrCustomerCopy("CUSTOMER COPY");
            MyPrinter.Print(FailedActivity.this);
        }
        else if(i == 1){ //PRINT MERCHANT COPY
            i++;
            MyPrinter.setMerchantOrCustomerCopy("MERCHANT COPY");
            MyPrinter.Print(FailedActivity.this);
        }
        else{ //PRINT REPRINT COPY
            MyPrinter.setMerchantOrCustomerCopy("REPRINT COPY");
            MyPrinter.Print(FailedActivity.this);
        }
        printButton.setEnabled(true);
    }

    public void doRefresh(){
        LoadingProgressDialog dialog = new LoadingProgressDialog(this);
        dialog.show();
        dialog.setProgressLabel("Performing a refresh, please wait.", true);
        RecyclerModel data = new RecyclerModel();
        TransDB eod = new TransDB(FailedActivity.this);
        try {
            eod.open();
            data = eod.getSpecificDataOnly(Emv.transactionType.toString(),Emv.getTransactionDatTime());
            eod.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String[] payload = data.getData().split("\\|");
        String requeryPayload = payload[payload.length-1];
        Log.d("Result", "Request: " + requeryPayload);


        Handler handler = new Handler(getMainLooper(), (msg)->{
            String response = msg.getData().getString("msg");
            Log.d("Result", "Response: " + response);
            if(!response.isEmpty()){
                String field39 = Keys.parseJson(response, "field39");
                if(field39.isEmpty()) field39 = Keys.parseJson(response, "responseCode");

                //Route transaction to NIBSS when response code is 91 3 times consecutively
                TransRoute.setRouteRespCode(FailedActivity.this, field39);
                //Response code 00 signifies approved
                if (field39.equals("00")) {
                    Emv.responseCode = field39;
                    Emv.responseMessage = "TRANSACTION APPROVED";
                    passOrFailedLogo.setImageResource(R.drawable.approved_icon);
                    paymentFailedId.setText(R.string.payment_successful);
                    int passGreen = getResources().getColor(R.color.pass_green);
                    paymentFailedId.setTextColor(passGreen);
                    updateEOD();
                }else if(field39.equals("01")){
                    String description = Keys.parseJson(response, "description");
                    if (description.isEmpty()) description = Keys.parseJson(response, "responseMessage");
                    Emv.responseCode = field39;
                    Emv.responseMessage = description;
                } else {
                    String description = Keys.parseJson(response, "description");
                    if (description.isEmpty()) description = Keys.parseJson(response, "responseMessage");
                    Emv.responseCode = field39;
                    Emv.responseMessage = description;
                    updateEOD();
                }
                respCode.setText(Emv.responseCode);
                respMessage.setText(Emv.responseMessage);
                doIsRefreshLogic();
            }
            dialog.dismiss();
            return true;
        });

        new Thread(()->{
            String response = Net.httpRequest(requeryPayload, "POST", Emv.requeryUrl);
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("msg",response);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }).start();
    }

    void updateEOD() {
        TransDB eod = new TransDB(FailedActivity.this);
        try {
            eod.open();
            eod.updateDb(Emv.getTransactionDatTime(),Emv.transactionType,Emv.responseCode,CardModel.getTransactionData(this));
            eod.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}