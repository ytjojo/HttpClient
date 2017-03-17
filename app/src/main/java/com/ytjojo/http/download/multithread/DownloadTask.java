package com.ytjojo.http.download.multithread;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2016/11/12 0012.
 */
public class DownloadTask {
    private DownloadInfo mDownloadInfo;
    private File mFile;
    final long startPos;
    final long endPos;
    long compeleteSize;
    private ProgressHandler mProgressHandler;
    private long contentLength;
    public AtomicBoolean isStoped;
    private long needDownloadLength;
    public DownloadTask(File file,ProgressHandler handler, DownloadInfo downloadInfo) {
        this.mFile = file;
        this.mProgressHandler = handler;
        this.mDownloadInfo = downloadInfo;
        this.startPos = mDownloadInfo.getStartPos();
        this.endPos = mDownloadInfo.getEndPos();
        this.compeleteSize = mDownloadInfo.getCompeleteSize();
        this.isStoped = new AtomicBoolean(false);
        this.needDownloadLength = downloadInfo.getEndPos() -(startPos + compeleteSize)+1;
    }

    public void execute(OkHttpClient client, Request request){

        Request rangeRequest = request.newBuilder().header("Range", "bytes=" + (startPos + compeleteSize) + "-" + endPos).build();
        RandomAccessFile raf = null;
        BufferedInputStream bis = null;
        ResponseBody responseBody =null;
        FileChannel channelOut = null;
        try {
            Response response = client.newCall(rangeRequest).execute();
            if (!response.isSuccessful()) {
                return;
            }
            responseBody = response.body();
            contentLength = responseBody.contentLength();
            Logger.e("contentLength = "+contentLength + "start"+startPos + " complete" + compeleteSize + "range" + (startPos + compeleteSize) + " start  end"+ endPos);
            File cacheFile = new File(mFile.getAbsolutePath());
            raf = new RandomAccessFile(cacheFile, "rwd");
            bis = new BufferedInputStream(responseBody.byteStream());
            channelOut = raf.getChannel();

            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE,startPos+compeleteSize, contentLength);
//            raf.seek(startPos + compeleteSize);
            int bytesRead = -1;
            byte[] buff = new byte[4096];
            while ((bytesRead = bis.read(buff, 0, buff.length)) != -1) {
                mappedBuffer.put(buff, 0, bytesRead);//效率高
//                raf.write(buff, 0, bytesRead);//
                this.compeleteSize = this.compeleteSize + bytesRead;
                this.mDownloadInfo.setCompeleteSize(compeleteSize);
                this.mProgressHandler.setProgress(mDownloadInfo);
//                Logger.e(mDownloadInfo.getThreadId()+ "  "+compeleteSize + " contentLength=" + contentLength);
                Dao.getInstance().updataInfos(mDownloadInfo.getThreadId(), compeleteSize, mDownloadInfo.getUrl());
                if (mProgressHandler.mSignal == ProgressHandler.AsyncAction.STOPE) {
                    isStoped.set(true);
                    break;
                }
            }
            boolean forceStop = isStoped.get();
            boolean isFinish= needDownloadLength ==contentLength;
            Logger.e(mDownloadInfo.getThreadId() +" id  "+contentLength + "完成 start" +mDownloadInfo.getStartPos()+ "endpos =" +mDownloadInfo.getEndPos()+ "comlete="+mDownloadInfo.getCompeleteSize());

//            if(!forceStop&& mDownloadInfo.getStartPos() + compeleteSize != mDownloadInfo.getEndPos()+1){
//                throw new DownLoadException("保存文件出错");
//            }

        } catch (FileNotFoundException e) {
            throw new DownLoadException("文件保存出错",e);
        } catch (IOException e) {
            Logger.e(e.toString() + " --" +e.getLocalizedMessage() + "--" + e.getMessage());
        } finally {
            okhttp3.internal.Util.closeQuietly(raf);
            okhttp3.internal.Util.closeQuietly(bis);
            okhttp3.internal.Util.closeQuietly( channelOut);
            if(responseBody !=null)
            responseBody.close();
        }
    }
    public boolean isFinish(){
        return compeleteSize == contentLength;
    }

    public void setHeaders(Request.Builder builder) {
        builder.header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) Gecko/2008092510 Ubuntu/8.04 (hardy) Firefox/3.0.3");
        builder.header("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
        builder.header("Accept-Encoding", "utf-8");
        builder.header("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        builder.header("connnection", "keep-alive");
//            builder.header("Accept-Language", "en-us,en;q=0.7,zh-cn;q=0.3");
//            builder.header("Keep-Alive", "300");//无意义，禁止添加
//            builder.header("If-Modified-Since", "Fri, 02 Jan 2009 17:00:05 GMT");//
//            builder.header("If-None-Match", "md5");//资源的md5
//            builder.header("Cache-conntrol", "max-age=0");
//            builder.header("Referer", mDownloadInfo.getUrl());
    }
}
