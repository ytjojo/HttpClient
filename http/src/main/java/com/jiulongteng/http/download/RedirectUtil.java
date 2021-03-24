package com.jiulongteng.http.download;

import androidx.annotation.NonNull;

import com.jiulongteng.http.download.cause.DownloadException;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Headers;

public class RedirectUtil {

    /**
     * How many redirects and auth challenges should we attempt? Chrome follows 21 redirects;
     * Firefox, curl, and wget follow 20; Safari follows 16; and HTTP/1.0 recommends 5.
     */
    public static final int MAX_REDIRECT_TIMES = 10;

    /**
     * The target resource resides temporarily under a different URI and the user agent MUST NOT
     * change the request method if it performs an automatic redirection to that URI.
     */
    static final int HTTP_TEMPORARY_REDIRECT = 307;
    /**
     * The target resource has been assigned a new permanent URI and any future references to this
     * resource ought to use one of the enclosed URIs.
     */
    static final int HTTP_PERMANENT_REDIRECT = 308;


    public static boolean isRedirect(int code) {
        return code == HttpURLConnection.HTTP_MOVED_PERM
                || code == HttpURLConnection.HTTP_MOVED_TEMP
                || code == HttpURLConnection.HTTP_SEE_OTHER
                || code == HttpURLConnection.HTTP_MULT_CHOICE
                || code == HTTP_TEMPORARY_REDIRECT
                || code == HTTP_PERMANENT_REDIRECT;
    }

    @NonNull
    public static String getRedirectedUrl(Headers headers, int responseCode)
            throws IOException {
        String url = headers.get("Location");
        if (url == null) {
            throw new DownloadException(
                    "Response code is " + responseCode + " but can't find Location field");

        }
        return url;
    }

}