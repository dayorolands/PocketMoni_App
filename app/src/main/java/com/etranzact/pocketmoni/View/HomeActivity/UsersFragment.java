package com.etranzact.pocketmoni.View.HomeActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.etranzact.pocketmoni.R;

public class UsersFragment extends Fragment {

    private static final String HOME_KEY = "KEY";

    private String data;

    public UsersFragment() {
    }

    public static UsersFragment newInstance(String value) {
        UsersFragment fragment = new UsersFragment();
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
        View v = inflater.inflate(R.layout.fragment_users, container, false);
        return v;
    }
}