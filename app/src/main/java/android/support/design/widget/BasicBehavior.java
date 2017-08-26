/*
 * Copyright © Yan Zhenjie. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.support.design.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * Created by YOLANDA on 2016/8/14.
 */
public class BasicBehavior<T extends View> extends CoordinatorLayout.Behavior<T> {

    // 源码讲解：http://blog.csdn.net/yanzhenjie1003/article/details/52205665

    private ListenerAnimatorEndBuild listenerAnimatorEndBuild;

    public BasicBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        listenerAnimatorEndBuild = new ListenerAnimatorEndBuild();
    }

    // We only support the FAB <> Snackbar shift movement on Honeycomb and above. This is
    // because we can use view translation properties which greatly simplifies the code.
    private static final boolean SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;

    private ValueAnimatorCompat mFabTranslationYAnimator;
    private float mFabTranslationY;
    private Rect mTmpRect;

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, T child, View dependency) {
        // We're dependent on all SnackbarLayouts (if enabled)
        return SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, T child, View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            updateFabTranslationForSnackbar(parent, child, dependency);
        } else if (dependency instanceof AppBarLayout) {
            // If we're depending on an AppBarLayout we will show/hide it automatically
            // if the FAB is anchored to the AppBarLayout
            updateFabVisibility(parent, (AppBarLayout) dependency, child);
        }
        return false;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, T child, View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            updateFabTranslationForSnackbar(parent, child, dependency);
        }
    }

    private boolean updateFabVisibility(CoordinatorLayout parent, AppBarLayout appBarLayout, T child) {
        final CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        if (lp.getAnchorId() != appBarLayout.getId()) {
            // The anchor ID doesn't match the dependency, so we won't automatically
            // show/hide the FAB
            return false;
        }

        if (child.getVisibility() != View.VISIBLE) {
            // The view isn't set to be visible so skip changing it's visibility
            return false;
        }

        if (mTmpRect == null) {
            mTmpRect = new Rect();
        }

        // First, let's get the visible rect of the dependency
        final Rect rect = mTmpRect;
        ViewGroupUtils.getDescendantRect(parent, appBarLayout, rect);

        if (rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
            if (listenerAnimatorEndBuild.isFinish())
                // If the anchor's bottom is below the seam, we'll animate our FAB out
                scaleHide(child, listenerAnimatorEndBuild.build());
        } else {
            // Else, we'll animate our FAB back in
            scaleShow(child, null);
        }
        return true;
    }

    private void updateFabTranslationForSnackbar(CoordinatorLayout parent, final T fab, View snackbar) {
        final float targetTransY = getFabTranslationYForSnackBar(parent, fab);
        if (mFabTranslationY == targetTransY) {
            // We're already at (or currently animating to) the target value, return...
            return;
        }

        final float currentTransY = ViewCompat.getTranslationY(fab);

        // Make sure that any current animation is cancelled
        if (mFabTranslationYAnimator != null && mFabTranslationYAnimator.isRunning()) {
            mFabTranslationYAnimator.cancel();
        }

        if (fab.isShown()
                && Math.abs(currentTransY - targetTransY) > (fab.getHeight() * 0.667f)) {
            // If the FAB will be travelling by more than 2/3 of it's height, let's animate
            // it instead
            if (mFabTranslationYAnimator == null) {
                mFabTranslationYAnimator = ViewUtils.createAnimator();
                mFabTranslationYAnimator.setInterpolator(
                        AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                mFabTranslationYAnimator.addUpdateListener(
                        new ValueAnimatorCompat.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimatorCompat animator) {
                                ViewCompat.setTranslationY(fab, animator.getAnimatedFloatValue());
                            }
                        });
            }
            mFabTranslationYAnimator.setFloatValues(currentTransY, targetTransY);
            mFabTranslationYAnimator.start();
        } else {
            // Now update the translation Y
            ViewCompat.setTranslationY(fab, targetTransY);
        }

        mFabTranslationY = targetTransY;
    }

    private float getFabTranslationYForSnackBar(CoordinatorLayout parent, T fab) {
        float minOffset = 0;
        final List<View> dependencies = parent.getDependencies(fab);
        for (int i = 0, z = dependencies.size(); i < z; i++) {
            final View view = dependencies.get(i);
            if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - view.getHeight());
            }
        }

        return minOffset;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, T child, int layoutDirection) {
        // First, lets make sure that the visibility of the FAB is consistent
        final List<View> dependencies = parent.getDependencies(child);
        for (int i = 0, count = dependencies.size(); i < count; i++) {
            final View dependency = dependencies.get(i);
            if (dependency instanceof AppBarLayout && updateFabVisibility(parent, (AppBarLayout) dependency, child)) {
                break;
            }
        }
        // Now let the CoordinatorLayout lay out the FAB
        parent.onLayoutChild(child, layoutDirection);
        return true;
    }

    public static final ViewPropertyAnimatorListener DEFAULT_OUT_ANIMATOR_LISTENER = new ViewPropertyAnimatorListener() {
        @Override
        public void onAnimationStart(View view) {
        }

        @Override
        public void onAnimationEnd(View view) {
            view.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationCancel(View view) {
        }
    };

    public static class ListenerAnimatorEndBuild {

        private boolean isOutExecute = false;

        private ViewPropertyAnimatorListener outAnimatorListener;

        public ListenerAnimatorEndBuild() {
            outAnimatorListener = new ViewPropertyAnimatorListener() {
                @Override
                public void onAnimationStart(View view) {
                    isOutExecute = true;
                }

                @Override
                public void onAnimationEnd(View view) {
                    view.setVisibility(View.GONE);
                    isOutExecute = false;
                }

                @Override
                public void onAnimationCancel(View view) {
                    isOutExecute = false;
                }
            };
        }

        public boolean isFinish() {
            return !isOutExecute;
        }

        public ViewPropertyAnimatorListener build() {
            return outAnimatorListener;
        }
    }

    public static final FastOutSlowInInterpolator FASTOUTSLOWININTERPOLATOR = new FastOutSlowInInterpolator();

    public static void scaleShow(View view) {
        scaleShow(view, null);
    }

    public static void scaleShow(View view, ViewPropertyAnimatorListener viewPropertyAnimatorListener) {
        view.setVisibility(View.VISIBLE);
        ViewCompat.animate(view)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(800)
                .setInterpolator(FASTOUTSLOWININTERPOLATOR)
                .setListener(viewPropertyAnimatorListener)
                .start();
    }

    public static void scaleHide(View view) {
        scaleHide(view, DEFAULT_OUT_ANIMATOR_LISTENER);
    }

    public static void scaleHide(View view, ViewPropertyAnimatorListener viewPropertyAnimatorListener) {
        ViewCompat.animate(view)
                .scaleX(0.0f)
                .scaleY(0.0f)
                .alpha(0.0f)
                .setDuration(800)
                .setInterpolator(FASTOUTSLOWININTERPOLATOR)
                .setListener(viewPropertyAnimatorListener)
                .start();
    }
}