package com.etranzact.pocketmoni.View.LoginActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.etranzact.pocketmoni.MainActivity;
import com.etranzact.pocketmoni.R;

import Utils.Emv;

public class SignInActivity extends AppCompatActivity {

    Button signIn;
    TextView createAcct;
    TextView forgotPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);
        signIn = findViewById(R.id.sign_in);
        createAcct = findViewById(R.id.create_acct);
        forgotPassword = findViewById(R.id.forgot_password);
        Emv.getDeviceLocation(this);
        signIn.setOnClickListener(OnBtnClicked);
        createAcct.setOnClickListener(OnBtnClicked);
        forgotPassword.setOnClickListener(OnBtnClicked);
    }

    View.OnClickListener OnBtnClicked = v -> {
        switch (v.getId()){
            case R.id.create_acct:
                startActivity(new Intent(SignInActivity.this, CreateAccountActivity.class));
                break;
            case R.id.sign_in:
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                break;
            case R.id.forgot_password:
                startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class));
                break;
        }
    };
}