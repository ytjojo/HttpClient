package com.ytjojo.http.download.multithread;

public class ProgressInfo {
    public enum State {CONNECT, DOWNLOADING, STOPE, FINISHED}

    public ProgressInfo(long bytesRead, long contentLength, State state) {
        this.bytesRead = bytesRead;
        this.contentLength = contentLength;
        this.mState = state;
    }

    public ProgressInfo(long bytesRead, long contentLength) {
        this.bytesRead = bytesRead;
        this.contentLength = contentLength;
    }

    /**
     * 文件大小
     */
    public long contentLength;
    /**
     * 已下载大小
     */
    public long bytesRead;

    public State mState;
    public long speed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgressInfo info = (ProgressInfo) o;

        if (contentLength != info.contentLength) return false;
        if (bytesRead != info.bytesRead) return false;
        if (speed != info.speed) return false;
        return mState == info.mState;

    }

    @Override
    public int hashCode() {
        int result = (int) (contentLength ^ (contentLength >>> 32));
        result = 31 * result + (int) (bytesRead ^ (bytesRead >>> 32));
        result = 31 * result + mState.hashCode();
        result = 31 * result + (int) (speed ^ (speed >>> 32));
        return result;
    }
}