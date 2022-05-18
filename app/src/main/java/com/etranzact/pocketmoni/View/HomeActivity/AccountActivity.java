package com.etranzact.pocketmoni.View.HomeActivity;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.R;

public class AccountActivity extends AppCompatActivity {

    ImageView backBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);
        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((v)->{
            onBackPressed();
        });
    }

}