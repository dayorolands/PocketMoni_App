package com.etranzact.pocketmoni.View.EodActivity;

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
import com.etranzact.pocketmoni.R;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import Utils.Emv;
import Utils.GenerateBitmap;
import Utils.Keys;
import Utils.MyPrinter;

import com.etranzact.pocketmoni.View.Airtime.Cash.AirtimeReceipt;
import com.etranzact.pocketmoni.View.CableTV.Cash.CableTVReceipt;
import com.etranzact.pocketmoni.View.Electricity.Cash.ElectricityReceipt;
import com.etranzact.pocketmoni.View.Payout.Transfer.Cash.DepositReceipt;
import Utils.TransDB;
import Utils.TransType;

public class ReprintActivity extends AppCompatActivity {

    TextView transDate, transTime, transStatus, cardHolder, cardNo,
            transType,cardType, amount, transRrn, meterNo, billName;
    Button reprintBtn;
    ImageView backBtn;
    private static String reprintDetails = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reprint_activity);
        isRefresh = true;
        reprintDetails = getIntent().getStringExtra(Emv.REPRINTKEY);

        reprintBtn = findViewById(R.id.print_receipt);
        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((v)->{
            onBackPressed();
        });
        reprintBtn.setOnClickListener(ReprintBtnClick);

        transDate = findViewById(R.id.terminal_date);
        transTime = findViewById(R.id.terminal_time);
        transStatus = findViewById(R.id.trans_status);
        cardHolder = findViewById(R.id.card_holder);
        cardNo = findViewById(R.id.card_no);
        transType = findViewById(R.id.trans_type);
        cardType = findViewById(R.id.card_type);
        meterNo = findViewById(R.id.trans_meter_no);
        billName = findViewById(R.id.bill_name_id);
        amount = findViewById(R.id.trans_amt);
        transRrn = findViewById(R.id.trans_rrn);
        SetParameters();
        doIsRefreshLogic();
    }

    View.OnClickListener ReprintBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isRefresh){
                doRefresh();
            }else{
                doPrint();
            }
        }
    };

    String[] result = null;
    String requeryPayload = "";
    private void SetParameters(){
        Emv.initializeEmv(this);
        result = reprintDetails.split("\\|");
        transDate.setText(result[0]);
        transTime.setText(result[1]);
        transStatus.setText(result[5].toUpperCase());
        cardHolder.setText(result[2]);
        cardNo.setText(result[3]);
        transType.setText(result[4]);
        cardType.setText(result[12]);
        transRrn.setText(result[8]);
        amount.setText(result[6]);
        meterNo.setText(result[14]);
        billName.setText(result[15]);
        requeryPayload = result[result.length-1];
        //amount 6
        //response code 7
        //rrn 8
        //stan 9
        //tvr 10
        //tsi 11
        //Application label  aka cardType 12
    }

    boolean isRefresh = true;
    private final static String REFRESH = "REFRESH";
    private final static String PRINT = "PRINT RECEIPT";
    private final static String TIMEOUT_MSG = "";
    private void doIsRefreshLogic() {
        String tStatus = transStatus.getText().toString();
        if(tStatus.isEmpty() || tStatus.equals("01")){
            transStatus.setText(TIMEOUT_MSG);
            reprintBtn.setText(REFRESH);
            isRefresh = true;
        }else{
            reprintBtn.setText(PRINT);
            isRefresh = false;
        }
    }

    public void doPrint(){
        Toast.makeText(ReprintActivity.this, "PRINTING", Toast.LENGTH_SHORT).show();
        if(TransType.valueOf(transType.getText().toString()) == TransType.DEPOSIT){
            DepositReceipt.doReprint(ReprintActivity.this, result);
        }else if(TransType.valueOf(transType.getText().toString()) == TransType.ELECTRICITY){
            ElectricityReceipt.doReprint(ReprintActivity.this, result);
        }else if(TransType.valueOf(transType.getText().toString()) == TransType.CABLE_TV){
            CableTVReceipt.doReprint(ReprintActivity.this, result);
        }else if(TransType.valueOf(transType.getText().toString()) == TransType.AIRTIME){
            AirtimeReceipt.doReprint(ReprintActivity.this, result);
        }else{
            //It is a card transaction
            MyPrinter.doReprint(ReprintActivity.this, result);
        }
    }

    public void doRefresh(){
        LoadingProgressDialog dialog = new LoadingProgressDialog(this);
        dialog.show();
        dialog.setProgressLabel("Performing a refresh, please wait.", true);
        Log.d("Result", "Request: " + requeryPayload);


        Handler handler = new Handler(getMainLooper(),(msg) -> {
            String response = msg.getData().getString("msg");
            Log.d("Result", "Response: " + response);
            if(!response.isEmpty()){
                String field39 = Keys.parseJson(response, "field39");
                if(field39.isEmpty()) field39 = Keys.parseJson(response, "responseCode");
                //Response code 00 signifies approved
                if (field39.equals("00")) {
                    result[7] = field39;
                    result[5] = "TRANSACTION APPROVED";
                    updateEOD();
                }else if(field39.equals("01")){
                    String description = Keys.parseJson(response, "description");
                    if (description.isEmpty()) description = Keys.parseJson(response, "responseMessage");
                    result[7] = field39;
                    result[5] = description;
                } else {
                    String description = Keys.parseJson(response, "description");
                    if (description.isEmpty()) description = Keys.parseJson(response, "responseMessage");
                    result[7] = field39;
                    result[5] = description;
                    updateEOD();
                }
                transStatus.setText(result[5]);
                doIsRefreshLogic();
            }
            dialog.dismiss();
            return true;
        });

        new Thread(()->{
            String response = httpRequest(requeryPayload, "POST", Emv.requeryUrl);
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("msg",response);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }).start();
    }


    void updateEOD() {
        TransDB eod = new TransDB(ReprintActivity.this);
        try {
            eod.open();
            eod.updateDb(getTransactionDatTime(),TransType.valueOf(result[4]),result[7],getTransactionData());
            eod.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getTransactionDatTime() {
        try {
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
            SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dt = from.parse(transDate.getText().toString() + transTime.getText().toString());
            return to.format(dt);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getTransactionData(){
        String data = result[0] + "|" + //0 TRANSACTION DATE
                result[1] + "|" + //1 TRANSACTION TIME
                result[2] + "|" + //2 CARD HOLDER NAME
                result[3] + "|" + //3 MASKED PAN
                result[4] + "|" + //4 TRANSACTION TYPE
                result[5] + "|" + //5 RESPONSE MESSAGE
                result[6] + "|" + //6 TRANSACTION MINOR AMOUNT
                result[7] + "|" + //7 RESPONSE CODE
                result[8] + "|" + //8 RRN
                result[9] + "|" + //9 STAN
                result[10] + "|" + //10 TVR
                result[11] + "|" + //11 TSI
                result[12] + "|"; //12 CARD TYPE
        if(TransType.valueOf(result[4]) == TransType.TRANSFER){
                data += result[13] + "|" + //13 Transfer Name
                result[14] + "|" + //14 Transfer Account No
                result[15]; //15 Transfer Ref
        }else  if(TransType.valueOf(result[4]) == TransType.ELECTRICITY){
                data += result[13] + "|" + //Customer Name
                result[14] + "|" + //Customer ID
                result[16] + "|" + //REF
                result[17] + "|" + //Description
                result[20] + "|" + //Token
                result[15]; //Biller Name
        }else  if(TransType.valueOf(result[4]) == TransType.CABLE_TV){
            data += result[13] + "|" + //Customer Name
                result[14] + "|" + //Customer ID
                result[17]; //CableTV Description
        }else  if(TransType.valueOf(result[4]) == TransType.AIRTIME){
            data += result[13] + "|" + //Airtime Customer Name
                result[14] + "|" + //Airtime Customer ID
                result[15] + "|" + //Airtime BillsName
                result[17]; //Description
        }
        return data;
    }

    public static String httpRequest(final String data, final String method, final String transUrl){
        final String[] result = {""};
        Thread t = new Thread(()->{
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Result", "Request url: " + transUrl);
                URL url = new URL(transUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setReadTimeout(60000);
                urlConnection.setConnectTimeout(60000);
                if(method.equals("GET")){
                    urlConnection.setDoOutput(false);
                }else{
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty( "Accept", "*/*" );
                    urlConnection.setRequestProperty("Content-Type", "application/xml");
                    urlConnection.setRequestProperty("Authorization", Emv.accessToken);
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(data.getBytes());
                    out.flush();
                }
                InputStream inputStream = urlConnection.getErrorStream();
                if (inputStream == null) {
                    inputStream = urlConnection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("ISO-8859-1"));
                    result[0] = Keys.readStream(reader);
                }else{
                    Log.d("Result", "Response: " + Keys.readStream(new InputStreamReader(inputStream, Charset.forName("ISO-8859-1"))));
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
            }
        });
        t.start();
        while (t.isAlive());
        return result[0];
    }

}