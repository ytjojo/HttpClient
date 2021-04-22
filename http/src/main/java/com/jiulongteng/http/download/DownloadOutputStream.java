package com.jiulongteng.http.download;

import java.io.IOException;
import java.io.OutputStream;


public abstract class DownloadOutputStream extends OutputStream {

    abstract void flushAndSync() throws IOException;

    abstract void seek(long offset) throws IOException;

    abstract void setLength(long newLength) throws IOException;

}
