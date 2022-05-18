package Utils;

import android.app.Application;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

//import com.horizonpay.sample.device.DeviceHelper;
import com.horizonpay.smartpossdk.PosAidlDeviceServiceUtil;
import com.horizonpay.smartpossdk.aidl.IAidlDevice;
import com.horizonpay.utils.BaseUtils;


/***************************************************************************************************
 *                          Copyright (C),  Shenzhen Horizon Technology Limited                    *
 *                                   http://www.horizonpay.cn                                      *
 ***************************************************************************************************
 * usage           :
 * Version         : 1
 * Author          : Ashur Liu
 * Date            : 2017/12/18
 * Modify          : create file
 **************************************************************************************************/

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    private static MyApplication INSTANCE;
    private IAidlDevice device;

    public static MyApplication getINSTANCE(){
        return INSTANCE;
    }

    public IAidlDevice getDevice(){
        return device;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        BaseUtils.init(this);

        bindDriverService();
    }



    private void bindDriverService() {
        PosAidlDeviceServiceUtil.connectDeviceService(this, new PosAidlDeviceServiceUtil.DeviceServiceListen() {
            @Override
            public void onConnected(IAidlDevice device) {
                MyApplication.this.device = device;
                try {
                    MyApplication.this.device.asBinder().linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(int errorcode) {
            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onUnCompatibleDevice() {
            }
        });
    }

    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            //AppLogUtils.error(true, "client", "binderDied");
            if (MyApplication.this.device == null) {
                Log.e("client","binderDied device is null");
                return;
            }

            MyApplication.this.device.asBinder().unlinkToDeath(deathRecipient, 0);
            MyApplication.this.device = null;

            //reBind driver Service
            bindDriverService();
        }
    };

}
