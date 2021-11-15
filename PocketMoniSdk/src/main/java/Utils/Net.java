package Utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.sdk.pocketmonisdk.Dialogs.CustomProgressDialog;
import com.sdk.pocketmonisdk.TransEnvironment.Middleware;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Net {
    public static String httpRequest(String data, final String method, final String transUrl){
        final String[] result = {""};
        Thread t = new Thread(()->{
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Result", "Request url: " + transUrl);
                URL url = new URL(transUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setReadTimeout(120000);
                urlConnection.setConnectTimeout(120000);
                if(method.equals("GET")){
                    urlConnection.setDoOutput(false);
                }else{
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty( "user-agent", "PocketMoni");
                    urlConnection.setRequestProperty( "Accept", "*/*");
                    urlConnection.setRequestProperty("Content-Type", "application/xml");
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
                if(urlConnection != null) urlConnection.disconnect();
            }
        });
        t.start();
        while (t.isAlive());
        return result[0];
    }

    public static String tcpClient(final String message){
        final String[] result = {""};
        Thread t = new Thread(()-> {
            try{
                int c;
                Socket s = new Socket(Emv.transip, Integer.parseInt(Emv.transport));
                s.setSoTimeout(70000);
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();
                Log.d("Result", "Sent: " + message);
                out.write(Keys.hexStringToByteArray(message));
                out.flush();
                String resp = "";
                while ((c = in.read()) != -1) {
                    resp += Character.toString((char)c);
                }
                Log.d("Result", "Rec: " + resp);
                s.close();
                result[0] = Keys.asciiToHex(resp);
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        });
        t.start();
        while(t.isAlive());
        return result[0];
    }

    public static String socketClient(SSLSocketFactory sslSocketFactory, String message, String domain, String port){
        final String[] result = {""};
        Thread t = new Thread(()-> {
            try{
                String ip = "";
                if(domain.contains("http")){
                    InetAddress address = InetAddress.getByName(new URL(domain).getHost());
                    ip = address.getHostAddress();
                }else{
                    ip = domain;
                }
                SSLSocket s =(SSLSocket) sslSocketFactory.createSocket(ip, Integer.parseInt(port));
                s.startHandshake();
                s.setSoTimeout(70000);
                s.setTcpNoDelay(true);
                s.setKeepAlive(true);
                Log.d("Result", "Request" + message);
                PrintWriter outWriter = new PrintWriter(s.getOutputStream(),true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                outWriter.println(message);
                result[0] = reader.readLine();
                s.close();
            }
            catch (Exception ex){
                ex.printStackTrace();
                Log.d("Result", "Response" + ex.toString());
            }
        });
        t.start();
        while(t.isAlive());
        return result[0];
    }

    public static String sslTcpClient(final String message){
        final String[] result = {""};
        Thread t = new Thread(()-> {
            try{
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                int c;
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                SSLSocketFactory factory = sslContext.getSocketFactory();
                SSLSocket s =(SSLSocket)factory.createSocket(Emv.transip, Integer.parseInt(Emv.transport));
                s.startHandshake();
                s.setSoTimeout(70000);
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();
                out.write(Keys.hexStringToByteArray(message));
                out.flush();
                String resp = "";
                while ((c = in.read()) != -1) {
                    resp += Character.toString((char)c);
                }
                s.close();
                result[0] = Keys.asciiToHex(resp);
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        });
        t.start();
        while(t.isAlive());
        return result[0];
    }


    private static CustomProgressDialog dialog;
    public static void remoteUpgrade(Activity activity, String data, String method, String apkUrl){
        dialog = new CustomProgressDialog(activity);
        dialog.show();
        Thread t = new Thread(()-> {
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Result", "Request url: " + apkUrl);
                URL url = new URL(apkUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setReadTimeout(60000);
                urlConnection.setConnectTimeout(60000);
                if(method.equals("GET")){
                    urlConnection.setDoOutput(false);
                    urlConnection.setRequestProperty("Authorization", Emv.accessToken);
                }else{
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty( "user-agent", "PocketMoni");
                    urlConnection.setRequestProperty( "Accept", "*/*" );
                    urlConnection.setRequestProperty("Content-Type", "application/xml");
                    //urlConnection.setRequestProperty("Authorization", Emv.accessToken);
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(data.getBytes());
                    out.flush();
                }

                int lengthOfFile = urlConnection.getContentLength();

                File appDir = new File(activity.getExternalFilesDir(null).getAbsolutePath() + "/download/");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                File file = new File(appDir, "update.apk");
                if (file.exists()) {
                    file.delete();
                }

                final Uri uri = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ?
                        FileProvider.getUriForFile(activity.getApplicationContext(), activity.getPackageName() + ".provider", file) :
                        Uri.fromFile(file);

                FileOutputStream fos = new FileOutputStream(file);
                InputStream inputStream = urlConnection.getErrorStream();
                if (inputStream == null) {
                    inputStream = urlConnection.getInputStream();
                    byte[] bs = new byte[1024];
                    int len;
                    long total = 0;
                    while ((len = inputStream.read(bs)) != -1) {
                        total = total + len;
                        if (lengthOfFile > 0) {
                            sendMessage((int)(total*100 / lengthOfFile));
                        }
                        fos.write(bs, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    inputStream.close();
                    Thread.sleep(1000);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activity.startActivity(intent);
                    Log.d("Result", "Download Complete");
                    activity.finish();
                }else{
                    activity.runOnUiThread(()->{
                        Toast.makeText(activity.getApplicationContext(), "Update failed", Toast.LENGTH_LONG).show();
                    });
                    Log.d("Result", "Update failed" + Keys.readStream(new InputStreamReader(inputStream, Charset.forName("ISO-8859-1"))));
                }
            }
            catch (Exception ex){
                sendMessage(404);
                ex.printStackTrace();
            }
            finally {
                if(urlConnection != null) urlConnection.disconnect();
            }
        });
        t.start();
    }

    private static final Handler handler = new Handler(Looper.getMainLooper(), (msg)->{
        dialog.sendProgress(msg.getData().getInt("key"));
        return true;
    });

    private static void sendMessage(int message){
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("key", message);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}