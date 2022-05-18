package com.etranzact.pocketmoni.View.SettingsActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.etranzact.pocketmoni.R;

import Utils.Emv;
import Utils.Keys;
import Utils.SharedPref;

public class ConfigurationActivity extends AppCompatActivity {

    TextView merchant_loc, merchant_id, terminal_id, terminal_mcc, agent_id, agent_loc, country_code, settings_status;
    Button loadConfig;
    ImageButton btnnibsskey, btnkimonoKey, btnTokenKey;
    Spinner nibssEnvSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration_activity);
        checkReadWritePermission();
        merchant_id = findViewById(R.id.merchant_id);
        terminal_id = findViewById(R.id.terminal_id);
        terminal_mcc = findViewById(R.id.terminal_mcc);
        country_code = findViewById(R.id.country_code);
        merchant_loc = findViewById(R.id.merchant_loc);
        agent_id = findViewById(R.id.agent_id);
        agent_loc = findViewById(R.id.agent_loc);
        settings_status = findViewById(R.id.settings_status);
        loadConfig = findViewById(R.id.load_config);
        btnnibsskey = findViewById(R.id.btn_nibss);
        btnkimonoKey = findViewById(R.id.btn_kimono);
        btnTokenKey = findViewById(R.id.btn_token);
        nibssEnvSpinner = findViewById(R.id.nibss_env_spinner);

        loadConfiguration();

        settings_status.setVisibility(View.GONE);

        nibssEnvSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPref.set(ConfigurationActivity.this,"nibssEnv", nibssEnvSpinner.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnnibsskey.setOnClickListener(onKeyDownload);
        btnkimonoKey.setOnClickListener(onKeyDownload);
        btnTokenKey.setOnClickListener(onKeyDownload);
        loadConfig.setOnClickListener(loadConfigListener);
    }

    private View.OnClickListener onKeyDownload = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String TACOnline = SharedPref.get(ConfigurationActivity.this, "taconline", "");
            if(TACOnline.isEmpty()){
                Toast.makeText(ConfigurationActivity.this, "Please load config first", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (v.getId()){
                case R.id.btn_kimono:
                    Emv.initializeEmv(ConfigurationActivity.this);
                    boolean kimonoKey = Keys.generateKimonoKeys(ConfigurationActivity.this, Emv.keyDownloadUrl,Emv.terminalId,Emv.keysetid);
                    settings_status.setVisibility(View.VISIBLE);
                    if(kimonoKey){
                        settings_status.setText("Key download successful");
                        settings_status.setTextColor(Color.GREEN);
                        SharedPref.set(ConfigurationActivity.this, "environment", "TMS");
                        Log.d("Result", "Key download was successful");
                    }else{
                        settings_status.setText("Key download failed");
                        settings_status.setTextColor(Color.RED);
                        Log.d("Result", "Key download failed");
                    }
                    break;
                case R.id.btn_token:
                    String result = Keys.generateToken("POST", Emv.accessTokenURL);
                    String token = Keys.parseJson(result,"access_token");
                    settings_status.setVisibility(View.VISIBLE);
                    if(token.length()>10) {
                        settings_status.setText("Token download was successful");
                        settings_status.setTextColor(Color.GREEN);
                        SharedPref.set(ConfigurationActivity.this, "accesstoken", "Bearer " + token);
                    }
                    else{
                        settings_status.setText("Token download failed");
                        settings_status.setTextColor(Color.RED);
                    }
                    break;
                case R.id.btn_nibss:
                    String Field62 = Keys.generateNibssKeys(ConfigurationActivity.this, "PRODUCTION");
                    if(!Field62.isEmpty()) {
                        SharedPref.set(ConfigurationActivity.this, "merchantlocation", Keys.parseTLV(Field62, "52"));
                        SharedPref.set(ConfigurationActivity.this, "merchantid", Keys.parseTLV(Field62, "03"));
                        SharedPref.set(ConfigurationActivity.this, "countrycode", "9F1A|" + Keys.padLeft(Keys.parseTLV(Field62, "05"), 4, '0'));
                        SharedPref.set(ConfigurationActivity.this, "mcc", Keys.parseTLV(Field62, "08"));
                        Emv.initializeEmv(ConfigurationActivity.this);
                        merchant_loc.setText("MERCHANT LOC: " + Keys.parseTLV(Field62, "52"));
                        merchant_id.setText("MERCHANT ID: " + Keys.parseTLV(Field62, "03"));
                        terminal_mcc.setText("MCC: " + Keys.parseTLV(Field62, "08"));
                        country_code.setText("COUNTRY CODE:: " + Keys.parseTLV(Field62, "05"));
                        settings_status.setVisibility(View.VISIBLE);
                        settings_status.setText("Key download was successful");
                        settings_status.setTextColor(Color.GREEN);
                        Log.d("Result", "Key download was successful");
                        SharedPref.set(ConfigurationActivity.this, "environment", "NIBSS");
                    }else{
                        settings_status.setVisibility(View.VISIBLE);
                        settings_status.setText("Key download failed");
                        settings_status.setTextColor(Color.RED);
                        Log.d("Result", "Key download failed");
                    }
                    break;
            }
        }
    };

    private View.OnClickListener loadConfigListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ShowFileDialog();
        }
    };

    public void ShowFileDialog(){
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Config"), 1);
    }

    public void checkReadWritePermission()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        else{
            ActivityCompat.requestPermissions(ConfigurationActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            ActivityCompat.requestPermissions(ConfigurationActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }

    private void loadConfiguration(){
        String nibssEnv = SharedPref.get(this, "nibssEnv", "CTMS");
        String merchantLocation = SharedPref.get(this, "merchantlocation", "");
        String terminalId = SharedPref.get(this, "terminalid", "");
        String merchantId = SharedPref.get(this, "merchantid", "");
        String mcc = SharedPref.get(this, "mcc", "");
        String TerminalCountryCode = SharedPref.get(this, "countrycode", "9F1A|0566");
        String agentId = SharedPref.get(this, "agentId", "");
        String agentLoc = SharedPref.get(this, "agentLoc", "");

        if(nibssEnv.equals("CTMS"))
            nibssEnvSpinner.setSelection(1);
        else
            nibssEnvSpinner.setSelection(0);

        merchant_loc.setText("MERCHANT LOC: " + merchantLocation);
        merchant_id.setText("MERCHANT ID: " + merchantId);
        terminal_id.setText("TERMINAL ID: " + terminalId);
        terminal_mcc.setText("MCC: " + mcc);
        country_code.setText("COUNTRY CODE: " + TerminalCountryCode.split("\\|")[1]);
        agent_id.setText("AGENT ID: " + agentId);
        agent_loc.setText("AGENT LOC: " + agentLoc);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                boolean success = Keys.setConfig(this, uri);
                if(success){
                    loadConfiguration();
                    Toast.makeText(ConfigurationActivity.this, "Configuration file was loaded successfully", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(ConfigurationActivity.this, "Invalid configuration details", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}