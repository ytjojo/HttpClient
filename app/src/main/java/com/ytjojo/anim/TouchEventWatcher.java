package com.ytjojo.anim;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Administrator on 2016/9/26 0026.
 */
public class TouchEventWatcher {
    private static final String TAG = "TouchEventWatcher";
    FrameLayout mParent;
    View mTarget;
    boolean isFollowMove;
    OnScrollListener mOnScrollListener;
    private ScrollerCompat mScroller;
    /**
     * Position of the last motion event.
     */
    private int mLastMotionY;
    private int mLastMotionX;
    private float mOneDrectionDeltaY;
    /**
     * True if the user is currently dragging this ScrollView around. This is
     * not the same as 'is being flinged', which can be checked by
     * mScroller.isFinished() (flinging begins when the user lifts his finger).
     */
    private boolean mIsBeingDragged = false;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;
    private int mActivePointerId = INVALID_POINTER;
    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;
    private TranslationAnim.Direction mTargetGravity;
    private boolean mLastScrollDrectionIsUp;
    private float mOrginalValue;
    TranslationAnim mTranslationAnim;

    public TouchEventWatcher(FrameLayout parent, View target, TranslationAnim.Direction gravity, boolean followMove) {
        this.isFollowMove = followMove;
        this.mParent = parent;
        this.mTarget = target;
        this.mTargetGravity = gravity;
        this.mTranslationAnim = new TranslationAnim(mTarget, gravity, null);
        init(mParent.getContext());
    }

    private void init(Context context) {
        mScroller = ScrollerCompat.create(context, null);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        setupAnimators();
    }

    ValueAnimator mValueAnimator;
    ValueAnimator.AnimatorUpdateListener mUpdateListener;

    private void setupAnimators() {
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if ((value != 0f && value != 1f) && value == mOrginalValue) {
                    return;
                }
                if (mScroller.computeScrollOffset()) {

                    int oldY = getScrollY();
                    int y = mScroller.getCurrY();

                    if (oldY != y) {
                        final int range = getScrollRange();
                        ViewHelper.setTranslationY(mTarget, y);
                    }
                } else {
                    stop();
                }
                mOrginalValue = value;
            }
        };
        mValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        mValueAnimator.setRepeatCount(Animation.INFINITE);
        mValueAnimator.setRepeatMode(Animation.RESTART);
        mValueAnimator.setDuration(1000);
        //fuck you! the default interpolator is AccelerateDecelerateInterpolator
        mValueAnimator.setInterpolator(new LinearInterpolator());
    }

    public void start() {
        mValueAnimator.removeAllUpdateListeners();
        mValueAnimator.addUpdateListener(mUpdateListener);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setDuration(1000);
        mValueAnimator.start();
    }

    public void stop() {
        mValueAnimator.removeUpdateListener(mUpdateListener);
        mValueAnimator.removeAllUpdateListeners();
        mValueAnimator.setRepeatCount(0);
        mValueAnimator.setDuration(0);
        mValueAnimator.end();
    }

    public void onDispatchTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        /*
        * Shortcut the most recurring case: the user is in the dragging
        * state and he is moving his finger.  We want to intercept this
        * motion.
        */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            final int activePointerIndex = MotionEventCompat.findPointerIndex(ev,
                    mActivePointerId);
            if (activePointerIndex == -1) {
                Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                return;
            }
            final int y = (int) MotionEventCompat.getY(ev, activePointerIndex);
            int deltaY = mLastMotionY - y;
            if (deltaY * mOneDrectionDeltaY > 0) {
                mOneDrectionDeltaY = deltaY + mOneDrectionDeltaY;
            } else {
                mOneDrectionDeltaY = deltaY;
            }
            doScroll(deltaY);
            mLastMotionY = y;
            initVelocityTrackerIfNotExists();
            mVelocityTracker.addMovement(ev);
            return;
        }

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                * Locally do absolute value. mLastMotionY is set to the y value
                * of the down event.
                */
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                if (pointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + activePointerId
                            + " in onInterceptTouchEvent");
                    break;
                }

                final int y = (int) MotionEventCompat.getY(ev, pointerIndex);
                final int x = (int) MotionEventCompat.getX(ev, pointerIndex);
                final int yDiff = Math.abs(y - mLastMotionY);
                if (yDiff > mTouchSlop
                        && (Math.abs(x - mLastMotionX) <= y)) {
                    mIsBeingDragged = true;
                    mOneDrectionDeltaY = y - mLastMotionY;
                    mLastMotionY = y;
                    mLastMotionX = x;
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final int y = (int) ev.getY();
//                if(mValueAnimator.isRunning()){
//                    stop();
//                }
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionY = y;
                mLastMotionX = (int) ev.getX();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                mOneDrectionDeltaY = 0;
                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't. mScroller.isFinished should be false when
                 * being flinged. We need to call computeScrollOffset() first so that
                 * isFinished() is correct.
                */
//                mScroller.computeScrollOffset();
//                mIsBeingDragged = !mScroller.isFinished();
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionY = (int) MotionEventCompat.getY(ev, index);
                mLastMotionX = (int) MotionEventCompat.getX(ev,index);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    smoothScrollToFinal();
                }
                mActivePointerId = INVALID_POINTER;
                endDrag();
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) VelocityTrackerCompat.getYVelocity(velocityTracker,
                            mActivePointerId);

