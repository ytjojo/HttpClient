package com.jiulongteng.http.download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.cause.EndCause;

public interface DownloadPostTreatment {

    void onEnd(DownloadTask task, @NonNull EndCause cause, @Nullable Throwable realCause);
}
