package com.jiulongteng.http.download;

import java.io.IOException;


public interface DownloadOutputStream {
    void write(byte[] b, int off, int len) throws IOException;

    void close() throws IOException;

    void flushAndSync() throws IOException;

    void seek(long offset) throws IOException;

    void setLength(long newLength) throws IOException;

}
