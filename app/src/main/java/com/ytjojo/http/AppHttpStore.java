package com.ytjojo.http;

/**
 * Created by Administrator on 2016/10/18 0018.
 */
public class AppHttpStore {
    private volatile String token;
    private volatile String clientId;

    public String getClientId() {
        return clientId;
    }

    public  void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
