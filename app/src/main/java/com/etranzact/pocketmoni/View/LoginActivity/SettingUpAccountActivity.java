package com.etranzact.pocketmoni.View.LoginActivity;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.etranzact.pocketmoni.R;

public class SettingUpAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_up_account_activity);
    }

    @Override
    public void onBackPressed() {
        return;
    }
}