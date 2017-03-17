package com.ytjojo.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.ytjojo.practice.R;
import com.ytjojo.fragmentstack.FragmentStacks;
import com.ytjojo.utils.DensityUtil;
import com.ytjojo.widget.SwipeBackLayout;

/**
 * Created by Administrator on 2016/4/16 0016.
 */
public class BaseActivity extends RxAppCompatActivity implements SwipeBackLayout.SwipeBackListener {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static final String KEY_DATA_STRING = "KEY_DATA_STRING";

    public static final String KEY_DATA_INTEGER = "KEY_DATA_INTEGER";

    public static final String KEY_DATA_LONG = "KEY_DATA_LONG";

    public static final String KEY_DATA_BOOLEN = "KEY_DATA_BOOLEN";

    public static final String KEY_DATA_SERIALIZABLE = "KEY_DATA_SERIALIZABLE";

    public static final String KEY_DATA_BITMAP = "KEY_DATA_BITMAP";

    public static final String KEY_DATA_STRING_ARRAYLIST = "KEY_DATA_STRING_ARRAYLIST";

    private boolean isDestroyed;

    private boolean isDestroy() {

        return isDestroyed;
    }


    @Override
    public Uri getReferrer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return super.getReferrer();
        }
        return getReferrerCompatible();
    }
    /** 在低于SDK 22版本时使用该方法获得引用者 */
    private Uri getReferrerCompatible() {
        Intent intent = this.getIntent();
        Uri referrerUri = intent.getParcelableExtra(Intent.EXTRA_REFERRER);
        if (referrerUri != null) {
            return referrerUri;
        }
        String referrer = intent.getStringExtra("android.intent.extra.REFERRER_NAME");
        if (referrer != null) {
            // 尝试parse引用者URL
            return Uri.parse(referrer);
        }
        return null;
    }



    public boolean isActive() {
        return !(isFinishing() || isDestroyed);
    }

    private boolean isResumed;

    public Drawable getDrawableBase(@DrawableRes  int id) {
        return ContextCompat.getDrawable(this,id);
    }
    FragmentStacks mFragmentStack ;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        isDestroyed = false;
        if(getRootFragmentClass() !=null){
            if(canSwipBack()){
                setContentView(SwipeBackLayout.build(getActivy()));
                SwipeBackLayout swipeRefreshLayout= $(R.id.fragment_container);
                swipeRefreshLayout.addOnSwipeBackListener(this);
            }else{
                setContentView(R.layout.activity_fragment_container);
            }
            mFragmentStack = new FragmentStacks(getActivy(),R.id.fragment_container);
            onSetFragmentArgs(getIntent().getExtras());
            mFragmentStack.onCreate(savedInstanceState,savedInstanceState);
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }
    public void setFlagForKitKat(){
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            // 设置全屏，并且不会Activity的布局让出状态栏的空间
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
    private void setMarginTopForKitkat() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            // 设置Toolbar对顶部的距离
            final View content =  ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
            content.setFitsSystemWindows(true);
            final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) content
                    .getLayoutParams();

            layoutParams.topMargin = DensityUtil.getStatusBarHeight(this);
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        this.setContentView(LayoutInflater.from(getActivy()).inflate(layoutResID,null));
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }
    public boolean canSwipBack(){
        return true;
    }
    public FragmentStacks getFragmentStack(){
        return mFragmentStack;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(mFragmentStack !=null){
            mFragmentStack.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if(mFragmentStack !=null){
            mFragmentStack.onResumeFragments();
        }
    }



    public Class<? extends BaseFragment> getRootFragmentClass(){
        return null;
    }
    public void onSetFragmentArgs(Bundle bundle){

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
        // 友盟统计分析
    }

    @SuppressWarnings("unchecked")
    final public <E extends View> E $(int id) {
        try {
            return (E) findViewById(id);
        } catch (ClassCastException e) {

            throw e;
        }
    }


    private boolean isResumed() {
        return isResumed;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mFragmentStack !=null){
            mFragmentStack.onDestroy();
        }
        isDestroyed = true;
    }

    @Override
    public void onBackPressed() {
        boolean handle = false;
        if(mFragmentStack !=null){
            handle =mFragmentStack.onBackPressed();
            if (handle) {
                return;
            }
        }
        super.onBackPressed();
    }
    protected ResultActivityAdaptor mResultActivityAdaptor;
    public void startActivityWithCallback(Intent intent, ResultActivityAdaptor.ResultActivityListener listener) {
        if(mResultActivityAdaptor ==null){
            mResultActivityAdaptor= new ResultActivityAdaptor(this);
        }
        mResultActivityAdaptor.startActivityForResult(intent, listener);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mResultActivityAdaptor !=null)
        mResultActivityAdaptor.onResult(requestCode, resultCode, data);
    }

    /**
     * 点击标题栏返回事件
     */
    public void finishWithAnim() {
        finish();
    }
    public void onDelayLoad(){

    }
    public final Handler mHandler = new Handler(Looper.getMainLooper());

    private void delayLoad(){
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onDelayLoad();
                    }
                });
            }
        });
    }

    public void showSoftKeyBoard(final EditText et, long delayMillis) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                et.requestFocus();
                InputMethodManager inputManager = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et, InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
        }, delayMillis);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public boolean isWindowFocuseChanged;//如果在渲染之前调用popup.show()会报错

    /**
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        isWindowFocuseChanged = hasFocus;
    }

    public View getContentView() {
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView();
//        android.R.id.content;
        return rootView.getChildAt(0);
    }

    /*Set all widget that need to implements OnClick() here*/
    protected void setClickableItems(View... views) {
        if (views != null && views.length > 0)
        {
            for (View v : views)
            {
                if (v != null)
                    v.setOnClickListener(mNoDoubleClickListener);
            }
        }
    }

    /*Set all widget that need to implements OnClick() here*/
    protected void setClickableItems(int... residGroup) {
        if (residGroup != null && residGroup.length > 0)
        {
            for (int resid : residGroup)
            {
                if (resid != 0)
                {
                    findViewById(resid).setOnClickListener(mNoDoubleClickListener);
                }
            }
        }
    }

    NoDoubleClickListener mNoDoubleClickListener =new NoDoubleClickListener(){

        @Override
        public void clickInternal(View v) {
            clickWrapper(v);
        }
    };
    private void clickWrapper(View v){
        onClick(v);
    }
    public NoDoubleClickListener getOnClick(){
        return mNoDoubleClickListener;
    }
    public void onClick(View v) {

    }
    public BaseActivity getActivy(){
        return this;
    }

    @Override
    public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {

    }

    @Override
    public boolean onSwipeBack(View target) {
        getFragmentStack().popbackImmediate();
        return false;
    }

    @Override
    public void onReleasedToBack() {

    }


}