//                  if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
//                  }
                    smoothScrollToFinal();
                }
                mActivePointerId = INVALID_POINTER;
                endDrag();
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

    }

    private void smoothScrollToFinal() {
        if ((mOneDrectionDeltaY == 0 && (ViewHelper.getTranslationY(mTarget) == 0 ||ViewHelper.getTranslationY(mTarget) ==getScrollRange())) || !isFollowMove) {
            return;
        }
        if (mOneDrectionDeltaY > 0) {
            startScrollDown();
        } else {
            startScrollUp();
        }
        mOneDrectionDeltaY = 0;
    }

    private void doScroll(int deltaY) {
        if (isFollowMove) {
            final int curScrollY = getScrollY();

            int finalScrollY = curScrollY + deltaY;
            if (mTargetGravity == TranslationAnim.Direction.Bottom) {
                if (finalScrollY <= 0) {
                    finalScrollY = 0;
                    mOneDrectionDeltaY = 0;
                }
                if (finalScrollY >= mTarget.getHeight()) {
                    finalScrollY = mTarget.getHeight();
                    mOneDrectionDeltaY = 0;
                }
            }
            if (mTargetGravity == TranslationAnim.Direction.Top) {
                if (finalScrollY >= 0) {
                    finalScrollY = 0;
                    mOneDrectionDeltaY = 0;
                }
                if (finalScrollY <= -mTarget.getHeight()) {
                    finalScrollY = mTarget.getHeight();
                    mOneDrectionDeltaY = 0;
                }
            }
            ViewHelper.setTranslationY(mTarget, finalScrollY);
        } else {
            if (Math.abs(mOneDrectionDeltaY) > 3 * mTouchSlop) {
                if (mOneDrectionDeltaY > 0) {
                    mOneDrectionDeltaY = 0;
                    startScrollDown();
                } else {
                    mOneDrectionDeltaY = 0;
                    startScrollUp();
                }
            }
        }
    }

    private void startScrollUp() {
        if (mTargetGravity == TranslationAnim.Direction.Top) {

            mTranslationAnim.animHide();
        } else {
            mTranslationAnim.animShow();
        }
    }

    private void startScrollDown() {
        if (mTargetGravity == TranslationAnim.Direction.Top) {
            mTranslationAnim.animShow();
        } else {
            mTranslationAnim.animHide();
        }
    }

    private int getScrollY() {
        return (int) (ViewHelper.getTranslationY(mTarget));
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >>
                MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = (int) MotionEventCompat.getY(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        mIsBeingDragged = false;
        mOneDrectionDeltaY = 0;
        recycleVelocityTracker();
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    public int getScrollRange() {
        if (mTargetGravity == TranslationAnim.Direction.Bottom) {
            return mTarget.getHeight();
        } else {
            return -mTarget.getHeight();
        }
    }

    public interface OnScrollListener {
        void onTriger(boolean isScrollUp);

        void onScroll(float dy);
    }
}
