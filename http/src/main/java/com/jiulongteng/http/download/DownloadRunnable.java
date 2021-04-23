package com.jiulongteng.http.download;

import com.jiulongteng.http.download.cause.DownloadException;
import com.jiulongteng.http.download.cause.ResumeFailedCause;
import com.jiulongteng.http.download.db.DownloadCache;
import com.jiulongteng.http.download.entry.BlockInfo;
import com.jiulongteng.http.download.entry.BreakpointInfo;
import com.jiulongteng.http.util.TextUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadRunnable extends AbstractDownloadRunnable {
    private final static String TAG = "DownloadRunnable";
    private MappedByteBuffer bytebuffer;//内存映射缓冲区
    private FileChannel fileChannel;

    RandomAccessFile raf;


    public DownloadRunnable(DownloadTask task, BlockInfo info, int index) {
        super(task, info, index);
    }


    @Override
    public void run() {
        setCurrentThread();
        BufferedInputStream bis = null;

        ResponseBody responseBody = null;
        try {
            if(!DownloadCache.getInstance().isNetPolicyValid(task)){
                throw new DownloadException(DownloadException.NETWORK_POLICY_ERROR,"invalid network state");
            }
            Request rangeRequest = task.getRawRequest().newBuilder().header(Util.RANGE, "bytes=" + blockInfo.getRangeLeft() + "-" + blockInfo.getRangeRight()).build();
            if (!TextUtils.isEmpty(task.getRedirectLocation())) {
                rangeRequest = rangeRequest.newBuilder().url(task.getRedirectLocation()).build();
            }
            if (!Util.isEmpty(task.getInfo().getEtag())) {
                rangeRequest = rangeRequest.newBuilder().header(Util.IF_MATCH, task.getInfo().getEtag()).build();
            }
            Response response = task.getClient().newCall(rangeRequest).execute();
            response = singleDownload(task.getInfo(), response);
            inspect(response);
            initContentLength(response);
            responseBody = response.body();
            bis = new BufferedInputStream(responseBody.byteStream());
//            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(
//                    task.getFile(), ParcelFileDescriptor.parseMode("rw"));
            raf = new RandomAccessFile(task.getTargetProvider().getTargetFile(), "rwd");
//            fileChannel = fos.getChannel();
            fileChannel = raf.getChannel();
//            fileChannel.position(blockInfo.getRangeLeft());
            raf.seek(blockInfo.getRangeLeft());
            bytebuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, blockInfo.getRangeLeft(), blockInfo.getContentLength());
            setBufferedLength(0);
            long byteRead = 0;
            readLength = 0;
            byte[] b = new byte[getByteBufferSize()];
            while ((byteRead = bis.read(b)) != -1) {
                if (task.isStoped()) {
                    Util.i(TAG," ----found stop");
                    break;
                }
                if(!DownloadCache.getInstance().isNetPolicyValid(task)){
                    throw new DownloadException(DownloadException.NETWORK_POLICY_ERROR,"invalid network state");
                }


                final long targetLength = readLength + byteRead;
                if (targetLength > blockInfo.getContentLength()) {
                    byteRead = blockInfo.getContentLength() - readLength;
                    bytebuffer.put(b, 0, (int) byteRead);
                    addAndGetBufferedLength(byteRead);
                    readLength += byteRead;
                    break;
                }
                bytebuffer.put(b, 0, (int) byteRead);
                addAndGetBufferedLength(byteRead);
                readLength += byteRead;
            }

            setIsReadByteFinished(true);
            task.getFlushRunnable().done(this);
            if (!task.isStoped()) {
                boolean isNotChunked = getContentLength() != Util.CHUNKED_CONTENT_LENGTH;
                if (isNotChunked) {
                    if (readLength != getContentLength()) {
                        throw new IOException("Fetch-length isn't equal to the response content-length, "
                                + readLength + "!= " + getContentLength());
                    }
                }
            }
            Util.i(TAG,"   parkThread");
            parkThread();

        } catch (Exception e){
            task.setThrowable(e);
        }
        finally {
            Util.i(TAG,"  finally");
            okhttp3.internal.Util.closeQuietly(fileChannel);
            okhttp3.internal.Util.closeQuietly(raf);
            okhttp3.internal.Util.closeQuietly(bis);
            okhttp3.internal.Util.closeQuietly(responseBody);
        }
    }

    private Response singleDownload(BreakpointInfo info, Response response) throws IOException {
        if (info.getBlockCount() == 1 && !info.isChunked()) {
            // only one block to download this resource
            // use this block response header instead of trial result if they are different.
            final long blockInstanceLength = DownloadUtils.getExactContentLengthRangeFrom0(response.headers());
            final long infoInstanceLength = info.getTotalLength();
            if (blockInstanceLength > 0 && blockInstanceLength != infoInstanceLength) {
                Util.d(TAG, "SingleBlock special check: the response instance-length["
                        + blockInstanceLength + "] isn't equal to the instance length from trial-"
                        + "connection[" + infoInstanceLength + "]");
                final BlockInfo blockInfo = info.getBlock(0);
                boolean isFromBreakpoint = blockInfo.getRangeLeft() != 0;

                final BlockInfo newBlockInfo = new BlockInfo(0, blockInstanceLength);
                info.resetBlockInfos();
                info.addBlock(newBlockInfo);

                if (isFromBreakpoint) {
                    final String msg = "Discard breakpoint because of on this special case, we have"
                            + " to download from beginning";
                    Util.w(TAG, msg);
                }
                okhttp3.internal.Util.closeQuietly(response);

                Request request = response.request().newBuilder().header(Util.RANGE, "bytes=" + blockInfo.getRangeLeft() + "-").build();
                return task.getClient().newCall(request).execute();

            }
        }
        return response;
    }


    public void inspect(Response response) throws IOException {
        Util.d(TAG, getIndex() + "  response" +response.body().contentLength() +  " contentLength " +DownloadUtils.getExactContentLengthRangeFrom0(response.headers()) + " exactContentLengthRange " + blockInfo.getContentLength());
        final BlockInfo blockInfo = getBlockInfo();
        final int code = response.code();
        final String newEtag = response.header(Util.ETAG);

        final ResumeFailedCause resumeFailedCause = DownloadPretreatment
                .getPreconditionFailedCause(code, blockInfo.getCurrentOffset() != 0,
                        task.getInfo().getEtag(), newEtag);
        if (resumeFailedCause != null) {
            // resume failed, relaunch from beginning.
            throw new DownloadException(DownloadException.RESUME_ERROR, resumeFailedCause.toString());
        }

        final boolean isServerCancelled = DownloadPretreatment
                .isServerCanceled(code, blockInfo.getCurrentOffset() != 0);
        if (isServerCancelled) {
            // server cancelled, end task.
            throw new DownloadException(DownloadException.SERVER_CANCEL_ERROR, "trial exception code = " + task.getResponseCode());
        }
    }

    public void initContentLength(Response response) {
        final long contentLength;
        final String contentLengthField = response.header(Util.CONTENT_LENGTH);
        if (contentLengthField == null || contentLengthField.length() == 0) {
            final String contentRangeField = response.header(Util.CONTENT_RANGE);
            contentLength = Util.parseContentLengthFromContentRange(contentRangeField);
        } else {
            contentLength = Util.parseContentLength(contentLengthField);
        }
        setContentLength(contentLength);
    }

    @Override
    public long flush() throws IOException {
        final long buffered = getBufferedLength();
        if ( buffered > 0) {
            bytebuffer.force();
            raf.getFD().sync();
            blockInfo.increaseCurrentOffset(buffered);
            addAndGetBufferedLength(-buffered);
            DownloadCache.getInstance().updateBlockInfo(blockInfo.getId(),blockInfo.getCurrentOffset());
            return buffered;
        }

        return 0;
    }


    /**
     * 显式回收MappedByteBuffer实例
     */
    private void unmap() throws IOException {
        flush();        //刷入数据
        if (bytebuffer == null)
            return;
        bytebuffer.clear();
        bytebuffer = null;
    }





}