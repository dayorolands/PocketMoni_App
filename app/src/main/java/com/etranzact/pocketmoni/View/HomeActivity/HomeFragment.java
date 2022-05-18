package com.etranzact.pocketmoni.View.HomeActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.Airtime.AirtimeDashboardActivity;
import com.etranzact.pocketmoni.View.CableTV.CableTVDashboardActivity;
import com.etranzact.pocketmoni.View.DataTopup.DataDashboardActivity;
import com.etranzact.pocketmoni.View.Electricity.ElectricityDashboardActivity;
import com.etranzact.pocketmoni.View.SettingsActivity.SettingsActivity;
import com.etranzact.pocketmoni.ViewAdapter.HomeRecyclerAdapter;
import com.sdk.pocketmonisdk.Model.RecyclerModel;
import com.sdk.pocketmonisdk.ViewModel.RecyclerViewModel;
import java.util.ArrayList;
import java.util.List;

import Utils.Emv;
import Utils.TransDB;
import Utils.TransType;

public class HomeFragment extends Fragment {

    private static final String HOME_KEY = "KEY";

    private String data;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String value) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(HOME_KEY, value);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            data = getArguments().getString(HOME_KEY);
        }
    }

    RecyclerViewModel model;
    List<RecyclerModel> recycler;
    HomeRecyclerAdapter adapter;
    ImageView settingsBtn, notifyBtn;
    TextView btnViewAll, noOfTransLabel;
    LinearLayout electricityBtn, cableTv, airtime, dataTopUp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        settingsBtn = v.findViewById(R.id.settings_btn);
        btnViewAll = v.findViewById(R.id.view_all_id);
        notifyBtn = v.findViewById(R.id.alert_btn);
        electricityBtn = v.findViewById(R.id.electricity_id);
        cableTv = v.findViewById(R.id.cable_tv_id);
        airtime = v.findViewById(R.id.airtime_id);
        dataTopUp = v.findViewById(R.id.data_id);
        noOfTransLabel = v.findViewById(R.id.show_all_id);
        settingsBtn.setOnClickListener(onClickListener);
        notifyBtn.setOnClickListener(onClickListener);
        btnViewAll.setOnClickListener(onClickListener);
        electricityBtn.setOnClickListener(onClickListener);
        cableTv.setOnClickListener(onClickListener);
        airtime.setOnClickListener(onClickListener);
        dataTopUp.setOnClickListener(onClickListener);
        recycler = new ArrayList<>();
        model = new ViewModelProvider(this).get(RecyclerViewModel.class);
        getLastFiveTransactions();
        model.setRecyclerData(recycler);
        RecyclerView recyclerView = v.findViewById(R.id.eod_home_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        model.getRecyclerData().observe(getViewLifecycleOwner(), (recyclerModels) -> adapter.notifyDataSetChanged());

        adapter = new HomeRecyclerAdapter(model.getRecyclerData().getValue(), getContext(), getActivity());
        recyclerView.setAdapter(adapter);

        return v;
    }

    private boolean showAll = true;
    View.OnClickListener onClickListener = (v) -> {
        if(v.getId() == R.id.electricity_id){
            Emv.transactionType = TransType.ELECTRICITY;
            startActivity(new Intent(getActivity(), ElectricityDashboardActivity.class));
        }
        else if(v.getId() == R.id.cable_tv_id){
            Emv.transactionType = TransType.CABLE_TV;
            startActivity(new Intent(getActivity(), CableTVDashboardActivity.class));
        }
        else if(v.getId() == R.id.airtime_id){
            Emv.transactionType = TransType.AIRTIME;
            startActivity(new Intent(getActivity(), AirtimeDashboardActivity.class));
        }
        else if(v.getId() == R.id.data_id){
            Emv.transactionType = TransType.DATA;
            startActivity(new Intent(getActivity(), DataDashboardActivity.class));
        }
        else if(v.getId() == R.id.settings_btn){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        else if(v.getId() == R.id.view_all_id){
            if(showAll) {
                showAll = false;
                getAllTransactionData();
                noOfTransLabel.setText(R.string.showing_all_transactions);
            }else if(!showAll){
                showAll = true;
                getLastFiveTransactions();
                noOfTransLabel.setText(R.string.show_last_five_transactions);
            }
            model.setRecyclerData(recycler);
        }else if(v.getId() == R.id.alert_btn){
            //Log.d("Result", "Alert Btn");
        }
    };

    private void getAllTransactionData() {
        recycler.clear();
        TransDB db = new TransDB(getContext());
        db.open();
        List<RecyclerModel> dbList = db.getAllData(0);
        db.close();
        for (RecyclerModel rm : dbList) {
            recycler.add(new RecyclerModel(rm.getPassImage(), rm.getTextColor(), rm.getRespCode(), rm.getCardNo(), rm.getTransAmt(), rm.getTransTime(), rm.getTransType(), rm.getData()));
        }
    }

    private void getLastFiveTransactions(){
        recycler.clear();
        TransDB db = new TransDB(getContext());
        db.open();
        List<RecyclerModel> dbList = db.getAllData(5);
        db.close();
        for (RecyclerModel rm : dbList) {
            recycler.add(new RecyclerModel(rm.getPassImage(), rm.getTextColor(), rm.getRespCode(), rm.getCardNo(), rm.getTransAmt(), rm.getTransTime(), rm.getTransType(), rm.getData()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getLastFiveTransactions();
        model.setRecyclerData(recycler);
    }
}