package com.jiulongteng.http.download.dispatcher;

import android.os.Handler;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.DownloadListener;
import com.jiulongteng.http.download.DownloadTask;
import com.jiulongteng.http.download.SpeedCalculator;
import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.db.DownloadCache;

import java.util.concurrent.atomic.AtomicLong;

public class CallbackDispatcher implements DownloadListener {
    private static final String TAG = "CallbackDispatcher";


    AtomicLong lastCallbackProcessTime = new AtomicLong(0);
    AtomicLong lastOffset = new AtomicLong(0);
    Handler uiHandler;
    private SpeedCalculator speedCalculator;

    public CallbackDispatcher(@NonNull Handler handler) {
        this.uiHandler = handler;
        speedCalculator = new SpeedCalculator();
    }


    @Override
    public void taskStart(@NonNull final DownloadTask task) {
        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    task.getDownloadListener().taskStart(task);
                }
            });
        } else {
            task.getDownloadListener().taskStart(task);
        }

    }

    @Override
    public void connectTrialStart(@NonNull DownloadTask task) {
        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    task.getDownloadListener().connectTrialStart(task);
                }
            });
        } else {
            task.getDownloadListener().connectTrialStart(task);
        }
    }

    @Override
    public void fetchStart(@NonNull DownloadTask task, boolean isFromBeginning) {
        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    task.getDownloadListener().fetchStart(task, isFromBeginning);
                }
            });
        } else {
            task.getDownloadListener().fetchStart(task, isFromBeginning);
        }
        lastCallbackProcessTime.set(nowMillis());
        lastOffset.set(task.getInfo().getTotalOffset());
        speedCalculator.downloading(0);
    }

    private long nowMillis() {
        if (DownloadCache.getInstance().isAndroid()) {
            return SystemClock.uptimeMillis();
        }
        return System.nanoTime() / 1000000;
    }

    @Override
    public void fetchProgress(@NonNull DownloadTask task, int currentProgress, long currentSize, long contentLength, long speed) {

        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    task.getDownloadListener().fetchProgress(task, currentProgress, currentSize, contentLength, speed);
                }
            });
        } else {
            task.getDownloadListener().fetchProgress(task, currentProgress, currentSize, contentLength, speed);
        }

    }

    @Override
    public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Throwable realCause) {
        speedCalculator.endTask();
        notifyFetchProgress(task);
        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {

                    task.getDownloadListener().taskEnd(task, cause, realCause);
                }
            });
        } else {
            task.getDownloadListener().taskEnd(task, cause, realCause);
        }
    }

    public void fetchProgress(@NonNull DownloadTask task, long increaseBytes) {
        speedCalculator.downloading(increaseBytes);
        lastOffset.addAndGet(increaseBytes);
        if (isFetchProcessMoment() || task.getTaskStatus() != DownloadCache.RUNNING) {
           notifyFetchProgress(task);
        }

    }
    private void notifyFetchProgress(DownloadTask task){
        final long currentTime = nowMillis();
        long offset = lastOffset.get();
        long contentLength = task.getInfo().getTotalLength();
        int currentProgress = (int) (offset * 100 / contentLength);
        dispatchSpeed(task);
        this.fetchProgress(task, currentProgress, offset, contentLength, speedCalculator.getBytesPerSecondFromBegin());
        lastCallbackProcessTime.set(currentTime);
    }

    private void dispatchSpeed(final DownloadTask task) {
        if (task.getSpeedListener() != null) {
            if (uiHandler != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        task.getSpeedListener().onProgress(task, speedCalculator);
                    }
                });
            } else {
                task.getSpeedListener().onProgress(task, speedCalculator);
            }

        }
    }


    public boolean isFetchProcessMoment() {

        final long minInterval = DownloadCache.getInstance().getProgressDispatchInterval();
        final long now = nowMillis();
        return minInterval <= 0
                || now - getLastCallbackProcessTime() >= minInterval;
    }

    public long getLastCallbackProcessTime() {
        return lastCallbackProcessTime.get();
    }

}