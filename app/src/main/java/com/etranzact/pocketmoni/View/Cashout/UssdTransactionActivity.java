package com.etranzact.pocketmoni.View.Cashout;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.R;

public class UssdTransactionActivity extends AppCompatActivity {

    ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ussd_activity);
        backBtn = findViewById(R.id.back_btn_id);
        backBtn.setOnClickListener((v)->{
            onBackPressed();
        });
    }

}