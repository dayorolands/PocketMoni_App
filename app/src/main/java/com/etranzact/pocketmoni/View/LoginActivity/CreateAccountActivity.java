package com.etranzact.pocketmoni.View.LoginActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.etranzact.pocketmoni.R;

public class CreateAccountActivity extends AppCompatActivity {

    Button createBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_activity);
        createBtn = findViewById(R.id.createAccountBtn);
        createBtn.setOnClickListener((view)->{
            startActivity(new Intent(CreateAccountActivity.this, VerificationCodeActivity.class));
        });
    }
}