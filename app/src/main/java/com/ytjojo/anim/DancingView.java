package com.ytjojo.anim;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;
import com.ytjojo.practice.R;

public class DancingView extends SurfaceView implements SurfaceHolder.Callback {
 
    public static final int STATE_DOWN = 1;//向下状态
    public static final int STATE_UP = 2;//向上状态
 
    public static final int DEFAULT_POINT_RADIUS = 10;
    public static final int DEFAULT_BALL_RADIUS = 13;
    public static final int DEFAULT_LINE_WIDTH = 200;
    public static final int DEFAULT_LINE_HEIGHT = 2;
    public static final int DEFAULT_LINE_COLOR = Color.parseColor("#FF9800");
    public static final int DEFAULT_POINT_COLOR = Color.parseColor("#9C27B0");
    public static final int DEFAULT_BALL_COLOR = Color.parseColor("#FF4081");
 
    public static final int DEFAULT_DOWN_DURATION = 600;//ms
    public static final int DEFAULT_UP_DURATION = 600;//ms
    public static final int DEFAULT_FREEDOWN_DURATION = 1000;//ms
 
    public static final int MAX_OFFSET_Y = 50;//水平下降最大偏移距离
 
 
    public int PONIT_RADIUS = DEFAULT_POINT_RADIUS;//小球半径
    public int BALL_RADIUS = DEFAULT_BALL_RADIUS;//小球半径
 
    private Paint mPaint;
    private Path mPath;
    private int mLineColor;
    private int mPonitColor;
    private int mBallColor;
    private int mLineWidth;
    private int mLineHeight;
 
    private float mDownDistance;
    private float mUpDistance;
    private float freeBallDistance;
 
    private ValueAnimator mDownController;//下落控制器
    private ValueAnimator mUpController;//上弹控制器
    private ValueAnimator mFreeDownController;//自由落体控制器
 
    private AnimatorSet animatorSet;
    private int state;
 
    private boolean ismUpControllerDied = false;
    private boolean isAnimationShowing = false;
    private boolean isBounced = false;
    private boolean isBallFreeUp = false;
 
    public DancingView(Context context) {
        super(context);
        init(context, null);
    }
 
    public DancingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
 
 
    public DancingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
 
 
    private void init(Context context, AttributeSet attrs) {
        initAttributes(context, attrs);
 
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mLineHeight);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
 
        mPath = new Path();
        getHolder().addCallback(this);
 
