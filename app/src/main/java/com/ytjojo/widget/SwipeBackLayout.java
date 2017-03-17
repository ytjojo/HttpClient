/*
 * Copyright 2015 Eric Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ytjojo.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import com.orhanobut.logger.Logger;
import com.ytjojo.practice.R;
import com.ytjojo.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Swipe or Pull to finish a Activity.
 * <p>
 * This layout must be a root layout and contains only one direct child view.
 * <p>
 * The activity must use a theme that with translucent style.
 * <style name="Theme.Swipe.Back" parent="AppTheme">
 * <item name="android:windowIsTranslucent">true</item>
 * <item name="android:windowBackground">@android:color/transparent</item>
 * </style>
 * <p>
 * Created by Eric on 15/1/8.
 */
public class SwipeBackLayout extends FrameLayout {

    private static final String TAG = "SwipeBackLayout";

    public enum DragEdge {
        LEFT,

        TOP,

        RIGHT,

        BOTTOM
    }

    public static LinkedList<SwipeBackLayout> mSwipeBackLayouts;

    private DragEdge dragEdge = DragEdge.LEFT;

    public void setDragEdge(DragEdge dragEdge) {
        this.dragEdge = dragEdge;
    }


    private static final double AUTO_FINISHED_SPEED_LIMIT = 2000.0;

    private final ViewDragHelper viewDragHelper;

    private View target;

    private View scrollChild;

    private int verticalDragRange = 0;

    private int horizontalDragRange = 0;

    private int draggingState = 0;

    private int draggingOffset;

    /**
     * Whether allow to pull this layout.
     */
    private boolean enablePullToBack = true;

    public static final float BACK_FACTOR = 0.3f;

    /**
     * the anchor of calling finish.
     */
    private float finishAnchor = 0;

    /**
     * Set the anchor of calling finish.
     *
     * @param offset
     */
    public void setFinishAnchor(float offset) {
        finishAnchor = offset;
    }

    private boolean enableFlingBack = true;

    private ArrayList<View> mIgnoreSwipeViews;

    /**
     * Whether allow to finish activity by fling the layout.
     *
     * @param b
     */
    public void setEnableFlingBack(boolean b) {
        enableFlingBack = b;
    }


