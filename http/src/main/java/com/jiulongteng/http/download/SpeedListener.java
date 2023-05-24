package com.jiulongteng.http.download;

public interface SpeedListener {

    void onProgress(DownloadTask task,SpeedCalculator speedCalculator);
}
