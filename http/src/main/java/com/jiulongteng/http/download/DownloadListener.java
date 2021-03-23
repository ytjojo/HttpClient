package com.jiulongteng.http.download;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.cause.ResumeFailedCause;
import com.jiulongteng.http.download.entry.BreakpointInfo;

import java.util.List;
import java.util.Map;

public interface DownloadListener {
    void taskStart(@NonNull DownloadTask task);

    void connectTrialStart(@NonNull DownloadTask task);

    void fetchStart(@NonNull DownloadTask task, boolean isFromBeginning);

    void fetchProgress(@NonNull DownloadTask task,int currentProgress, long currentSize, long contentLength);

    void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
                 @Nullable Throwable realCause);
}