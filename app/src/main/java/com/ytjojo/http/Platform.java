package com.ytjojo.http;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Platform
{
    private static final Platform PLATFORM = findPlatform();

    public static Platform get()
    {
        return PLATFORM;
    }

    private static Platform findPlatform()
    {
        try
        {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0)
            {
                return new Android();
            }
        } catch (ClassNotFoundException ignored)
        {
        }
        return new Platform();
    }

    public Executor defaultCallbackExecutor()
    {
        return Executors.newCachedThreadPool();
    }

    public void execute(Runnable runnable)
    {
        defaultCallbackExecutor().execute(runnable);
    }


    static class Android extends Platform
    {
        @Override
        public Executor defaultCallbackExecutor()
        {
            return new MainThreadExecutor();
        }

        static class MainThreadExecutor implements Executor
        {
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void execute(Runnable r)
            {
                handler.post(r);
            }
        }
    }

    private String generateTimestamp() {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        return String.valueOf(timestamp.getTime());
    }

    private static final int SIGNUM = 1;
    private static final int BYTES = 1;
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