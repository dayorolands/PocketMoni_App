package Utils;

import android.os.Handler;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetworkManager {
    private static String connectionTestHTTPRequest(String data, String transUrl) {
        final String[] result = {""};
        Thread t = new Thread(() -> {
            HttpsURLConnection urlConnection = null;
            try {
                Log.d("Result", "Request url: " + transUrl);
                URL url = new URL(transUrl);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setSSLSocketFactory(getGlobalSSlFactory());
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setReadTimeout(60000);
                urlConnection.setConnectTimeout(60000);
                urlConnection.setDoOutput(false);
                InputStream inputStream = urlConnection.getErrorStream();
                if (inputStream == null) {
                    inputStream = urlConnection.getInputStream();
                    result[0] = inputStream.toString();
                } else {
                    result[0] = "";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
            Log.d("Result", "Response: " + result[0]);
        });
        t.start();
        while (t.isAlive());
        return result[0];
    }


    private static SSLSocketFactory getGlobalSSlFactory() {
        try {
            TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    for(X509Certificate certificate : chain){
                        //Log.d("Result", "checkClientTrusted Certificates: " + certificate + " AuthType: " + authType);
                    }
                }
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    for(X509Certificate certificate : chain){
                        //Log.d("Result", "checkServerTrusted Certificates: " + certificate + " AuthType: " + authType);
                    }
                }
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{tm}, null);
            return sslContext.getSocketFactory();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void checkInternet(Handler handler){
        new Thread(()->{
            int i;
            String result = connectionTestHTTPRequest("","https://www.google.com");
            Log.d("Result", "Result: " + result);
            if(result.equals("")) i = 2;
            else i = 0;
            handler.sendEmptyMessage(i);
        }).start();
    }
}
