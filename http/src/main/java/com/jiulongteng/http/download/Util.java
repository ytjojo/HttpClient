package com.jiulongteng.http.download;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.entry.BlockInfo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Util {

    // request method
    public static final String METHOD_HEAD = "HEAD";

    // request header fields.
    public static final String RANGE = "Range";
    public static final String IF_MATCH = "If-Match";
    public static final String USER_AGENT = "User-Agent";

    public static final String CONTENT_MD5 = "Content-MD5";

    public static final String CONTENT_TYPE ="Content-Type";

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


    // 1 connection: [0, 1MB)
    private static final long ONE_CONNECTION_UPPER_LIMIT = 1024 * 1024; // 1MiB
    // 2 connection: [1MB, 5MB)
    private static final long TWO_CONNECTION_UPPER_LIMIT = 5 * 1024 * 1024; // 5MiB
    // 3 connection: [5MB, 50MB)
    private static final long THREE_CONNECTION_UPPER_LIMIT = 50 * 1024 * 1024; // 50MiB
    // 4 connection: [50MB, 100MB)
    private static final long FOUR_CONNECTION_UPPER_LIMIT = 100 * 1024 * 1024; // 100MiB

    public interface Logger {
        void e(String tag, String msg, Throwable e);

        void w(String tag, String msg);

        void d(String tag, String msg);

        void i(String tag, String msg);
    }

    public static class EmptyLogger implements Logger {
        @Override
        public void e(String tag, String msg, Throwable e) {
        }

        @Override
        public void w(String tag, String msg) {
        }

        @Override
        public void d(String tag, String msg) {
        }

        @Override
        public void i(String tag, String msg) {
        }
    }

    @SuppressWarnings("PMD.LoggerIsNotStaticFinal")
    private static Logger logger = new EmptyLogger();

    /**
     * Enable logger used for okdownload, and print each log with {@link Log}.
     */
    public static void enableConsoleLog() {
        logger = null;
    }

    /**
     * Set the logger which using on okdownload.
     * default one is {@link EmptyLogger}.
     *
     * @param l if provide logger is {@code null} we will using {@link Log} as default.
     */
    public static void setLogger(@Nullable Logger l) {
        logger = l;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void e(String tag, String msg, Throwable e) {
        if (logger != null) {
            logger.e(tag, msg, e);
            return;
        }

        Log.e(tag, msg, e);
    }

    public static void w(String tag, String msg) {
        if (logger != null) {
            logger.w(tag, msg);
            return;
        }

        Log.w(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (logger != null) {
            logger.d(tag, msg);
            return;
        }

        Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (logger != null) {
            logger.i(tag, msg);
            return;
        }

        Log.i(tag, msg);
    }

    // For avoid mock whole android framework methods on unit-test.
    public static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable runnable) {
                final Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }

    @Nullable
    public static String md5(String string) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ignored) {
        }

        if (hash != null) {
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) hex.append('0');
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        }

        return null;
    }

    public static boolean isCorrectFull(long fetchedLength, long contentLength) {
        return fetchedLength == contentLength;
    }


    public static long getFreeSpaceBytes(@NonNull StatFs statFs) {
        // NEED CHECK PERMISSION?
        long freeSpaceBytes;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            freeSpaceBytes = statFs.getAvailableBytes();
        } else {
            //noinspection deprecation
            freeSpaceBytes = statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
        }

        return freeSpaceBytes;
    }

    /**
     * @param si whether using SI unit refer to International System of Units.
     */
    public static String humanReadableBytes(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.ENGLISH, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }


    public static long parseContentLength(@Nullable String contentLength) {
        if (contentLength == null) return CHUNKED_CONTENT_LENGTH;

        try {
            return Long.parseLong(contentLength);
        } catch (NumberFormatException ignored) {
            Util.d("Util", "parseContentLength failed parse for '" + contentLength + "'");
        }

        return CHUNKED_CONTENT_LENGTH;
    }

    public static boolean isNetworkNotOnWifiType(ConnectivityManager manager) {
        if (manager == null) {
            Util.w("Util", "failed to get connectivity manager!");
            return true;
        }

        //noinspection MissingPermission, because we check permission accessable when invoked
        @SuppressLint("MissingPermission") final NetworkInfo info = manager.getActiveNetworkInfo();

        return info == null || info.getType() != ConnectivityManager.TYPE_WIFI;
    }

    public static long parseContentLengthFromContentRange(@Nullable String contentRange) {
        if (contentRange == null || contentRange.length() == 0) return CHUNKED_CONTENT_LENGTH;
        final String pattern = "bytes (\\d+)-(\\d+)/\\d+";
        try {
            final Pattern r = Pattern.compile(pattern);
            final Matcher m = r.matcher(contentRange);
            if (m.find()) {
                final long rangeStart = Long.parseLong(m.group(1));
                final long rangeEnd = Long.parseLong(m.group(2));
                return rangeEnd - rangeStart + 1;
            }
        } catch (Exception e) {
            Util.w("Util", "parse content-length from content-range failed " + e);
        }
        return CHUNKED_CONTENT_LENGTH;
    }

    public static boolean isUriContentScheme(@NonNull Uri uri) {
        return uri.getScheme().equals(ContentResolver.SCHEME_CONTENT);
    }

    public static boolean isUriFileScheme(@NonNull Uri uri) {
        return uri.getScheme().equals(ContentResolver.SCHEME_FILE);
    }

    @Nullable
    public static String getFilenameFromContentUri(Context context, @NonNull Uri contentUri) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cursor = resolver.query(contentUri, null, null, null, null);
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                return cursor
                        .getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } finally {
                cursor.close();
            }
        }

        return null;
    }

    @NonNull
    public static File getParentFile(final File file) {
        final File candidate = file.getParentFile();
        return candidate == null ? new File("/") : candidate;
    }

    public static long getSizeFromContentUri(Context context, @NonNull Uri contentUri) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cursor = resolver.query(contentUri, null, null, null, null);
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                return cursor
                        .getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            } finally {
                cursor.close();
            }
        }
        return -1;
    }

    public static boolean isNetworkAvailable(ConnectivityManager manager) {
        if (manager == null) {
            Util.w("Util", "failed to get connectivity manager!");
            return true;
        }

        //noinspection MissingPermission, because we check permission accessable when invoked
        @SuppressLint("MissingPermission") final NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static void inspectUserHeader(@NonNull Map<String, List<String>> headerField)
            throws IOException {
        if (headerField.containsKey(IF_MATCH) || headerField.containsKey(RANGE)) {
            throw new IOException(IF_MATCH + " and " + RANGE + " only can be handle by internal!");
        }
    }


    public static void assembleBlock(@NonNull DownloadTask task) {

        final int blockCount = task.isAcceptRange() ? determineBlockCount(task) : 1;

        task.getInfo().resetBlockInfos();
        final long eachLength = task.getInstanceLength() / blockCount;
        long startOffset = 0;
        long contentLength = 0;
        for (int i = 0; i < blockCount; i++) {
            startOffset = startOffset + contentLength;
            if (i == 0) {
                // first block
                final long remainLength = task.getInstanceLength() % blockCount;
                contentLength = eachLength + remainLength;
            } else {
                contentLength = eachLength;
            }

            final BlockInfo blockInfo = new BlockInfo(startOffset, contentLength);
            task.getInfo().addBlock(blockInfo);
        }
    }

    public static int determineBlockCount(@NonNull DownloadTask task) {
        long totalLength = task.getInstanceLength();
        if (task.getConnectionCount() != null) return task.getConnectionCount();

        if (totalLength < ONE_CONNECTION_UPPER_LIMIT) {
            return 1;
        }

        if (totalLength < TWO_CONNECTION_UPPER_LIMIT) {
            return 2;
        }

        if (totalLength < THREE_CONNECTION_UPPER_LIMIT) {
            return 3;
        }

        if (totalLength < FOUR_CONNECTION_UPPER_LIMIT) {
            return 4;
        }

        return 5;
    }

    public static void resetBlockIfDirty(BlockInfo info) {
        boolean isDirty = false;

        if (info.getCurrentOffset() < 0) {
            isDirty = true;
        } else if (info.getCurrentOffset() > info.getContentLength()) {
            isDirty = true;
        }

        if (isDirty) {
            w("resetBlockIfDirty", "block is dirty so have to reset: " + info);
            info.resetBlock();
        }
    }


}