    public void addOnSwipeBackListener(SwipeBackListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<SwipeBackListener>();
        }
        mListeners.add(0, listener);
    }

    public void removeSwipeListener(SwipeBackListener listener) {
        if (mListeners == null) {
            return;
        }
        mListeners.remove(listener);
    }

    ArrayList<SwipeBackListener> mListeners;

    public SwipeBackLayout(Context context) {
        this(context, null);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (mSwipeBackLayouts == null)
            mSwipeBackLayouts = new LinkedList<>();
        mSwipeBackLayouts.add(this);

        viewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelperCallBack());
    }

    public static SwipeBackLayout build(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        activity.getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
        activity.getWindow().getAttributes().format = PixelFormat.TRANSLUCENT;
        if (viewGroup.getChildCount() > 0)
            viewGroup.getChildAt(0).setBackgroundColor(Color.TRANSPARENT);
        return (SwipeBackLayout) LayoutInflater.from(activity).inflate(R.layout.swipback_fragment_container, null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mSwipeBackLayouts.remove(this);
    }

    public void setScrollChild(View view) {
        scrollChild = view;
    }

    public void setEnablePullToBack(boolean b) {
        enablePullToBack = b;
    }

    public void addIgnoreSwipeView(View v) {
        if (mIgnoreSwipeViews == null) {
            mIgnoreSwipeViews = new ArrayList<>();
        }
        if (v.getParent() == this) {
            mIgnoreSwipeViews.add(v);
        }
    }

    public boolean isIgnoreView(View child) {
        if (CollectionUtils.isEmpty(mIgnoreSwipeViews)) {
            return false;
        }
        return mIgnoreSwipeViews.contains(child);
    }

    private void ensureTarget(MotionEvent event) {
        target = null;
        scrollChild = null;
        if (target == null || (target != null && target.getParent() != this)) {
            if (getChildCount() <= 0) {
                return;
            }
            final View topChild = getChildAt(getChildCount() - 1);
            if (isIgnoreView(topChild)) {
                return;
            }
            target = topChild;
            if (target != null) {
                if (target instanceof ViewGroup) {
                    findScrollView((ViewGroup) target, event);
                } else {
                    scrollChild = target;
                }

            }
        }
    }


    /**
     * Find out the scrollable child view from a ViewGroup.
     *
     * @param viewGroup
     */
    private void findScrollView(ViewGroup viewGroup, MotionEvent ev) {
        scrollChild = viewGroup;
        int rawX = (int) ev.getRawX();
        int rawY = (int) ev.getRawY();
        int[] locations = new int[2];
        Rect rect = new Rect();
        if (viewGroup.getChildCount() > 0) {
            int count = viewGroup.getChildCount();
            View child;
            for (int i = count - 1; i >= 0; i--) {
                child = viewGroup.getChildAt(i);
                child.getLocationOnScreen(locations);
                rect.left = locations[0];
                rect.top = locations[1];
                rect.right = rect.left + child.getWidth();
                rect.bottom = rect.top + child.getHeight();
                if (rect.contains(rawX, rawY)) {
                    if (child instanceof ViewGroup) {
                        findScrollChild((ViewGroup) child);
                    }
                    return;
                }
            }
        }
    }

    private void findScrollChild(ViewGroup viewGroup) {
        if (isScollChild(viewGroup)) {
            return;
        } else if (!shouldFindChild(viewGroup)) {
            return;
        }
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                if (!shouldFindChild((ViewGroup) child)) {
                    if (isScollChild((ViewGroup) child)) {
                        return;
                    }
                } else {
                    ViewGroup group = (ViewGroup) child;
                    findScrollChild(group);
                }
            }
        }
    }

    private boolean shouldFindChild(ViewGroup child) {
        if (child instanceof ViewPager || child instanceof HorizontalScrollView || child instanceof RecyclerView || child instanceof ScrollView || child instanceof AbsListView || child instanceof NestedScrollView || child instanceof WebView || child instanceof NestedScrollingParent || child instanceof NestedScrollingChild) {
            return false;
        }
        return true;
    }

    private boolean isScollChild(ViewGroup child) {
        if (dragEdge == DragEdge.BOTTOM || dragEdge == DragEdge.TOP) {
            if (child instanceof RecyclerView || child instanceof ScrollView || child instanceof AbsListView || child instanceof NestedScrollView || child instanceof WebView || child instanceof NestedScrollingParent || child instanceof NestedScrollingChild) {
                if (scrollChild instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) child;
                    if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (manager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                            return false;
                        }
                    }
                }
                scrollChild = child;
                return true;
            }
        } else {
            if (child instanceof RecyclerView || child instanceof ViewPager || child instanceof WebView || child instanceof HorizontalScrollView) {
                if (scrollChild instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) child;
                    if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (manager.getOrientation() == LinearLayoutManager.VERTICAL) {
                            return false;
                        }
                    }
                }
                scrollChild = child;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    private final Rect mTmpRect = new Rect();
    private boolean mOverlayPre = false;
    private int mCoveredFadeColor = 0x99000000;
    private float mSlideOffset;
    private int mCheckInterceptdy, mCheckInterceptdx;

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child == target && draggingState != ViewDragHelper.STATE_IDLE) {
            canvas.save();
            canvas.getClipBounds(mTmpRect);
            if (mCoveredFadeColor != 0 && mSlideOffset > 0) {
                switch (dragEdge) {
                    case TOP:
                        mTmpRect.bottom = Math.min(mTmpRect.bottom, target.getTop());
                        break;
                    case BOTTOM:
                        mTmpRect.top = Math.min(mTmpRect.top, target.getBottom());
                        break;
                    case LEFT:
                        mTmpRect.right = Math.min(mTmpRect.right, target.getLeft());
                        break;
                    case RIGHT:
                        mTmpRect.left = Math.min(mTmpRect.left, target.getRight());
                        break;
                    default:
                        break;
                }
                canvas.clipRect(mTmpRect);
                final int baseAlpha = (mCoveredFadeColor & 0xff000000) >>> 24;
                final int imag = (int) (baseAlpha * (1 - mSlideOffset));
                final int color = imag << 24 | (mCoveredFadeColor & 0xffffff);
                if (mOverlayPre) {
                    canvas.drawColor(color);
                }
                drawShadow(canvas);
                canvas.restore();
            }
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    private Paint mScrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Drawable mShadowLeft;

    private void drawShadow(Canvas canvas) {
        final Rect childRect = mTmpRect;
        if (mShadowLeft != null) {
            mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,
                    childRect.left, childRect.bottom);
            mShadowLeft.setAlpha((int) ((1 - mSlideOffset) * 255));
            mShadowLeft.draw(canvas);
        } else {
//            LinearGradient shader = new LinearGradient();
            GradientDrawable drawable = null;
            int[] colors = new int[3];
            int distance = 50;
            switch (dragEdge) {
                case TOP:
                    colors[0] = 0xFF000000;
                    colors[1] = 0x88000000;
                    colors[2] = 0x00000000;
                    drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
                    drawable.setBounds(mTmpRect.left, mTmpRect.bottom - distance, mTmpRect.right, mTmpRect.bottom);
                    break;
                case BOTTOM:
                    colors[0] = 0xFF000000;
                    colors[1] = 0x88000000;
                    colors[2] = 0xFF000000;
                    drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                    drawable.setBounds(mTmpRect.left, mTmpRect.top, mTmpRect.right, mTmpRect.top + distance);
                    break;
                case LEFT:
                    colors[0] = 0xccaaaaaa;
                    colors[1] = 0x68cccccc;
                    colors[2] = 0x00FFFFFF;
                    drawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
                    drawable.setBounds(mTmpRect.right - distance, mTmpRect.top, mTmpRect.right, mTmpRect.bottom);
                    break;
                case RIGHT:
                    colors[0] = 0xFF000000;
                    colors[1] = 0xFF000000;
                    colors[2] = 0xFF000000;
                    drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                    drawable.setBounds(mTmpRect.left, mTmpRect.top, mTmpRect.left + distance, mTmpRect.bottom);
                    break;
                default:
                    break;
            }
            drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
//            drawable.setAlpha((int)(1-mSlideOffset)*255);
            drawable.draw(canvas);
        }


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        verticalDragRange = h;
        horizontalDragRange = w;

        switch (dragEdge) {
            case TOP:
            case BOTTOM:
                finishAnchor = finishAnchor > 0 ? finishAnchor : verticalDragRange * BACK_FACTOR;
                break;
            case LEFT:
            case RIGHT:
                finishAnchor = finishAnchor > 0 ? finishAnchor : horizontalDragRange * BACK_FACTOR;
                break;
        }
    }

    private int getDragRange() {
        switch (dragEdge) {
            case TOP:
            case BOTTOM:
                return verticalDragRange;
            case LEFT:
            case RIGHT:
                return horizontalDragRange;
            default:
                return verticalDragRange;
        }
    }

    private boolean isVilidEvent(MotionEvent ev) {
        int downx = (int) ev.getX();
        int downy = (int) ev.getY();
        Rect rect = new Rect(0, 0, getWidth() / 5, getHeight());
        if (rect.contains(downx, downx)) {
            return true;
        }
        return false;
    }
    private boolean mNeedDrag;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean handled = false;
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mCheckInterceptdy = mCheckInterceptdx = 0;
            mNeedDrag = isVilidEvent(ev);
            if (!mNeedDrag) {
                return false;
            }
            mNeedDrag = shouldProcess(ev);
            if (!mNeedDrag) {
                return false;
            }

        }
        if(!mNeedDrag){
            return false;
        }
        if (target != null && isEnabled()) {
            handled = viewDragHelper.shouldInterceptTouchEvent(ev);
        } else {
            viewDragHelper.cancel();
        }
        return !handled ? super.onInterceptTouchEvent(ev) : handled;
    }

    private boolean shouldProcess(MotionEvent ev) {
        ensureTarget(ev);
        Logger.e(scrollChild.getClass().getName());
        switch (dragEdge) {
            case TOP:
                if (canChildScrollDown()) {
                    return false;
                }
                break;
            case BOTTOM:
                if (canChildScrollUp()) {
                    return false;
                }
                break;
            case LEFT:
                if (canChildScrollRight()) {
                    return false;
                }
                break;
            case RIGHT:
                if (canChildScrollLeft()) {
                    return false;
                }
                break;
            default:
                return true;

        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Logger.e(mNeedDrag+"");
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mCheckInterceptdy = mCheckInterceptdx = 0;
            mNeedDrag = isVilidEvent(event);
            if (!mNeedDrag) {
                return false;
            }
        }
        if(!mNeedDrag){
            return false;
        }
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public boolean canChildScrollUp() {
        if (scrollChild == null) {
            return false;
        }
        return ViewCompat.canScrollVertically(scrollChild, -1);
    }

    public boolean canChildScrollDown() {
        if (scrollChild == null) {
            return false;
        }
        return ViewCompat.canScrollVertically(scrollChild, 1);
    }

    private boolean canChildScrollRight() {
        if (scrollChild == null) {
            return false;
        }
        return ViewCompat.canScrollHorizontally(scrollChild, -1);
    }

    private boolean canChildScrollLeft() {
        if (scrollChild == null) {
            return false;
        }
        return ViewCompat.canScrollHorizontally(scrollChild, 1);
    }

    private int offset = 300;

    private void onRelateSlide() {
        View view = getPreView();
        if (view != null) {
            view.setTranslationX(-offset * (1 - mSlideOffset));
        } else {
            if (mSwipeBackLayouts.size() > 1) {
                view = mSwipeBackLayouts.get(mSwipeBackLayouts.size() - 2);
                Activity activity = (Activity) view.getContext();
                if (view.getParent() != null && activity.getWindow() != null && activity.getWindow().getDecorView().getWindowToken() != null) {
                    view.setTranslationX(-offset * (1 - mSlideOffset));
                }
            }
        }
    }

    private View getPreView() {
        if (getChildCount() > 1) {
            return getChildAt(getChildCount() - 2);
        }
        return null;
    }

    private void finish() {
        Activity act = (Activity) getContext();
        if(!act.isFinishing()){
            act.finish();
            act.overridePendingTransition(0, android.R.anim.fade_out);
        }

    }

    private class ViewDragHelperCallBack extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == target && enablePullToBack;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            if (viewDragHelper.getViewDragState() != ViewDragHelper.STATE_DRAGGING) {
                if (Math.abs(mCheckInterceptdy) < Math.abs(mCheckInterceptdx) || (dragEdge == DragEdge.LEFT || dragEdge == DragEdge.RIGHT)) {
                    return 0;
                }
            }
            return verticalDragRange;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            if (viewDragHelper.getViewDragState() != ViewDragHelper.STATE_DRAGGING) {
                if (Math.abs(mCheckInterceptdy) > Math.abs(mCheckInterceptdx) || (dragEdge == DragEdge.TOP || dragEdge == DragEdge.BOTTOM)) {
                    return 0;
                }
            }
            return horizontalDragRange;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (viewDragHelper.getViewDragState() != ViewDragHelper.STATE_DRAGGING) {
                mCheckInterceptdy = dy;
            }
            int result = 0;

            if (dragEdge == DragEdge.TOP && !canChildScrollUp() && top > 0) {
                final int topBound = getPaddingTop();
                final int bottomBound = verticalDragRange;
                result = Math.min(Math.max(top, topBound), bottomBound);
            } else if (dragEdge == DragEdge.BOTTOM && !canChildScrollDown() && top < 0) {
                final int topBound = -verticalDragRange;
                final int bottomBound = getPaddingTop();
                result = Math.min(Math.max(top, topBound), bottomBound);
            }

            return result;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (viewDragHelper.getViewDragState() != ViewDragHelper.STATE_DRAGGING) {
                mCheckInterceptdx = dx;
            }
            int result = 0;

            if (dragEdge == DragEdge.LEFT && !canChildScrollRight() && left > 0) {
                final int leftBound = getPaddingLeft();
                final int rightBound = horizontalDragRange;
                result = Math.min(Math.max(left, leftBound), rightBound);
            } else if (dragEdge == DragEdge.RIGHT && !canChildScrollLeft() && left < 0) {
                final int leftBound = -horizontalDragRange;
                final int rightBound = getPaddingLeft();
                result = Math.min(Math.max(left, leftBound), rightBound);
            }

            return result;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == draggingState) return;

            if ((draggingState == ViewDragHelper.STATE_DRAGGING || draggingState == ViewDragHelper.STATE_SETTLING) &&
                    state == ViewDragHelper.STATE_IDLE) {
                // the view stopped from moving.
                if (draggingOffset == getDragRange()) {
                    boolean isHandled = false;
                    int count = getChildCount();
                    if (mListeners != null && !mListeners.isEmpty()) {
                        for (SwipeBackListener listener : mListeners) {
                            if (listener.onSwipeBack(getChildAt(getChildCount() - 1))) {
                                isHandled = true;
                                break;
                            }
                        }
                    }
                    if (!isHandled && count == 1) {
                        finish();
                    }
                }
            }

            draggingState = state;
        }


        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            switch (dragEdge) {
                case TOP:
                case BOTTOM:
                    draggingOffset = Math.abs(top);
                    break;
                case LEFT:
                case RIGHT:
                    draggingOffset = Math.abs(left);
                    break;
                default:
                    break;
            }

            //The proportion of the sliding.
            float fractionAnchor = (float) draggingOffset / finishAnchor;
            if (fractionAnchor >= 1) fractionAnchor = 1;

            float fractionScreen = (float) draggingOffset / (float) getDragRange();
            if (fractionScreen >= 1) fractionScreen = 1;

            mSlideOffset = fractionScreen;
            onRelateSlide();
            if (mListeners != null && !mListeners.isEmpty()) {
                for (SwipeBackListener listener : mListeners) {
                    listener.onViewPositionChanged(fractionAnchor, fractionScreen);
                }
            }
            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (draggingOffset == 0) return;

            if (draggingOffset == getDragRange()) {
                if (mListeners != null && !mListeners.isEmpty()) {
                    for (SwipeBackListener listener : mListeners) {
                        listener.onReleasedToBack();
                    }
                }
                return;
            }

            boolean isBack = false;

            if (enableFlingBack && backBySpeed(xvel, yvel)) {
                isBack = !canChildScrollUp();
            } else if (draggingOffset >= finishAnchor) {
                isBack = true;
            } else if (draggingOffset < finishAnchor) {
                isBack = false;
            }

            if (mListeners != null && !mListeners.isEmpty()) {
                for (SwipeBackListener listener : mListeners) {
                    listener.onReleasedToBack();
                }
            }

            int finalLeft = 0;
            int finalTop = 0;
            switch (dragEdge) {
                case LEFT:
                    finalLeft = isBack ? horizontalDragRange : 0;
                    smoothScrollToX(finalLeft);
                    break;
                case RIGHT:
                    finalLeft = isBack ? -horizontalDragRange : 0;
                    smoothScrollToX(finalLeft);
                    break;
                case TOP:
                    finalTop = isBack ? verticalDragRange : 0;
                    smoothScrollToY(finalTop);
                    break;
                case BOTTOM:
                    finalTop = isBack ? -verticalDragRange : 0;
                    smoothScrollToY(finalTop);
                    break;
            }
