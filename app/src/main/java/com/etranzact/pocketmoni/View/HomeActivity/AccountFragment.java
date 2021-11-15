package com.etranzact.pocketmoni.View.HomeActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.etranzact.pocketmoni.R;

public class AccountFragment extends Fragment {

    private static final String HOME_KEY = "KEY";

    private String data;

    public AccountFragment() {
    }

    public static AccountFragment newInstance(String value) {
        AccountFragment fragment = new AccountFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);
        return v;
    }
}