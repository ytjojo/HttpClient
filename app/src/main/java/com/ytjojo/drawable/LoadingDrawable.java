package com.ytjojo.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.text.TextPaint;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.ValueAnimator;
import com.ytjojo.BaseApplication;
import com.ytjojo.anim.SinInterpolator;
import com.ytjojo.utils.DensityUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/23 0023.
 */
public class LoadingDrawable extends Drawable implements Animatable {
    private static final int MAX_LEVEL = 10000;
    private int mDuration = 1500;
    private float mDownfactor = 0.1875f;
    private float mRebfactor = 0.0625f;
    public int mRadianDeep =  19;
    public int mMedWidth = 41 ;
    public int mMedHeight = 96;
    float mMaxCoordinatey = 60;
    public int mHeight;
    public int mWidth;
    SweepGradient mSweepGradient;
    RadialGradient mRadialGradient;
    float mCurrentDegrees = 0;
    float mTranslateY;
    Paint mPaint;
    private float mToDegrees = 180;
    private float mFromDegrees = 0;
    Matrix mMatrix;
    Matrix mMatrixForShader;
    ArrayList<Float> mFactors = new ArrayList<>();
    TextPaint mTextPaint;
    String mMessage = "Please wait...";
    float mTextWidth;
    float mTextOffsetY;

    public LoadingDrawable() {
        setupAnimators();
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DensityUtil.sp2px(BaseApplication.getInstance(),16f));
        mTextOffsetY = Math.abs(mTextPaint.getFontMetrics().descent);
        mTextPaint.setColor(Color.parseColor("#818181"));
        mTextWidth = mTextPaint.measureText(mMessage);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mMatrix = new Matrix();
        mMatrixForShader = new Matrix();