//            viewDragHelper.settleCapturedViewAt(finalLeft, finalTop);

        }
    }

    public void attachToActivity(final Activity activity) {
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        decorChild.setBackgroundResource(background);
        decor.removeView(decorChild);
        addView(decorChild);
//        setContentView(decorChild);
        decor.addView(this);
    }

    private boolean backBySpeed(float xvel, float yvel) {
        switch (dragEdge) {
            case TOP:
            case BOTTOM:
                if (Math.abs(yvel) > Math.abs(xvel) && Math.abs(yvel) > AUTO_FINISHED_SPEED_LIMIT) {
                    return dragEdge == DragEdge.TOP ? !canChildScrollUp() : !canChildScrollDown();
                }
                break;
            case LEFT:
            case RIGHT:
                if (Math.abs(xvel) > Math.abs(yvel) && Math.abs(xvel) > AUTO_FINISHED_SPEED_LIMIT) {
                    return dragEdge == DragEdge.LEFT ? !canChildScrollLeft() : !canChildScrollRight();
                }
                break;
        }
        return false;
    }

    public void smoothScrollToX(int finalLeft) {
        if (viewDragHelper.settleCapturedViewAt(finalLeft, 0)) {
            ViewCompat.postInvalidateOnAnimation(SwipeBackLayout.this);
        }
    }

    public void smoothScrollToY(int finalTop) {
        if (viewDragHelper.settleCapturedViewAt(0, finalTop)) {
            ViewCompat.postInvalidateOnAnimation(SwipeBackLayout.this);
        }
    }

    private static final int OVERSCROLL_DISTANCE = 10;

    /**
     * Scroll out contentView and finish the activity
     */
    public void scrollToFinishActivity() {
        final int childWidth = target.getWidth();
        int left = 0, top = 0;
        left = childWidth + OVERSCROLL_DISTANCE;
        viewDragHelper.smoothSlideViewTo(target, left, top);
        invalidate();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return super.generateDefaultLayoutParams();
    }

    public interface SwipeBackListener {

        /**
         * Return scrolled fraction of the layout.
         *
         * @param fractionAnchor relative to the anchor.
         * @param fractionScreen relative to the screen.
         */
        void onViewPositionChanged(float fractionAnchor, float fractionScreen);

        boolean onSwipeBack(View target);

        void onReleasedToBack();

    }

}
