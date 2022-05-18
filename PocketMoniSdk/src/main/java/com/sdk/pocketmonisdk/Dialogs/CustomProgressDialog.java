package com.sdk.pocketmonisdk.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sdk.pocketmonisdk.R;

public class CustomProgressDialog extends Dialog {

    private Activity activity;
    public CustomProgressDialog(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    ProgressBar progressBar;
    TextView progressLabel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_progress_dialog);
        progressBar = findViewById(R.id.downloading_progress);
        progressLabel = findViewById(R.id.progress_label);
        setCanceledOnTouchOutside(false);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private int progress = 0;
    public void sendProgress(int progress){
        progressBar.setProgress(progress);
        progressLabel.setText(String.valueOf(progress));
        this.progress = progress;
        if(progress > 99) dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(progress != 100){
            activity.finish();
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }
}
