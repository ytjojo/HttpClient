/*
 * Copyright (c) 2017 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiulongteng.http.download.entry;


import androidx.annotation.IntRange;

import com.jiulongteng.http.download.DownloadTask;
import com.jiulongteng.http.download.Util;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;


public class BlockInfo {
    @IntRange(from = 0)
    private final long startOffset;
    @IntRange(from = 0)
    private final long contentLength;
    private final AtomicLong currentOffset;
    private int id;

    public BlockInfo(long startOffset, long contentLength) {
        this(-1,startOffset, contentLength, 0);
    }

    public BlockInfo(int id,long startOffset, long contentLength, @IntRange(from = 0) long currentOffset) {
        if (startOffset < 0 || (contentLength < 0 && contentLength != Util.CHUNKED_CONTENT_LENGTH)
                || currentOffset < 0) {
            throw new IllegalArgumentException();
        }

        this.startOffset = startOffset;
        this.contentLength = contentLength;
        this.currentOffset = new AtomicLong(currentOffset);
        this.id = id;
    }

    public long getCurrentOffset() {
        return this.currentOffset.get();
    }

    public long getStartOffset() {
        return startOffset;
    }

    public long getRangeLeft() {
        return startOffset + currentOffset.get();
    }

    public long getContentLength() {
        return contentLength;
    }

    public long getRangeRight() {
        return startOffset + contentLength - 1;
    }

    public void increaseCurrentOffset(@IntRange(from = 1) long increaseLength) {
        this.currentOffset.addAndGet(increaseLength);
    }

    public void resetBlock() {
        this.currentOffset.set(0);
    }

    public BlockInfo copy() {
        return new BlockInfo(id,startOffset, contentLength, currentOffset.get());
    }

    @Override public String toString() {
        return "[" + startOffset + ", " + getRangeRight() + ")" + "-current:" + currentOffset;
    }

    public boolean isDone(){
        return getRangeLeft() == getRangeRight();
    }

    public int getId() {
        return id;
    }
}
