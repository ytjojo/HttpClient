package com.jiulongteng.http.download.dispatcher;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.DownloadListener;
import com.jiulongteng.http.download.DownloadTask;
import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.db.DownloadCache;

import java.util.concurrent.atomic.AtomicLong;

public class CallbackDispatcher implements DownloadListener {
    private static final String TAG = "CallbackDispatcher";


    AtomicLong lastCallbackProcessTime = new AtomicLong(0);
    AtomicLong lastOffset = new AtomicLong(0);
    Handler uiHandler;

    public CallbackDispatcher(@NonNull Handler handler) {
        this.uiHandler = handler;
    }

    public CallbackDispatcher() {

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
        lastCallbackProcessTime.set(System.currentTimeMillis());
        lastOffset.set(task.getInfo().getTotalOffset());
    }

    @Override
    public void fetchProgress(@NonNull DownloadTask task, int currentProgress, long currentSize, long contentLength) {

        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    task.getDownloadListener().fetchProgress(task, currentProgress, currentSize, contentLength);
                }
            });
        } else {
            task.getDownloadListener().fetchProgress(task, currentProgress, currentSize, contentLength);
        }

    }

    @Override
    public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Throwable realCause) {
        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (cause == EndCause.COMPLETED && realCause == null) {
                        //100%
                        fetchProgress(task);
                    }
                    task.getDownloadListener().taskEnd(task, cause, realCause);
                }
            });
        } else {
            if (cause == null && realCause == null) {
                //100%
                fetchProgress(task);
            }
            task.getDownloadListener().taskEnd(task, cause, realCause);
        }
    }

    public void fetchProgress(@NonNull DownloadTask task) {
        if (isFetchProcessMoment() || task.getTaskStatus() == DownloadCache.COMPLETE) {
            final long lastTime = lastCallbackProcessTime.get();
            final long currentTime = System.currentTimeMillis();

            long offset = task.getInfo().getTotalOffset();
            if (offset == lastOffset.get()) {
                return;
            }
            long contentLength = task.getInfo().getTotalLength();
            int currentProgress = (int) (offset * 100 / contentLength);
            this.fetchProgress(task, currentProgress, offset, contentLength);
            long timeCost = currentTime - lastTime;
            float speed = (offset - lastOffset.get()) / timeCost;
            System.out.println("speed" + speed);
            lastOffset.set(offset);
            lastCallbackProcessTime.set(currentTime);
        }

    }


    public boolean isFetchProcessMoment() {

        final long minInterval = DownloadCache.getInstance().getCallbackInterval();
        final long now = System.currentTimeMillis();
        return minInterval <= 0
                || now - getLastCallbackProcessTime() >= minInterval;
    }

    public long getLastCallbackProcessTime() {
        return lastCallbackProcessTime.get();
    }

}