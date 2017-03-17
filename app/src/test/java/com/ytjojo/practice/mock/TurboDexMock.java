package com.ytjojo.practice.mock;

import com.lody.turbodex.TurboDex;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by Administrator on 2016/11/5 0005.
 */
public class TurboDexMock {
    public static void mockTurboDex(){
        mockStatic(TurboDex.class);
    }
}
