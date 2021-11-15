package Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

public class FormDataRequest {
    private String boundary;
    private static final String LINE = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;
    private Activity activity;

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     *
     * @param requestURL
     * @param charset
     * @param headers
     * @throws IOException
     */
    public FormDataRequest(Activity activity, String requestURL, String charset, Map<String, String> headers, IConnectionStatus connectionStatusCallBacks) {
        this.connectionListener = connectionStatusCallBacks;
        this.charset = charset;
        this.activity = activity;
        new Thread(()->{
            boundary = UUID.randomUUID().toString();
            try{
                URL url = new URL(requestURL);
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setUseCaches(false);
                httpConn.setDoOutput(true);    // indicates POST method
                httpConn.setDoInput(true);
                httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                if (headers != null && headers.size() > 0) {
                    for (String key : headers.keySet()) {
                        String value = headers.get(key);
                        httpConn.setRequestProperty(key, value);
                    }
                }
                outputStream = httpConn.getOutputStream();
                writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
                activity.runOnUiThread(()->connectionListener.connectionStatus(true));

            }catch (Exception e){
                e.printStackTrace();
                activity.runOnUiThread(()->connectionListener.connectionStatus(false));
            }
        }).start();
    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE);
        writer.append("Content-Type: text/plain; charset=" + charset).append(LINE);
        writer.append(LINE);
        writer.append(value).append(LINE);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName Filed name e.g: imfFile
     * @param uri Uri to file
     */
    public void addFilePart(String fieldName, String uploadName, Uri uri){
        try{
            File appDir = new File(activity.getExternalFilesDir(null).getAbsolutePath() + "/download/");
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            File file = new File(appDir,uploadName);
            if(file.exists()){
                file.delete();
            }
            FileOutputStream os = new FileOutputStream(file);
            os.write(Keys.uriToByteArray(activity,activity.getContentResolver().openInputStream(uri)));

            String fileName = file.getName();
            writer.append("--" + boundary).append(LINE);
            writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE);
            writer.append("Content-Transfer-Encoding: binary").append(LINE);
            writer.append(LINE);
            writer.flush();

            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            writer.append(LINE);
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName Filed name e.g: imfFile
     * @param bitmap Bitmap image
     */
    public void addFilePart(String fieldName, String uploadName, Bitmap bitmap){
        try{
            File appDir = new File(activity.getExternalFilesDir(null).getAbsolutePath() + "/download/");
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            File file = new File(appDir,uploadName);
            if(file.exists()){
                file.delete();
            }
            FileOutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,os);

            String fileName = file.getName();
            writer.append("--" + boundary).append(LINE);
            writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE);
            writer.append("Content-Transfer-Encoding: binary").append(LINE);
            writer.append(LINE);
            writer.flush();

            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            writer.append(LINE);
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return String as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public void upload(IConnectionStatus uploadCallBacks) {
        this.connectionListener = uploadCallBacks;
        writer.flush();
        writer.append("--" + boundary + "--").append(LINE);
        writer.close();
        new Thread(()->{
            String resp = "";
            try{
                InputStream inputStream = httpConn.getErrorStream();
                if (inputStream == null) {
                    inputStream = httpConn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("ISO-8859-1"));
                    resp = Keys.readStream(reader);
                }else{
                    resp = Keys.readStream(new InputStreamReader(inputStream, Charset.forName("ISO-8859-1")));
                }
                Log.d("Result", "Response: " + resp);
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                String finalResp = resp;
                activity.runOnUiThread(()->connectionListener.uploadResponse(finalResp));
            }
        }).start();
    }

    public void disconnect(){
        if(httpConn != null){
            httpConn.disconnect();
        }
    }

    private IConnectionStatus connectionListener;
    public interface IConnectionStatus{
        default void connectionStatus(boolean iSConnected){}
        default void uploadResponse(String response){}
    }
}
