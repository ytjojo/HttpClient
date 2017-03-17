package com.ytjojo.anim;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.ytjojo.utils.DensityUtil;

/**
 * Created by Administrator on 2016/9/27 0027.
 */
public class ScrollFooterFramLayout extends FrameLayout {
    TouchEventWatcher mTouchEventWatcher;
    public ScrollFooterFramLayout(Context context) {
        this(context,null);
    }
    public ScrollFooterFramLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public ScrollFooterFramLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScrollFooterFramLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        TextView textView = new TextView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getContext(),40));
        params.gravity = Gravity.BOTTOM;
        textView.setText( "我是底部");
        textView.setBackgroundColor(Color.WHITE);
        this.addView(textView,params);
        mTouchEventWatcher = new TouchEventWatcher(this,textView, TranslationAnim.Direction.Bottom,false);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Logger.e("eee");
        mTouchEventWatcher.onDispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
