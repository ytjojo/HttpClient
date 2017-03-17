package com.ytjojo.anim;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.io.Serializable;
import java.util.ArrayList;

public class ZoomTutorial {
    public static class ViewInfo implements Serializable,Parcelable{
        public int width;
        public int height;
        public int leftOnScreen;
        public int topOnScreen;
        public int positon;

        public ViewInfo() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.width);
            dest.writeInt(this.height);
            dest.writeInt(this.leftOnScreen);
            dest.writeInt(this.topOnScreen);
            dest.writeInt(this.positon);
        }

        protected ViewInfo(Parcel in) {
            this.width = in.readInt();
            this.height = in.readInt();
            this.leftOnScreen = in.readInt();
            this.topOnScreen = in.readInt();
            this.positon = in.readInt();
        }

        public static final Creator<ViewInfo> CREATOR = new Creator<ViewInfo>() {
            public ViewInfo createFromParcel(Parcel source) {
                return new ViewInfo(source);
            }

            public ViewInfo[] newArray(int size) {
                return new ViewInfo[size];
            }
        };
    }
    final private int mAnimationDuration = 300;// 动画持续的时间，300比较合适
    private Animator mCurrentAnimator;//当前的动画对象
    private ViewInfo mCurViewInfo;
    private ViewInfo mSourceViewInfo;
    private View mTargetView;
    private int mVisiablePositonStart;
    private int mVisiablePositonEnd;
    private final Spring mSpring = SpringSystem
            .create()
            .createSpring()
            .addListener(new SimpleSpringListener(){
                @Override
                public void onSpringUpdate(Spring spring){
                    double CurrentValue = spring.getCurrentValue();
                    double scale = mCurViewInfo.width>mSourceViewInfo.width?mSourceViewInfo.width* 1D/mCurViewInfo.width:mCurViewInfo.width* 1D/mSourceViewInfo.width ;
                    float mappedValue = (float) SpringUtil.mapValueFromRangeToRange(CurrentValue, 0, 1, 1, scale);
                }
            });
    public void animSpring(){
        if (mSpring.getEndValue() == 0) {
            mSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(170, 5));
            mSpring.setEndValue(1);
            return;
        }
        mSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(1, 5));
        mSpring.setEndValue(0);
    }
    public ZoomTutorial(View target,ViewInfo source) {
        this.mCurViewInfo = getViewInfo(target);
        this.mSourceViewInfo = source;
        this.mTargetView = target;
    }

    public void expand(){
        startZoomAnim(mSourceViewInfo,mCurViewInfo);
    }
    public void closeAnim(){
        startZoomAnim(mCurViewInfo, mSourceViewInfo);
    }




    public void startZoomAnim(ViewInfo start,ViewInfo end) {
        if(mCurrentAnimator !=null){
            return;
        }
        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(1);
        animSet.play(ObjectAnimator.ofFloat(mTargetView, "pivotX", 0f))
                .with(ObjectAnimator.ofFloat(mTargetView, "pivotY", 0f))
                .with(ObjectAnimator.ofFloat(mTargetView, "alpha", 1.0f));
        animSet.start();
        AnimatorSet set = new AnimatorSet();
        float startScale =1f;
        float endScale =1f;
        if(start.width> end.width){
            endScale = start.width/end.width;
            startScale = 1;
        }else{
            endScale = 1;
            startScale = (1f*start.width)/end.width;
        }
        if(mTargetView.getWidth() != start.width){
            ViewHelper.setX(mTargetView,start.leftOnScreen);
            ViewHelper.setY(mTargetView,start.topOnScreen);
            ViewHelper.setScaleX(mTargetView,start.width*1f/end.width);
            ViewHelper.setScaleY(mTargetView,start.width*1f/end.width);

        }
        if(start.width> end.width){
            if(!isVisible(mVisiablePositonEnd)){
                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mTargetView, "alpha", 0.1f);
                ObjectAnimator transilation = ObjectAnimator.ofFloat(mTargetView, "y", mTargetView.getHeight()/10);

                ViewHelper.setPivotX(mTargetView,0.5f);
                ViewHelper.setPivotY(mTargetView,0.5f);
                set.play(alphaAnimator)
                        .with(transilation)
                        .with(ObjectAnimator.ofFloat(mTargetView, "scaleX", 1, 0.7f))
                        .with(ObjectAnimator.ofFloat(mTargetView, "scaleY", 1, 0.7f));
                set.setDuration(mAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCurrentAnimator = null;
                        if (listener != null) {
                            listener.onEnd();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mCurrentAnimator = null;
                        if (listener != null) {
                            listener.onEnd();
                        }
                    }
                });
                set.start();
                return;
            }
        }
        set.play(
            ObjectAnimator.ofFloat(mTargetView, "x", start.leftOnScreen, end.leftOnScreen))
            .with(ObjectAnimator.ofFloat(mTargetView, "y", start.topOnScreen, start.topOnScreen))
            .with(ObjectAnimator.ofFloat(mTargetView, "scaleX", startScale, endScale))
            .with(ObjectAnimator.ofFloat(mTargetView, "scaleY", startScale, endScale));
        
        set.setDuration(mAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
                if (listener != null) {
                    listener.onEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
                if (listener != null) {
                    listener.onEnd();
                }
            }
        });
        set.start();
        mCurrentAnimator = set;
    }

    /**
     * 在GridView中，使用getChildAt(index)的取值，只能是当前可见区域（列表可滚动）的子项！
     * 因为子项会进行复用。这里强制转换了下，变成了GridView，实际使用中需要进行修改
     * 【参考】
     * http://xie2010.blog.163.com/blog/static/211317365201402395944633/
     * http://blog.csdn.net/you_and_me12/article/details/7271006
     * 
     * @param position
     * @return 判断这个position的view是否现在显示在屏幕上，如果没有显示就返回false
     */
    public boolean isVisible(int position,AdapterView<?> adapterView) {
        //得到显示区域中第一个子视图的序号
        int firstPosition = adapterView.getFirstVisiblePosition();
        int lastPositon = adapterView.getLastVisiblePosition();
        if(position>= firstPosition &&position<lastPositon){
            return true;
        }
        return false;
    }

    public boolean isVisible(int postion, RecyclerView recyclerView){
        return recyclerView.findViewHolderForLayoutPosition(postion)!=null;
    }
    public static int getFirstVisiblePosition(RecyclerView recyclerView){
        return recyclerView.getChildAdapterPosition(recyclerView.getChildAt(recyclerView.getChildCount()-1))-recyclerView.getChildCount();

    }
    public static int getLastVisiblePosition(RecyclerView recyclerView){
//        recyclerView.getLayoutManager().findViewByPosition(0);
//        recyclerView.findViewHolderForLayoutPosition()
        return recyclerView.getChildAdapterPosition(recyclerView.getChildAt(recyclerView.getChildCount()-1));

    }
    public boolean isVisible(int positon){
        if(positon>=mVisiablePositonStart &&positon<=mVisiablePositonEnd){
            return true;
        }
        return false;
    }


    
    private OnZoomListener listener;
    
    public void setOnZoomListener(OnZoomListener l) {
        listener = l;
    }
    
    public interface OnZoomListener {
        public void onEnd();//点击后展示大图成功后调用
    }

    public static ViewInfo getViewInfo(View view){
        int[] opt = new int[2];
        view.getLocationOnScreen(opt);
        ViewInfo viewInfo = new ViewInfo();
        viewInfo.width = view.getWidth();
        viewInfo.height = view.getHeight();
        viewInfo.leftOnScreen = opt[0];
        viewInfo.topOnScreen = opt[1];
        return viewInfo;
    }
    public static ArrayList<ViewInfo> getViewInfo(RecyclerView recyclerView){
        int firstPosition = getFirstVisiblePosition(recyclerView);
        int count = recyclerView.getChildCount();
        ArrayList<ViewInfo> list = new ArrayList<>(count);
        for (int i=0;i < count ;i++){
            View child = recyclerView.getChildAt(i);
            ViewInfo viewInfo = getViewInfo(child);
            viewInfo.positon = firstPosition+i;
            list.add(viewInfo);
        }
        return list;
    }
    public static ArrayList<ViewInfo> getViewInfo(AdapterView<?> adapterView){
        int firstPosition = adapterView.getFirstVisiblePosition();
        int count = adapterView.getChildCount();
        ArrayList<ViewInfo> list = new ArrayList<>(count);
        for (int i=0;i < count ;i++){
            View child = adapterView.getChildAt(i);
            ViewInfo viewInfo = getViewInfo(child);
            viewInfo.positon = firstPosition+i;
            list.add(viewInfo);
        }
        return list;
    }
    private void moveShow() {
        ObjectAnimator.ofFloat(mTargetView, "alpha", 0.8f).setDuration(0).start();
        AnimatorSet set = new AnimatorSet();
        float toX = mCurViewInfo.width/2 -(mSourceViewInfo.leftOnScreen + mSourceViewInfo.width/2);
        float toY = mCurViewInfo.height/2 -(mSourceViewInfo.topOnScreen + mSourceViewInfo.height/2);
        set.playTogether(
                ObjectAnimator.ofFloat(mTargetView, "translationX",toX).setDuration(200),
                ObjectAnimator.ofFloat(mTargetView, "translationY", toY).setDuration(200),
                ObjectAnimator.ofFloat(mTargetView, "alpha", 1).setDuration(200)

        );
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ((ImageView)mTargetView).setScaleType(ImageView.ScaleType.FIT_XY);
                mSpring.setEndValue(1);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        set.start();

    }


    private void moveBack() {
        mSpring.setEndValue(0);
        mTargetView.postDelayed(new Runnable() {
            public void run() {
                AnimatorSet set = new AnimatorSet();
                set.playTogether(
                        ObjectAnimator.ofFloat(mTargetView, "translationX",0).setDuration(200),
                        ObjectAnimator.ofFloat(mTargetView, "translationY", 0).setDuration(200)
                );
                set.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        ((ImageView)mTargetView).setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                set.start();

            }
        }, 300);

    }

}