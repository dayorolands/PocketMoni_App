package com.etranzact.pocketmoni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.etranzact.pocketmoni.View.HomeActivity.AccountFragment;
import com.etranzact.pocketmoni.View.Cashout.CashoutFragment;
import com.etranzact.pocketmoni.View.HomeActivity.HomeFragment;
import com.etranzact.pocketmoni.View.Payout.PayOutFragment;
import com.etranzact.pocketmoni.View.HomeActivity.UsersFragment;
import com.etranzact.pocketmoni.ViewAdapter.HomeViewHolderAdapter;
import com.google.android.material.tabs.TabLayout;
import com.sdk.pocketmonisdk.BindServices.ApplicationServices;

import java.util.ArrayList;
import java.util.List;

import Utils.MyPrinter;

public class MainActivity extends AppCompatActivity {

    public static ApplicationServices appService;
    boolean isBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ApplicationServices.LocalBinder binder = (ApplicationServices.LocalBinder) service;
            appService = binder.getService();
            Log.d("Result", "OnServiceConnected");
            isBound = true;
            appService.doActivities(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Log.d("Result", "Disconnected");
        }
    };

    public static Activity activity;
    ViewPager viewPager;
    List<Fragment> fragments;
    public static HomeViewHolderAdapter adapter;
    TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkReadWritePermission();
        initializeViews();
        activity = this;
        //Starts call home service and other services at intervals specified for them.
        new Thread(()->{
            Intent i = new Intent(this, ApplicationServices.class);
            bindService(i,serviceConnection, Context.BIND_AUTO_CREATE);
        }).start();
    }

    public void checkReadWritePermission()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            },1);
        }
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        setFragmentsToDisplay();

        adapter = new HomeViewHolderAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.POSITION_UNCHANGED,fragments);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position), true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    HomeFragment homeFragment;
    CashoutFragment cashoutFragment;
    AccountFragment accountFragment;
    UsersFragment usersFragment;
    PayOutFragment payOutFragment;
    private void setFragmentsToDisplay() {
        fragments = new ArrayList<>();
        fragments.add((homeFragment != null)? homeFragment : new HomeFragment());
        fragments.add((cashoutFragment != null)? cashoutFragment : new CashoutFragment());
        fragments.add((payOutFragment != null)? payOutFragment : new PayOutFragment());
        fragments.add((accountFragment != null)? accountFragment : new AccountFragment());
        fragments.add((usersFragment != null)? usersFragment : new UsersFragment());
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appService.stopActivity();
        unbindService(serviceConnection);
    }
}