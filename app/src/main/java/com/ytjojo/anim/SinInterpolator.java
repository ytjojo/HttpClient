package com.ytjojo.anim;

import android.view.animation.Interpolator;

/**
 * Created by Administrator on 2016/9/26 0026.
 */
public class SinInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
        return (float) Math.sin(Math.PI *input/2);
    }
}
