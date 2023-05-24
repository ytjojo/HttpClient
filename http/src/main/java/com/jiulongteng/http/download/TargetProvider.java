package com.jiulongteng.http.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TargetProvider<T> {

    T getTarget();

    InputStream getInputStream() throws IOException;

    DownloadOutputStream getOutputStream() throws IOException;

    boolean isExists();

    void allocateLength(long length) throws IOException;

    boolean createNewTarget(String mineType) throws IOException;

    String getFileName();

    boolean delete();

    void setFileName(String fileName);

    File getParentFile();

    File getTargetFile();
}
