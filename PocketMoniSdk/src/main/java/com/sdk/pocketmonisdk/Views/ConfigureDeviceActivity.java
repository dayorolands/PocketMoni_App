package com.sdk.pocketmonisdk.Views;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sdk.pocketmonisdk.R;
import com.sdk.pocketmonisdk.TransEnvironment.Middleware;

import java.util.HashMap;

import Utils.Emv;
import Utils.HttpRequest;
import Utils.Keys;
import Utils.SharedPref;

public class ConfigureDeviceActivity extends AppCompatActivity {

    Handler configurationHandler;
    TextView configureLabel;
    ProgressBar progress;
    Button retryBtn;
    public static Activity parentActivity;
    private static final String KEY = "cable_cat_key";
    private static final String EKEY = "cat_key";
    private static final String AKEY = "airtime_cat_key";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configure_device_activity);
        configureLabel = findViewById(R.id.configure_label_id);
        progress = findViewById(R.id.config_loading_progressbar);
        retryBtn = findViewById(R.id.retry_btn);
        configurationHandler = new Handler(getMainLooper());
    }

    public static boolean isConfigurationRunning = false;
    @Override
    protected void onStart() {
        super.onStart();
        isConfigurationRunning = true;
        progress.setVisibility(View.VISIBLE);
        configurationHandler.postDelayed(configurationRunnable, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isConfigurationRunning = false;
    }

    public void onRetryBtnClicked(View view){
        retryBtn.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
        configurationHandler.postDelayed(configurationRunnable, 1000);
    }


    int cnt = 0;
    Runnable configurationRunnable = new Runnable() {
        @Override
        public void run() {
            canExit = false;
            new Thread(()->{
                switch (cnt){
                    case 0:
                        displayMessage("Running system configuration");
                        boolean configSuccess = doConfiguration();
                        if(configSuccess){
                            configurationHandler.postDelayed(configurationRunnable, 1000);
                        }
                        break;
                    case 1:
                        displayMessage("Running processor configuration");
                        boolean kimonoSuccess = configureKIMONO();
                        if(kimonoSuccess){
                            configurationHandler.postDelayed(configurationRunnable, 1000);
                        }
                        break;
                    case 2:
                        displayMessage("Setting up processor key");
                        boolean nibssSuccess = configureNIBSS();
                        if(nibssSuccess){
                            configurationHandler.postDelayed(configurationRunnable, 1000);
                        }
                        break;
                    case 3:
                        displayMessage("Synchronizing...");
                        boolean syncSuccess = doSynchronization();
                        if(syncSuccess){
                            configurationHandler.postDelayed(configurationRunnable, 1000);
                        }
                        break;
                    case 4:
                        displayMessage("Setting up Cable TV");
                        boolean configureCableTV = configureCableTV(ConfigureDeviceActivity.this);
                        if(configureCableTV){
                            configurationHandler.postDelayed(configurationRunnable, 1000);
                        }
                        break;
                    case 5:
                        displayMessage("Setting up Electricity");
                        boolean configureElectric = configureElectricity(ConfigureDeviceActivity.this);
                        if(configureElectric){
                            configurationHandler.postDelayed(configurationRunnable, 1000);
                        }
                        break;
                    case 6:
                        displayMessage("Finishing Up..");
                        boolean configureAirtime = configureAirtime(ConfigureDeviceActivity.this);
                        if(configureAirtime){
                            configurationHandler.postDelayed(configurationRunnable, 1000);
                        }
                        break;
                    case 7:
                        finish();
                        break;
                }
            }).start();
        }
    };

    private boolean doConfiguration(){
        boolean isSuccessful = Keys.downloadConfig(ConfigureDeviceActivity.this);
        if(isSuccessful) {
            displayMessage("System configuration is complete");
            cnt = 1;
            return true;
        }
        setRetryBtnVisible();
        displayMessage("System configuration failed");
        return false;
    }

    private boolean configureKIMONO(){
        Emv.initializeEmv(ConfigureDeviceActivity.this);
        boolean isSuccessful = Keys.generateKimonoKeys(ConfigureDeviceActivity.this,Emv.keyDownloadUrl,Emv.terminalId, Emv.keysetid);
        if(isSuccessful) {
            SharedPref.set(ConfigureDeviceActivity.this, "environment", "TMS");
            displayMessage("Processor configuration is completed");
            cnt = 2;
            return true;
        }
        /*
        setRetryBtnVisible();
        displayMessage("Processor configuration failed");
        return false;
        */

        //Setting to false will pause configuration until the server is up again
        //I am returning true to bypass this config even if the server is down.
        cnt = 2;
        displayMessage("Processor configuration is set to default");
        return true;
    }

    private boolean configureNIBSS(){
        if(Emv.terminalId.startsWith("2ISW")){
            displayMessage("Almost done");
            cnt = 3;
            return true;
        }

        String Field62 = Keys.generateNibssKeys(ConfigureDeviceActivity.this, "PRODUCTION");
        if(Field62.length() > 0) {
            SharedPref.set(ConfigureDeviceActivity.this, "merchantlocation", Keys.parseTLV(Field62, "52"));
            SharedPref.set(ConfigureDeviceActivity.this, "merchantid", Keys.parseTLV(Field62, "03"));
            SharedPref.set(ConfigureDeviceActivity.this, "countrycode", "9F1A|" + Keys.padLeft(Keys.parseTLV(Field62, "05"), 4, '0'));
            SharedPref.set(ConfigureDeviceActivity.this, "mcc", Keys.parseTLV(Field62, "08"));
            SharedPref.set(ConfigureDeviceActivity.this, "environment", "NIBSS");
            displayMessage("Almost done");
            cnt = 3;
            return true;
        }
        setRetryBtnVisible();
        displayMessage("Setting up processor key failed");
        return false;
    }

    private boolean doSynchronization(){
        boolean isSuccessful = Middleware.doSynchronization(ConfigureDeviceActivity.this);
        if(isSuccessful) {
            cnt = 4;
            return true;
        }
        setRetryBtnVisible();
        displayMessage("Setup Failed");
        return false;
    }

    private boolean configureCableTV(Activity activity){
        cnt = 5;
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json");
        headers.put("Authorization",Emv.accessToken);
        String json = HttpRequest.reqHttp("GET",Emv.cableTvCategoryUrl,"",headers);
        String respCode = Keys.parseJson(json, "responseCode");
        if(respCode.equals("00")){
            displayMessage("Setting up Cable TV complete");
            SharedPref.set(parentActivity, KEY, json);
            return true;
        }
        return false;
    }

    private boolean configureElectricity(Activity activity){
        cnt = 6;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type","application/json");
        headers.put("Authorization",Emv.accessToken);
        String json = HttpRequest.reqHttp("GET", Emv.electricityCategoryUrl, "", headers);
        if((!json.isEmpty()) &&  (!json.contains("responseCode"))){
            displayMessage("Electricity Setup is complete");
            SharedPref.set(parentActivity, EKEY,json);
            return true;
        }
        return false;
    }

    private boolean configureAirtime(Activity activity){
        cnt = 7;
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json");
        headers.put("Authorization",Emv.accessToken);
        String json = HttpRequest.reqHttp("GET",Emv.airtimeCategoryURl,"",headers);
        String respCode = Keys.parseJson(json,"responseCode");
        if(respCode.equals("00")){
            displayMessage("Setup is complete");
            SharedPref.set(parentActivity,AKEY,json);
            return true;
        }
        return false;
    }

    void displayMessage(String message){
        Handler handler = new Handler(getMainLooper(),(msg)->{
            String data = msg.getData().getString("key");
            configureLabel.setText(data);
            return true;
        });
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("key", message);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }


    private static boolean canExit = false;
    public void setRetryBtnVisible() {
        this.runOnUiThread(()->{
            canExit = true;
            progress.setVisibility(View.INVISIBLE);
            retryBtn.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onBackPressed() {
        if(canExit){
            super.onBackPressed();
            parentActivity.finish();
        }
        else return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        configurationHandler.removeCallbacks(configurationRunnable);
    }
}
