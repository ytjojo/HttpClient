package com.jiulongteng.http.download;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.cause.DownloadException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtils {
    public static final String TAG = "DownloadUtils";

    // request method
    public static final String METHOD_HEAD = "HEAD";

    // request header fields.
    public static final String RANGE = "Range";
    public static final String IF_MATCH = "If-Match";
    public static final String USER_AGENT = "User-Agent";

    public static final String LOCATION = "Location";
    public static final int HTTP_REDIRCTCODE = HttpURLConnection.HTTP_MOVED_TEMP;

    // response header fields.
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_RANGE = "Content-Range";
    public static final String ETAG = "Etag";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String ACCEPT_RANGES = "Accept-Ranges";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    // response header value.
    public static final String VALUE_CHUNKED = "chunked";
    public static final int CHUNKED_CONTENT_LENGTH = -1;

    // response special code.
    public static final int RANGE_NOT_SATISFIABLE = 416;

    public boolean isSurpportMultiThread(Headers headers) {
        String bytes = headers.get("Accept-Ranges");
        String contentRange = headers.get("Content-Range");
        if ("bytes".equals(bytes) || (contentRange != null && contentRange.startsWith("bytes"))) {
            return true;
        }
        return false;
    }


    /**
     * Check whether the response Etag is the same to the local Etag if the local Etag is provided
     * on connect.
     *
     * @return whether the local Etag is overdue.
     */
    public boolean isEtagOverdue(String etag, String responseEtag) {
        return etag != null && !etag.equals(responseEtag);
    }

    public static boolean isAcceptRange(@NonNull Headers headers, int code)
            throws IOException {
        if (code == HttpURLConnection.HTTP_PARTIAL) return true;

        final String acceptRanges = headers.get(ACCEPT_RANGES);
        return "bytes".equals(acceptRanges);
    }

    @Nullable
    public static String findFilename(Headers headers)
            throws IOException {
        return parseContentDisposition(headers.get(CONTENT_DISPOSITION));
    }

    public static final Pattern CONTENT_DISPOSITION_QUOTED_PATTERN =
            Pattern.compile("attachment;\\s*filename\\s*=\\s*\"([^\"]*)\"");
    // no note
    public static final Pattern CONTENT_DISPOSITION_NON_QUOTED_PATTERN =
            Pattern.compile("attachment;\\s*filename\\s*=\\s*(.*)");

    /**
     * The same to com.android.providers.downloads.Helpers#parseContentDisposition.
     * </p>
     * Parse the Content-Disposition HTTP Header. The format of the header
     * is defined here: http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html
     * This header provides a filename for content that is going to be
     * downloaded to the file system. We only support the attachment type.
     */
    @Nullable
    public static String parseContentDisposition(String contentDisposition)
            throws IOException {
        if (contentDisposition == null) {
            return null;
        }

        try {
            String fileName = null;
            Matcher m = CONTENT_DISPOSITION_QUOTED_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                fileName = m.group(1);
            } else {
                m = CONTENT_DISPOSITION_NON_QUOTED_PATTERN.matcher(contentDisposition);
                if (m.find()) {
                    fileName = m.group(1);
                }
            }

            if (fileName != null && fileName.contains("../")) {
                throw new DownloadException(DownloadException.DOWNLOAD_SECURITY_ERROR,"The filename [" + fileName + "] from"
                        + " the response is not allowable, because it contains '../', which "
                        + "can raise the directory traversal vulnerability");
            }

            return fileName;
        } catch (IllegalStateException ex) {
            // This function is defined as returning null when it can't parse the header
        }
        return null;
    }

    @Nullable
    public static String findEtag(Headers headers) {
        return headers.get(ETAG);
    }

    public static long findInstanceLength(Headers headers) {
        // Content-Range
        final long instanceLength = parseContentRangeFoInstanceLength(
                headers.get(CONTENT_RANGE));
        if (instanceLength != CHUNKED_CONTENT_LENGTH) return instanceLength;

        // chunked on here
        final boolean isChunked = parseTransferEncoding(headers
                .get(TRANSFER_ENCODING));
        if (!isChunked) {
            Util.w(TAG, "Transfer-Encoding isn't chunked but there is no "
                    + "valid instance length found either!");
        }

        return CHUNKED_CONTENT_LENGTH;
    }

    public static boolean isNeedTrialHeadMethodForInstanceLength(
            long oldInstanceLength, @NonNull Headers headers) {
        if (oldInstanceLength != CHUNKED_CONTENT_LENGTH) {
            // the instance length already has certain value.
            return false;
        }

        final String contentRange = headers.get(CONTENT_RANGE);
        if (contentRange != null && contentRange.length() > 0) {
            // because of the Content-Range can certain the result is right, so pass.
            return false;
        }

        final boolean isChunked = parseTransferEncoding(
                headers.get(TRANSFER_ENCODING));
        if (isChunked) {
            // because of the Transfer-Encoding can certain the result is right, so pass.
            return false;
        }

        final String contentLengthField = headers.get(CONTENT_LENGTH);
        if (contentLengthField == null || contentLengthField.length() <= 0) {
            // because of the response header isn't contain the Content-Length so the HEAD method
            // request is useless, because we plan to get the right instance-length on the
            // Content-Length field through the response header of non 0-0 Range HEAD method request
            return false;
        }

        // because of the response header contain Content-Length, but because of we using Range: 0-0
        // so we the Content-Length is always 1 now, we can't use it, so we try to use HEAD method
        // request just for get the certain instance-length.
        return true;
    }

    // if instance length is can't certain through transfer-encoding and content-range but the
    // content-length is exist but can't be used, we will request HEAD method request to find out
    // right one.
    public static long trialHeadMethodForInstanceLength(OkHttpClient client, Request request) throws IOException {
        Response response = null;
        try {
            response = client.newCall(request.newBuilder().head().build()).execute();
            Headers headers = response
                    .headers();
            return Util.parseContentLength(
                    headers.get(CONTENT_LENGTH));
        } finally {
            okhttp3.internal.Util.closeQuietly(response);
        }


    }

    public static boolean parseTransferEncoding(@Nullable String transferEncoding) {
        return transferEncoding != null && transferEncoding.equals(VALUE_CHUNKED);
    }

    public static long parseContentRangeFoInstanceLength(@Nullable String contentRange) {
        if (contentRange == null) return CHUNKED_CONTENT_LENGTH;

        final String[] session = contentRange.split("/");
        if (session.length >= 2) {
            try {
                return Long.parseLong(session[1]);
            } catch (NumberFormatException e) {
                Util.w(TAG, "parse instance length failed with " + contentRange);
            }
        }

        return -1;
    }


    public static final Pattern CONTENT_RANGE_RIGHT_VALUE = Pattern
            .compile(".*\\d+ *- *(\\d+) */ *\\d+");

    /**
     * Get the exactly content-length, on this method we assume the range is from 0.
     */
    @IntRange(from = -1)
    static long getExactContentLengthRangeFrom0(Headers headers) {
        final String contentRangeField = headers.get(CONTENT_RANGE);
        long contentLength = -1;
        if (!Util.isEmpty(contentRangeField)) {
            final long rightRange = getRangeRightFromContentRange(contentRangeField);
            // for the range from 0, the contentLength is just right-range +1.
            if (rightRange > 0) contentLength = rightRange + 1;
        }

        if (contentLength < 0) {
            // content-length
            final String contentLengthField = headers.get(CONTENT_LENGTH);
            if (!Util.isEmpty(contentLengthField)) {
                contentLength = Long.parseLong(contentLengthField);
            }
        }

        return contentLength;
    }

    @IntRange(from = -1)
    static long getRangeRightFromContentRange(@NonNull String contentRange) {
        Matcher m = CONTENT_RANGE_RIGHT_VALUE.matcher(contentRange);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }

        return -1;
    }
    private static final Pattern TMP_FILE_NAME_PATTERN = Pattern
            .compile(".*\\\\|/([^\\\\|/|?]*)\\??");


    public static String determineFilename(@Nullable String responseFileName,
                                       @NonNull String url) throws IOException {

        if (Util.isEmpty(responseFileName)) {

            Matcher m = TMP_FILE_NAME_PATTERN.matcher(url);
            String filename = null;
            while (m.find()) {
                filename = m.group(1);
            }

            if (Util.isEmpty(filename)) {
                filename = Util.md5(url);
            }

            if (filename == null) {
                throw new DownloadException(DownloadException.FILENAME_NOT_FOUND_ERROR,"Can't find valid filename.");
            }

            return filename;
        }

        return responseFileName;
    }


    public static void allocateLength(File file, long length) throws IOException{
        RandomAccessFile r = null;
        try {
            r = new RandomAccessFile(file, "rw");
            r.setLength(length);
        } finally{
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
