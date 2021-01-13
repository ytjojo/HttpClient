package com.jiulongteng.http.https;


import android.os.Build;

import androidx.core.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Https相关
 * Created by weibo.kang on 2015/11/10.
 */
public class HttpsDelegate {


    public static enum KeyStoreType {
        BKS,
        PKCS12
    }
    public static KeyStoreType sKeyStoreType = KeyStoreType.BKS;


    private HttpsDelegate() {

    }


    public static Pair<SSLSocketFactory, X509TrustManager> getUnsafeSslSocketFactory() {
        return getSslSocketFactory(null, null, null);
    }

    public static Pair<SSLSocketFactory, X509TrustManager> getUnsafeSslSocketFactory(InputStream[] certificates) {
        return getSslSocketFactory(certificates, null, null);
    }

    public static Pair<SSLSocketFactory, X509TrustManager> getSslSocketFactory(InputStream bksFile, String password) {
        return getSslSocketFactory(null, bksFile, password);

    }

    public static Pair<SSLSocketFactory, X509TrustManager> getSslSocketFactory(InputStream[] certificates, InputStream bksFile, String password) {
        Pair<SSLSocketFactory, X509TrustManager> sslParams = null;
        try {
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            KeyManager[] keyManagers = getKeyManagers(bksFile, password.toCharArray());
            SSLContext sslContext = null;

            if (Build.VERSION.SDK_INT >= 29) {
                sslContext = SSLContext.getInstance("TLSv1.3");
            } else {
                sslContext = SSLContext.getInstance("TLSv1.2");

            }
            X509TrustManager trustManager = null;
            if (trustManagers != null) {
                trustManager = new TrustManagerCompat(chooseTrustManager(trustManagers));
            } else {
                trustManager = new UnSafeTrustManager();
            }
            sslContext.init(keyManagers, new TrustManager[]{trustManager}, null);
            sslParams = new Pair<>(sslContext.getSocketFactory(), trustManager);
            return sslParams;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (KeyManagementException e) {
            throw new AssertionError(e);
        } catch (KeyStoreException e) {
            throw new AssertionError(e);
        }
    }


    private static class UnSafeTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
    }

    private static TrustManager[] prepareTrustManager(InputStream... certificates) {
        if (certificates == null || certificates.length <= 0) return null;
        try {

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                }
            }

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.
                    getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            return trustManagers;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }



    /**
     * 客户端证书
     *
     * @return
     */
    private static KeyManager[] getKeyManagers(InputStream inputStream, char[] password) {
        if (inputStream == null ||  password == null) {
            return null;
        }
        KeyStore clientKeyStore = null;
        try {
            clientKeyStore = KeyStore.getInstance(sKeyStoreType.name());
            clientKeyStore.load(inputStream, password);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password);
            return keyManagerFactory.getKeyManagers();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }


    private static class TrustManagerCompat implements X509TrustManager {
        private X509TrustManager defaultTrustManager;
        private X509TrustManager localTrustManager;

        public TrustManagerCompat(X509TrustManager localTrustManager) throws NoSuchAlgorithmException, KeyStoreException {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            defaultTrustManager = chooseTrustManager(trustManagerFactory.getTrustManagers());
            this.localTrustManager = localTrustManager;
        }


        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException ce) {
                localTrustManager.checkServerTrusted(chain, authType);
            }
        }


        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}