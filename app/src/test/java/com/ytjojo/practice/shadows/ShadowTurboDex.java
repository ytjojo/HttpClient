package com.ytjojo.practice.shadows;

import com.lody.turbodex.TurboDex;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Administrator on 2016/11/4 0004.
 */
@Implements(TurboDex.class)
public class ShadowTurboDex {

    @Implementation
    public static void enableTurboDex(){
    }

}
