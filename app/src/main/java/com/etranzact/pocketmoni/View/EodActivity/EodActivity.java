package com.etranzact.pocketmoni.View.EodActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

import Utils.MyPrinter;
import Utils.TransDB;
import Utils.TransType;

public class EodActivity extends AppCompatActivity {

    RecyclerViewModel model;
    List<RecyclerModel> recycler;
    EodRecyclerAdapter adapter;
    TextView approvedLabel, failedLabel, totalAmtLabel, eodTotalCntLabel, approvedCntLabel, failedCntLabel;
    Spinner transList, dateList;
    Button printButton, clearButton;
    ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eod_activity);
        approvedLabel = findViewById(R.id.eod_approved_label);
        failedLabel = findViewById(R.id.eod_fail_label);
        approvedCntLabel = findViewById(R.id.no_of_approved_eod);
        failedCntLabel = findViewById(R.id.no_of_failed_eod);
        eodTotalCntLabel = findViewById(R.id.eod_total_cnt);
        totalAmtLabel = findViewById(R.id.eod_total_amt);
        transList = findViewById(R.id.eod_trans_list);
        dateList = findViewById(R.id.eod_date_list);
        backBtn = findViewById(R.id.back_btn_id);
        printButton = findViewById(R.id.print_eod_print_btn);
        clearButton = findViewById(R.id.clear_eod_btn);
        printButton.setOnClickListener(OnButtonClicked);
        clearButton.setOnClickListener(OnButtonClicked);
        transList.setOnItemSelectedListener(OnItemSelected);
        dateList.setOnItemSelectedListener(OnItemSelected);

        backBtn.setOnClickListener((v)->{
            onBackPressed();
        });
        recycler = new ArrayList<>();

        model = new ViewModelProvider(this).get(RecyclerViewModel.class);
        recycler.add(new RecyclerModel(R.drawable.fail, Color.BLACK,"","","","","",""));
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

        setSpinnerItems();
    }

    String dateSelected = "";
    String typeSelected = "";
    List<String> eodPrintList;
    String dateRange = "";
    double approvedAmt = 0, failedAmt = 0;
    long approvedCnt = 0, failedCnt = 0;

    private void getRecyclerData() {
        recycler.clear();
        if(dateList.getAdapter().getCount() == 0) return;
        dateSelected = dateList.getSelectedItem().toString();
        typeSelected = transList.getSelectedItem().toString();
        TransDB db = new TransDB(this);
        db.open();
        List<RecyclerModel> dbList = db.getData(typeSelected, dateSelected);
        db.close();
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
            eodPrintList.add(String.format("%-9s%-7s%-5s%-12s%s",
                    rm.getTransTime().substring(rm.getTransTime().indexOf(" ") + 1),
                    rm.getData().split("\\|")[9], //STAN
                    rm.getCardNo().substring(rm.getCardNo().length()-4),
                    "N"+rm.getTransAmt().replace(",", ""),
                    "|"+rm.getRespCode()));
                    //(rm.getRespCode().equals("00") ? "PASS" : "FAIL")));
        }
        if(adapter.getItemCount() > 0){
            dateRange = "From: " +  dbList.get(dbList.size()-1).getTransTime() + " to " + dbList.get(0).getTransTime();
        }
        approvedLabel.setText("₦" + String.format("%,.2f", approvedAmt));
        totalAmtLabel.setText("TOTAL: ₦" + String.format("%,.2f", (approvedAmt + failedAmt)));
        approvedCntLabel.setText("PASS: " + approvedCnt);
        eodTotalCntLabel.setText("TOTAL: " + (approvedCnt + failedCnt));
        failedLabel.setText("₦" + String.format("%,.2f", failedAmt));
        failedCntLabel.setText("FAIL: " + failedCnt);
    }

    private void setSpinnerItems() {
        TransDB db = new TransDB(this);
        db.open();
        dateList.setAdapter(db.getTransDates());
        transList.setAdapter(db.getTransTypes());
        db.close();
        if (transList.getAdapter().getCount() > 0) {
            for(int i=0; i<transList.getAdapter().getCount(); i++){
                if(transList.getAdapter().getItem(i).toString().equals("ALL")){
                    transList.setSelection(i);
                }
            }
        }
    }

    AdapterView.OnItemSelectedListener OnItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            getRecyclerData();
            model.setRecyclerData(recycler);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    View.OnClickListener OnButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (adapter.getItemCount() == 0) return;
            switch (v.getId()) {
                case R.id.print_eod_print_btn:
                    AlertDialog ad = new AlertDialog.Builder(EodActivity.this).create();
                    ad.setTitle("You are about to print your EOD");
                    ad.setMessage("Please choose the option you want below.");
                    ad.setButton(AlertDialog.BUTTON_NEGATIVE, "PRINT SUMMARY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(EodActivity.this, "PRINTING", Toast.LENGTH_SHORT).show();
                            MyPrinter.doEodPrint(EodActivity.this, eodPrintList, (approvedCnt + failedCnt), approvedCnt, failedCnt, approvedAmt, failedAmt, (approvedAmt + failedAmt), dateRange, true);
                        }
                    });
                    ad.setButton(AlertDialog.BUTTON_POSITIVE, "PRINT FULL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(EodActivity.this, "PRINTING", Toast.LENGTH_SHORT).show();
                            MyPrinter.doEodPrint(EodActivity.this, eodPrintList, (approvedCnt + failedCnt), approvedCnt, failedCnt, approvedAmt, failedAmt, (approvedAmt + failedAmt), dateRange,false);
                        }
                    });
                    ad.show();
                    break;
                case R.id.clear_eod_btn:
                    AlertDialog ac = new AlertDialog.Builder(EodActivity.this).create();
                    ac.setTitle("You are about to clear your EOD");
                    ac.setMessage("Confirm?");
                    ac.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    ac.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TransDB db = new TransDB(EodActivity.this);
                            db.open();
                            db.deleteData(typeSelected, dateSelected);
                            db.close();
                            getRecyclerData();
                            model.setRecyclerData(recycler);
                            Toast.makeText(EodActivity.this, "Record has been cleared", Toast.LENGTH_LONG).show();
                        }
                    });
                    ac.show();
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        getRecyclerData();
        model.setRecyclerData(recycler);
    }
}