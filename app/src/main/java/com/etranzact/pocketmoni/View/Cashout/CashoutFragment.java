package com.etranzact.pocketmoni.View.Cashout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.SettingsActivity.SettingsActivity;

import Utils.Emv;
import Utils.TransType;

public class CashoutFragment extends Fragment {

    private static final String HOME_KEY = "KEY";

    private String data;

    public CashoutFragment() {
    }

    public static CashoutFragment newInstance(String value) {
        CashoutFragment fragment = new CashoutFragment();
        Bundle args = new Bundle();
        args.putString(HOME_KEY, value);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            data = getArguments().getString(HOME_KEY);
        }
    }

    CardView debitCard, transUSSD;
    ImageView settingsBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cashout, container, false);
        debitCard = v.findViewById(R.id.debit_card_id);
        transUSSD = v.findViewById(R.id.ussd_id);
        settingsBtn = v.findViewById(R.id.settings_btn);
        debitCard.setOnClickListener(OnTransButtonSelected);
        transUSSD.setOnClickListener(OnTransButtonSelected);
        settingsBtn.setOnClickListener((view)->{
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        });
        return v;
    }

    View.OnClickListener OnTransButtonSelected = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.debit_card_id:
                    Emv.transactionType = TransType.CASHOUT;
                    startActivity(new Intent(getActivity(), AmountActivity.class));
                    break;
                case R.id.ussd_id:
                    Emv.transactionType = TransType.USSD;
                    startActivity(new Intent(getActivity(), UssdTransactionActivity.class));
                    break;
            }
        }
    };
}