package com.etranzact.pocketmoni.Dialogs;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.fragment.app.FragmentActivity;

import com.etranzact.pocketmoni.R;

import Utils.CardInfo;
import Utils.Emv;

public class AccountSelectionDialog extends Dialog {

    private FragmentActivity activity;
    public AccountSelectionDialog(FragmentActivity activity) {
        super(activity);
        this.activity = activity;
    }

    LinearLayout buttonLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.account_selection_dialog);
        buttonLayout = findViewById(R.id.btn_layout);

        int count = buttonLayout.getChildCount();
        for(int i=0; i<count; i++){
            Button b = (Button)buttonLayout.getChildAt(i);
            b.setOnClickListener(onClickListener);
        }

        setCanceledOnTouchOutside(false);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    View.OnClickListener onClickListener = (v)->{
        Button b = (Button)v;
        String acctText = b.getText().toString();
        String acctCode = b.getTag().toString();
        Emv.processingCode = "00" + acctCode + "00";
        Emv.accountType = acctText;
        dismiss();
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CardInfo.StopTransaction(activity);
    }
}
