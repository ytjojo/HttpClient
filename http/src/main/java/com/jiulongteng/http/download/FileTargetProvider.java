package com.jiulongteng.http.download;

import android.os.StatFs;

import com.jiulongteng.http.download.cause.DownloadException;
import com.jiulongteng.http.download.db.DownloadCache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileTargetProvider implements TargetProvider<File> {
    File targetFile;
    File parentFile;
    String fileName;

    public FileTargetProvider(File targetFile) {
        if (targetFile.isDirectory()) {
            parentFile = targetFile;
        } else {
            this.targetFile = targetFile;
            fileName = this.targetFile.getName();
            parentFile = Util.getParentFile(targetFile);
        }
    }

    @Override
    public File getTarget() {
        return targetFile;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(targetFile);
    }

    @Override
    public DownloadOutputStream getOutputStream() throws IOException {
        return new DownloadFileOutputStream(targetFile);
    }

    @Override
    public boolean isExists() {
        if (fileName == null || targetFile == null) {
            throw new IllegalStateException("please set file name");
        }
        return targetFile.exists();
    }

    @Override
    public void allocateLength(long length) throws IOException {
        final long requireSpace = length - getTargetFile().length();
        if (requireSpace > 0) {
            if(DownloadCache.getInstance().isAndroid()){
                try{
                    StatFs statFs = new StatFs(getTargetFile().getAbsolutePath());
                    final long freeSpace = Util.getFreeSpaceBytes(statFs);
                    if (freeSpace < requireSpace) {
                        throw new DownloadException(DownloadException.PROTOCOL_ERROR, "There is Free space less than Require space: " + freeSpace + " < " + requireSpace);
                    }
                }catch (IllegalArgumentException e){
                }

            }
            DownloadUtils.allocateLength(getTargetFile(), length);
        }
    }

    @Override
    public boolean createNewTarget(String mineType) throws IOException {

        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (!targetFile.exists()) {
            return targetFile.createNewFile();
        }
        return targetFile.exists();

    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean delete() {
        return targetFile.delete();
    }

    @Override
    public void setFileName(String fileName) {
        if (targetFile == null) {
            this.fileName = fileName;
            targetFile = new File(parentFile, fileName);
        }
    }

    @Override
    public File getParentFile() {
        return parentFile;
    }

    @Override
    public File getTargetFile() {
        return targetFile;
    }
}
