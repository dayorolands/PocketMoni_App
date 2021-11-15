package com.sdk.pocketmonisdk.Model;

public class NotificationModel {
    String time = "";
    String data = "";

    public NotificationModel(String time, String data) {
        this.time = time;
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public String getData() {
        return data;
    }
}
