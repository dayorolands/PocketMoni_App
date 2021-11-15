package com.sdk.pocketmonisdk.BindServices;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.sdk.pocketmonisdk.Model.NotificationModel;
import com.sdk.pocketmonisdk.TransEnvironment.Middleware;
import com.sdk.pocketmonisdk.Views.ConfigureDeviceActivity;

import java.io.File;
import java.util.List;

import Utils.Emv;
import Utils.Keys;
import Utils.Net;
import Utils.SharedPref;
import Utils.TransDB;

public class ApplicationServices extends Service {

    private final IBinder binderService = new LocalBinder();
    public ApplicationServices() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binderService;
    }

    public void stopActivity(){
        if(checkRouteHandler != null) {
            notificationHandler.removeCallbacks(notificationRunnable);
            checkRouteHandler.removeCallbacks(checkRouteRunnable);
            callHomeHandler.removeCallbacks(callHomeRunnable);
//            ecoHandler.removeCallbacks(ecoHandlerRunnable);
        }
    }

//    private Handler ecoHandler;
//    private final int ECO_INTERVAL = 300000; //5 minutes

    private Handler notificationHandler;
    private final int NOTIFICATION_INTERVAL = 120000; //2 minutes

    private Handler callHomeHandler;
    private final int CALL_HOME_INTERVAL = 600000; //10 minutes

    private Handler checkRouteHandler;
    private final int CHECK_ROUTE_INTERVAL = 650000; //15 minutes

    private static Activity activity;
    public void doActivities(Activity a){
        activity = a;
        notificationHandler = new Handler(getMainLooper());
        notificationHandler.postDelayed(notificationRunnable, NOTIFICATION_INTERVAL);

        checkRouteHandler = new Handler(getMainLooper());
        checkRouteRunnable.run();
        checkRouteHandler.postDelayed(checkRouteRunnable,CHECK_ROUTE_INTERVAL);

        callHomeHandler = new Handler(getMainLooper());
        callHomeHandler.postDelayed(callHomeRunnable, CALL_HOME_INTERVAL);

//        ecoHandler = new Handler(getMainLooper());
//        ecoHandler.postDelayed(ecoHandlerRunnable,ECO_INTERVAL);
    }

    Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            try{
                new Thread(()-> sendFailedNotification()).start();
            }finally {
                notificationHandler.postDelayed(this,NOTIFICATION_INTERVAL);
            }
        }
    };

    Runnable checkRouteRunnable = new Runnable() {
        @Override
        public void run() {
            try{
                if(ConfigureDeviceActivity.isConfigurationRunning) return;
                new Thread(()->{
                    String isSuccessful = Middleware.checkRoute(activity);
                    if(isSuccessful.equals("configure")) {
                        activity.runOnUiThread(()->{
                            Intent intent = new Intent(getApplicationContext(), ConfigureDeviceActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ConfigureDeviceActivity.parentActivity = activity;
                            startActivity(intent);
                        });
                    }else if(isSuccessful.equals("forceUpgrade")){
                        activity.runOnUiThread(()->{showUpgradeDialog();});
                    }
                }).start();
            }finally {
                checkRouteHandler.postDelayed(this,CHECK_ROUTE_INTERVAL);
            }
        }
    };

    Runnable callHomeRunnable = new Runnable() {
        @Override
        public void run() {
            try{
                new Thread(()->Keys.doCallHome(activity)).start();
            }finally {
                callHomeHandler.postDelayed(this,CALL_HOME_INTERVAL);
            }
        }
    };

//    Runnable ecoHandlerRunnable = new Runnable() {
//        @Override
//        public void run() {
//            try{
//                new Thread(()->Keys.doEco(activity)).start();
//            }finally {
//                ecoHandler.postDelayed(this,ECO_INTERVAL);
//            }
//        }
//    };

    public class LocalBinder extends Binder {
        public ApplicationServices getService(){
            return ApplicationServices.this;
        }
    }

    private void sendFailedNotification(){
        try{
            Emv.accessToken = SharedPref.get(this, "accesstoken", "");
            TransDB db = new TransDB(this);
            db.open();
            List<NotificationModel> notificationData = db.getNotificationRecords();
            if(notificationData.size() < 1) {
                db.close();
                return;
            }
            activity.runOnUiThread(()->{
                Toast.makeText(ApplicationServices.this, "Sending Notification", Toast.LENGTH_SHORT).show();
            });
            String data = notificationData.get(0).getData();
            String time = notificationData.get(0).getTime();
            data = Keys.removeSpecialCharacters(data);
            String result = Middleware.httpRequest(data,"POST", Emv.notificationURL);
            String respCode = Keys.parseJson(result, "responseCode");
            if(respCode.equals("00")){
                db.deleteData(time);
            }
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showUpgradeDialog(){
        AlertDialog ac = new AlertDialog.Builder(activity).create();
        ac.setTitle("New Update Available");
        ac.setMessage("Please upgrade to continue enjoying our service?");
        ac.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
                return;
            }
        });
        ac.setButton(AlertDialog.BUTTON_POSITIVE, "UPGRADE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(getExternalFilesDir(null).getAbsolutePath() + "/download/", "update.apk");
                if (file.exists()) {
                    try{
                        String appVersion = Keys.readAPKVersionName(activity, file.getPath());
                        int currentVersion = Integer.parseInt(Emv.getAppVersion(activity).replace(".",""));
                        int newVersion = Integer.parseInt(appVersion.replace(".",""));
                        if (newVersion > currentVersion) {
                            final Uri uri = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ?
                                    FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file) :
                                    Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                            activity.finish();
                        } else {
                            Net.remoteUpgrade(activity, "", "GET", Emv.upgradeURL);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Net.remoteUpgrade(activity, "", "GET", Emv.upgradeURL);
                    }
                }else{
                    Net.remoteUpgrade(activity, "", "GET", Emv.upgradeURL);
                }
            }
        });
        ac.setCanceledOnTouchOutside(false);
        ac.setCancelable(false);
        ac.show();
    }
}