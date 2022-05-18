package com.etranzact.pocketmoni.Model;

import android.app.Activity;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import Utils.DateTime;
import Utils.Emv;
import Utils.HttpRequest;

public class BankAccountModel {
    public String bankCode;
    public String acctNo;

    private final Activity activity;
    public BankAccountModel(Activity activity) {
        this.activity = activity;
    }

    public String getData() {
        return "{\n" +
            "  \"accountNo\": \"" + acctNo + "\",\n" +
            "  \"bankCode\": \"" + bankCode + "\",\n" +
            "  \"serialNo\": \"" + Emv.serialNumber + "\",\n" +
            "  \"terminalId\": \"" + Emv.terminalId + "\"\n" +
            "}";
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public void setAcctNo(String acctNo) {
        this.acctNo = acctNo;
    }

    public void accountNoValidation(ICreateWallet internetCallbacks) {
        internetListener = internetCallbacks;
        new Thread(() -> {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("Accept", "*/*");
            hashMap.put("Content-Type", "application/json");
            hashMap.put("Authorization", Emv.accessToken);
            for (Map.Entry<String, String> header : hashMap.entrySet()) {
                Log.d("Result", "Header> " + header.getKey() + " : " + header.getValue());
            }
            Log.d("Result", "Request " + getData());
            String response = HttpRequest.reqHttp("POST", Emv.acctValidationUrl, getData(), hashMap);
            activity.runOnUiThread(() -> {
                internetListener.requestResponse(response);
            });
        }).start();
    }

    private static ICreateWallet internetListener;

    public interface ICreateWallet {
        void requestResponse(String message);
    }
}
