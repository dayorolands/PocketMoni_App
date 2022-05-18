package com.etranzact.pocketmoni.View.Payout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.Cashout.CashoutFragment;
import com.etranzact.pocketmoni.View.Payout.Billpayment.Junks.BillpaymentDashboard;
import com.etranzact.pocketmoni.View.Payout.Transfer.Card.CardTransferAmountActivity;
import com.etranzact.pocketmoni.View.Payout.Transfer.Cash.DepositAmountActivity;
import com.etranzact.pocketmoni.View.SettingsActivity.SettingsActivity;

import Utils.Emv;
import Utils.TransType;

public class PayOutFragment extends Fragment {

    private static final String HOME_KEY = "KEY";

    private String data;

    public PayOutFragment() {
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

    CardView debitCard, billsPayment, deposit;
    ImageView settingsBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_payout, container, false);
        debitCard = v.findViewById(R.id.debit_card_id);
        billsPayment = v.findViewById(R.id.bill_btn_id);
        deposit = v.findViewById(R.id.deposit_id);
        settingsBtn = v.findViewById(R.id.settings_btn);
        debitCard.setOnClickListener(OnTransButtonSelected);
        deposit.setOnClickListener(OnTransButtonSelected);
        billsPayment.setOnClickListener(OnTransButtonSelected);
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
                    Emv.transactionType = TransType.TRANSFER;
                    startActivity(new Intent(getActivity(), CardTransferAmountActivity.class));
                    break;
                case R.id.deposit_id:
                    Emv.transactionType = TransType.DEPOSIT;
                    startActivity(new Intent(getActivity(), DepositAmountActivity.class));
                    break;
                case R.id.bill_btn_id:
                    Emv.transactionType = TransType.ELECTRICITY;
                    startActivity(new Intent(getActivity(), BillpaymentDashboard.class));
                    break;
            }
        }
    };
}