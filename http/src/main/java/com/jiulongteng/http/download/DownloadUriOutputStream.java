package com.jiulongteng.http.download;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;

import androidx.annotation.NonNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DownloadUriOutputStream extends DownloadOutputStream {

    @NonNull
    private final FileChannel channel;
    @NonNull
    final ParcelFileDescriptor pdf;
    @NonNull
    final BufferedOutputStream out;
    @NonNull
    final FileOutputStream fos;

    public DownloadUriOutputStream(Context context, Uri uri, int bufferSize) throws
            FileNotFoundException {
        final ParcelFileDescriptor pdf = context.getContentResolver().openFileDescriptor(uri, "rw");
        if (pdf == null) throw new FileNotFoundException("result of " + uri + " is null!");
        this.pdf = pdf;

        this.fos = new FileOutputStream(pdf.getFileDescriptor());
        this.channel = fos.getChannel();
        this.out = new BufferedOutputStream(fos, bufferSize);
    }

    public DownloadUriOutputStream(Context context, File file, int bufferSize) throws
            FileNotFoundException {
        this(context, Uri.fromFile(file), bufferSize);
    }

    DownloadUriOutputStream(@NonNull FileChannel channel, @NonNull ParcelFileDescriptor pdf,
                            @NonNull FileOutputStream fos,
                            @NonNull BufferedOutputStream out) {
        this.channel = channel;
        this.pdf = pdf;
        this.fos = fos;
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        out.close();
        fos.close();
        pdf.close();
    }

    @Override
    public void flushAndSync() throws IOException {
        out.flush();
        pdf.getFileDescriptor().sync();
    }

    @Override
    public void seek(long offset) throws IOException {
        channel.position(offset);
    }

    @Override
    public void setLength(long newLength) {
        final String tag = "DownloadUriOutputStream";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Os.posix_fallocate(pdf.getFileDescriptor(), 0, newLength);
            } catch (Throwable e) {
                if (e instanceof ErrnoException) {
                    if (((ErrnoException) e).errno == OsConstants.ENOSYS
                            || ((ErrnoException) e).errno == OsConstants.ENOTSUP) {
                        Util.w(tag, "fallocate() not supported; falling back to ftruncate()");
                        try {
                            Os.ftruncate(pdf.getFileDescriptor(), newLength);
                        } catch (Throwable e1) {
                            Util.w(tag, "It can't pre-allocate length(" + newLength + ") on the sdk"
                                    + " version(" + Build.VERSION.SDK_INT + "), because of " + e1);
                        }
                    }
                } else {
                    Util.w(tag, "It can't pre-allocate length(" + newLength + ") on the sdk"
                            + " version(" + Build.VERSION.SDK_INT + "), because of " + e);
                }
            }
        } else {
            Util.w(tag,
                    "It can't pre-allocate length(" + newLength + ") on the sdk "
                            + "version(" + Build.VERSION.SDK_INT + ")");
        }
    }


}