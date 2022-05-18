package com.etranzact.pocketmoni.View.Payout.Billpayment.Junks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.etranzact.pocketmoni.R;
import com.sdk.pocketmonisdk.TransEnvironment.Middleware;

import Utils.Emv;
import Utils.Keys;

public class StartBillpaymentActivity extends AppCompatActivity {

    public static String billerId = "";
    public static String customerId = "";
    public static String clientRef = "";
    public static String paymentRef = "";
    public static String billerName = "";
    public static String customerName = "";
    public static String mobileNo = "";
    public static Fragment toFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        //CardInfo.StartTransaction(this, toFragment);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static String getTransactionData(Activity context){
        String stan = Emv.getStan(context);
        String resultRRN = Keys.genKimonoRRN(context, stan);
        Emv.transactionStan = stan;

//        String data = Emv.getTransactionDate() + "|" + //0 TRANSACTION DATE
//                Emv.getTransactionTime() + "|" + //1 TRANSACTION TIME
//                customerName + "|" + //2 CARD HOLDER NAME
//                "000000************0000" + "|" +  //3 MASKED PAN
//                Emv.transactionType.toString() + "|" + //4 TRANSACTION TYPE
//                Emv.responseMessage + "|" + //5 RESPONSE MESSAGE
//                String.format("%,.2f",Double.parseDouble(Middleware.amount)) + "|" +  //6 TRANSACTION MINOR AMOUNT
//                Emv.responseCode + "|" + //7 RESPONSE CODE
//                resultRRN + "|" + //8 RRN
//                stan + "|" + //9 STAN
//                "N/A" + "|" + //10 TVR
//                "N/A" + "|" + //11 TSI
//                "CASH" + "|" + //12 CARD TYPE
//                Middleware.sendersName + "|" + //13 Transfer Name
//                Middleware.acctNo + "|" + //14 Transfer Account No
//                Middleware.transferRef + "|" + //15 Transfer Ref
//                customerName + "|" + //16 Billpayment Customer Name
//                customerId + "|" + //17 Billpayment Customer ID
//                billerName + "|" + //18 Billpayment Biller Name
//                paymentRef; //19 Billpayment Ref
        return "";//data;
    }
}