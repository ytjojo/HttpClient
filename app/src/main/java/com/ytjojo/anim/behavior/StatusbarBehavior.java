package com.ytjojo.anim.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.orhanobut.logger.Logger;
import com.ytjojo.utils.DensityUtil;

/**
 * Created by Administrator on 2016/9/30 0030.
 */
public class StatusbarBehavior extends CoordinatorLayout.Behavior<View> {
    int statusHeight;
    float startY;
    public StatusbarBehavior(Context context, AttributeSet attrs){
        super(context,attrs);
        statusHeight = DensityUtil.getStatusBarHeight(context);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if(dependency instanceof AppBarLayout){
            dependency.setPadding(dependency.getPaddingLeft(),statusHeight,dependency.getPaddingRight(),dependency.getPaddingBottom());
            return true;
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if(startY ==0){
            startY =  dependency.getY();
        }
        float percent = dependency.getY()/startY;
        float curDependecyY = -dependency.getY();
        curDependecyY -= statusHeight;
        Logger.e("   "+ curDependecyY);
        if(curDependecyY >= statusHeight){
            curDependecyY =  statusHeight;
        }

        child.setY(curDependecyY);

        return true;

    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        child.getLayoutParams().height = statusHeight;
        // Now let the CoordinatorLayout lay out the FAB
        parent.onLayoutChild(child, layoutDirection);
        // Now offset it if needed
        child.offsetTopAndBottom(-statusHeight);
        parent.post(new Runnable() {
            @Override
            public void run() {
                parent.bringChildToFront(child);
            }
        });
//        child.setY(-statusHeight);
        return true;
    }
}
