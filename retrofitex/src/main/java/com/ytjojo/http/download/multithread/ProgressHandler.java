package com.ytjojo.http.download.multithread;

import com.ytjojo.rx.UnsubscribeFiter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/11/12 0012.
 */
public class ProgressHandler {
    enum AsyncAction {IDLE,STARTED,STOPE,FINISHED,FAILED}
    ConcurrentHashMap<Integer,Long> mTaskProgress;
    long contentLength ;
    ProgressInfo mProgressInfo ;
    long lastCompletSize;
    public  volatile AsyncAction mSignal = AsyncAction.IDLE;
    AtomicLong mAtomicLong =new AtomicLong(0);
    private  Manager mManager;
    public ProgressHandler(Manager manager){
        this.mManager = manager;
    }
    public void setTaskInfos(List<DownloadInfo> infos){
        this.mDownloadInfos = infos;
        if(mTaskProgress == null){
            this.mTaskProgress = new ConcurrentHashMap<>();
        }
        for(DownloadInfo info:infos){
           this. mTaskProgress.put(info.getThreadId(),new Long(info.getCompeleteSize()));
        }

    }
    public void setContentLength(long contentLength){
        this.contentLength=contentLength;
    }
    public synchronized boolean isAllTaskStoped(){
        return mAtomicLong.get()==4;
    }
    List<DownloadInfo> mDownloadInfos;
    public void setProgress(DownloadInfo info){
        if(mTaskProgress == null){
            mTaskProgress = new ConcurrentHashMap<>();
        }
        mTaskProgress.put(info.getThreadId(),info.getCompeleteSize());
    }
    public static final int DELAY = 30;
    public Observable<ProgressInfo> getProgress(){
        return  Observable.interval(DELAY, TimeUnit.MILLISECONDS).map(new Func1<Long, ProgressInfo>() {
            @Override
            public ProgressInfo call(Long value) {
                int compeleteSize=0;
                for(DownloadInfo info: mDownloadInfos){
                    compeleteSize += mTaskProgress.get(info.getThreadId());
                }
                if(mProgressInfo ==null){
                    mProgressInfo = new ProgressInfo(compeleteSize,contentLength);
                }
                mProgressInfo.bytesRead = compeleteSize;
                mProgressInfo.contentLength = contentLength;
                final int state  = (int) mAtomicLong.get();
                switch (state){
                    case 0:
                        mProgressInfo.mState = ProgressInfo.State.CONNECT;
                        break;
                    case 1:
                        mProgressInfo.mState = ProgressInfo.State.CONNECT;
                        break;
                    case 2:
                        mProgressInfo.mState = ProgressInfo.State.CONNECT;
                        break;
                    case 3:
                        mProgressInfo.mState = ProgressInfo.State.DOWNLOADING;

                        break;
                    case 4:
                        if(contentLength ==compeleteSize){
                            mProgressInfo.mState = ProgressInfo.State.FINISHED;
                        }else{
                            mProgressInfo.mState = ProgressInfo.State.STOPE;
                        }

                        break;
                }

                mProgressInfo.speed = (mProgressInfo.bytesRead - lastCompletSize)*1000/DELAY;
                lastCompletSize = mProgressInfo.bytesRead;
                return mProgressInfo;
            }
        }).distinctUntilChanged()
        .lift(new UnsubscribeFiter<>(new Func1<ProgressInfo, Boolean>() {
            @Override
            public Boolean call(ProgressInfo progressInfo) {
                if(progressInfo.mState != ProgressInfo.State.CONNECT && progressInfo.mState != ProgressInfo.State.DOWNLOADING){
                    return false;
                }
                return true;
            }
        }));

    }
    public ProgressInfo getCurProgressInfo(){
        int compeleteSize=0;
        for(DownloadInfo info: mDownloadInfos){
            compeleteSize += mTaskProgress.get(info.getThreadId());
        }
        if(mProgressInfo ==null){
           mProgressInfo = new ProgressInfo(compeleteSize,contentLength);
        }
        mProgressInfo.bytesRead = compeleteSize;
        mProgressInfo.contentLength = contentLength;
        mProgressInfo.speed = (mProgressInfo.contentLength - lastCompletSize)*1000/DELAY;
        return mProgressInfo;
    }
    public boolean isDownloadFinish(){
        int compeleteSize=0;
        for(DownloadInfo info: mDownloadInfos){
            compeleteSize += mTaskProgress.get(info.getThreadId());
        }
        return compeleteSize ==contentLength;
    }

}
