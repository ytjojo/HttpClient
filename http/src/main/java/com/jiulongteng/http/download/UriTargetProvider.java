package com.jiulongteng.http.download;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import com.jiulongteng.http.download.db.DownloadCache;
import com.jiulongteng.http.util.TextUtils;
import com.jiulongteng.http.util.UriUtil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

public class UriTargetProvider implements TargetProvider<Uri> {
    private Uri uri;
    private String fileName;

    private String relativePath;

    private File parentFile;
    private File targetFile;
    private Uri rootUri;


    /**
     * @param rootUri      [MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)]
     *                     [MediaStore.Downloads.EXTERNAL_CONTENT_URI]
     *                     [MediaStore.Audio.Media.EXTERNAL_CONTENT_URI]
     *                     [MediaStore.Video.Media.EXTERNAL_CONTENT_URI]
     *                     [MediaStore.Images.Media.EXTERNAL_CONTENT_URI]
     * @param fileName
     * @param relativePath
     */
    public UriTargetProvider(Uri rootUri, @Nullable String fileName, @Nullable String relativePath) {
        this.rootUri = rootUri;
        this.fileName = fileName;
        this.relativePath = relativePath;
    }

    public UriTargetProvider(Uri uri) {
        this.uri = uri;
    }

    @Override
    public Uri getTarget() {
        if (uri == null) {
            initUriData();
        }
        return uri;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return DownloadCache.getContext().getContentResolver().openInputStream(uri);
    }

    @Override
    public DownloadOutputStream getOutputStream() throws IOException {
        return new DownloadUriOutputStream(DownloadCache.getContext(), uri, 8096);
    }

    @Override
    public boolean isExists() {
        if (targetFile == null && uri != null) {
            initUriData();
        } else if (rootUri != null) {
            if (uri == null) {
                if (relativePath != null) {
                    uri = UriUtil.query(rootUri, DownloadCache.getContext(), fileName, relativePath);
                } else {
                    uri = UriUtil.queryByFileName(rootUri, DownloadCache.getContext(), fileName);
                }
                if (uri == null) {
                    return false;
                }
            }


            initUriData();

            boolean b1 = parentFile.exists();
            boolean b2 = targetFile.exists();

        }
        return uri != null;
    }

    @Override
    public void allocateLength(long length) throws IOException {
        try {
            DownloadUriOutputStream outputStream = (DownloadUriOutputStream) getOutputStream();
            outputStream.setLength(length);
            outputStream.close();
        } catch (FileNotFoundException e) {
            UriUtil.delete(uri);
            String minetype = URLConnection.getFileNameMap().getContentTypeFor(getFileName());
            String path =  uri.getPath();
            if(rootUri == null){
                rootUri = uri.buildUpon().path(path.substring(0,path.lastIndexOf('/'))).build();
            }
            uri = UriUtil.insert(minetype, rootUri, fileName,relativePath);
        }

    }

    @Override
    public boolean createNewTarget(String mineType) {
        if (rootUri != null && fileName != null) {
            if (uri == null) {
                uri = UriUtil.insert(mineType, rootUri, fileName, relativePath);
                initUriData();
            }
        } else if (uri != null) {
            initUriData();

        }
        return isExists();
    }

    @Override
    public String getFileName() {
        if (!TextUtils.isEmpty(fileName)) {
            return fileName;
        }
        if (uri != null && fileName == null) {
            initUriData();
        }
        return fileName;

    }

    @TargetApi(Build.VERSION_CODES.Q)
    private void initUriData() {
        if (targetFile != null && parentFile != null) {
            return;
        }
        Cursor cursor = DownloadCache.getContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            String data = cursor.getString(dataIndex);
            String displayName = cursor.getString(displayNameIndex);
            long size = cursor.getLong(sizeIndex);
            targetFile = new File(data);
            setFileName(targetFile.getName());
            parentFile = Util.getParentFile(targetFile);

            //关闭游标
            cursor.close();
        }
    }

    @Override
    public boolean delete() {
        if (uri != null) {
            FileWriter fileWriter = null;
            try {
                FileDescriptor fd = DownloadCache.getContext().getContentResolver().openFileDescriptor(uri, "rw").getFileDescriptor();
                fileWriter = new FileWriter(fd);
                fileWriter.write("");
                return true;
            } catch (Exception e) {
                okhttp3.internal.Util.closeQuietly(fileWriter);
            }

        }
        return false;

    }

    @Override
    public void setFileName(String fileName) {
        if (uri == null) {
            this.fileName = fileName;
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
