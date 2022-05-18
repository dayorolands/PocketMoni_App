package com.etranzact.pocketmoni.View.Payout.Billpayment.Junks;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.R;

public class BillsTransactionActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deposite_transaction_activity);
        //startTransaction();
    }

//    void startTransaction() {
//        Thread t = new Thread(()->{
//            String response = "";
//            switch (Emv.transactionType){
//                case TRANSFER:
//                    //response = Middleware.processCashTransfer(BillsTransactionActivity.this);
//                    break;
//                case ELECTRICITY:
//                    //response = Middleware.processCashBillpayment(BillsTransactionActivity.this);
//                    break;
//                default:
//                    //Do nothing
//                    break;
//            }
//
//            Bundle bundle = new Bundle();
//            Message msg = new Message();
//            bundle.putString("RESPONSE", response);
//            msg.setData(bundle);
//            OnTransactionResponse.sendMessage(msg);
//        });
//        t.start();
//    }
//
//    Handler OnTransactionResponse = new Handler(Looper.getMainLooper(), (msg)->{
//        Bundle bundle = msg.getData();
//        String response = bundle.getString("RESPONSE");
//
//        if(response == null){
//            Toast.makeText(BillsTransactionActivity.this, "TRANSACTION ROUTE NOT FOUND", Toast.LENGTH_LONG).show();
//            return false;
//        }
//
//        String respCode = Keys.parseJson(response, "responseCode");
//
//        //PROCESS TMS RESPONSE
//        if(respCode.equals("")){
//            Toast.makeText(BillsTransactionActivity.this,"No response from server, Try again", Toast.LENGTH_LONG).show();
//            finish();
//        } else if (respCode.equals("00")) {
//            Emv.responseCode = respCode;
//            Emv.responseMessage = "TRANSACTION APPROVED";
//            ShowApproved();
//        } else {
//            String respMessage = Keys.parseJson(response, "description");
//            if(respMessage.isEmpty()) respMessage = Keys.parseJson(response, "responseMessage");
//            Emv.responseCode = respCode;
//            Emv.responseMessage = respMessage;
//            ShowDeclined();
//        }
//        return true;
//    });
//
//    private void ShowApproved() {
//        TransDB eod = new TransDB(this);
//        try {
//            //String amt = String.format("%,.2f", Double.parseDouble(Middleware.amount));
//            eod.open();
//            //eod.insert(Emv.getTransactionDatTime(), Emv.transactionType, amt, Emv.responseCode, "000000************0000", getTransactionData(this));
//            eod.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        startActivity(new Intent(this, BillsApprovedActivity.class));
//        finish();
//    }
//
//    void ShowDeclined() {
//        TransDB eod = new TransDB(this);
//        try {
//            //String amt = String.format("%,.2f", Double.parseDouble(Middleware.amount));
//            eod.open();
//            //eod.insert(Emv.getTransactionDatTime(), Emv.transactionType, amt, Emv.responseCode, "000000************0000", getTransactionData(this));
//            eod.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        startActivity(new Intent(this, BillsFailedActivity.class));
//        finish();
//    }
//
//    private String getTransactionData(Activity activity) {
//        if(Emv.transactionType == TransType.ELECTRICITY){
//            return StartBillpaymentActivity.getTransactionData(activity);
//        }else{
//            return CashTransferTransactionActivity.getTransactionData(activity);
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        return;
//    }
}