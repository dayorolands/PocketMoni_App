package Utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.sdk.pocketmonisdk.BindServices.ApplicationServices;
import com.sdk.pocketmonisdk.TransEnvironment.Middleware;
import com.sdk.pocketmonisdk.TransEnvironment.XpressPay;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;


public class Keys {

    public static String rsaEncrypt(String data, String modulus, String pubExp) {
        try{
            // generating the key from modulus & private exponent
            KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus, 16), new BigInteger(pubExp, 16));
            RSAPublicKey pubKey = (RSAPublicKey) rsaFactory.generatePublic(spec);
            // using it in a raw cipher
            Cipher c= Cipher.getInstance("RSA/ECB/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] outBytes = c.doFinal(hexStringToByteArray(data));
            return byteToHexString(outBytes);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static String rsaDecrypt(String data, String modulus, String pubExp) {
        try{
            // generating the key from modulus & private exponent
            KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKeySpec spec = new RSAPrivateKeySpec(new BigInteger(modulus, 16), new BigInteger(pubExp, 16));
            RSAPrivateKey pubKey = (RSAPrivateKey) rsaFactory.generatePrivate(spec);
            // using it in a raw cipher
            Cipher c= Cipher.getInstance("RSA/ECB/NoPadding");
            c.init(Cipher.DECRYPT_MODE, pubKey);
            byte[] outBytes = c.doFinal(hexStringToByteArray(data));
            return byteToHexString(outBytes);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static String trippleDesEncrypt(String data, String Key){
        try{
            final SecretKey key = new SecretKeySpec(hexStringToByteArray(Key),"DESede");
            //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            final Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            final byte[] cipherText = cipher.doFinal(hexStringToByteArray(data));
            return byteToHexString(cipherText);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static String trippleDesDecrypt(String data, String Key){
        try{
            final SecretKey key = new SecretKeySpec(hexStringToByteArray(Key),"DESede");
            //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            final Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            final byte[] cipherText = cipher.doFinal(hexStringToByteArray(data));
            return byteToHexString(cipherText);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static String desEncrypt(String data, String Key){
        try{
            final SecretKey key = new SecretKeySpec(hexStringToByteArray(Key),"DES");
            //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            final Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            final byte[] cipherText = cipher.doFinal(hexStringToByteArray(data));
            return byteToHexString(cipherText).substring(0,16);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static String desEncryptDukpt(String workingKey, String pan, String clearPin){
        try{
            String pinblock = XORorANDorORfuction(workingKey, encryptPinBlock(pan, clearPin), "^");
            byte[] dData = hexStringToByteArray(pinblock);
            byte[] keyData = hexStringToByteArray(workingKey);
            final SecretKey key = new SecretKeySpec(keyData,"DES");
            //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            final Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            final byte[] cipherText = cipher.doFinal(dData);
            return XORorANDorORfuction(workingKey, byteToHexString(cipherText).substring(0,16), "^");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static String desDecrypt(String data, String Key){
        try{
            final SecretKey key = new SecretKeySpec(hexStringToByteArray(Key), "DES");
            //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            final Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            final byte[] cipherText = cipher.doFinal(hexStringToByteArray(data));
            return byteToHexString(cipherText).substring(0,16);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean SilentInstall(Activity activity, String filePath){
        PackageManager packageManager = activity.getPackageManager();
        Class<?> pmClz = packageManager.getClass();
        try {
            if(filePath.isEmpty()){
                Toast.makeText(activity, "Invalid file path", Toast.LENGTH_SHORT).show();
                return false;
            }
            String folder = filePath.substring(0, filePath.lastIndexOf("/")+1);
            String apk = filePath.substring(filePath.lastIndexOf("/")+1);
            File file = new File(folder,apk);
            if (file.exists()) {
                final Uri apkUri = Uri.fromFile(file);
                Method method = pmClz.getDeclaredMethod("installPackage", Uri.class, Class.forName("android.content.pm.IPackageInstallObserver"), int.class, String.class);
                method.setAccessible(true);
                final int INSTALL_REPLACE_EXISTING = 0x00000002;
                method.invoke(packageManager,apkUri, null, INSTALL_REPLACE_EXISTING, Keys.readAPKVersionName(activity,filePath));
                return true;
            }else{
                Toast.makeText(activity, "Could not find application in the path specified", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(activity, "Install Failed. Reason: Invalid file path", Toast.LENGTH_LONG).show();
        return false;
    }

    public static void inputStreamToOutputStream(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            target.write(buf, 0, length);
        }
    }

    public static String setBits(String variable, int bytes, int bit) {
        String tag = "", val = "";
        if (variable.contains("|"))
        {
            val = variable.split("\\|")[1];
            tag = variable.split("\\|")[0] + "|";
        }
        else val = variable;

        String result = ""; int u = 1; int bytecnt = 1;
        for (int i = 0; i < val.length(); i += 2)
        {
            if (bytecnt == bytes)
            {
                char[] bytesArray = hexStringToBinary(val.substring(i, (i+2))).toCharArray();
                for (char bits : bytesArray)
                {
                    if ((bytesArray.length - u) == (bit - 1)) { result += 1; u++; continue; };
                    result += bits;
                    u++;
                }
            }
            else
            {
                result += hexStringToBinary(val.substring(i, (i+2)));
                bytecnt++;
            }
        }
        return tag + padLeft(padLeft(new BigInteger(result, 2).toString(16), 2, '0'), val.length(), '0');
    }

    public static String returnKernelId(String aid) {
        String[] RequestedKernelIds = {
                "AmericanExpress(amex)|00000100",
                "Discover|00000110",
                "JCB|00000101",
                "MasterCard|00000010",
                "UnionPay|00000111",
                "Visa|00000011",
                "Other|00000000"
        };

        for (AidClass aids : Emv.StoredAids)
        {
            if (aid.contains(aids.Aid))
            {
                for(String theaid : RequestedKernelIds)
                {
                    if (theaid.split("\\|")[0].toLowerCase().contains(aids.Name.replace(" ", "").toLowerCase()))
                    {
                        return theaid;
                    }
                }
            }
        }
        return "Other|00000000";
    }

    public static String generateTransKsn(String ksn) {
        String ksnVal = ksn.substring(0, ksn.length() - 5);
        long counter = Long.parseLong(ksn.substring(ksn.length() - 5));

        ksn = ksnVal + padLeft(String.valueOf(Long.toHexString(counter)),5,'0');
        return padLeft(ksn,20,'0').substring(4);
    }

    public static String ksnIncrement(Context context, String iksn) {
        String ksn = iksn;
        ksn = ksn.substring(0, ksn.length() - 5);
        long counter = Long.parseLong(SharedPref.get(context,"ksncounter", "0"))+1;
        SharedPref.set(context,"ksncounter",String.valueOf(counter));
        if(counter > 99997 ){
            SharedPref.set(context,"ksncounter", "0");
        }
        ksn = ksn + padLeft(String.valueOf(counter),5,'0');
        return ksn;
    }

    public static String getMillisecondsTime(String timeString){
        Calendar calendar = Calendar.getInstance();
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String dateString = "timeString";
            Date date = sdf.parse(dateString);
            //Setting the Calendar date and time to the given date and time
            calendar.setTime(date);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return String.valueOf(calendar.getTimeInMillis());
    }

    public static String removeSpecialCharacters(String request){
        return request.replaceAll("[^a-zA-Z0-9</> {},.:\"*_|-]", "");
    }

    public static boolean checkIfBitIsSet(String variable, int bytes, int bit) {
        int count = 1;
        String value = "";
        if (variable.contains("|"))
        {
            value = variable.split("\\|")[1];
        }
        else value = variable;

        for (int i = 0; i < value.length(); i += 2)
        {
            String bits = hexStringToBinary(value.substring(i, (i+2)));
            if (bytes == count++)
            {
                int u = 0;
                for (int j = bits.length(); j > 0; j--)
                {
                    if ((j == bit) && (bits.substring(u, (u+1)).equals("1"))) return true;
                    u++;
                }
            }
        }
        return false;
    }

    public static String generateReference(){
        return Sdk.getSerialNo()+"|"+Emv.terminalId +"|"+ DateTime.Now.getTimeStamp();
    }

    public static String returnAIDIfExists(String aid) {
        for (AidClass aids : Emv.StoredAids)
        {
            if (aid.contains(aids.Aid)) return aids.Aid;
        }
        return "";
    }

    public static boolean checkIfAidExists(String aid) {
        for (AidClass aids : Emv.StoredAids)
        {
            if (aid.contains(aids.Aid)) return true;
        }
        return false;
    }

    public static int findMatch(String hexString, String value) {
        int count = 0;
        for(int i=0; i<hexString.length(); i += 2)
        {
            if(hexString.substring(i,(i+2)).equals(value))
            {
                count++;
            }
        }
        return count;
    }

    public static boolean setConfig(Context context, Uri uri) {
        try{
            InputStream is = context.getContentResolver().openInputStream(uri);
            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            XPath xpath = XPathFactory.newInstance().newXPath();

            SharedPref.set(context, "taconline", xpath.compile("/emv/terminal/taconline").evaluate(doc));
            SharedPref.set(context, "tacdefault", xpath.compile("/emv/terminal/tacdefault").evaluate(doc));
            SharedPref.set(context, "tacdenial", xpath.compile("/emv/terminal/tacdenial").evaluate(doc));
            SharedPref.set(context, "floorlimit", String.valueOf(Integer.parseInt(xpath.compile("/emv/terminal/floorlimit").evaluate(doc))));
            SharedPref.set(context, "currencycode", "5F2A|" + xpath.compile("/emv/terminal/currencycode").evaluate(doc));
            SharedPref.set(context, "countrycode", "9F1A|" + xpath.compile("/emv/terminal/countrycode").evaluate(doc));
            SharedPref.set(context, "currencylabel", xpath.compile("/emv/terminal/currencylabel").evaluate(doc));
            SharedPref.set(context, "terminalid", xpath.compile("/emv/terminal/terminalid").evaluate(doc));
            SharedPref.set(context, "merchantid", xpath.compile("/emv/terminal/merchantid").evaluate(doc));
            SharedPref.set(context, "transactionurl", xpath.compile("/emv/terminal/transactionurl").evaluate(doc));
            SharedPref.set(context, "keydownloadurl", xpath.compile("/emv/terminal/keydownloadurl").evaluate(doc));
            SharedPref.set(context, "keysetid", xpath.compile("/emv/terminal/keysetid").evaluate(doc));
            SharedPref.set(context, "posdatacode", xpath.compile("/emv/terminal/posdatacode").evaluate(doc));
            SharedPref.set(context, "merchantlocation", xpath.compile("/emv/terminal/merchantlocation").evaluate(doc));
            SharedPref.set(context, "threshold", String.valueOf(Integer.parseInt(xpath.compile("/emv/terminal/threshold").evaluate(doc))));
            SharedPref.set(context, "pinbypass", String.valueOf(Boolean.parseBoolean(xpath.compile("/emv/terminal/pinbypass").evaluate(doc))));
            SharedPref.set(context, "terminalcapability", "9F33|" + xpath.compile("/emv/terminal/terminalcapability").evaluate(doc));
            SharedPref.set(context, "terminalriskmanagement", "9F1D|" + xpath.compile("/emv/terminal/terminalriskmanagement").evaluate(doc));
            SharedPref.set(context, "extendedterminalcapability", "C7|" + xpath.compile("/emv/terminal/extendedterminalcapability").evaluate(doc));
            SharedPref.set(context, "extendedterminalcapability", "9F40|" + xpath.compile("/emv/terminal/extendedterminalcapability").evaluate(doc));
            SharedPref.set(context, "version", xpath.compile("/emv/terminal/version").evaluate(doc));
            SharedPref.set(context, "terminalTransQualifiers", "9F66|" + xpath.compile("/emv/terminal/terminalTransQualifiers").evaluate(doc));
            SharedPref.set(context, "terminaltype", "9F35|" + xpath.compile("/emv/terminal/terminaltype").evaluate(doc));
            SharedPref.set(context, "contactlessCVMLRequiredLimit", xpath.compile("/emv/terminal/contactlessCVMLRequiredLimit").evaluate(doc));
            SharedPref.set(context, "readerContactlessTransactionLimit", xpath.compile("/emv/terminal/readerContactlessTransactionLimit").evaluate(doc));
            SharedPref.set(context, "mcc", xpath.compile("/emv/terminal/mcc").evaluate(doc));
            SharedPref.set(context, "transIp", xpath.compile("/emv/terminal/transIp").evaluate(doc));
            SharedPref.set(context, "transPort", xpath.compile("/emv/terminal/transPort").evaluate(doc));
            SharedPref.set(context, "nibssKey", xpath.compile("/emv/terminal/nibssKey").evaluate(doc));
            SharedPref.set(context, "nibssEnv", xpath.compile("/emv/terminal/nibssEnv").evaluate(doc));
            SharedPref.set(context, "environment", xpath.compile("/emv/terminal/environment").evaluate(doc));
            SharedPref.set(context, "agentId", xpath.compile("/emv/terminal/agentId").evaluate(doc));
            SharedPref.set(context, "agentLoc", xpath.compile("/emv/terminal/agentLoc").evaluate(doc));
            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean downloadConfig(Activity activity) {
        try{
            Context context = activity.getApplicationContext();
            String data = " {\n" +
                    "\t\"current_version\" : \""+Emv.getAppVersion(activity)+"\",\n" +
                    "\t\"serial_no\" : \""+Sdk.getSerialNo()+"\" \n" +
                    "}";
            Emv.accessToken = SharedPref.get(context, "accesstoken", "");
            String response = Middleware.httpRequest(data,"POST", Emv.routeConfigURL);
            if(response.isEmpty()){
                return false;
            }

            SharedPref.set(context, "taconline", "DC4004F800");
            SharedPref.set(context, "tacdefault", "DC4000A800");
            SharedPref.set(context, "tacdenial", "0010000000");
            SharedPref.set(context, "floorlimit", "0");
            SharedPref.set(context, "contactlessfloorlimit", "1000");
            SharedPref.set(context, "currencycode", "5F2A|" + padLeft(parseJson(response, "currencyCode"),4,'0'));
            SharedPref.set(context, "countrycode", "9F1A|" + padLeft(parseJson(response, "countryCode"),4,'0'));
            SharedPref.set(context, "currencylabel", "NGN");
            SharedPref.set(context, "terminalid", parseJson(response, "terminalId"));
            SharedPref.set(context, "merchantid", parseJson(response, "nibbsMerchantId"));
            SharedPref.set(context, "baseurl",  parseJson(response, "tmsBaseUrl"));
            SharedPref.set(context, "keysetid", "000002");
            SharedPref.set(context, "posdatacode", parseJson(response, "posDataCode"));
            SharedPref.set(context, "merchantlocation", parseJson(response, "processorMerchantLocation"));
            SharedPref.set(context, "threshold", "5000");
            SharedPref.set(context, "pinbypass", "false");
            SharedPref.set(context, "terminalcapability", "9F33|" + parseJson(response, "terminalCapability"));
            SharedPref.set(context, "terminalriskmanagement", "9F1D|" + "6C00000000000000");
            SharedPref.set(context, "extendedterminalcapability", "C7|" + "E000F0A001");
            SharedPref.set(context, "extendedterminalcapability", "9F40|" + "E000F0A001");
            SharedPref.set(context, "version", "0002");
            SharedPref.set(context, "terminalTransQualifiers", "9F66|" + "36200000");
            SharedPref.set(context, "terminaltype", "9F35|" + "22");
            SharedPref.set(context, "contactlessCVMLRequiredLimit", parseJson(response, "contactlessCvmLimit"));
            SharedPref.set(context, "readerContactlessTransactionLimit", parseJson(response, "contactlessTransLimit"));
            SharedPref.set(context, "mcc", "1731");
            SharedPref.set(context, "transIp", parseJson(response, "nibbsIp"));
            SharedPref.set(context, "transPort", parseJson(response, "nibbsPort"));
            SharedPref.set(context, "nibssKey", parseJson(response, "nibbsKey"));
            SharedPref.set(context, "nibssEnv", parseJson(response, "nibbsEnv"));
            SharedPref.set(context, "environment", "NIBSS");
            SharedPref.set(context, "agentId", parseJson(response, "agentId"));
            SharedPref.set(context, "agentLoc", parseJson(response, "agentLocation"));
            SharedPref.set(context, "agentName", parseJson(response, "agentName"));
            Emv.initializeEmv(activity);
            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isAgentRestricted(Activity activity){
        //Check for configuration
        String TACOnline = SharedPref.get(activity.getApplicationContext(), "taconline", "");
        if(TACOnline.isEmpty()){
            Toast.makeText(activity.getApplicationContext(), "Device is not configured yet", Toast.LENGTH_SHORT).show();
            return true;
        }

        //Check if upgrade is available
        String routeResp = SharedPref.get(activity, "routeResp", "");
        String forceUpgrade = Keys.parseJson(routeResp, "forceUpgrade");
        if(forceUpgrade.toLowerCase().equals("true")){
            Toast.makeText(activity.getApplicationContext(), "Please upgrade your device to continue transacting", Toast.LENGTH_LONG).show();
            return true;
        }

        //Check if device has been disabled
        String active = Keys.parseJson(routeResp, "active");
        if(!active.isEmpty()){
            if(!Boolean.parseBoolean(active)){
                Toast.makeText(activity.getApplicationContext(), "Device has been disabled", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    public static boolean isMinimumAmount(FragmentActivity activity, String amount){
        //Check is amount is above configured amount
        String minimumAmt = SharedPref.get(activity.getApplicationContext(), "minimumAmount", "36");
        String amt = amount.replace(",", "");
        if(Double.parseDouble(amt) < Double.parseDouble(minimumAmt)){
            Toast.makeText(activity, "Amount cannot be less than minimum amount", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public static String readAPKVersionName(Activity activity, String apkPath){
        PackageManager pm = activity.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, 0);
        return info.versionName;
    }

    public static boolean generateKimonoKeys(FragmentActivity activity, String url, String termialid, String keysetid) {
        try
        {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024, new SecureRandom());
            KeyPair pair = keyGen.generateKeyPair();
            //Get some publick keys and private keys
            RSAPrivateKey privatekey = (RSAPrivateKey)pair.getPrivate();
            RSAPublicKey publickey = (RSAPublicKey)pair.getPublic();

            String modul = publickey.getModulus().toString(16);
            String pubExp = publickey.getPublicExponent().toString(16);
            String privateExp = privatekey.getPrivateExponent().toString(16);

            String modulus = Base64.encodeToString(hexStringToByteArray(modul), Base64.DEFAULT).replace("\n","");
            String pubExponent = Base64.encodeToString(hexStringToByteArray(pubExp), Base64.DEFAULT).replace("\n", "");
            String privateExponent = Base64.encodeToString(hexStringToByteArray(privateExp), Base64.DEFAULT).replace("\n","");

            modulus = URLEncoder.encode(modulus, "UTF-8");
            pubExponent = URLEncoder.encode(pubExponent, "UTF-8");
            //Generate the ursl using the public key and public exponent generated
            String req = url + "?cmd=key&terminal_id=" + termialid + "&pkmod=" + modulus + "&pkex=" + pubExponent + "&pkv=1&keyset_id=" + keysetid + "&der_en=1";
            String responseData = Net.httpRequest("", "GET", req);

            Log.d("Result",responseData);
            modulus = URLDecoder.decode(modulus, "UTF-8");
            String data = asciiToHex(responseData);
            //Decrypt using the private key exponent
            byte[] exp = Base64.decode(privateExponent, Base64.DEFAULT);
            byte[] mod = Base64.decode(modulus, Base64.DEFAULT);
            String ep = byteToHexString(exp);
            String m = byteToHexString(mod);
            String d = data.substring(0, 256);
            String rsaval = rsaDecrypt(d, m, ep);
            String temp = rsaval.substring(204, (204+32));
            String checkDigit = trippleDesEncrypt("0000000000000000", temp);
            Log.d("Result", "CHECK DIGIT: " + checkDigit);
            String ipek = trippleDesEncrypt("FFFF" + keysetid + "DDDDE0", temp);
            Log.d("Result", "IPEK: " + ipek.substring(0, 3));
            SharedPref.set(activity.getApplicationContext(),"Ipek", ipek);
            SharedPref.set(activity.getApplicationContext(),"Iksn", "0000" + keysetid + "DDDDE00000");
            SharedPref.set(activity.getApplicationContext(),"ksncounter", "0");
            return true;
        }
        catch(Exception ex)
        {
            Log.d("Result", "CHECK DIGIT: 8CA64DE9C1B123A7");
            SharedPref.set(activity.getApplicationContext(),"Ipek", "3F2216D8297BCE9C");
            SharedPref.set(activity.getApplicationContext(),"Iksn", "0000000002DDDDE00000");
            SharedPref.set(activity.getApplicationContext(),"ksncounter", "0");
            return false;
        }
    }

    public static String generateToken(String method, String transUrl) {
        final String[] result = {""};
        Thread t = new Thread(()-> {
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Result", "Request url: " + transUrl);
                URL url = new URL(transUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setChunkedStreamingMode(0);

                if(method.equals("GET")){
                    urlConnection.setDoOutput(false);
                }else{
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    urlConnection.setRequestProperty("SerialNumber", Emv.serialNumber);
                    urlConnection.setRequestProperty("TerminalID", Emv.terminalId);
                    urlConnection.setRequestProperty("Authorization", "Basic VVNFUl9DTElFTlRfQVBQOnNlY3JldA==");
                    String urlParameters  = "username=user&password=secret&grant_type=password";
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    Log.d("url_parameters","The Url Pamaters are: " + out);
                    out.write(urlParameters.getBytes());
                    out.flush();
                }
                InputStream inputStream = urlConnection.getErrorStream();
                if (inputStream == null) {
                    inputStream = urlConnection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("ISO-8859-1"));
                    result[0] = Keys.readStream(reader);
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }
        });
        t.start();
        while (t.isAlive());
        Log.d("Result", result[0]);
        return result[0];
    }

    public static boolean doCallHome(Activity activity) {
        try
        {
            int printerState = Sdk.getPrinterState();
            String batteryInfo = Emv.getBatteryLevel(activity.getApplicationContext());
            int len = (printerState + "" + batteryInfo).length(); //get the length of both value
            int len2 = ("BS:" + "::PS:").length(); //get length of the added text.
            String tag10 = "10" + padLeft (""+(len+len2),3,'0') + "BS:" + batteryInfo + "::PS:" + printerState;
            String serial = "01" + padLeft(String.valueOf(Emv.serialNumber.length()), 3, '0') + Emv.serialNumber;
            IsoCreator callHome = new IsoCreator();
            callHome.Field[0] = "0800";
            callHome.Field[3] = "9D0000";
            callHome.Field[7] = DateTime.Now.ToString("MMddHHmmss");
            callHome.Field[11] = Emv.getStan(activity.getApplicationContext());
            callHome.Field[12] = DateTime.Now.ToString("HHmmss");
            callHome.Field[13] = DateTime.Now.ToString("MMdd");
            callHome.Field[41] = Emv.terminalId;
            callHome.Field[62] = (serial + tag10);
            callHome.Field[64] = "";
            String packedhex = callHome.getPackedISO(16);
            Log.d("Result", "Request: " + packedhex);
            String result = Net.sslTcpClient(packedhex);
            Log.d("Result", "Response: " + result);
            String respCode = parseISO(result, "39");
            if(respCode.equals("00")){
                Log.d("Result", "Call home was successful");
                return true;
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String generateNibssKeys(FragmentActivity activity, String TestORProduction) {
        try
        {
            String key = SharedPref.get(activity.getApplicationContext(), "nibssKey", "A050F63AFF366A4B0588D818D23C6C77");

            if(TestORProduction.equals("TEST")) key = "DBEECACCB4210977ACE73A1D873CA59F";
            SharedPref.set(activity.getApplicationContext(), "nibssKey", key);

            Emv.initializeEmv(activity);

            String packedhex = "";
            String result = "";
            IsoCreator master = new IsoCreator();
            master.Field[0] = "0800";
            master.Field[3] = "9A0000";
            master.Field[7] = DateTime.Now.ToString("MMddHHmmss");
            master.Field[11] = Emv.getStan(activity.getApplicationContext());
            master.Field[12] = DateTime.Now.ToString("HHmmss");
            master.Field[13] = DateTime.Now.ToString("MMdd");
            master.Field[41] = Emv.terminalId;
            packedhex = master.getPackedISO(16);
            Log.d("Result", "Request: " + packedhex);
            result = Net.sslTcpClient(packedhex);
            Log.d("Result", "Response: " + result);
            if(result.isEmpty()) return "";
            String masterkey = XpressPay.decryptMasterKey(parseISO(result, "53")); //trippleDesDecrypt(parseISO(result, "53"), Emv.nibssKey).substring(0,32);

            IsoCreator session = new IsoCreator();
            session.Field[0] = "0800";
            session.Field[3] = "9B0000";
            session.Field[7] = DateTime.Now.ToString("MMddHHmmss");
            session.Field[11] = Emv.getStan(activity.getApplicationContext());
            session.Field[12] = DateTime.Now.ToString("HHmmss");
            session.Field[13] = DateTime.Now.ToString("MMdd");
            session.Field[41] = Emv.terminalId;
            packedhex = session.getPackedISO(16);
            Log.d("Result", "Request: " + packedhex);
            result = Net.sslTcpClient(packedhex);
            Log.d("Result", "Response: " + result);
            if(result.isEmpty()) return "";
            Emv.sessionKey = trippleDesDecrypt(parseISO(result, "53"), masterkey).substring(0, 32);
            SharedPref.set(activity.getApplicationContext(), "sessionkey", Emv.sessionKey);

            IsoCreator pin = new IsoCreator();
            pin.Field[0] = "0800";
            pin.Field[3] = "9G0000";
            pin.Field[7] = DateTime.Now.ToString("MMddHHmmss");
            pin.Field[11] = Emv.getStan(activity.getApplicationContext());
            pin.Field[12] = DateTime.Now.ToString("HHmmss");
            pin.Field[13] = DateTime.Now.ToString("MMdd");
            pin.Field[41] = Emv.terminalId;
            packedhex = pin.getPackedISO(16);
            Log.d("Result", "Request: " + packedhex);
            result = Net.sslTcpClient(packedhex);
            Log.d("Result", "Response: " + result);
            if(result.isEmpty()) return "";
            Emv.pinKey = trippleDesDecrypt(parseISO(result, "53"), masterkey).substring(0, 32);
            SharedPref.set(activity.getApplicationContext(), "pinkey", Emv.pinKey);

            IsoCreator param = new IsoCreator();
            param.Field[0] = "0800";
            param.Field[3] = "9C0000";
            param.Field[7] = DateTime.Now.ToString("MMddHHmmss");
            param.Field[11] = Emv.getStan(activity.getApplicationContext());
            param.Field[12] = DateTime.Now.ToString("HHmmss");
            param.Field[13] = DateTime.Now.ToString("MMdd");
            param.Field[41] = Emv.terminalId;
            String serialLenghtVal  = "01" + padLeft (String.valueOf(Emv.serialNumber.length()), 3, '0') + Emv.serialNumber;
            param.Field[62] = serialLenghtVal;
            param.Field[64] = "";
            packedhex = param.getPackedISO(16);
            Log.d("Result", "Request: " + packedhex);
            result = Net.sslTcpClient(packedhex);
            Log.d("Result", "Response: " + result);
            String Field62 = parseISO(result, "62");
            return Field62;
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static boolean doEco(Activity activity) {
        try {
            String result = "", packedhex = "";
            IsoCreator SignOn = new IsoCreator();
            SignOn.Field[0] = "0800";
            SignOn.Field[7] = DateTime.Now.ToString("MMddHHmmss");
            SignOn.Field[11] = Emv.getStan(activity);
            SignOn.Field[12] = DateTime.Now.ToString("HHmmss");
            SignOn.Field[13] = DateTime.Now.ToString("MMdd");
            SignOn.Field[41] = Emv.terminalId;
            SignOn.Field[70] = "301";
            packedhex = SignOn.getPackedISO(32);
            Log.d("Result", "Request: " + result);
            result = Net.tcpClient(packedhex);
            Log.d("Result", "Response: " + result);
            return !result.isEmpty();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean generateSwitchKeys(FragmentActivity activity) {
        try {
            //ZMK KEY
            //Part 1: E38FD6D9EF85A892F2FBFDD083A407AE
            //Part 2: D0085DBFFB3723B926CB7980B9EA6268
            //Key check value: 308EE5
            //Derived value of ZMK
            String ZMK = "33878B6614B28B2BD43084503A4E65C6"; //KCV 308EE5
            SharedPref.set(activity.getApplicationContext(), "nibssKey", ZMK);

            Emv.initializeEmv(activity);

            String result = "", packedhex = "";
            IsoCreator SignOn = new IsoCreator();
            SignOn.Field[0] = "0800";
            SignOn.Field[7] = DateTime.Now.ToString("MMddHHmmss");
            SignOn.Field[11] = Emv.getStan(activity);
            SignOn.Field[12] = DateTime.Now.ToString("HHmmss");
            SignOn.Field[13] = DateTime.Now.ToString("MMdd");
            SignOn.Field[41] = Emv.terminalId;
            SignOn.Field[70] = "001";
            packedhex = SignOn.getPackedISO(32);
            Log.d("Result", "Request: " + result);
            result = Net.tcpClient(packedhex);
            Log.d("Result", "Response: " + result);
            if(result.isEmpty()) return false;


            IsoCreator keyExchange = new IsoCreator();
            keyExchange.Field[0] = "0800";
            keyExchange.Field[7] = DateTime.Now.ToString("MMddHHmmss");
            keyExchange.Field[11] = Emv.getStan(activity);
            keyExchange.Field[12] = DateTime.Now.ToString("HHmmss");
            keyExchange.Field[13] = DateTime.Now.ToString("MMdd");
            SignOn.Field[41] = Emv.terminalId;
            keyExchange.Field[70] = "101";
            packedhex = keyExchange.getPackedISO(32);
            Log.d("Result", "Request: " + result);
            result = Net.tcpClient(packedhex);
            Log.d("Result", "Response: " + result);
            if(result.isEmpty()) return false;
            String field125 = parseISO(result, "125").substring(0,32);
            Emv.pinKey = trippleDesDecrypt(field125, Emv.nibssKey).substring(0,32);
            SharedPref.set(activity, "pinkey", Emv.pinKey);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static String encryptPinBlock(String pan, String pin) {
        pan = padLeft(pan.substring((pan.length() - 13), (pan.length() - 13)+12), 16, '0');
        pin = padLeft(Integer.toHexString(pin.length()), 2, '0') + padRight(pin, 16, 'F');
        return XORorANDorORfuction(pan, pin, "^");
    }

    public static String XORorANDorORfuction(String valueA, String valueB, String symbol) {
        char[] a = valueA.toCharArray();
        char[] b = valueB.toCharArray();
        String result = "";
        for (int i = 0; i < a.length; i++)
        {
            if (symbol.equals("&")) result += Integer.toHexString((Integer.parseInt(String.valueOf(a[i]),16) & Integer.parseInt(String.valueOf(b[i]),16))).toUpperCase();
            else if (symbol.equals("^")) result += Integer.toHexString((Integer.parseInt(String.valueOf(a[i]),16) ^ Integer.parseInt(String.valueOf(b[i]),16))).toUpperCase();
            else result += Integer.toHexString((Integer.parseInt(String.valueOf(a[i]),16) | Integer.parseInt(String.valueOf(b[i]),16))).toUpperCase();
        }
        return result;
    }

    public static String asciiToHex(String ascii) {
        byte[] c = ascii.getBytes(Charset.forName("ISO-8859-1"));
        return byteToHexString(c);
    }

    public static String padRight(String value, int count, Character symbol){
        return String.format("%1$-" + count + "s", value).replace(' ', symbol);
    }

    public static String padLeft(String value, int count, Character symbol){
        return String.format("%1$" + count + "s", value).replace(' ', symbol);
    }

    public static float pixelToDpi(Activity activity, Float value){
        // Converts 14 dip into its equivalent px
        float dip = value;
        Resources r = activity.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        return px;
    }

    public static String genNibssRRN(Context context, String stan){
        String sepVal = SharedPref.get(context, "sepVal", "1");
        if(Integer.parseInt(stan) > 999998){
            if((Integer.parseInt(sepVal)+2) > 9){
                SharedPref.set(context, "sepVal", "1");
            }else{
                SharedPref.set(context, "sepVal", String.valueOf(Integer.parseInt(sepVal) + 2));
            }
        }
        return Emv.serialNumber.substring(Emv.serialNumber.length()-5) + sepVal + Keys.padLeft(stan,6,'0');
    }

    public static String genKimonoRRN(Context context, String stan){
        String sepVal = SharedPref.get(context, "sepVal", "1");
        if(Integer.parseInt(stan) > 999998){
            if((Integer.parseInt(sepVal)+2) > 9){
                SharedPref.set(context, "sepVal", "1");
            }else{
                SharedPref.set(context, "sepVal", String.valueOf(Integer.parseInt(sepVal) + 2));
            }
        }
        return Emv.serialNumber.substring(Emv.serialNumber.length()-5) + (Integer.parseInt(sepVal)-1) + Keys.padLeft(stan,6,'0');
    }

    public static String binaryStringToHexString(String binary) {
        BigInteger dec = new BigInteger(binary,2);
        return dec.toString(16).toUpperCase();
    }

    public static String parseTLV(String resp, String Tag) {
        String data = "", len = ""; int nextTag = 0;
        for (int i = 0; i < resp.length(); i++)
        {
            String tag = resp.substring(nextTag, (nextTag + Tag.length()));
            if (tag.equals(Tag))
            {
                len = resp.substring(Tag.length(), (Tag.length() + 3));
                data = resp.substring(5, ( 5 + Integer.parseInt(len)));
                return data;
            }
            len = resp.substring(2, 5);
            data = resp.substring(5, (5 + Integer.parseInt(len)));
            resp = resp.substring(data.length() + 5);
        }
        return data;
    }

    public static String parseXml(String response, String tag) {
        String[] resp = response.split("[<>/]");
        for (int i = 0; i < resp.length; i++)
        {
            if ((resp[i].equals(tag)) && (resp[i].equals(resp[i + 1]) == false))
            {
                return resp[i + 1];
            }
        }
        return "";
    }

    public static String parseJson(String response, String tag) {
        response = response.replaceAll("[{\"]", "#").replace("[","#").replace("]","#");
        response = response.replace(",#","#,#").replace("##", "").replace(":,", "#,");
        response = response.replace("#:#", "#").replace("#:","#").replace(",#","#,#");
        response = response.replace("}#","#").replace("}}","#,").replace("{{","#,");
        response = response.replace("#,","#");
        String[] resp = response.split("[#]");
        for (int i = 0; i < resp.length; i++)
        {
            if (resp[i].equals(tag))
            {
                return resp[i + 1];
            }
        }
        return "";
    }

    public static List<String> parseJsonCnt(String response, String tag) {
        response = response.replaceAll("[{\"]", "#").replace("[","#").replace("]","#");
        response = response.replace(",#","#,#").replace("##", "").replace(":,", "#,");
        response = response.replace("#:#", "#").replace("#:","#").replace(",#","#,#");
        response = response.replace("}#","#").replace("}}","#,").replace("{{","#,");
        response = response.replace("#,","#");
        String[] resp = response.split("[#]");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < resp.length; i++)
        {
            if (resp[i].equals(tag))
            {
                result.add(resp[i + 1]);
            }
        }
        return result;
    }

    public static String SHA1Encrypt(String data, String algorithm){
        try {
            MessageDigest md = null;
            if(algorithm.equals("01")){
                md = MessageDigest.getInstance("SHA-1");
                byte[] messageDigest = md.digest(hexStringToByteArray(data));
                return byteToHexString(messageDigest).toUpperCase();
            }
            else if(algorithm.equals("02")){
                md = MessageDigest.getInstance("SHA-256");
                byte[] byteval = hexStringToByteArray(data);
                byte[] hash = md.digest(byteval);
                return byteToHexString(hash).toUpperCase();
            }
            return "";
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String byteToHexString(byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String hex) {
        if((hex.length()%2) != 0){
            hex = "0" + hex;
        }
        int numberChars = hex.length();
        byte bytes[] = new byte[numberChars / 2];
        for (int i = 0; i < numberChars; i += 2)
            bytes[i / 2] = (byte)Integer.parseInt(hex.substring(i, (i+2)), 16);
        return bytes;
    }

    public static String hexStringToASCII(String hex){
        if(hex.length()%2!=0){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hex.length(); i = i + 2) {
            String s = hex.substring(i, i + 2);
            int n = Integer.valueOf(s, 16);
            builder.append((char)n);
        }
        return builder.toString();
    }

    public static String hexStringToBinary(String hexstring) {
        String binary = "";
        for (int i = 0; i < hexstring.length(); i += 2)
        {
            binary += padLeft((new BigInteger(hexstring.substring(i,(i+2)),16).toString(2)), 8, '0');
        }
        return binary;
    }

    public static void setSpinnerDefaultItem(Spinner spinner, String selection){
        for(int i = 0; i < spinner.getCount(); i++){
            if(spinner.getItemAtPosition(i).toString().equals(selection)){
                spinner.setSelection(i);
                break;
            }
        }
    }

    public static String parseISO(String resp, String fieldNo) {
        try
        {
            resp = hexStringToASCII(resp.substring(resp.indexOf("30")));
            final String mti = resp.substring(0, 4);
            int bitmapLength = (hexStringToBinary(resp.substring(4, 6)).substring(0,1).equals("1")) ? 32 : 16; //The first digit of the bitmap indicates secodary bitmap if "1"
            final String bitmap = resp.substring(4, bitmapLength+4);
            String isoval = resp.substring(4);
            String binaryBitmap = "1" + hexStringToBinary(bitmap).substring(1);
            LinkedHashMap<String, String> isodata = new LinkedHashMap<String, String>();
            for(int i=0; i<128; i++){
                isodata.put(""+i,"");
            }
            isodata.put("0", mti);
            isodata.put("1", bitmap);
            String isotrim = "";
            String iso = isoval;
            int i = 0;

            LinkedHashMap<String, String> fields = new LinkedHashMap<String, String>();
            fields.put( "0", "4" ); fields.put( "1", "16" ); fields.put( "2", "19" ); fields.put( "3", "6" ); fields.put( "4", "12" ); fields.put( "5", "12" ); fields.put( "6", "12" ); fields.put( "7", "10" ); fields.put( "8", "8" ); fields.put( "9", "8" ); fields.put( "10", "8" ); fields.put( "11", "6" ); fields.put( "12", "6" ); fields.put( "13", "4" ); fields.put( "14", "4" ); fields.put( "15", "4" ); fields.put( "16", "4" ); fields.put( "17", "4" ); fields.put( "18", "4" ); fields.put( "19", "3" ); fields.put( "20", "3" ); fields.put( "21", "3" ); fields.put( "22", "3" ); fields.put( "23", "3" ); fields.put( "24", "3" ); fields.put( "25", "2" ); fields.put( "26", "2" ); fields.put( "27", "1" ); fields.put( "28", "9" ); fields.put( "29", "9" ); fields.put( "30", "9" ); fields.put( "31", "9" ); fields.put( "32", "11" ); fields.put( "33", "11" ); fields.put( "34", "28" ); fields.put( "35", "37" ); fields.put( "36", "104" ); fields.put( "37", "12" ); fields.put( "38", "6" ); fields.put( "39", "2" ); fields.put( "40", "3" ); fields.put( "41", "8" ); fields.put( "42", "15" ); fields.put( "43", "40" ); fields.put( "44", "25" ); fields.put( "45", "76" ); fields.put( "46", "999" ); fields.put( "47", "999" ); fields.put( "48", "999" ); fields.put( "49", "3" ); fields.put( "50", "3" ); fields.put( "51", "3" ); fields.put( "52", "16" ); fields.put( "53", "48" ); fields.put( "54", "120" ); fields.put( "55", "999" ); fields.put( "56", "4" ); fields.put( "57", "999" ); fields.put( "58", "999" ); fields.put( "59", "999" ); fields.put( "60", "999" ); fields.put( "61", "999" ); fields.put( "62", "999" ); fields.put( "63", "999" ); fields.put( "64", "64" ); fields.put( "65", "8" ); fields.put( "66", "1" ); fields.put( "67", "2" ); fields.put( "68", "3" ); fields.put( "69", "3" ); fields.put( "70", "3" ); fields.put( "71", "4" ); fields.put( "72", "4" ); fields.put( "73", "6" ); fields.put( "74", "10" ); fields.put( "75", "10" ); fields.put( "76", "10" ); fields.put( "77", "10" ); fields.put( "78", "10" ); fields.put( "79", "10" ); fields.put( "80", "10" ); fields.put( "81", "10" ); fields.put( "82", "12" ); fields.put( "83", "12" ); fields.put( "84", "12" ); fields.put( "85", "12" ); fields.put( "86", "15" ); fields.put( "87", "15" ); fields.put( "88", "15" ); fields.put( "89", "15" ); fields.put( "90", "42" ); fields.put( "91", "1" ); fields.put( "92", "2" ); fields.put( "93", "5" ); fields.put( "94", "7" ); fields.put( "95", "42" ); fields.put( "96", "8" ); fields.put( "97", "17" ); fields.put( "98", "25" ); fields.put( "99", "11" ); fields.put( "100", "11" ); fields.put( "101", "17" ); fields.put( "102", "28" ); fields.put( "103", "28" ); fields.put( "104", "100" ); fields.put( "105", "999" ); fields.put( "106", "999" ); fields.put( "107", "999" ); fields.put( "108", "999" ); fields.put( "109", "999" ); fields.put( "110", "999" ); fields.put( "111", "999" ); fields.put( "112", "999" ); fields.put( "113", "999" ); fields.put( "114", "999" ); fields.put( "115", "999" ); fields.put( "116", "999" ); fields.put( "117", "999" ); fields.put( "118", "999" ); fields.put( "119", "999" ); fields.put( "120", "999" ); fields.put( "121", "999" ); fields.put( "122", "999" ); fields.put( "123", "999" ); fields.put( "124", "999" ); fields.put( "125", "999" ); fields.put( "126", "999" ); fields.put( "127", "48" ); fields.put( "128", "64" );
            for (char bit : binaryBitmap.toCharArray())
            {
                i++;
                if (bit == '0') continue;
                String bitField = String.valueOf(binaryBitmap.substring(binaryBitmap.length() - i).length());
                if (Integer.parseInt(bitField) == 1) { fields.put("1" , String.valueOf(bitmapLength)); iso = iso.substring(0); }
                if (Integer.parseInt(bitField) == 2) { fields.put("2" , iso.substring(0, 2)); iso = iso.substring(2); }
                if (Integer.parseInt(bitField) == 35) { fields.put("35" , iso.substring(0, 2)); iso = iso.substring(2); }
                if (Integer.parseInt(bitField) == 32) { fields.put("32" , iso.substring(0, 2)); iso = iso.substring(2); }
                if (Integer.parseInt(bitField) == 33) { fields.put("33" , iso.substring(0, 2)); iso = iso.substring(2); }
                if (Integer.parseInt(bitField) == 54) { fields.put("54" , iso.substring(0, 3)); iso = iso.substring(3); }
                if (Integer.parseInt(bitField) == 55) { fields.put("55" , iso.substring(0, 3)); iso = iso.substring(3); }
                if (Integer.parseInt(bitField) == 56) { fields.put("56" , iso.substring(0, 3)); iso = iso.substring(3); }
                if (Integer.parseInt(bitField) == 59) { fields.put("59" , iso.substring(0, 3)); iso = iso.substring(3); }
                if (Integer.parseInt(bitField) == 62) { fields.put("62" , iso.substring(0, 3)); iso = iso.substring(3); }
                if (Integer.parseInt(bitField) == 100) { fields.put("100" , iso.substring(0, 2)); iso = iso.substring(2); }
                if (Integer.parseInt(bitField) == 102) { fields.put("102" , iso.substring(0, 2)); iso = iso.substring(2); }
                if (Integer.parseInt(bitField) == 103) { fields.put("103" , iso.substring(0, 2)); iso = iso.substring(2); }
                if (Integer.parseInt(bitField) == 123) { fields.put("123" , iso.substring(0, 3)); iso = iso.substring(3); }


                Set set = fields.entrySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext())
                {
                    Map.Entry fieldValPairs = (Map.Entry)iterator.next();
                    String key = (String) fieldValPairs.getKey();
                    String val = (String) fieldValPairs.getValue();
                    if (bitField.equals(key))
                    {
                        isotrim = iso.substring(0, ((Integer.parseInt(val) > iso.length()) ? iso.length() : Integer.parseInt(val)));
                        iso = iso.substring((Integer.parseInt(val) > iso.length()) ? iso.length() : Integer.parseInt(val));
                        isodata.put(bitField.toString(), isotrim);
                        if (iso.isEmpty()) return isodata.get(fieldNo);
                    }
                }
            }
            return isodata.get(fieldNo);
        }
        catch(Exception ex){ return ""; }
    }

    public static void logToFile(FragmentActivity activity){
        File appDir = new File(activity.getExternalFilesDir(null).getAbsolutePath() + "/download/");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File logFile = new File(appDir, "log.txt");
        if (logFile.exists()) {
            logFile.delete();
        }
        // clear the previous logcat and then write the new one to the file
        try {
            Runtime.getRuntime().exec("logcat -f " + logFile);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public static String getIsoMessage(String respCode) {
        List<String> msgList = new ArrayList<String>();
        msgList.add("00|Approved or completed successfully"); msgList.add("01|Refer to card issuer"); msgList.add("02|Refer to card issuer"); msgList.add("special condition");
        msgList.add("03|Invalid merchant"); msgList.add("04|Pick-up card"); msgList.add("05|Do not honor"); msgList.add("06|Error"); msgList.add("07|Pick-up card"); msgList.add("special condition");
        msgList.add("08|Honor with identification"); msgList.add("09|Request in progress"); msgList.add("10|Approved"); msgList.add("partial"); msgList.add("11|Approved"); msgList.add("VIP"); msgList.add("12|Invalid transaction");
        msgList.add("13|Invalid amount"); msgList.add("14|Invalid card number"); msgList.add("15|No such issuer"); msgList.add("16|Approved"); msgList.add("update track 3"); msgList.add("17|Customer cancellation");
        msgList.add("18|Customer dispute"); msgList.add("19|Re-enter transaction"); msgList.add("20|Invalid response"); msgList.add("21|No action taken"); msgList.add("22|Suspected malfunction");
        msgList.add("23|Unacceptable transaction fee"); msgList.add("24|File update not supported"); msgList.add("25|Unable to locate record"); msgList.add("26|Duplicate record");
        msgList.add("27|File update field edit error"); msgList.add("28|File update file locked"); msgList.add("29|File update failed"); msgList.add("30|Format error"); msgList.add("31|Bank not supported");
        msgList.add("32|Completed partially"); msgList.add("33|Expired card"); msgList.add("pick-up"); msgList.add("34|Suspected fraud"); msgList.add("pick-up"); msgList.add("35|Contact acquirer"); msgList.add("pick-up");
        msgList.add("36|Restricted card"); msgList.add("pick-up"); msgList.add("37|Call acquirer security"); msgList.add("pick-up"); msgList.add("38|PIN tries exceeded"); msgList.add("pick-up"); msgList.add("39|No credit account");
        msgList.add("40|Function not supported"); msgList.add("41|Lost card"); msgList.add("pick-up"); msgList.add("42|No universal account"); msgList.add("43|Stolen card"); msgList.add("pick-up"); msgList.add("44|No investment account");
        msgList.add("45|Account closed"); msgList.add("46|Identification required"); msgList.add("47|Identification cross-check required"); msgList.add("48|No customer record");
        msgList.add("51|Not sufficient funds"); msgList.add("52|No check account"); msgList.add("53|No savings account"); msgList.add("54|Expired card"); msgList.add("55|Incorrect PIN");
        msgList.add("56|No card record"); msgList.add("57|Transaction not permitted to cardholder"); msgList.add("58|Transaction not permitted on terminal");
        msgList.add("59|Suspected fraud"); msgList.add("60|Contact acquirer"); msgList.add("61|Exceeds withdrawal limit"); msgList.add("62|Restricted card"); msgList.add("63|Security violation");
        msgList.add("64|Original amount incorrect"); msgList.add("65|Exceeds withdrawal frequency"); msgList.add("66|Call acquirer security"); msgList.add("67|Hard capture");
        msgList.add("68|Response received too late"); msgList.add("69|Advice received too late"); msgList.add("75|PIN tries exceeded"); msgList.add("76|Reserved for future Realtime use");
        msgList.add("77|Intervene"); msgList.add("bank approval required"); msgList.add("78|Intervene"); msgList.add("bank approval required for partial amount"); msgList.add("90|Cut-off in progress");
        msgList.add("91|Issuer or switch inoperative"); msgList.add("92|Routing error"); msgList.add("93|Violation of law"); msgList.add("94|Duplicate transaction"); msgList.add("95|Reconcile error");
        msgList.add("96|System malfunction"); msgList.add("97|Reserved for future Realtime use"); msgList.add("98|Exceeds cash limit"); msgList.add("99|Reserved for future Realtime use");
        msgList.add("A1|ATC not incremented"); msgList.add("A2|ATC limit exceeded"); msgList.add("A3|ATC configuration error"); msgList.add("A4|CVR check failure"); msgList.add("A5|CVR configuration error");
        msgList.add("A6|TVR check failure"); msgList.add("A7|TVR configuration error"); msgList.add("C0|Unacceptable PIN"); msgList.add("C1|PIN Change failed"); msgList.add("C2|PIN Unblock failed");
        msgList.add("D1|MAC Error"); msgList.add("E1|Prepay error");
        for(String msg : msgList)
        {
            String msgArray[] = msg.split("\\|");
            if (msgArray[0].equals(respCode)) return msgArray[1];
        }
        return "";
    }

    public static String readStream(InputStreamReader reader) {
        try {
            String result = "";
            int data = reader.read();
            while (data != -1)
            {
                //Do something with data e.g. append to StringBuffer
                result += (char)data;
                data = reader.read();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String readStream(InputStream inputStream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            return total.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static List<AidClass> getStoredAid(Context context){
        try{
            List<AidClass> aidList = new ArrayList<AidClass>();
            InputStream fileLoc = context.getResources().openRawResource(context.getResources().getIdentifier("@raw/emv", null, context.getPackageName()));
            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fileLoc);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getDocumentElement().getElementsByTagName("emvcard");
            for(int temp=0; temp < nList.getLength(); temp++){
                Node nNode = nList.item(temp);
                if(nNode.getNodeType() == Node.ELEMENT_NODE){
                    String name = nNode.getChildNodes().item(1).getTextContent();
                    String aids = nNode.getChildNodes().item(3).getTextContent();
                    AidClass aid = new AidClass();
                    aid.Name = name;
                    aid.Aid = aids;
                    aidList.add(aid);
                }
            }
            return aidList;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static String genUnpredictableNum() {
        Random ran = new Random();
        String unpredictNum = String.valueOf(ran.nextInt(88888888) + 11111111);
        String hex = "";
        for (int i = 0; i < unpredictNum.length(); i += 2)
        {
            hex += Keys.padLeft(Integer.toHexString(Integer.parseInt(unpredictNum.substring(i, (i+2)))), 2, '0');
        }
        return hex.toUpperCase();
    }

    public static String getSessionKey(String IPEK, String KSN) {
        String initialIPEK = IPEK, ksn = padLeft(KSN, 20, '0');
        String sessionkey = "";
        //Get ksn with a zero counter by ANDing it with FFFFFFFFFFFFFFE00000
        String newKSN = XORorANDorORfuction(ksn, "0000FFFFFFFFFFE00000", "&");
        String counterKSN = padLeft(ksn.substring(ksn.length() - 5), 16, '0');
        //get the number of binaray associated with the counterKSN number
        String newKSNtoleft16 = newKSN.substring(newKSN.length() - 16);
        String counterKSNbin = Integer.toBinaryString(Integer.parseInt(counterKSN));
        int count = Integer.toBinaryString(Integer.parseInt(counterKSN)).replace("0", "").length();
        String binarycount = counterKSNbin;
        for (int i = 0; i < counterKSNbin.length(); i++)
        {
            int len = binarycount.length(); String result = "";
            if (binarycount.substring(0, 1).equals("1"))
            {
                result = padRight("1", len, '0');
                binarycount = binarycount.substring(1);
            }
            else { binarycount = binarycount.substring(1); continue; }
            String counterKSN2 = padLeft(padLeft(Integer.toHexString(Integer.parseInt(result,2)), 2, '0'),16,'0');
            String newKSN2 = XORorANDorORfuction(newKSNtoleft16, counterKSN2, "|");
            sessionkey = BlackBoxLogic(newKSN2, initialIPEK);   //Call the blackbox from here
            newKSNtoleft16 = newKSN2;
            initialIPEK = sessionkey;
        }
        return XORorANDorORfuction(sessionkey, "00000000000000FF00000000000000FF", "^");
    }

    public static String BlackBoxLogic(String ksn, String ipek) {
        if (ipek.length() < 32)
        {
            String msg = XORorANDorORfuction(ipek, ksn, "^");
            String desreslt = desEncrypt(msg, ipek);
            String rsesskey = XORorANDorORfuction(desreslt, ipek, "^");
            return rsesskey;
        }
        String current_sk = ipek;
        String ksn_mod = ksn;
        String leftIpek = XORorANDorORfuction(current_sk, "FFFFFFFFFFFFFFFF0000000000000000", "&").substring(0, 16);
        String rightIpek = XORorANDorORfuction(current_sk, "0000000000000000FFFFFFFFFFFFFFFF", "&").substring(16);
        String message = XORorANDorORfuction(rightIpek, ksn_mod, "^");
        String desresult = desEncrypt(message, leftIpek);
        String rightSessionKey = XORorANDorORfuction(desresult, rightIpek, "^");
        String resultCurrent_sk = XORorANDorORfuction(current_sk, "C0C0C0C000000000C0C0C0C000000000", "^");
        String leftIpek2 = XORorANDorORfuction(resultCurrent_sk, "FFFFFFFFFFFFFFFF0000000000000000", "&").substring(0, 16);
        String rightIpek2 = XORorANDorORfuction(resultCurrent_sk, "0000000000000000FFFFFFFFFFFFFFFF", "&").substring(16);
        String message2 = XORorANDorORfuction(rightIpek2, ksn_mod, "^");
        String desresult2 = desEncrypt(message2, leftIpek2);
        String leftSessionKey = XORorANDorORfuction(desresult2, rightIpek2, "^");
        String sessionkey = leftSessionKey + rightSessionKey;
        return sessionkey;
    }

    public static byte[] uriToByteArray(Activity activity, InputStream inputStream){
        try {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int len;
            while((len = inputStream.read(buffer)) != -1){
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFileFromRaw(Context context, int rawResourceId){
        try{
            InputStreamReader isr = new InputStreamReader(context.getResources().openRawResource(rawResourceId));
            BufferedReader br = new BufferedReader(isr);
            String read;
            String result = "";
            while(true){
                read = br.readLine();
                if(read == null){
                    break;
                }
                result += read;
            }
            return result;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static String readFile(Context context, String path){
        try{
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String read;
            String result = "";
            while(true){
                read = br.readLine();
                if(read == null){
                    break;
                }
                result += read;
            }
            return result;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static void readXmlFile(Context context, int rawResourceId){
        try{
            InputStream filePath = context.getResources().openRawResource(rawResourceId);
            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(filePath);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getDocumentElement().getElementsByTagName("emvcard");
            for(int temp=0; temp < nList.getLength(); temp++){
                Node nNode = nList.item(temp);
                if(nNode.getNodeType() == Node.ELEMENT_NODE){
                    NodeList nKey = nNode.getChildNodes().item(7).getChildNodes();
                    for(int temp2=1; temp2 <nKey.getLength(); temp2+=2 ){
                        String name = nNode.getChildNodes().item(1).getTextContent();
                        String aid = nNode.getChildNodes().item(3).getTextContent();
                        String keyindex = nKey.item(temp2).getChildNodes().item(1).getTextContent();
                        String expdate = nKey.item(temp2).getChildNodes().item(3).getTextContent();
                        String modulus = nKey.item(temp2).getChildNodes().item(5).getTextContent();
                        String exponent = nKey.item(temp2).getChildNodes().item(7).getTextContent();
                        String checksum = nKey.item(temp2).getChildNodes().item(9).getTextContent();
                        Log.d("Result", "-------------------------------------------------");
                        Log.d("Result", "Name: " +name);
                        Log.d("Result", "Aid: "+aid);
                        Log.d("Result", "Keyindex: " + keyindex);
                        Log.d("Result", "Exponent: " + expdate);
                        Log.d("Result", "Modulus: " + modulus);
                        Log.d("Result", "Exponent: " + exponent);
                        Log.d("Result", "Checksum: " + checksum);
                    }
                }
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
