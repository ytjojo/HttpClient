package com.ytjojo.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.OverScroller;
import android.widget.ScrollView;

import com.orhanobut.logger.Logger;
import com.ytjojo.utils.CollectionUtils;

import java.util.ArrayList;


public class StickyNavLayout extends ViewGroup {

    private static String TAG = "TAG";

    /**
     * 最顶部的View
     */
    private View mTopView;
    /**
     * 导航的View
     */
    private ViewPager mViewPager;

    private int mVerticalRange;
    //    private ScrollView mInnerScrollView;
    private boolean isTopHidden = false;

    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMaximumVelocity, mMinimumVelocity;

    private float mLastMotionY;
    private float mLastMotionX;
    // Down时纪录的Y坐标
    private float mFirstMotionY;
    private float mFirstMotionX;
    // 是否是下拉
    private boolean isDownSlide;

    private boolean mDragging;

    private View mScrollableView;
    private View mContentView;
    private boolean mHasSendCancelEvent;
    private MotionEvent mLastMoveEvent;
    private boolean mPreventForHorizontal;
    private int mContentMarginTop;
    private int mMaxTopTranslationY ;

    public StickyNavLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new OverScroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();

        mVelocityTracker = VelocityTracker.obtain();

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() >= 2) {
            mTopView = getChildAt(0);
            mContentView = getChildAt(1);
            mContentView.setBackgroundColor(Color.WHITE);
            findViewPagerAndScrollView((ViewGroup) mContentView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() < 2) {
            return;
        }
        if (mContentView == null) {
            mTopView = getChildAt(0);
            mContentView = getChildAt(1);
            findViewPagerAndScrollView((ViewGroup) mContentView);
        }
        if(mViewPager !=null){
            findPagerInitScrollView();
        }
        // 这是为了设置ViewPager的高度，保证TopView消失之后，能够正好和NavView填充整个屏幕
        measureChildWithMargins(mTopView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        mMaxTopTranslationY = mTopView.getMeasuredHeight()/2;
        int heghtSize = MeasureSpec.getSize(heightMeasureSpec) -mContentMarginTop;
        int childHeightSpec = MeasureSpec.makeMeasureSpec(heghtSize, MeasureSpec.EXACTLY);
        measureChild(mContentView, widthMeasureSpec, childHeightSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() < 2) {
            return;
        }
        layoutVerticalWithMargin(mTopView, mContentView);
    }

    private void layoutVerticalWithMargin(View... childs) {
        int paddingLeft = getPaddingLeft(), paddingTop = getPaddingTop();
        if (childs != null) {
            int curTop = paddingTop;
            for (View child : childs) {
                if (child.getVisibility() == VISIBLE) {
                    MarginLayoutParams curParams = (MarginLayoutParams) child.getLayoutParams();
                    curTop += curParams.topMargin;
                    child.layout(paddingLeft, curTop, paddingLeft + child.getMeasuredWidth(), curTop + child.getMeasuredHeight());
                    curTop += curParams.bottomMargin + child.getMeasuredHeight();
                }
            }
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mVerticalRange = mTopView.getMeasuredHeight() - mContentMarginTop;
    }

    public boolean reachChildTop() {
        if (mScrollableView == null) {
            return true;
        }
        return !ViewCompat.canScrollVertically(mScrollableView, -1);
    }

    public boolean reachChildBottom() {
        if (mScrollableView == null) {
            return true;
        }
        return !ViewCompat.canScrollVertically(mScrollableView, 1);
    }

    private void sendCancelEvent() {
        // The ScrollChecker will update position and lead to send cancel event when mLastMoveEvent is null.
        // fix #104, #80, #92
        if (mLastMoveEvent == null) {
            return;
        }
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEventSupper(e);
    }

    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }


    private MotionEvent createCancel() {
        final long time = SystemClock.uptimeMillis();
        final MotionEvent ev = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, 0, mLastMotionY, 0);
        ev.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        return ev;
    }

    private boolean inChild(View child, int x, int y) {
        if (getChildCount() > 0) {
            final int scrollY = getScrollY();
            final int scrollX = getScrollX();
            return !(y < child.getTop() - scrollY
                    || y >= child.getBottom() - scrollY
                    || x < child.getLeft() - scrollX
                    || x >= child.getRight() - scrollX);
        }
        return false;
    }

    private final static int INVALID_ID = -1;
    private int mActivePointerId = INVALID_ID;
    private int mSecondaryPointerId = INVALID_ID;
    private float mPrimaryLastX = -1;
    private float mPrimaryLastY = -1;
    private float mSecondaryLastX = -1;
    private float mSecondaryLastY = -1;
    private ParentState mParentState = ParentState.EXPANDED;
    private DragState mDragState;

    public enum DragState {
        DRAGGING_NODIRECT,
        DRAGGING_DONOTHING,
        DRAGGING_TO_EXPANDED,
        DRAGGING_TO_COLLAPSED
    }

    public enum ParentState {
        EXPANDED,
        COLLAPSED,
        SCROLL
    }

    private ParentState getParnetState() {
        final float scrollY = getScrollY();
        if (scrollY == 0) {
            return ParentState.EXPANDED;
        } else if (scrollY == mVerticalRange) {
            return ParentState.COLLAPSED;
        } else {
            return ParentState.SCROLL;
        }
    }

    boolean isChanged;
    boolean isVerticalScroll = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return isVerticalScroll;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled() || mContentView == null || mTopView == null) {
            return dispatchTouchEventSupper(event);
        }

        int action = MotionEventCompat.getActionMasked(event);
        if (action != MotionEvent.ACTION_DOWN && !isVerticalScroll) {
            return dispatchTouchEventSupper(event);
        }
        boolean isHandlar = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished())
                    mScroller.abortAnimation();
                mVelocityTracker.clear();
                mLastMotionY = event.getY();
                mLastMotionX = event.getX();
                mFirstMotionY = event.getY();
                mFirstMotionX = event.getX();
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                mPrimaryLastY = event.getY();
                mParentState = getParnetState();
                dispatchTouchEventSupper(event);
                isChanged = false;
                isHandlar = true;
                isVerticalScroll = true;
                break;
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = event;
                int activePointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
                if (activePointerIndex == -1) {
                    isHandlar = dispatchTouchEventSupper(event);
                    break;
                }
                float x = MotionEventCompat.getX(event, activePointerIndex);
                float y = MotionEventCompat.getY(event, activePointerIndex);
                if (!mDragging) {
                    float dy = y - mFirstMotionY;
                    float dx = x - mFirstMotionX;
                    if (Math.abs(dy) > mTouchSlop) {
                        if (Math.abs(dy) > Math.abs(dx)) {
                            mDragging = true;
                            isHandlar = doScrollAndCallSupper(event, dy);
                            mLastMotionY = y;
                            mLastMotionX = x;
                            break;
                        } else {
                            mDragging = false;
                            isVerticalScroll = false;
                            isHandlar = dispatchTouchEventSupper(event);
                            break;
                        }

                    }else{
                        dispatchTouchEventSupper(event);
                        isHandlar = true;
                        mLastMotionY = y;
                        mLastMotionX = x;
                        break;
                    }


                }
                float dy = y - mLastMotionY;
                isHandlar = doScrollAndCallSupper(event, dy);
                ParentState curState = getParnetState();
                if (mParentState != curState && curState != ParentState.SCROLL) {
                    mParentState = curState;
                    isChanged = false;
                }
                mLastMotionY = y;
                mLastMotionX = x;
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(event);
                mLastMotionY = MotionEventCompat.getY(event, index);
                mLastMotionX = MotionEventCompat.getX(event,index);
                mActivePointerId = MotionEventCompat.getPointerId(event, index);
                mFirstMotionY = mLastMotionY;
                mFirstMotionX = mLastMotionX;
                isHandlar = dispatchTouchEventSupper(event);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
