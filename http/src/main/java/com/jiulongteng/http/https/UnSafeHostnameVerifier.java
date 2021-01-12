package com.jiulongteng.http.https;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class UnSafeHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}