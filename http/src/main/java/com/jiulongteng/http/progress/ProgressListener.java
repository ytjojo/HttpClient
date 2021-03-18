package com.jiulongteng.http.progress;

import com.jiulongteng.http.entities.Progress;

interface ProgressListener {
    void update(Progress progress);
}