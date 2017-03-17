package com.ytjojo.fragmentstack;

import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

import java.util.List;

public class AnimateOnHWLayerIfNeededListener implements Animation.AnimationListener {
    private Animation.AnimationListener mOrignalListener = null;
    private boolean mShouldRunOnHWLayer = false;
    private View mView = null;

    public AnimateOnHWLayerIfNeededListener(final View v, Animation anim) {
        if (v == null || anim == null) {
            return;
        }
        mView = v;
    }

    public AnimateOnHWLayerIfNeededListener(final View v, Animation anim,
                                            Animation.AnimationListener listener) {
        if (v == null || anim == null) {
            return;
        }
        mOrignalListener = listener;
        mView = v;
    }

    public void onAttachToAnim(){

    }
    @Override
    @CallSuper
    public void onAnimationStart(Animation animation) {
        if (mView != null) {
            mShouldRunOnHWLayer = shouldRunOnHWLayer(mView, animation);
            if (mShouldRunOnHWLayer) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        ViewCompat.setLayerType(mView, ViewCompat.LAYER_TYPE_HARDWARE, null);
                    }
                });
            }
        }
        if (mOrignalListener != null) {
            mOrignalListener.onAnimationStart(animation);
        }
    }

    @Override
    @CallSuper
    public void onAnimationEnd(Animation animation) {
        if (mView != null && mShouldRunOnHWLayer) {
            mView.post(new Runnable() {
                @Override
                public void run() {
                    ViewCompat.setLayerType(mView, ViewCompat.LAYER_TYPE_NONE, null);
                }
            });
        }
        if (mOrignalListener != null) {
            mOrignalListener.onAnimationEnd(animation);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        if (mOrignalListener != null) {
            mOrignalListener.onAnimationRepeat(animation);
        }
    }
    static boolean shouldRunOnHWLayer(View v, Animation anim) {
        return Build.VERSION.SDK_INT >= 19
                && ViewCompat.getLayerType(v) == ViewCompat.LAYER_TYPE_NONE
                && ViewCompat.hasOverlappingRendering(v)
                && modifiesAlpha(anim);
    }

    static boolean modifiesAlpha(Animation anim) {
        if (anim instanceof AlphaAnimation) {
            return true;
        } else if (anim instanceof AnimationSet) {
            List<Animation> anims = ((AnimationSet) anim).getAnimations();
            for (int i = 0; i < anims.size(); i++) {
                if (anims.get(i) instanceof AlphaAnimation) {
                    return true;
                }
            }
        }
        return false;
    }
}