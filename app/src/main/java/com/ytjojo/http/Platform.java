package com.ytjojo.http;

import android.os.Build;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;

public class Platform {
    public enum PlatFormType{
        Android,Java;
    }
    public static final PlatFormType PLATFORM = findPlatform();

    public static Platform.PlatFormType get()
    {
        return PLATFORM;
    }

    private static Platform.PlatFormType findPlatform()
    {
        try
        {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0)
            {
                return PlatFormType.Android;
            }
        } catch (ClassNotFoundException ignored)
        {
        }
        return PlatFormType.Java;
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