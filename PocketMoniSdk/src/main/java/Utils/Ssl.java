package Utils;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class Ssl {
    public static int DEFAULT_BUFFER_SIZE = 8 * 1024;

    public static SSLSocketFactory getSSLSocketFactory(InputStream certificate, InputStream key) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        String password = "password";

        //Reading certificate
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate clientCert = (X509Certificate) certificateFactory.generateCertificate(certificate);

        //Reading key
        String privateKeyContent = new String(inputStreamToByteArray(key));
        privateKeyContent = privateKeyContent.replace("-----BEGIN PRIVATE KEY-----\n", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----\n", "")
                .replace("-----END RSA PRIVATE KEY-----", "");
        byte[] decoded = Base64.decode(privateKeyContent, Base64.DEFAULT);
        KeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey clientKey = keyFactory.generatePrivate(keySpec);

        //Loading private key and certificate
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("certificate", clientCert);
        keyStore.setKeyEntry("private-key", clientKey, password.toCharArray(), new Certificate[]{clientCert});

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);


        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        KeyManager[] keyManager = keyManagerFactory.getKeyManagers();
        sslContext.init(keyManager, trustAllCerts(), null);
        return sslContext.getSocketFactory();
    }

    public static SSLSocketFactory getSSLSocketFactory(InputStream certificate, String password) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        //Reading certificate
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate clientCert = (X509Certificate) certificateFactory.generateCertificate(certificate);

        //Loading private key and certificate
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("certificate", clientCert);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        KeyManager[] keyManager = keyManagerFactory.getKeyManagers();

        sslContext.init(keyManager, trustAllCerts(), null);
        return sslContext.getSocketFactory();
    }

    private static X509TrustManager[] trustAllCerts() {
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        return new X509TrustManager[]{trustManager};
    }

    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(DEFAULT_BUFFER_SIZE, inputStream.available()));
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int bytes = inputStream.read(buffer);
        while (bytes >= 0) {
            out.write(buffer, 0, bytes);
            bytes = inputStream.read(buffer);
        }
        inputStream.close();
        return out.toByteArray();
    }
}
