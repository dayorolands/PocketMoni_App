package Utils;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.sdk.pocketmonisdk.R;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {

    String requestUrl;
    String fileName;
    String method;
    String data;

    public HttpRequest(String requestUrl, String method, String fileName, String data){
        this.requestUrl = requestUrl;
        this.method = method;
        this.fileName = fileName;
        this.data = data;
    }

    Activity activity;
    public void downloadFile(Activity activity, HashMap<String,String> headers, IDownloadProgress downloadCallbacks){
        this.activity = activity;
        downloadProgressListener = downloadCallbacks;
        Thread t = new Thread(()-> {
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Result", "Request url: " + requestUrl);
                URL url = new URL(requestUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setReadTimeout(60000);
                urlConnection.setConnectTimeout(60000);
                if(method.equals("GET")){
                    urlConnection.setDoOutput(false);
                    for(Map.Entry<String,String> header : headers.entrySet()){
                        urlConnection.setRequestProperty(header.getKey(), header.getValue());
                    }
                }else{
                    urlConnection.setRequestMethod(method);
                    urlConnection.setDoOutput(true);
                    for(Map.Entry<String,String> header : headers.entrySet()){
                        urlConnection.setRequestProperty(header.getKey(), header.getValue());
                    }
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(data.getBytes());
                    out.flush();
                }

                int lengthOfFile = urlConnection.getContentLength();

                File appDir = new File(activity.getExternalFilesDir(null).getAbsolutePath() + "/download/");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                File file = new File(appDir,fileName);
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
                        if (lengthOfFile > 0 || lengthOfFile == -1) {
                            DownloadProgress((int)(total*100 / lengthOfFile));
                        }
                        fos.write(bs, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    inputStream.close();
                    Log.d("Result", "Download Complete");
                    DownloadComplete(uri);
                }else{
                    String error = Keys.readStream(new InputStreamReader(inputStream, Charset.forName("ISO-8859-1")));
                    DownloadFailed(error);
                    Log.d("Result", "Update failed" + error);
                }
            }
            catch (Exception ex){
                DownloadFailed(ex.getMessage());
                ex.printStackTrace();
            }
            finally {
                if(urlConnection != null) urlConnection.disconnect();
            }
        });
        t.start();
    }

    public void downloadImage(Activity activity, HashMap<String,String> headers, ImageView imageView){
        this.activity = activity;
        Thread t = new Thread(()-> {

            HttpURLConnection urlConnection = null;
            try {
                Log.d("Result", "Request url: " + requestUrl);
                URL url = new URL(requestUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setReadTimeout(60000);
                urlConnection.setConnectTimeout(60000);
                if(method.equals("GET")){
                    urlConnection.setDoOutput(false);
                    for(Map.Entry<String,String> header : headers.entrySet()){
                        urlConnection.setRequestProperty(header.getKey(), header.getValue());
                    }
                }else{
                    urlConnection.setRequestMethod(method);
                    urlConnection.setDoOutput(true);
                    for(Map.Entry<String,String> header : headers.entrySet()){
                        urlConnection.setRequestProperty(header.getKey(), header.getValue());
                    }
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(data.getBytes());
                    out.flush();
                }

                int lengthOfFile = urlConnection.getContentLength();

                File appDir = new File(activity.getExternalFilesDir(null).getAbsolutePath() + "/download/");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                File file = new File(appDir,fileName);
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
                        if (lengthOfFile > 0 || lengthOfFile == -1) {
                            //DownloadProgress((int)(total*100 / lengthOfFile));
                        }
                        fos.write(bs, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    inputStream.close();
                    Log.d("Result", "Download Complete");
                    activity.runOnUiThread(()->{
                        try {
                            InputStream is  = activity.getContentResolver().openInputStream(uri);
                            imageView.setImageBitmap(BitmapFactory.decodeStream(is));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }else{
                    String error = Keys.readStream(new InputStreamReader(inputStream, Charset.forName("ISO-8859-1")));
                    Log.d("Result Response:", error);
                    activity.runOnUiThread(()-> imageView.setImageBitmap(null));
                }
            }
            catch (Exception ex){
                activity.runOnUiThread(()-> imageView.setImageBitmap(null));
                ex.printStackTrace();
            }
            finally {
                if(urlConnection != null) urlConnection.disconnect();
            }
        });
        t.start();
    }

    void DownloadProgress(int i){
        activity.runOnUiThread(()->{
            downloadProgressListener.onDownloadProgress(i);
        });
    }

    void DownloadComplete(Uri uri){
        activity.runOnUiThread(()->{
            downloadProgressListener.onDownloadComplete(uri);
        });
    }

    void DownloadFailed(String message){
        activity.runOnUiThread(()->{
            downloadProgressListener.onDownloadFailed(message);
        });
    }


    public static String reqHttp(String inMethod, String reqUrl, String inData, HashMap<String,String> headers, int timeoutSec){
        final String[] result = {""};
        Thread t = new Thread(()-> {
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Result", "Request url: " + reqUrl);
                URL url = new URL(reqUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setReadTimeout(timeoutSec*1000);
                urlConnection.setConnectTimeout(timeoutSec*1000);
                if(inMethod.equals("GET")){
                    urlConnection.setDoOutput(false);
                    for(Map.Entry<String,String> header : headers.entrySet()){
                        urlConnection.setRequestProperty(header.getKey(), header.getValue());
                    }
                }else{
                    urlConnection.setRequestMethod(inMethod);
                    urlConnection.setDoOutput(true);
                    for(Map.Entry<String,String> header : headers.entrySet()){
                        urlConnection.setRequestProperty(header.getKey(), header.getValue());
                    }
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(inData.getBytes());
                    out.flush();
                }

                InputStream inputStream = urlConnection.getErrorStream();
                if (inputStream == null) {
                    inputStream = urlConnection.getInputStream();
                    result[0] = Keys.readStream(inputStream);
                }else{
                    Log.d("Result","Response: " + Keys.readStream(inputStream));
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
        while (t.isAlive()){
            SystemClock.sleep(10);
        }
        return result[0];
    }

    public static String reqHttp(String inMethod, String reqUrl, String inData, HashMap<String,String> headers){
        final String[] result = {""};
        Thread t = new Thread(()-> {
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Result", "Request url: " + reqUrl);
                URL url = new URL(reqUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setReadTimeout(60000);
                urlConnection.setConnectTimeout(60000);
                if(inMethod.equals("GET")){
                    urlConnection.setDoOutput(false);
                    for(Map.Entry<String,String> header : headers.entrySet()){
                        urlConnection.setRequestProperty(header.getKey(), header.getValue());
                    }
                }else{
                    urlConnection.setRequestMethod(inMethod);
                    urlConnection.setDoOutput(true);
                    for(Map.Entry<String,String> header : headers.entrySet()){
                        urlConnection.setRequestProperty(header.getKey(), header.getValue());
                    }
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(inData.getBytes());
                    out.flush();
                }

                InputStream inputStream = urlConnection.getErrorStream();
                if (inputStream == null) {
                    inputStream = urlConnection.getInputStream();
                    result[0] = Keys.readStream(inputStream);
                }else{
                    Log.d("Result","Response: " + Keys.readStream(inputStream));
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
        while (t.isAlive()){
            SystemClock.sleep(10);
        }
        return result[0];
    }


    IDownloadProgress downloadProgressListener;
    public interface IDownloadProgress{
        void onDownloadProgress(int i);
        void onDownloadComplete(Uri uri);
        void onDownloadFailed(String message);
    }
}
