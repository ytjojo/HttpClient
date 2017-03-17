package com.ytjojo.anim.behavior;

import android.animation.Animator;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;

public class FooterBehavior extends CoordinatorLayout.Behavior<View> {
 
    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();
 
 
    private int sinceDirectionChange;
 
 
    public FooterBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
//1.判断滑动的方向 我们需要垂直滑动
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }
 
//2.根据滑动的距离显示和隐藏footer view
    boolean isRevers = true;
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        if (dy > 0 && sinceDirectionChange < 0 || dy < 0 && sinceDirectionChange > 0) {
//            child.animate().cancel();
            sinceDirectionChange = 0;
        }
        sinceDirectionChange += dy;
        if(Math.abs(sinceDirectionChange) > child.getHeight()/2){
            if(sinceDirectionChange >0){
                hide(child);
            }else{
                show(child);


            }
        }
    }

    ViewPropertyAnimator mAnimator;
    private void hide(final View view) {
        sinceDirectionChange = 0;
        if(mAnimator !=null){
            mAnimator.cancel();
        }
        mAnimator = view.animate().translationY(view.getHeight()).setInterpolator(INTERPOLATOR).setDuration(200);
        mAnimator.setListener(mAnimatorListener);
        mAnimator.start();
    }
 
 
    private void show(final View view) {
        sinceDirectionChange = 0;
        if(mAnimator !=null){
            mAnimator.cancel();
        }
        mAnimator = view.animate().translationY(0).setInterpolator(INTERPOLATOR).setDuration(200);
        mAnimator.setListener(mAnimatorListener);
        mAnimator.start();
    }
    Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimator = null;
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };
}