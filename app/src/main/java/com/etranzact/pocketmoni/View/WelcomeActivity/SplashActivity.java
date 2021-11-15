package com.etranzact.pocketmoni.View.WelcomeActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.etranzact.pocketmoni.MainActivity;
import com.etranzact.pocketmoni.Model.AirtimeModel;
import com.etranzact.pocketmoni.Model.CableTVModel;
import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.R;
import Utils.DateTime;
import Utils.Emv;
import Utils.Keys;
import Utils.SharedPref;

public class SplashActivity extends AppCompatActivity {

    private Handler handler;
    TextView versionNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        versionNo = findViewById(R.id.splash_version_id);
        String version = "Version " + Emv.getAppVersion(this);
        versionNo.setText(version);
        handler = new Handler(getMainLooper());
        handler.postDelayed(runnable, 1000);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            configureToken();
            if(!isTokenSuccessful){
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 5000);
            }
        }
    };

    boolean isTokenSuccessful = false;
    private void configureToken(){
        Handler handler = new Handler(getMainLooper(), (msg)->{
            if(msg.what == 0){
                isTokenSuccessful = true;
                Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                startActivity(intent);
                new ElectricityModel(this,(resp)-> Log.d("Result", "Electricity Discos loaded"));
                new CableTVModel(this,(resp)-> Log.d("Result", "CableTV Discos loaded"));
                new AirtimeModel(this,(resp)-> Log.d("Result", "AirTime Discos loaded"));
            }else if(msg.what == 1){
                isTokenSuccessful = false;
                Toast.makeText(SplashActivity.this,"No internet access", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        new Thread(()->{
            Emv.initializeEmv(this);
            String result = "";
            int month = DateTime.Now.Day();
            int prevMonth = Integer.parseInt(SharedPref.get(SplashActivity.this,"tokenmonth","0"));
            if(month != prevMonth){
                result = Keys.generateToken("POST", Emv.accessTokenURL);
                Log.d("Result", "Response: " + result);
                String token = Keys.parseJson(result,"access_token");
                if(token.length()>10) {
                    //Todo just to remind myself on where I cleared the glide memory.
                    Glide.get(this).clearDiskCache();
                    SharedPref.set(SplashActivity.this, "tokenmonth", String.valueOf(month));
                    SharedPref.set(SplashActivity.this, "accesstoken", "Bearer " + token);
                    handler.sendEmptyMessage(0);
                }else{
                    handler.sendEmptyMessage(1);
                }
            }else{
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        finish();
    }
}