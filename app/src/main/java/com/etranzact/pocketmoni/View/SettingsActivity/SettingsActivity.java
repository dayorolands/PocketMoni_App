package com.etranzact.pocketmoni.View.SettingsActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.MainActivity;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.EodActivity.EodActivity;

import Utils.Emv;
import Utils.Keys;
import Utils.SharedPref;

public class SettingsActivity extends AppCompatActivity {

    ImageView closeBtn;
    RelativeLayout transHistoryBtn, transEodBtn, accountBtn, notificationBtn;
    TextView logoutBtn, versionLabel, policyBtn, refreshTokenBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        closeBtn = findViewById(R.id.close_settings_id);
        transHistoryBtn = findViewById(R.id.trans_history_id);
        transEodBtn = findViewById(R.id.trans_eod);
        accountBtn = findViewById(R.id.account_btn);
        notificationBtn = findViewById(R.id.trans_notify_id);
        logoutBtn = findViewById(R.id.logout_id);
        versionLabel = findViewById(R.id.version_id);
        policyBtn = findViewById(R.id.policy_id);
        refreshTokenBtn = findViewById(R.id.refresh_token);
        versionLabel.setText("Version " + Emv.getAppVersion(this));
        logoutBtn.setOnClickListener(onSettingsBtnClicked);
        transEodBtn.setOnClickListener(onSettingsBtnClicked);
        transHistoryBtn.setOnClickListener(onSettingsBtnClicked);
        accountBtn.setOnClickListener(onSettingsBtnClicked);
        notificationBtn.setOnClickListener(onSettingsBtnClicked);
        policyBtn.setOnClickListener(onSettingsBtnClicked);
        refreshTokenBtn.setOnClickListener(onSettingsBtnClicked);
        closeBtn.setOnClickListener(onSettingsBtnClicked);
    }

    private View.OnClickListener onSettingsBtnClicked = (v) -> {
        switch (v.getId()){
            case R.id.account_btn:
                //Disabled due to no login
                //startActivity(new Intent(SettingsActivity.this, AccountActivity.class));
                break;
            case R.id.close_settings_id:
                onBackPressed();
               break;
            case R.id.trans_eod:
                startActivity(new Intent(SettingsActivity.this, EodActivity.class));
                break;
            case R.id.trans_history_id:
                startActivity(new Intent(SettingsActivity.this, TransactionHistoryActivity.class));
                break;
            case R.id.trans_notify_id:
                startActivity(new Intent(SettingsActivity.this, NotificationActivity.class));
                break;
            case R.id.logout_id:
                this.finish();
                MainActivity.activity.finish();
                break;
            case R.id.policy_id:
                startActivity(new Intent(SettingsActivity.this, PolicyActivity.class));
                break;
            case R.id.refresh_token:
                String result = Keys.generateToken("POST", Emv.accessTokenURL);
                String token = Keys.parseJson(result,"access_token");
                if(token.length()>10) {
                    Toast.makeText(this, "Token Refreshed", Toast.LENGTH_SHORT).show();
                    SharedPref.set(this, "accesstoken", "Bearer " + token);
                }
                else{
                    Toast.makeText(this, "Token Failed", Toast.LENGTH_SHORT).show();
                    refreshTokenBtn.setTextColor(Color.RED);
                }
                refreshTokenBtn.setText("Refresh Token");
                break;
        }
    };
}