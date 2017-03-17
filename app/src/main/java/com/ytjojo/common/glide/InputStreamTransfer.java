package com.ytjojo.common.glide;

import java.io.InputStream;

/**
 * Created by Administrator on 2016/4/8 0008.
 */
public interface InputStreamTransfer {
    InputStream transfer(String url);
    void cancel();
}
