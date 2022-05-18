package com.sdk.pocketmonisdk.TransEnvironment;

import android.app.Activity;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import Utils.Emv;
import Utils.Keys;
import Utils.SharedPref;
import Utils.TransRoute;

public class Middleware {
    public static String checkRoute(Activity activity) {
        String data = " {\n" +
                "\t\"current_version\" : \""+Emv.getAppVersion(activity)+"\",\n" +
                "\t\"serial_no\" : \""+Emv.serialNumber+"\" \n" +
                "}";
        Log.d("Result", "Request: " + data);
        Emv.accessToken = SharedPref.get(activity, "accesstoken", "");
        String result = httpRequest(data, "POST", Emv.routeConfigURL);
        Log.d("Result", "Response: " + result);

        String resp = SharedPref.get(activity,"routeResp", "");
        String respCode = Keys.parseJson(result, "responseCode");
        if(respCode.equals("00")){
            //Here I am checking to see if the response that is stored in shared preference is same as response returned when the
            //terminal automatically checks for the route to use. If the route is different, the route from TMS overrides the
            //route set by the terminal due to server downtime on one route.
            if(!resp.isEmpty()){
                if(!result.substring(0,result.indexOf("timeStamp")).equals(resp.substring(0,resp.indexOf("timeStamp")))){
                    TransRoute.resetRoute();
                }
            }

            //Set minimum charge
            SharedPref.set(activity, "minimumAmount", Keys.parseJson(result, "minimum_amount"));

            SharedPref.set(activity, "routeResp", result);

            String forceUpgrade = Keys.parseJson(result, "forceUpgrade");
            //This line is used to determine weather the terminal can upgrade or not, Values is usually set on the TMS.
            String canUpgrade = Keys.parseJson(result, "can_upgrade");
            String forceConfig = Keys.parseJson(result, "forceConfig");
            if(forceUpgrade.toLowerCase().equals("true") && canUpgrade.toLowerCase().equals("true")) return "forceUpgrade";
            else if(forceConfig.equals("true")) return "configure";
            else return "true";
        }
        return "false";
    }

    public static boolean doSynchronization(Activity activity){
        Emv.initializeEmv(activity);

        String data = "{\n" +
                "\t\"mcc\":\""+Emv.mcc+"\",\n" +
                "\t\"processor_addr\":\""+Emv.merchantLocation+" \",\n" +
                "\t\"merchant_id\":\""+Emv.merchantId+"\",\n" +
                "\t\"serial_no\":\""+Emv.serialNumber+"\",\n" +
                "\t\"country_code\":\""+ Emv.TerminalCountryCode.split("\\|")[1]+"\",\n" +
                "\t\"processor_name\":\"NIBSS\",\n" +
                "\t\"force_config\": true\n" +
                "}";

        Log.d("Result", "Request: " + data);
        String result = httpRequest(data, "POST", Emv.synchronizationURL);
        Log.d("Result", "Response: " + result);
        String respCode = Keys.parseJson(result, "responseCode");
        if(respCode.equals("00")){
            return true;
        }
        return false;
    }


    public static String httpRequest(final String data, final String method, final String transUrl){
        final String[] result = {""};
        Thread t = new Thread(()->{
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Result", "Request url: " + transUrl);
                URL url = new URL(transUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setReadTimeout(60000);
                urlConnection.setConnectTimeout(60000);
                if(method.equals("GET")){
                    urlConnection.setDoOutput(false);
                }else{
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty( "Accept", "*/*" );
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Authorization", Emv.accessToken);
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(data.getBytes());
                    out.flush();
                }
                InputStream inputStream = urlConnection.getErrorStream();
                if (inputStream == null) {
                    inputStream = urlConnection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("ISO-8859-1"));
                    result[0] = Keys.readStream(reader);
                }else{
                    Log.d("Result", "Response: " + Keys.readStream(new InputStreamReader(inputStream, Charset.forName("ISO-8859-1"))));
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
            }
        });
        t.start();
        while (t.isAlive());
        return result[0];
    }
}
