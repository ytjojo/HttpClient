package com.jiulongteng.http.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import okhttp3.MediaType;

public class MediaTypeUtil {

    public MediaType getMediaType(Context context, Uri fileUri){
        return MediaType.parse(context.getContentResolver().getType(fileUri));
    }
}
