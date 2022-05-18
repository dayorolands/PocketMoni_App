package com.etranzact.pocketmoni.View.SettingsActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.ViewAdapter.EodRecyclerAdapter;
import com.sdk.pocketmonisdk.Model.RecyclerModel;
import com.sdk.pocketmonisdk.ViewModel.RecyclerViewModel;

import java.util.ArrayList;
import java.util.List;

import Utils.TransDB;

public class TransactionHistoryActivity extends AppCompatActivity {

    RecyclerViewModel model;
    List<RecyclerModel> recycler;
    EodRecyclerAdapter adapter;
    TextView approvedLabel, failedLabel, totalAmtLabel, eodTotalCntLabel, approvedCntLabel, failedCntLabel;
    Spinner fromDate, endDate;
    SearchView search;
    Button filterBtn;
    ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trans_history_activity);
        backBtn = findViewById(R.id.back_btn_id);
        approvedLabel = findViewById(R.id.balance_narration);
        fromDate = findViewById(R.id.from_date);
        endDate = findViewById(R.id.end_date);
        search = findViewById(R.id.search_box_id);
        filterBtn = findViewById(R.id.filter_btn_id);
        search.setOnQueryTextListener(onSearch);
        filterBtn.setOnClickListener(onFilterButtonClicked);
        backBtn.setOnClickListener((v)->{
            onBackPressed();
        });
        checkReadWritePermission();

        recycler = new ArrayList<>();

        model = new ViewModelProvider(this).get(RecyclerViewModel.class);
        setSpinnerItems();
        getRecyclerData();
        model.setRecyclerData(recycler);
        RecyclerView recyclerView = findViewById(R.id.eod_home_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        model.getRecyclerData().observe(this, new Observer<List<RecyclerModel>>() {
            @Override
            public void onChanged(List<RecyclerModel> recyclerModels) {
                adapter.notifyDataSetChanged();
            }
        });

        adapter = new EodRecyclerAdapter(model.getRecyclerData().getValue(), this, this);
        recyclerView.setAdapter(adapter);
    }


    String startDateSelected = "";
    String endDateSelected = "";
    List<String> eodPrintList;
    String dateRange = "";
    double approvedAmt = 0, failedAmt = 0;
    long approvedCnt = 0, failedCnt = 0;

    private void getRecyclerData() {
        recycler.clear();
        if(fromDate.getAdapter().getCount() == 0) return;
        startDateSelected = fromDate.getSelectedItem().toString();
        endDateSelected = endDate.getSelectedItem().toString();
        TransDB db = new TransDB(this);
        db.open();
        List<RecyclerModel> dbList = db.geTransBetweenDate(startDateSelected, endDateSelected);
        db.close();
        setRecyclerData(dbList);
    }

    private void setSpinnerItems() {
        TransDB db = new TransDB(this);
        db.open();
        fromDate.setAdapter(db.getTransHistoryDate());
        endDate.setAdapter(db.getTransHistoryDate());
        db.close();
    }

    View.OnClickListener onFilterButtonClicked = ((v)->{
        getRecyclerData();
        model.setRecyclerData(recycler);
    });

    SearchView.OnQueryTextListener onSearch = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            recycler.clear();
            TransDB db = new TransDB(TransactionHistoryActivity.this);
            db.open();
            List<RecyclerModel> dbList = db.searchByDate(newText);
            db.close();
            setRecyclerData(dbList);
            model.setRecyclerData(recycler);
            return false;
        }
    };

    private void setRecyclerData(List<RecyclerModel> dbList) {
        approvedAmt = 0;
        failedAmt = 0;
        approvedCnt = 0;
        failedCnt = 0;
        eodPrintList = new ArrayList<>();
        for (RecyclerModel rm : dbList) {
            if (rm.getRespCode().equals("00")) {
                approvedCnt++;
                approvedAmt += Double.parseDouble(rm.getTransAmt().replace(",", ""));
            } else {
                failedCnt++;
                failedAmt += Double.parseDouble(rm.getTransAmt().replace(",", ""));
            }
            recycler.add(new RecyclerModel(rm.getPassImage(), rm.getTextColor(), rm.getRespCode(), rm.getCardNo(), rm.getTransAmt(), rm.getTransTime(), rm.getTransType(), rm.getData()));
            eodPrintList.add(String.format("%-8s %-15s%-5s%-5s",
                    rm.getTransTime().substring(rm.getTransTime().indexOf(" ") + 1),
                    rm.getTransAmt(), rm.getCardNo().substring(rm.getCardNo().length()-4), (rm.getRespCode().equals("00") ? "PASS" : "FAIL")));
        }
        approvedLabel.setText("₦" + String.format("%,.2f", approvedAmt));

        if(adapter == null) return;
        if(adapter.getItemCount() > 0){
            dateRange = "From: " +  dbList.get(dbList.size()-1).getTransTime() + " to " + dbList.get(0).getTransTime();
        }
        //totalAmtLabel.setText("TOTAL: ₦" + String.format("%,.2f", (approvedAmt + failedAmt)));
        //approvedCntLabel.setText("PASS: " + approvedCnt);
        //eodTotalCntLabel.setText("TOTAL: " + (approvedCnt + failedCnt));
        //failedLabel.setText("₦" + String.format("%,.2f", failedAmt));
        //failedCntLabel.setText("FAIL: " + failedCnt);
    }

    public void checkReadWritePermission()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        else{
            ActivityCompat.requestPermissions(TransactionHistoryActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            ActivityCompat.requestPermissions(TransactionHistoryActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }
}