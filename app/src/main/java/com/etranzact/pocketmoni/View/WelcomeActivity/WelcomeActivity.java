package com.etranzact.pocketmoni.View.WelcomeActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.etranzact.pocketmoni.MainActivity;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.ViewAdapter.WelcomeViewHolderAdapter;
import com.etranzact.pocketmoni.ViewModel.WelcomeViewHolderModel;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

import Utils.Emv;

public class WelcomeActivity extends AppCompatActivity {

    public static int position = 0;
    List<WelcomeViewHolderModel> viewHolderList;
    ViewPager adapter;
    Button createAcct;
    LinearLayout signInBtn;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);
        createAcct = findViewById(R.id.continue_to_app);
        signInBtn = findViewById(R.id.sign_in_layout);
        handler = new Handler(getMainLooper());
        getWelcomeImages();
        createAcct.setOnClickListener(onClickBtn);
        signInBtn.setOnClickListener(onClickBtn);
        adapter = findViewById(R.id.viewHolderId);
        WelcomeViewHolderAdapter vha = new WelcomeViewHolderAdapter(viewHolderList, this);
        adapter.setAdapter(vha);
        TabLayout tabL = findViewById(R.id.tabLayoutId);
        tabL.setupWithViewPager(adapter);
    }

    View.OnClickListener onClickBtn = v -> {
        switch (v.getId()){
            case R.id.continue_to_app:
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                Emv.getDeviceLocation(this);
                break;
            //case R.id.sign_in_layout:
                //startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
                //break;
        }
    };

    private void getWelcomeImages() {
        viewHolderList = new ArrayList<>();
        viewHolderList.add(new WelcomeViewHolderModel("Unlock endless possibilities", "Make seamless transactions, swiftly", R.drawable.ic_welcome_frame1));
        viewHolderList.add(new WelcomeViewHolderModel("Visit any agent location", "Make seamless transactions, swiftly", R.drawable.ic_welcome_frame2));
        viewHolderList.add(new WelcomeViewHolderModel("24/7 support", "Make seamless transactions, swiftly", R.drawable.ic_welcome_frame3));
    }


    Runnable swingWallpaper = new Runnable() {
        @Override
        public void run() {
            try{
                if(position == viewHolderList.size()-1){
                    position = 0;
                }else{
                    position++;
                }
                adapter.setCurrentItem(position);
                handler.postDelayed(swingWallpaper, 4000);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(swingWallpaper, 4000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(swingWallpaper);
    }
}