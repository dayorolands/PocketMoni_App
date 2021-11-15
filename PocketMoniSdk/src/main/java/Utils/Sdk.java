package Utils;

import android.os.Bundle;
import android.os.RemoteException;


import com.horizonpay.smartpossdk.aidl.printer.IAidlPrinter;
import com.horizonpay.smartpossdk.data.PrinterConst;
import com.horizonpay.smartpossdk.data.SysConst;

public class Sdk {

    private static Bundle devInfo;
    static {
        try {
            devInfo = MyApplication.getINSTANCE().getDevice().getSysHandler().getDeviceInfo();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static int getPrinterState(){
        try {
            IAidlPrinter printer = MyApplication.getINSTANCE().getDevice().getPrinter();
            return printer.getPrinterState();
        } catch (RemoteException e) {
            e.printStackTrace();
            return PrinterConst.RetCode.ERROR_PRINT_NOPAPER;
        }
    }

    public static String getFirmware(){
        return devInfo.getString(SysConst.DeviceInfo.DEVICE_FIRMWARE_VER);
    }
    public static String sdkVersion(){
        return devInfo.getString(SysConst.DeviceInfo.DEVICE_SDK_VER);
    }
    public static String getSerialNo(){
        return devInfo.getString(SysConst.DeviceInfo.DEVICE_SN);
    }

    public static String getVendor(){
        return devInfo.getString(SysConst.DeviceInfo.DEVICE_VENDOR);
    }

    public static String getModel(){
        return devInfo.getString(SysConst.DeviceInfo.DEVICE_MODEL);
    }

    public static String getOSVersion(){
        return devInfo.getString(SysConst.DeviceInfo.DEVICE_OS_VER);
    }
}
