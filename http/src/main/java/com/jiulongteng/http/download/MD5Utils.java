package com.jiulongteng.http.download;

import android.os.Build;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MD5Utils {

    public static String getFileMD5(String inputFile) throws IOException {
        // 缓冲区大小（这个可以抽出一个参数）
        int bufferSize = 256 * 1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;
        try {
            // 拿到一个MD5转换器（同样，这里可以换成SHA1）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 使用DigestInputStream
            fileInputStream = new FileInputStream(new File(inputFile));
            digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
            // read的过程中进行MD5处理，直到读完文件
            byte[] buffer = new byte[bufferSize];
            while (digestInputStream.read(buffer) > 0) {
                ;
            }
            // 获取最终的MessageDigest
            messageDigest = digestInputStream.getMessageDigest();
            // 拿到结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            // 同样，把字节数组转换成字符串
            return byteArrayToHex(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } finally {
            try {
                digestInputStream.close();
            } catch (Exception e) {
            }
            try {
                fileInputStream.close();
            } catch (Exception e) {
            }
        }
    }

    //下面这个函数用于将字节数组换成成16进制的字符串

    public static String byteArrayToHex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
            if (n < b.length - 1) {
                hs = hs + "";
            }
        }
        return hs;
    }

    public static boolean checkFileMd5Validation(String filePath, String md5) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                String fileMD5 = getFileMD5(filePath);
                return fileMD5.equalsIgnoreCase(md5);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    public static String md5String(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }


    public static long getFreeSpaceBytes(final String path) {
        long freeSpaceBytes;
        final StatFs statFs = new StatFs(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            freeSpaceBytes = statFs.getAvailableBytes();
        } else {
            //noinspection deprecation
            freeSpaceBytes = statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
        }

        return freeSpaceBytes;
    }

}