package com.etranzact.pocketmoni.View.Electricity.Cash;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.R;
import com.horizonpay.smartpossdk.aidl.printer.AidlPrinterListener;
import com.horizonpay.smartpossdk.aidl.printer.IAidlPrinter;
import com.horizonpay.smartpossdk.data.PrinterConst;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Locale;

import Utils.CombBitmap;
import Utils.Emv;
import Utils.GenerateBitmap;
import Utils.Keys;
import Utils.MyApplication;
import Utils.TransType;

public class ElectricityReceipt {

    private static IAidlPrinter printer;
    private static Activity activity;

    public static void Print(FragmentActivity a) {
        activity = a;
        try {
            printer = MyApplication.getINSTANCE().getDevice().getPrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            printer.printBmp(true, false, printBitmap(), 0, new AidlPrinterListener.Stub() {
                @Override
                public void onError(int i) throws RemoteException {
                    switch (i) {
                        case PrinterConst.RetCode.ERROR_PRINT_NOPAPER:
                            ShowToast("PRINTER OUT TO PAPER");
                            break;
                        case PrinterConst.RetCode.ERROR_DEV:
                            ShowToast("ERROR PRINTING, TRY AGAIN");
                            break;
                        case PrinterConst.RetCode.ERROR_DEV_IS_BUSY:
                            ShowToast("DEVICE IS BUSY");
                            break;
                        default:
                        case PrinterConst.RetCode.ERROR_OTHER:
                            ShowToast("SOMETHING HAPPENED, TRY AGAIN");
                            break;
                    }
                }

                @Override
                public void onPrintSuccess() throws RemoteException {
                    Log.d("Result", "PRINTING COMPLETE");
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void doReprint(FragmentActivity a, final String[] result) {
        activity = a;
        Emv.initializeEmv(activity);
        try {
            printer = MyApplication.getINSTANCE().getDevice().getPrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            printer.printBmp(true, false, reprintBitmap(result), 0, new AidlPrinterListener.Stub() {
                @Override
                public void onError(int i) throws RemoteException {
                    switch (i) {
                        case PrinterConst.RetCode.ERROR_PRINT_NOPAPER:
                            ShowToast("PRINTER OUT OF PAPER");
                            break;
                        case PrinterConst.RetCode.ERROR_DEV:
                            ShowToast("ERROR PRINTING, TRY AGAIN");
                            break;
                        case PrinterConst.RetCode.ERROR_DEV_IS_BUSY:
                            ShowToast("DEVICE IS BUSY");
                            break;
                        default:
                        case PrinterConst.RetCode.ERROR_OTHER:
                            ShowToast("SOMETHING HAPPENED, TRY AGAIN");
                            break;
                    }
                }

                @Override
                public void onPrintSuccess() throws RemoteException {
                    Log.d("Result", "PRINTING COMPLETE");
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap printBitmap() {
        //Print Logo
        CombBitmap combBitmap = new CombBitmap();
        ElectricityModel electricityModel = new ElectricityModel();
        //Display the logo here
        Bitmap bitmap;
        String filename = electricityModel.getSessionCategory() + ".jpg";
        File electricityFile = new File(activity.getExternalFilesDir(null).getAbsolutePath() + "/Android/data" + activity.getApplicationContext().getPackageName() + "/ElectricityLogos" + "/" + filename);
        Log.d("Result", "Here I want to check the path the terminal is displaying " + electricityFile);
        try {
            FileInputStream fileInputStream = new FileInputStream(electricityFile);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
            combBitmap.addBitmap(GenerateBitmap.formatBitmap(bitmap, GenerateBitmap.AlignEnum.CENTER));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //Title
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap(Emv.agentName, 30, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap(Emv.agentLoc, 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.generateLine(1)); // print one line

        ElectricityModel model = new ElectricityModel();
        //Content
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("MERCHANT ID: ", Emv.merchantId, 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("TERMINAL ID: ", Emv.terminalId, 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("DATE: ", Emv.getTransactionDate(), 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("TIME: ", Emv.getTransactionTime(), 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("AGENT ID: ", Emv.agentId, 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("TRANS TYPE: ", Emv.transactionType.toString(), 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("REF: ", model.getPaymentRef(), 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("NAME: ", model.getCustomerName(), 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("SERVICE ADDRESS:", 22, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap(model.getAddress(), 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        if(!Emv.responseCode.equals("00")){
            combBitmap.addBitmap(GenerateBitmap.str2Bitmap("TRANSACTION DECLINED", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        }else{
            try {
                if(!model.getToken().isEmpty()){
                    combBitmap.addBitmap(GenerateBitmap.str2Bitmap("TOKEN:", 30, GenerateBitmap.AlignEnum.CENTER, true, true));
                    combBitmap.addBitmap(GenerateBitmap.str2Bitmap(model.getToken(), 30, GenerateBitmap.AlignEnum.CENTER, true, true));
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                combBitmap.addBitmap(GenerateBitmap.str2Bitmap(model.getDescription(), 22, GenerateBitmap.AlignEnum.CENTER, true, false));
            }
        }
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap(Emv.responseMessage, 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("RESPONSE CODE: ", Emv.responseCode, 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("AMOUNT: ", String.format(Locale.getDefault(),"%,.2f", Double.parseDouble(model.getAmount()))+" NGN", 30, true, true));
        String resultRRN = (Emv.environment.equals("TMS")) ?
                Keys.genKimonoRRN(activity.getApplicationContext(), Emv.transactionStan) :
                Keys.genNibssRRN(activity.getApplicationContext(),Emv.transactionStan);
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("RRN: ", resultRRN, 20,true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("STAN: ", Keys.padLeft(Emv.transactionStan, 6, '0'), 20,true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap(copy.toUpperCase(), 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("Contact Us: cic@etranzactng.com",20, GenerateBitmap.AlignEnum.CENTER, true,false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("Center line: 09087989094", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("WhatsApp: 08188639818", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("--------------------------------------", 32, GenerateBitmap.AlignEnum.CENTER, true, false)); // 打印一行直线
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("Thanks for choosing pocketmoni", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("Pocketmoni V" + Emv.getAppVersion(activity), 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.generateGap(60)); // print row gap
        combBitmap.addBitmap(GenerateBitmap.generateLine(1)); // print one line
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("--X--X--X--X--X--X--X--X--X--X--X--X", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.generateGap(60)); // print row gap
        Bitmap bp = combBitmap.getCombBitmap();

        return bp;
    }

    private static Bitmap reprintBitmap(final String[] result) {
        //Print Logo
        CombBitmap combBitmap = new CombBitmap();
        Bitmap bitmap;
        String filename = result[19] + ".jpg";
        File electricityFile = new File(activity.getExternalFilesDir(null).getAbsolutePath() + "/Android/data" + activity.getApplicationContext().getPackageName() + "/ElectricityLogos" + "/" + filename);
        Log.d("Result", "Here I want to check the path the terminal is displaying " + electricityFile);
        try {
            FileInputStream fileInputStream = new FileInputStream(electricityFile);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
            combBitmap.addBitmap(GenerateBitmap.formatBitmap(bitmap, GenerateBitmap.AlignEnum.CENTER));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //MERCHANT LOCATION
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap(Emv.agentName, 30, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap(Emv.agentLoc, 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.generateLine(1)); // print one line

        //Content
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("MERCHANT ID: ", Emv.merchantId, 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("TERMINAL ID: ", Emv.terminalId, 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("DATE: ", result[0], 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("TIME: ", result[1], 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("AGENT ID: ", Emv.agentId, 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("TRANS TYPE: ", result[4], 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("CUSTOMER ID: ", result[14], 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("DISCO: ", result[15], 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("NAME: ", result[13], 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("REF: ", result[16], 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("SERVICE ADDRESS:", 22, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap(result[18], 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        if(!result[7].equals("00")){
            combBitmap.addBitmap(GenerateBitmap.str2Bitmap("TRANSACTION DECLINED", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        }else{
            try {
                if(!result[20].isEmpty()){
                    combBitmap.addBitmap(GenerateBitmap.str2Bitmap("TOKEN", 30, GenerateBitmap.AlignEnum.CENTER, true, true));
                    combBitmap.addBitmap(GenerateBitmap.str2Bitmap(result[20], 30, GenerateBitmap.AlignEnum.CENTER, true, true));
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                combBitmap.addBitmap(GenerateBitmap.str2Bitmap(result[17], 24, GenerateBitmap.AlignEnum.CENTER, true, false));
            }
        }
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap(result[5],20, GenerateBitmap.AlignEnum.CENTER, true, false)); //RESPONSE MESSAGE
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("RESPONSE CODE: ", result[7], 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("AMOUNT: ", result[6]+" NGN", 30, true, true));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("RRN: ", result[8], 20,true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("STAN: ", result[9], 20, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("REPRINT COPY", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("Contact Us: cic@etranzactng.com",20, GenerateBitmap.AlignEnum.CENTER, true,false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("Center line: 09087989094", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("WhatsApp: 08188639818", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("--------------------------------------", 32, GenerateBitmap.AlignEnum.CENTER, true, false)); // 打印一行直线
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("Thanks for choosing pocketmoni", 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("Pocketmoni V" + Emv.getAppVersion(activity), 20, GenerateBitmap.AlignEnum.CENTER, true, false));
        combBitmap.addBitmap(GenerateBitmap.generateGap(60)); // print row gap
        combBitmap.addBitmap(GenerateBitmap.generateLine(1)); // print one line
        combBitmap.addBitmap(GenerateBitmap.str2Bitmap("--X--X--X--X--X--X--X--X--X--X--X--X", 20, GenerateBitmap.AlignEnum.CENTER, true, false));

        combBitmap.addBitmap(GenerateBitmap.generateGap(60)); // print row gap
        Bitmap bp = combBitmap.getCombBitmap();

        return bp;
    }

    private static String copy = "";
    public static void setMerchantOrCustomerCopy(String cpy) {
        copy = cpy;
    }

    public static void ShowToast(String message){
        Handler handler = new Handler(Looper.getMainLooper(), (msg)->{
            Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            return true;
        });
        handler.sendEmptyMessage(0);
    }
}
