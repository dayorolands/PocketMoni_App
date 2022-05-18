package com.sdk.pocketmonisdk.TransEnvironment;

import android.util.Log;

import okhttp3.CertificatePinner;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class XpressPay {

    public static String decryptMasterKey(String masterKey){
        String data = "{\n" +
                        "  \"MasterKey\": \""+masterKey+"\"\n" +
                        "}";

        Log.d("Result", "Request: " + data);
        String result = httpRequest(data, "POST", "http://80.88.8.56:552/api/GetPlainMasterKey");
        result = result.replace("\"","");
        Log.d("Result", "Response: " + result);
        if(!result.equals("")){
            return result;
        }
        return "";
    }

    public static String httpRequest(String data, String method, String url){

        final String[] result = {""};
        Thread t = new Thread(()->{
            try {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(data, mediaType);
                Request request = new Request.Builder()
                        .url(url)
                        .method(method, body)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Basic RXRyYW56YWN0UE9TOjIzNjhkOGZjLTA0ZDMtNDIwNy05YjFhLTUzZjY1YjAxYWZiZA==")
                        .build();
                Response response = client.newCall(request).execute();
                result[0] = response.body().string().replace("\"", "");
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        });
        t.start();
        while (t.isAlive());
        return result[0];
    }
}