//                mLastMotionY = (int) MotionEventCompat.getY(event,
//                        MotionEventCompat.findPointerIndex(event, mActivePointerId));
                isHandlar = dispatchTouchEventSupper(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                isVerticalScroll = true;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                isHandlar = dispatchTouchEventSupper(event);
                reset();
                break;
            case MotionEvent.ACTION_UP:
                isVerticalScroll = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityY = (int) VelocityTrackerCompat.getYVelocity(mVelocityTracker, mActivePointerId);
                if (mDragging && getParnetState() == ParentState.SCROLL) {
                    mDragging = false;
                    isHandlar = true;
                    isFlingToNestScroll = false;
                    sendCancelEvent();
                    // 手指离开之后，根据加速度进行滑动
                    if (Math.abs(velocityY) > mMinimumVelocity) {
                        fling(velocityY);

                    } else {
                        int currentY = getScrollY();
                        // 下拉
                        isDownSlide = (event.getY() - mFirstMotionY) > 0;
                        if (isDownSlide) {
                            if (currentY < mVerticalRange) {
                                mScroller.startScroll(0, currentY, 0, -currentY);
                                ViewCompat.postInvalidateOnAnimation(this);
                            }
                        } else {
                            if (currentY > 0) {
                                mScroller.startScroll(0, currentY, 0, mVerticalRange
                                        - currentY);
                                ViewCompat.postInvalidateOnAnimation(this);
                            }
                        }
                    }

                } else {
                    if (Math.abs(velocityY) >= mMinimumVelocity) {
                        flingToNestScroll(velocityY);
                    }
                    if(mDragging &&Math.abs(velocityY) < mMinimumVelocity ){
                        sendCancelEvent();
                    }else{
                        isHandlar = dispatchTouchEventSupper(event);
                    }
                }
                reset();
                break;
        }
        mVelocityTracker.addMovement(event);
        return isHandlar;
    }

    private boolean doScrollAndCallSupper(MotionEvent event, float dy) {
        int activePointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
        float curX = MotionEventCompat.getX(event,activePointerIndex);
        float curY = MotionEventCompat.getY(event,activePointerIndex);
//        event.offsetLocation(mLastMotionX - curX ,0);
        event.setLocation(mFirstMotionX,curY);
        if (mParentState == ParentState.EXPANDED) {
            if (dy > 0) {
                mDragState = DragState.DRAGGING_DONOTHING;
            } else {
                isChanged = true;
                mDragState = DragState.DRAGGING_TO_COLLAPSED;
            }
        } else if (mParentState == ParentState.COLLAPSED) {
            if (dy > 0 && reachChildTop()) {
                isChanged = true;
                mDragState = DragState.DRAGGING_TO_EXPANDED;
            } else {
                mDragState = DragState.DRAGGING_DONOTHING;
            }
        } else {
            isChanged = true;
            mDragState = DragState.DRAGGING_NODIRECT;
        }
        if (isChanged) {
            scrollBy(0, (int) -dy);
            return true;
        }
        switch (mDragState) {
            case DRAGGING_NODIRECT:
            case DRAGGING_TO_COLLAPSED:
            case DRAGGING_TO_EXPANDED:
                scrollBy(0, (int) -dy);
                return true;
            case DRAGGING_DONOTHING:
                return dispatchTouchEventSupper(event);

        }
        return dispatchTouchEventSupper(event);
    }

    private void reset() {
        mVelocityTracker.clear();
        mActivePointerId = INVALID_ID;
        mDragging = false;
        isChanged = false;
        mLastMoveEvent = null;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >>
                MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
        int index = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        Log.e(TAG, pointerIndex + "pointerIndex" + pointerId + " = id    up" + index + "count =" + MotionEventCompat.getPointerCount(ev));
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

    private boolean isScrollView(View child) {
        if (child instanceof android.support.v4.view.NestedScrollingChild || child instanceof AbsListView || child instanceof ScrollView || child instanceof ViewPager || child instanceof WebView || child instanceof RecyclerView) {
            mScrollableView = child;
            return true;
        }
        return false;
    }

    /**
     * Find out the scrollable child view from a ViewGroup.
     *
     * @param viewGroup
     */
    public void findScrollView(ViewGroup viewGroup) {
        if(viewGroup ==null){
            return;
        }
        if (isScrollView(viewGroup)) {
            return;
        }
        if (viewGroup.getChildCount() > 0) {
            int count = viewGroup.getChildCount();
            View child;
            for (int i = 0; i < count; i++) {
                child = viewGroup.getChildAt(i);
                if (isScrollView(child)) {
                    return;
                } else if (child instanceof ViewGroup) {
                    findScrollView((ViewGroup) child);
                }
            }
        }
    }

    private void findPagerInitScrollView(){

        final PagerAdapter a = mViewPager.getAdapter();
        int currentItem = mViewPager.getCurrentItem();
        if (a == null||mScrollableView !=null) {
            return;
        }
        if (a instanceof FragmentPagerAdapter) {
            FragmentPagerAdapter fadapter = (FragmentPagerAdapter) a;
            Fragment item = fadapter.getItem(currentItem);
            findScrollView((ViewGroup) item.getView());
        } else if (a instanceof FragmentStatePagerAdapter) {
            FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
            Fragment item = fsAdapter.getItem(currentItem);
            findScrollView((ViewGroup) item.getView());
        } else if (a instanceof CurrentPagerAdapter) {
            final CurrentPagerAdapter adapter = (CurrentPagerAdapter) a;
            if (adapter.getPrimaryItem() != null) {
                findScrollView((ViewGroup) adapter.getPrimaryItem());
            } else {
                mViewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        findScrollView((ViewGroup) adapter.getPrimaryItem());
                    }
                });
            }
        }
    }

    private boolean isViewPager(View viewGroup) {
        if (viewGroup instanceof ViewPager) {
            mViewPager = (ViewPager) viewGroup;
            final PagerAdapter a = mViewPager.getAdapter();
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);

            return true;
        }
        return false;
    }

    private void findViewPagerAndScrollView(ViewGroup viewGroup) {
        if (isViewPager(viewGroup)) {
        } else if (isScrollView(viewGroup)) {

        } else {
            if (viewGroup.getChildCount() > 0) {
                int count = viewGroup.getChildCount();
                View child;
                for (int i = 0; i < count; i++) {
                    child = viewGroup.getChildAt(i);
                    if (isViewPager( child)) {

                    } else if (isScrollView(child)) {

                    } else if (child instanceof ViewGroup) {
                        findViewPagerAndScrollView((ViewGroup) child);
                    }
                }
            }
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        final int offset = getScrollY();
        if (direction > 0) {
            return offset < mVerticalRange;
        } else {
            return offset > 0;
        }
    }

    public void fling(int velocityY) {
        isFlingToNestScroll = false;
        if (velocityY > 0) {
            mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, 0, 0);
        } else {
            mScroller.fling(0, getScrollY(), 0, -velocityY, 0, 0, mVerticalRange, mVerticalRange);

        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    boolean isFlingToNestScroll;

    private void flingToNestScroll(int velocityY) {
        // For reasons I do not understand, scrolling is less janky when maxY=Integer.MAX_VALUE
        // then when maxY is set to an actual value.
        if (velocityY > 0) {
            isFlingToNestScroll = true;
            mScroller.fling(0, mContentView.getHeight() + getScrollY(), 0, -velocityY, 0, 0, 0, 0);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }


    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mVerticalRange) {
            y = mVerticalRange;
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);

            float offsetRatio = ((float) getScrollY()) / mVerticalRange;
            if(mOnScollListener !=null){
                if(offsetRatio ==0){
                    mOnScollListener.onStateChanged(OnScollListener.STATE_EXPAND);

                }else if(offsetRatio ==1){
                    mOnScollListener.onStateChanged(OnScollListener.STATE_EXPAND);
                }
                mOnScollListener.onScroll(offsetRatio,getScrollY(),mVerticalRange);
            }
            int totalOffset = mMaxTopTranslationY;
            float verticalOffset = totalOffset * offsetRatio;
            ViewCompat.setTranslationY(mTopView, (int) verticalOffset);
        }
        isTopHidden = getScrollY() == mVerticalRange;

    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int curY = mScroller.getCurrY();
            int startY = mScroller.getStartY();
            int finalY = mScroller.getFinalY();
            float velocityY = mScroller.getCurrVelocity();
            if (isFlingToNestScroll) {
//                Logger.e( reachChildTop() + "cury" + curY +"velocityY = "+velocityY + "  startY=" + startY +" finalY= " +finalY);
                if (reachChildTop()) {
                    if (Math.abs(velocityY) >= mMinimumVelocity) {
                        mScroller.abortAnimation();
                        fling((int) velocityY);
//                        int currentY =getScrollY();
//                        mScroller.startScroll(0, currentY, 0, -currentY);
                    }
                }
            } else {
                scrollTo(0, curY);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p.width, p.height);
    }
    public OnScollListener mOnScollListener;
    public void setOnScollListener(OnScollListener l){
        this.mOnScollListener = l;
    }
    public  interface OnScollListener{
        public final int STATE_EXPAND = 1;
        public final int STATE_COLLAPSED = -1;
        public final int STATE_DRAGING =0;
        void onScroll(float positionOffset, int positionOffsetPixels,int offsetRange);
        void onStateChanged(int state);
        int getContentCollapsedMarginTop(View top);
        int getMaxTopViewTranslationY(View top,int verticalRange);
    }
    /**
     * Interpolator that enforces a specific starting velocity. This is useful to avoid a
     * discontinuity between dragging speed and flinging speed.
     * <p>
     * Similar to a {@link android.view.animation.AccelerateInterpolator} in the sense that
     * getInterpolation() is a quadratic function.
     */
    private static class AcceleratingFlingInterpolator implements Interpolator {

        private final float startingSpeedPixelsPerFrame;
        private final float durationMs;
        private final int pixelsDelta;
        private final float numberFrames;

        public AcceleratingFlingInterpolator(int durationMs, float startingSpeedPixelsPerSecond,
                                             int pixelsDelta) {
            startingSpeedPixelsPerFrame = startingSpeedPixelsPerSecond / getRefreshRate();
            this.durationMs = durationMs;
            this.pixelsDelta = pixelsDelta;
            numberFrames = this.durationMs / getFrameIntervalMs();
        }

        @Override
        public float getInterpolation(float input) {
            final float animationIntervalNumber = numberFrames * input;
            final float linearDelta = (animationIntervalNumber * startingSpeedPixelsPerFrame)
                    / pixelsDelta;
            // Add the results of a linear interpolator (with the initial speed) with the
            // results of a AccelerateInterpolator.
            if (startingSpeedPixelsPerFrame > 0) {
                return Math.min(input * input + linearDelta, 1);
            } else {
                // Initial fling was in the wrong direction, make sure that the quadratic component
                // grows faster in order to make up for this.
                return Math.min(input * (input - linearDelta) + linearDelta, 1);
            }
        }

        private float getRefreshRate() {
            // TODO
//            DisplayInfo di = DisplayManagerGlobal.getInstance().getDisplayInfo(
//                    Display.DEFAULT_DISPLAY);
//            return di.refreshRate;
            return 30f;
        }

        public long getFrameIntervalMs() {
            return (long) (1000 / getRefreshRate());
        }
    }

    /**
     * Interpolator from android.support.v4.view.ViewPager. Snappier and more elastic feeling
     * than the default interpolator.
     */
    private static final Interpolator INTERPOLATOR = new Interpolator() {

        /**
         * {@inheritDoc}
         */
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    public static class CurrentPagerAdapter extends PagerAdapter {

        ArrayList<View> mViews;
        ArrayList<String> mTitles;
        View mCurrentView;

        public CurrentPagerAdapter(ArrayList<View> views, ArrayList<String> titles) {
            this.mViews = views;
            this.mTitles = titles;
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            ((ViewPager) container).removeView(mViews.get(position));
        }

        //每次滑动的时候生成的组件
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if(mViews.get(position).getParent() ==container){
                container.removeView(mViews.get(position));
            }
            ((ViewPager) container).addView(mViews.get(position));
            return mViews.get(position);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Logger.e(object+"");
            mCurrentView = (View) object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (CollectionUtils.isEmpty(mTitles)) {
                return super.getPageTitle(position);
            }
            return mTitles.get(position);
        }

        public View getPrimaryItem() {
            return mCurrentView;
        }
        public View getCurentView(int postion){
            return mViews.get(postion);
        }
    }

    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mScrollableView = null;
            final PagerAdapter a = mViewPager.getAdapter();
            if (a instanceof FragmentPagerAdapter) {
                FragmentPagerAdapter fadapter = (FragmentPagerAdapter) a;
                Fragment item = fadapter.getItem(position);
                findScrollView((ViewGroup) item.getView());
            } else if (a instanceof FragmentStatePagerAdapter) {
                FragmentStatePagerAdapter fsAdapter = (FragmentStatePagerAdapter) a;
                Fragment item = fsAdapter.getItem(position);
                findScrollView((ViewGroup) item.getView());
            } else if (a instanceof CurrentPagerAdapter) {
                CurrentPagerAdapter currentPagerAdapter = (CurrentPagerAdapter)a;
                findScrollView((ViewGroup)currentPagerAdapter.getCurentView(position));
                Log.e(TAG,currentPagerAdapter.getPrimaryItem()+ "             onPageSelected" + mScrollableView );
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }


    };
}
