package com.etranzact.pocketmoni.View.LoginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.etranzact.pocketmoni.R;

public class VerificationCodeActivity extends AppCompatActivity {

    PinEntryEditText pinEntry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification_code_activity);
        pinEntry = findViewById(R.id.txt_pin_entry);
        pinEntry.setOnPinEnteredListener((str)-> {
            //str returns CharSequence
            if (str.toString().equals("12345")) {
                Toast.makeText(this, "SUCCESS", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(VerificationCodeActivity.this, SignInActivity.class));
            } else {
                Toast.makeText(this, "FAIL", Toast.LENGTH_SHORT).show();
                pinEntry.setText(null);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}