package com.jiulongteng.http.download;

import java.io.Closeable;
import java.io.IOException;


public interface DownloadOutputStream extends Closeable {
    void write(byte[] b, int off, int len) throws IOException;


    void flushAndSync() throws IOException;

    void seek(long offset) throws IOException;

    void setLength(long newLength) throws IOException;

}
