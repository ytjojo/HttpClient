package com.jiulongteng.http.progress;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.jiulongteng.http.download.db.DownloadCache;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class UriRequestBody extends RequestBody {
    final private Uri uri;
    @Nullable
    final MediaType contentType;

    public UriRequestBody(MediaType contentType, Uri uri) {
        this.contentType = contentType;
        this.uri = uri;
    }
    @Override
    public MediaType contentType() {

        if (contentType != null) return contentType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            return getMediaType((uri.getLastPathSegment()));
        } else {
            String contentType = DownloadCache.getContext().getContentResolver().getType(uri);
            return contentType != null ? MediaType.parse(contentType) : MediaType.parse("application/octet-stream");
        }
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        try (Source source = Okio.source(DownloadCache.getContext().getContentResolver().openInputStream(uri))) {
            sink.writeAll(source);
        }
    }

    @Override
    public long contentLength() throws IOException {
        return fileLength();
    }

    public long fileLength() {
        switch (uri.getScheme()) {
            case ContentResolver.SCHEME_FILE:
                return new File(uri.getPath()).length();
            case ContentResolver.SCHEME_CONTENT:
                Cursor cursor = DownloadCache.getContext().getContentResolver()
                        .query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    return new File(cursor.getString(cursor.getColumnIndex("_data"))).length();

                }
                break;
        }
        return 0;
    }

    public static MediaType getMediaType(String filename) {
        int index = filename.lastIndexOf(".") + 1;
        String fileSuffix = filename.substring(index);
        String contentType = URLConnection.guessContentTypeFromName(fileSuffix);
        return contentType != null ? MediaType.parse(contentType) : null;
    }
}

