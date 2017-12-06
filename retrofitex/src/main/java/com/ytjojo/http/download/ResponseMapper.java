package com.ytjojo.http.download;

import android.os.SystemClock;

import com.ytjojo.http.download.multithread.Manager;
import com.ytjojo.http.download.multithread.ProgressInfo;
import com.ytjojo.rx.SourceGenerator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/11/11 0011.
 */
public class ResponseMapper  implements Func1<ResponseBody,File> {
    final String mAbsDir;
    final String mFileName;
    SourceGenerator<ProgressInfo> mGenerator = new SourceGenerator<ProgressInfo>(){
        @Override
        public void onStart() {

        }
    };
    public void subscribe(Subscriber<ProgressInfo> subscriber){
        Observable.unsafeCreate(mGenerator).sample(30, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }

    public ResponseMapper(String absDir,String  fileName){
       this.mAbsDir = absDir;
        this.mFileName = fileName;
    }
    private long offset = 0;
    ProgressInfo mProgressInfo ;
    long contentLength;
    long lastTime;
    @Override
    public File call(ResponseBody responseBody) {
        RandomAccessFile raf = null;
        contentLength = responseBody.contentLength();
        mProgressInfo = new ProgressInfo(0,contentLength, ProgressInfo.State.DOWNLOADING);
        mGenerator.onNext(mProgressInfo);
        lastTime = SystemClock.uptimeMillis();
        File target = null;
        try {
            raf = new RandomAccessFile(new File(mAbsDir,mFileName+ Manager.S_FILECACHE_NAME), "rw");
            BufferedInputStream bis = new BufferedInputStream(responseBody.byteStream());
            int bytesRead;
            byte[] buff = new byte[4096];
            while ((bytesRead = bis.read(buff, 0, buff.length)) != -1) {
                raf.seek(this.offset);
                raf.write(buff, 0, bytesRead);
                this.offset = this.offset + bytesRead;

                if(SystemClock.uptimeMillis() >30 +lastTime){
                    mProgressInfo.bytesRead = offset;
                    mProgressInfo.contentLength = contentLength;
                    mProgressInfo.mState = ProgressInfo.State.DOWNLOADING;
                    mGenerator.onNext(mProgressInfo);
                }
                File file = new File(mAbsDir,mFileName +Manager.S_FILECACHE_NAME);
                target = new File(mAbsDir,mFileName);
                file.renameTo(target);

            }
            raf.close();
            bis.close();
            responseBody.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
        }

//        return new File(mFile.getAbsolutePath(),mFile.getName());
        return target;
    }
}
