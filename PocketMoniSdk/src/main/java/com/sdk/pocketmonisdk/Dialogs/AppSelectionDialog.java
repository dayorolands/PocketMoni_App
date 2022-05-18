package com.sdk.pocketmonisdk.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sdk.pocketmonisdk.R;
import com.sdk.pocketmonisdk.ViewAdapter.AppSelectAdapter;

import java.util.List;

import Utils.AidClass;
import Utils.CardInfo;

public class AppSelectionDialog extends Dialog {
    private static List<AidClass> aidList;
    private Activity activity;
    public AppSelectionDialog(Activity activity, List<AidClass> value) {
        super(activity);
        this.activity = activity;
        aidList = value;
    }

    List<AidClass> appSelect;
    AppSelectAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.appselection_dialog);

        setCanceledOnTouchOutside(false);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView recyclerView = findViewById(R.id.app_selection_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        getAppSelectionData();
        adapter = new AppSelectAdapter(appSelect, activity,this);
        recyclerView.setAdapter(adapter);
    }

    private void getAppSelectionData(){
        appSelect = aidList;
    }

    @Override
    public void onBackPressed() {
        CardInfo.StopTransaction(activity);
    }
}
