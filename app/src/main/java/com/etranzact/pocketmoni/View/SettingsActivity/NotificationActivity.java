package com.etranzact.pocketmoni.View.SettingsActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.etranzact.pocketmoni.R;

public class NotificationActivity extends AppCompatActivity {

    ImageView backBtn;
    Switch pushNotification, emailNotification;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);
        backBtn = findViewById(R.id.back_btn_id);
        pushNotification = findViewById(R.id.push_notification);
        emailNotification = findViewById(R.id.push_email);
        pushNotification.setOnCheckedChangeListener(onCheckedChangeListener);
        emailNotification.setOnCheckedChangeListener(onCheckedChangeListener);
        backBtn.setOnClickListener((v)->{
            onBackPressed();
        });
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (view, isChecked) ->{
        switch (view.getId()){
            case R.id.push_notification:
                Toast.makeText(this, "Push email changed " + isChecked, Toast.LENGTH_SHORT).show();
                setSwitchBtnBackground(view, isChecked);
            case R.id.push_email:
                Toast.makeText(this, "Push Notification changed " + isChecked, Toast.LENGTH_SHORT).show();
                setSwitchBtnBackground(view, isChecked);
        }
    };

    private void setSwitchBtnBackground(View view, boolean isChecked){
        Switch btn = (Switch)view;
        if(isChecked)
            btn.setBackground(getResources().getDrawable(R.drawable.custom_border_switch));
        else
            btn.setBackground(getResources().getDrawable(R.drawable.custom_border_switch_off));
    }



}