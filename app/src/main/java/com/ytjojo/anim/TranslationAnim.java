package com.ytjojo.anim;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Administrator on 2016/9/8 0008.
 */
public class TranslationAnim {
    public static enum Direction{
        Left,Top,Right,Bottom
    }
    int mTranslateValue;
    ValueAnimator mShowAnim;
    ValueAnimator mHideAinim;
    boolean isAnimShow;

    private View mMaskedback;
    private View mShowView;
    private Direction mDirection;
    public TranslationAnim(@NonNull View target, @NonNull Direction direction, @Nullable View maskback){
        this.mShowView = target;
        this.mDirection =direction;
        this.mMaskedback = maskback;
        if(mMaskedback !=null){
            mMaskedback.setBackgroundColor(Color.parseColor("#a800000"));
        }
    }

    private void setValue(float value){
        switch (mDirection){
            case Top:
                ViewHelper.setTranslationY(mShowView,-value);
                break;
            case Bottom:
                ViewHelper.setTranslationY(mShowView,value);
                break;
            case Left:
                ViewHelper.setTranslationX(mShowView,-value);
                break;
            case Right:
                ViewHelper.setTranslationX(mShowView,value);
                break;
        }
    }

    private boolean isVertical(){
        return mDirection == Direction.Top||mDirection == Direction.Bottom;
    }
    public void hide(){
        mShowView.post(new Runnable() {
            @Override
            public void run() {
                mTranslateValue = isVertical()?mShowView.getHeight() : mShowView.getWidth();
                if(mMaskedback !=null)
                ViewHelper.setAlpha(mMaskedback,0);
                setValue(mTranslateValue);

            }
        });
    }
    public void show(){
        mShowView.post(new Runnable() {
            @Override
            public void run() {
                mTranslateValue = isVertical()?mShowView.getHeight() : mShowView.getWidth();
                if(mMaskedback !=null)
                ViewHelper.setAlpha(mMaskedback,1f);
                setValue(0);
            }
        });
    }
    public void animHide() {
        getFinalValue();
        if (mShowAnim != null) {
            mShowAnim.cancel();//cancel为异步
            mShowAnim = null;
        }
        if (mHideAinim != null || ViewHelper.getTranslationY(mShowView) == mTranslateValue) {
            return;
        }
        isAnimShow = false;
        mHideAinim = ValueAnimator.ofFloat(ViewHelper.getTranslationY(mShowView), mTranslateValue);
        mHideAinim.setDuration(250).setInterpolator(new FastOutSlowInInterpolator());
        mHideAinim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!isAnimShow && mHideAinim != null) {
                    float curX = (float) mHideAinim.getAnimatedValue();
                    setValue(curX);
                    if(mMaskedback !=null)
                    ViewHelper.setAlpha(mMaskedback,1 - curX/ mTranslateValue);
                }
            }
        });
        mHideAinim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mHideAinim = null;
                if(mMaskedback !=null)
                mMaskedback.setClickable(false);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mHideAinim = null;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mHideAinim.start();

    }

    private void getFinalValue() {
        switch (mDirection){
            case Top:
                mTranslateValue = -mShowView.getHeight();
                break;
            case Bottom:
                mTranslateValue = mShowView.getHeight();
                break;
            case Left:
                mTranslateValue = -mShowView.getWidth();;
                break;
            case Right:
                mTranslateValue = mShowView.getWidth();;;
                break;
        }
    }

    public void animShow() {
        getFinalValue();
        if (mHideAinim != null) {
            mHideAinim.cancel();//cancel为异步
            mHideAinim = null;
        }
        if (mShowAnim != null || ViewHelper.getTranslationY(mShowView) == 0) {
            return;
        }
        isAnimShow = true;
        mShowAnim = ValueAnimator.ofFloat(ViewHelper.getTranslationY(mShowView), 0);
        mShowAnim.setDuration(250).setInterpolator(new FastOutLinearInInterpolator());
        mShowAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (isAnimShow && mShowAnim != null) {
                    float curX = (float) mShowAnim.getAnimatedValue();
                    setValue(curX);
                    if(mMaskedback !=null)
                    ViewHelper.setAlpha(mMaskedback,1 - curX/ mTranslateValue);
                }
            }
        });
        mShowAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mShowAnim = null;
                if(mMaskedback !=null)
                mMaskedback.setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mShowAnim = null;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mShowAnim.start();

    }
}
