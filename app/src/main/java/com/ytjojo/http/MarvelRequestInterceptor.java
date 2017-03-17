package com.ytjojo.http;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor to add the auth key, and generate the hash for every request
 * The body is exposed in the constructor
 *
 * @author glomadrian
 */
public class MarvelRequestInterceptor implements Interceptor {

    public static final String PARAM_KEY = "apikey";
    public static final String PARAM_TIMESTAMP = "ts";
    public static final String PARAM_HASH = "hash";
    private static final int SIGNUM = 1;
    private static final int BYTES = 1;

    private String publicKey;
    private String privateKey;

    public MarvelRequestInterceptor(String publicKey, String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String timeStamp = generateTimestamp();
        HttpUrl url = request.url().newBuilder().addQueryParameter(
                PARAM_TIMESTAMP, timeStamp)
                .addEncodedQueryParameter(PARAM_KEY, publicKey)
                .addEncodedQueryParameter(PARAM_HASH, generateMarvelHash(timeStamp, privateKey, publicKey))
                .build();
        request = request.newBuilder().url(url).build();
        return chain.proceed(request);
    }


    private String generateTimestamp() {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        return String.valueOf(timestamp.getTime());
    }


    private String generateMarvelHash(String timeStamp, String privateKey, String publicKey) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            String marvelHash = timeStamp + privateKey + publicKey;
            byte[] bytes = marvelHash.getBytes();
            return new BigInteger(SIGNUM, messageDigest.digest(bytes)).toString(BYTES);

        } catch (NoSuchAlgorithmException e) {
            return "invalid";
        }
    }

}