        initController();
    }
 
    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.DancingView);
        mLineColor = typeArray.getColor(R.styleable.DancingView_lineColor, DEFAULT_LINE_COLOR);
        mLineWidth = typeArray.getDimensionPixelOffset(R.styleable.DancingView_lineWidth, DEFAULT_LINE_WIDTH);
        mLineHeight = typeArray.getDimensionPixelOffset(R.styleable.DancingView_lineHeight, DEFAULT_LINE_HEIGHT);
        mPonitColor = typeArray.getColor(R.styleable.DancingView_pointColor, DEFAULT_POINT_COLOR);
        mBallColor = typeArray.getColor(R.styleable.DancingView_ballColor, DEFAULT_BALL_COLOR);
        typeArray.recycle();
    }
 
 
    private void initController() {
        mDownController = ValueAnimator.ofFloat(0, 1);
        mDownController.setDuration(DEFAULT_DOWN_DURATION);
        mDownController.setInterpolator(new DecelerateInterpolator());
        mDownController.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDownDistance = MAX_OFFSET_Y * (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mDownController.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                state = STATE_DOWN;
            }
 
            @Override
            public void onAnimationEnd(Animator animation) {
            }
 
            @Override
            public void onAnimationCancel(Animator animation) {
            }
 
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
 
        mUpController = ValueAnimator.ofFloat(0, 1);
        mUpController.setDuration(DEFAULT_UP_DURATION);
        mUpController.setInterpolator(new DancingInterpolator());
        mUpController.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mUpDistance = MAX_OFFSET_Y * (float) animation.getAnimatedValue();
                if (mUpDistance >= MAX_OFFSET_Y) {
                    //进入自由落体状态
                    isBounced = true;
                    if (!mFreeDownController.isRunning() && !mFreeDownController.isStarted() && !isBallFreeUp) {
                        mFreeDownController.start();
                    }
                }
                postInvalidate();
            }
        });
        mUpController.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                state = STATE_UP;
            }
 
            @Override
            public void onAnimationEnd(Animator animation) {
                ismUpControllerDied = true;
            }
 
            @Override
            public void onAnimationCancel(Animator animation) {
            }
 
            @Override
            public void onAnimationRepeat(Animator animation) {
 
            }
        });
 
        mFreeDownController = ValueAnimator.ofFloat(0, 8f);
        mFreeDownController.setDuration(DEFAULT_FREEDOWN_DURATION);
        mFreeDownController.setInterpolator(new DecelerateInterpolator());
        mFreeDownController.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //该公式解决上升减速 和 下降加速
                float t = (float) animation.getAnimatedValue();
                freeBallDistance = 40 * t - 5 * t * t;
 
                if (ismUpControllerDied) {//往上抛,到临界点
                    postInvalidate();
                }
            }
        });
        mFreeDownController.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isBallFreeUp = true;
            }
 
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimationShowing = false;
                //循环第二次
                startAnimations();
            }
 
            @Override
            public void onAnimationCancel(Animator animation) {
            }
 
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
 
        animatorSet = new AnimatorSet();
        animatorSet.play(mDownController).before(mUpController);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimationShowing = true;
            }
 
            @Override
            public void onAnimationEnd(Animator animation) {
            }
 
            @Override
            public void onAnimationCancel(Animator animation) {
            }
 
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }
 
    /**
     * 启动动画,外部调用
     */
    public void startAnimations() {
        if (isAnimationShowing) {
            return;
        }
        if (animatorSet.isRunning()) {
            animatorSet.end();
            animatorSet.cancel();
        }
        isBounced = false;
        isBallFreeUp = false;
        ismUpControllerDied = false;
 
        animatorSet.start();
    }
 
 
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
 
 
        // 一条绳子用左右两部分的二阶贝塞尔曲线组成
        mPaint.setColor(mLineColor);
        mPath.reset();
        //起始点
        mPath.moveTo(getWidth() / 2 - mLineWidth / 2, getHeight() / 2);
 
        if (state == STATE_DOWN) {//下落
            /**************绘制绳子开始*************/
            //左部分 的贝塞尔
            mPath.quadTo((float) (getWidth() / 2 - mLineWidth / 2 + mLineWidth * 0.375), getHeight() / 2 + mDownDistance,
                    getWidth() / 2, getHeight() / 2 + mDownDistance);
            //右部分 的贝塞尔
            mPath.quadTo((float) (getWidth() / 2 + mLineWidth / 2 - mLineWidth * 0.375), getHeight() / 2 + mDownDistance,
                    getWidth() / 2 + mLineWidth / 2, getHeight() / 2);
 
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);
            /**************绘制绳子结束*************/
 
 
            /**************绘制弹跳小球开始*************/
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mBallColor);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2 + mDownDistance - BALL_RADIUS, BALL_RADIUS, mPaint);
            /**************绘制弹跳小球结束*************/
        } else if (state == STATE_UP) { //向上弹
            /**************绘制绳子开始*************/
            //左部分的贝塞尔
            mPath.quadTo((float) (getWidth() / 2 - mLineWidth / 2 + mLineWidth * 0.375), getHeight() / 2 + 50 - mUpDistance,
                    getWidth() / 2,
                    getHeight() / 2 + (50 - mUpDistance));
 
            //右部分的贝塞尔
            mPath.quadTo((float) (getWidth() / 2 + mLineWidth / 2 - mLineWidth * 0.375), getHeight() / 2 + 50 - mUpDistance,
                    getWidth() / 2 + mLineWidth / 2,
                    getHeight() / 2);
 
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);
            /**************绘制绳子结束*************/
 
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mBallColor);
 
            //弹性小球,自由落体
            if (!isBounced) {
                //上升
                canvas.drawCircle(getWidth() / 2, getHeight() / 2 + (MAX_OFFSET_Y - mUpDistance) - BALL_RADIUS, BALL_RADIUS, mPaint);
            } else {
                //自由落体
                canvas.drawCircle(getWidth() / 2, getHeight() / 2 - freeBallDistance - BALL_RADIUS, BALL_RADIUS, mPaint);
            }
        }
        mPaint.setColor(mPonitColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(getWidth() / 2 - mLineWidth / 2, getHeight() / 2, PONIT_RADIUS, mPaint);
        canvas.drawCircle(getWidth() / 2 + mLineWidth / 2, getHeight() / 2, PONIT_RADIUS, mPaint);
    }
 
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();//锁定整个SurfaceView对象,获取该Surface上的Canvas.
        draw(canvas);
        holder.unlockCanvasAndPost(canvas);//释放画布，提交修改
    }
 
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
 
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}