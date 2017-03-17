package com.ytjojo.domin.request;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2016/10/27 0027.
 */
public class LoginRequest {
    public String uid ="15715781021";
    public String pwd = stringToMD5("123456");
    public String rid ="doctor";
    public boolean forAccessToken =true;

    @Override
    public String toString() {
        return "LoginRequest{" +
                "forAccessToken=" + forAccessToken +
                ", uid='" + uid + '\'' +
                ", pwd='" + pwd + '\'' +
                ", rid='" + rid + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoginRequest request = (LoginRequest) o;

        if (forAccessToken != request.forAccessToken) return false;
        if (!uid.equals(request.uid)) return false;
        if (!pwd.equals(request.pwd)) return false;
        return rid.equals(request.rid);

    }

    @Override
    public int hashCode() {
        int result = uid.hashCode();
        result = 31 * result + pwd.hashCode();
        result = 31 * result + rid.hashCode();
        result = 31 * result + (forAccessToken ? 1 : 0);
        return result;
    }

    /**
     * 将字符串转成MD5值
     *
     * @param string
     * @return
     */
    public static String stringToMD5(String string) {
        try {
            MessageDigest bmd5 = MessageDigest.getInstance("MD5");
            bmd5.update(string.getBytes());
            int i;
            StringBuffer buf = new StringBuffer();
            byte[] b = bmd5.digest();
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