        initValue();
    }

    private void initValue() {
        mRadianDeep = DensityUtil.dip2px(BaseApplication.getInstance(),13);
        mMedWidth =  DensityUtil.dip2px(BaseApplication.getInstance(),27);;
        mMedHeight =  DensityUtil.dip2px(BaseApplication.getInstance(),64);
        mMaxCoordinatey = mTextWidth/2;
        mHeight = mMedHeight * 2 + mMedWidth;
        mWidth = mMedHeight * 2;
//        mRadialGradient = new RadialGradient((mWidth - mMedWidth) / 2, 0, mMedHeight/2, Color.parseColor("#96D8D2"), mDarkColor, Shader.TileMode.CLAMP);
        mRadialGradient = new RadialGradient((mWidth - mMedWidth) / 2, -mMedHeight/2, mMedHeight*1.3f, Color.parseColor("#96D8D2"), mDarkColor, Shader.TileMode.CLAMP);
    }

    private void init() {
        mFactors.add(0f);
        mFactors.add(mDownfactor);
        mFactors.add(mDownfactor + 2 * mRebfactor);
        mFactors.add(2 * mDownfactor + 2 * mRebfactor);
        mFactors.add(3 * mDownfactor + 2 * mRebfactor);
        mFactors.add(3 * mDownfactor + 4 * mRebfactor);
        mFactors.add(4 * mDownfactor + 4 * mRebfactor);
    }

    private int mDarkColor = Color.parseColor("#00A191");
    private int mLightColor = Color.parseColor("#B2DBC1");
    ValueAnimator mValueAnimator;
    ValueAnimator.AnimatorUpdateListener mUpdateListener;
    Path mPath = new Path();
    private final Callback mCallback = new Callback() {
        @Override
        public void invalidateDrawable(Drawable d) {
            invalidateSelf();
        }

        @Override
        public void scheduleDrawable(Drawable d, Runnable what, long when) {
            scheduleSelf(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable d, Runnable what) {
            unscheduleSelf(what);
        }
    };


    @Override
    public void start() {
        mValueAnimator.removeAllUpdateListeners();
        mValueAnimator.addUpdateListener(mUpdateListener);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setDuration(mDuration);
        mValueAnimator.start();
    }

    @Override
    public void stop() {
        mValueAnimator.removeUpdateListener(mUpdateListener);
        mValueAnimator.removeAllUpdateListeners();
        mValueAnimator.setRepeatCount(0);
        mValueAnimator.setDuration(0);
        mValueAnimator.end();
    }

    private float mOrginalValue;

    private void setupAnimators() {
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if((value !=0f && value != 1f) &&value == mOrginalValue){
                    return;
                }
//                mCurrentDegrees = mFromDegrees + (mToDegrees -mFromDegrees)*value;
                updateDegree(value);
                invalidateSelf();
                mOrginalValue = value;
            }
        };
        mValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        mValueAnimator.setRepeatCount(Animation.INFINITE);
        mValueAnimator.setRepeatMode(Animation.RESTART);
        mValueAnimator.setDuration(mDuration);
        //fuck you! the default interpolator is AccelerateDecelerateInterpolator
        mValueAnimator.setInterpolator(new LinearInterpolator());
    }

    float maxTranslateY;
    Interpolator mPathInterpolatorCompat = PathInterpolatorCompat.create(0.8f, -0.5f * 0.5f);
    Interpolator mSinInterpolator = new SinInterpolator();

    private void updateDegree(float value) {

        maxTranslateY = mMedHeight;

        mCoordinatey = 0;
        if (value <= mDownfactor) {
            float ratio = value / mDownfactor;
            mCurrentDegrees = 90 * ratio;
            mTranslateY = ratio * ratio * maxTranslateY;
            mCoordinatey = 0;
//            mTranslateY = getAccelerateInterpolator(ratio)*maxTranslateY ;
        } else if (value >= mDownfactor && value <= mDownfactor + 2 * mRebfactor) {
            mCurrentDegrees = 90f;

            float ratio = ((value - mDownfactor)) / mRebfactor;

//            if(value>= mDownfactor + mRebfactor){
//                ratio = (value - mDownfactor - mRebfactor)/mRebfactor;
//                ratio = 1- ratio;
//            }
//            Logger.e("0.1875 - 0.3125   curvalue = " + value + " first " + ratio);
            mTranslateY = maxTranslateY + mMaxCoordinatey * .3f * mSinInterpolator.getInterpolation(ratio);
            mCoordinatey = mSinInterpolator.getInterpolation(ratio) * mMaxCoordinatey;
        } else if (value >= mDownfactor + 2 * mRebfactor && value <= 2 * mDownfactor + 2 * mRebfactor) {
            float ratio = (value - mDownfactor - 2 * mRebfactor) / mDownfactor;
            mCurrentDegrees = (1 - ratio) * 90f;
            mTranslateY = (1 - ratio) * (1 - ratio) * maxTranslateY;
//            mTranslateY = getDecelerateInterpolator(1-ratio) *maxTranslateY ;

        } else if (value >= 2 * mDownfactor + 2 * mRebfactor && value <= 3 * mDownfactor + 2 * mRebfactor) {
            float ratio = (value - 2 * mDownfactor - 2 * mRebfactor) / mDownfactor;
            mCurrentDegrees = 360 - ratio * 90;
            mTranslateY = ratio * ratio * maxTranslateY;
//            mTranslateY = getAccelerateInterpolator(ratio)*maxTranslateY ;;

        } else if (value >= 3 * mDownfactor + 2 * mRebfactor && value <= 3 * mDownfactor + 4 * mRebfactor) {
            mCurrentDegrees = 270;

            float ratio = ((value - 3 * mDownfactor - 2 * mRebfactor)) / mRebfactor;

//            if(value>= 3 *mDownfactor + 3 *mRebfactor){
//                ratio = (value - 3 *mDownfactor - 3 *mRebfactor)/mRebfactor;
//                ratio = 1- ratio;
//            }
//            Logger.e("0.6875 - 0.8125   curvalue = " + value + " sec " + ratio);
            mCoordinatey = mSinInterpolator.getInterpolation(ratio) * mMaxCoordinatey;
            mTranslateY = maxTranslateY + mMaxCoordinatey * .3f * mSinInterpolator.getInterpolation(ratio);
        } else if (value >= 3 * mDownfactor + 4 * mRebfactor && value <= 4 * mDownfactor + 4 * mRebfactor) {
            float ratio = (value - 3 * mDownfactor - 4 * mRebfactor) / mDownfactor;
            mCurrentDegrees = 270 + (ratio) * 90;
            mTranslateY = (1 - ratio) * (1 - ratio) * maxTranslateY;
//            mTranslateY = getDecelerateInterpolator(1-ratio) *maxTranslateY  ;


        }
    }

    float mCoordinatey;

    private void drawText(Canvas canvas) {
        canvas.save();
        canvas.translate(0, maxTranslateY + mMedHeight - mTextOffsetY);
        mPath.reset();
        mPath.moveTo((mWidth - mTextWidth) / 2, 0);
        mPath.quadTo(mWidth / 2, mCoordinatey, mWidth / 2 + mTextWidth / 2, 0);
        mPath.close();
        canvas.drawTextOnPath(mMessage, mPath, 0, 0, mTextPaint);
        canvas.restore();
    }

    private void drawMed(Canvas canvas) {
        int count = canvas.save();
        mMatrix.reset();
        float preTranslateY = 0;//mRadianDeep*4f/3f;
        mMatrix.postRotate(mCurrentDegrees, mWidth / 2, preTranslateY + mMedHeight / 2);
        mMatrix.postTranslate(0, mTranslateY);
        canvas.concat(mMatrix);
        mPath.reset();
        int offset = mRadianDeep / 3;
        mPaint.setColor(mDarkColor);
        float left = (mWidth - mMedWidth) / 2f;
        mPath.moveTo(left, mRadianDeep);
        mPath.cubicTo(left, -offset, mMedWidth + left, -offset, left + mMedWidth, mRadianDeep);
        mPath.lineTo(left + mMedWidth, mMedHeight / 2);
        mPath.lineTo(left, mMedHeight / 2);
        mPath.close();
        mMatrixForShader.reset();
        float shaderDegree =0;
        if(mCurrentDegrees>=0 &&mCurrentDegrees <=90){
            shaderDegree =  360-mCurrentDegrees;
        }else{
            shaderDegree = 360-mCurrentDegrees;
        }
        mMatrixForShader.postRotate(shaderDegree,mWidth / 2, preTranslateY + mMedHeight / 2);
        mRadialGradient.setLocalMatrix(mMatrixForShader);
        mPaint.setShader(mRadialGradient);
        canvas.drawPath(mPath, mPaint);
        mPaint.setShader(null);
        mPath.reset();
        mPath.moveTo(left, mMedHeight / 2);
        mPath.lineTo(left + mMedWidth, mMedHeight / 2);
        mPath.lineTo(left + mMedWidth, mMedHeight - mRadianDeep);
        mPath.cubicTo(left + mMedWidth, mMedHeight + offset, left, mMedHeight + offset, left, mMedHeight - mRadianDeep);
        mPath.close();
        mPaint.setColor(mLightColor);
        canvas.drawPath(mPath, mPaint);
        canvas.restoreToCount(count);
    }

    public float getDecelerateInterpolator(float input) {
        float result;
        float mFactor = 4f;
        if (mFactor == 1.0f) {
            result = (float) (1.0f - (1.0f - input) * (1.0f - input));
        } else {
            result = (float) (1.0f - Math.pow((1.0f - input), 2 * mFactor));
        }
        return result;
    }

    public float getAccelerateInterpolator(float input) {
        float mFactor = 4f;
        if (mFactor == 1.0f) {
            return input * input;
        } else {
            return (float) Math.pow(input, 2 * mFactor);
        }
    }

    @Override
    public boolean isRunning() {
        return mValueAnimator.isRunning();
    }

    @Override
    public void draw(Canvas canvas) {
//        mPaint.setShader(mRadialGradient);
//        mPaint.setColor(Color.RED);
//        canvas.drawRect(0,0,mWidth,mHeight,mPaint);
        drawMed(canvas);
        drawText(canvas);

    }

    /**
     * Sets the start angle for rotation.
     *
     * @param fromDegrees starting angle in degrees
     * @attr ref android.R.styleable#RotateDrawable_fromDegrees
     * @see #getFromDegrees()
     */
    public void setFromDegrees(float fromDegrees) {
        if (mFromDegrees != fromDegrees) {
            mFromDegrees = fromDegrees;
            invalidateSelf();
        }
    }

    /**
     * @return starting angle for rotation in degrees
     * @attr ref android.R.styleable#RotateDrawable_fromDegrees
     * @see #setFromDegrees(float)
     */
    public float getFromDegrees() {
        return mFromDegrees;
    }

    /**
     * Sets the end angle for rotation.
     *
     * @param toDegrees ending angle in degrees
     * @attr ref android.R.styleable#RotateDrawable_toDegrees
     * @see #getToDegrees()
     */
    public void setToDegrees(float toDegrees) {
        if (mToDegrees != toDegrees) {
            mToDegrees = toDegrees;
            invalidateSelf();
        }
    }

    public float getToDegrees() {
        return mToDegrees;
    }

    @Override
    protected boolean onLevelChange(int level) {

        return super.onLevelChange(level);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }
}
