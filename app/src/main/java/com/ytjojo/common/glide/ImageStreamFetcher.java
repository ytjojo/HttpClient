package com.ytjojo.common.glide;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import java.io.IOException;
import java.io.InputStream;
/**
 * Created by John on 2015/9/19.
 */
public class ImageStreamFetcher implements DataFetcher<InputStream> {

    // 检查是否取消任务的标识
    private volatile boolean mIsCanceled;

    private final RequestImageUrl mImageUrlRequest;
    private InputStreamTransfer mInputStreamTransfer;
    private InputStream mInputStream;

    public ImageStreamFetcher(RequestImageUrl imageFid, InputStreamTransfer transfer) {
        mImageUrlRequest = imageFid;
        this.mInputStreamTransfer =transfer;
    }

    /**
     * 在后台线程中调用，用于获取图片的数据流，给Glide处理
     *
     * @param priority
     * @return
     * @throws Exception
     */
    @Override
    public InputStream loadData(Priority priority) throws Exception {
        // mImageFid有可能是来自缓存的，先从此对象获取url
        String url = mImageUrlRequest.getUrl();
        if (url == null) {
            if (mIsCanceled) {
                return null;
            }
            // 建立http请求，从网络上获取fid对应的的url
            url = mImageUrlRequest.syncUrlAcquire();
            if (url == null) {
                return null;
            }
            // 存储获取到的url，以供缓存使用
            mImageUrlRequest.setUrl(url);
        }
        if (mIsCanceled) {
            return null;
        }
        // 再次建立http请求，获取url的流
        mInputStream = mInputStreamTransfer.transfer(url);
        return mInputStream;
    }


    /**
     * 在后台线程中调用，在Glide处理完{@link #loadData(Priority)}返回的数据后，进行清理和回收资源
     */
    @Override
    public void cleanup() {
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            } finally {
                mInputStream = null;
            }
        }
    }

    /**
     * 在UI线程中调用，返回用于区别数据的唯一id
     *
     * @return
     */
    @Override
    public String getId() {
        return mImageUrlRequest.getId();
    }

    /**
     * 在UI线程中调用，取消加载任务
     */
    @Override
    public void cancel() {
        mIsCanceled = true;
        // 取消下载文件
        if (mImageUrlRequest != null) {
            mImageUrlRequest.cancel();
        }
        // 取消获取url
        if (mInputStreamTransfer != null) {
            mInputStreamTransfer.cancel();
        }

    }
}