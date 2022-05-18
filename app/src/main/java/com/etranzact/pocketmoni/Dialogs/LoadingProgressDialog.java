package com.etranzact.pocketmoni.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.etranzact.pocketmoni.R;

import Utils.CardInfo;

public class LoadingProgressDialog extends Dialog {

    public LoadingProgressDialog(Activity activity) {
        super(activity);
    }

    TextView progressLabel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_progress_dialog);
        progressLabel = findViewById(R.id.loading_label_id);
        setCanceledOnTouchOutside(false);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    boolean lock = false;
    public void setProgressLabel(String message, Boolean disableBackPress){
        lock = disableBackPress;
        progressLabel.setText(message);
    }

    @Override
    public void onBackPressed() {
        if(lock) return;
        else {
            dismiss();
            CardInfo.Disconnect();
            super.onBackPressed();
        }
    }
}
