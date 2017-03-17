package com.ytjojo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.orhanobut.logger.Logger;
import com.ytjojo.fragmentstack.AnimateOnHWLayerIfNeededListener;
import com.ytjojo.fragmentstack.LaunchMode;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Created by Administrator on 2016/4/16 0016.
 */
public abstract class BaseFragment extends Fragment {
    public static SavedState mFragmentState = null;

    public BaseActivity mActivity;

    public View mContentView;


//    public HintViewFramelayout mHintView;


    private void init() {
        setInitialSavedState(mFragmentState);
    }

//    public ActionbarFrameLayout mActionBarView;


    @Override
    final public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mContentView != null && mContentView.getParent() == container) {
            container.removeView(mContentView);
//            return mContentView;
        }
        final int layoutId = getLayoutResource();
        final View layout = getLayout();
        if (layout != null) {
            mContentView = layout;
        }
        if (mContentView == null) {
            mContentView = inflater.inflate(layoutId, container, false);
//            mHintView = new HintViewFramelayout(container.getContext());
//            mContentView = mHintView;
//            mHintView.setContentView(inflater.inflate(layoutId, mHintView, false));
        }
        initView(inflater, mContentView, container, savedInstanceState);
        mContentView.setBackgroundColor(Color.WHITE);
        mContentView.setClickable(true);
        return mContentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.e("onViewCreated"+getClass().getSimpleName());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Logger.e(getClass().getSimpleName()+"setUserVisibleHint:" + isVisibleToUser);
        if(isVisibleToUser && isActivityCreated){
            loadData();
        }

    }
    boolean isActivityCreated;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.e("onActivityCreated"+getClass().getSimpleName());
        isActivityCreated = true;
        if(getUserVisibleHint()){
            loadData();
        }
    }

    public abstract void loadData();

    public abstract View getLayout();

    //    public void setRootContentView(ViewGroup layout,HintViewFramelayout hintView){
//        mContentView = layout;
//        mHintView = hintView;
//    }
    public abstract int getLayoutResource();

    public void initView(LayoutInflater inflater, View contentView, ViewGroup container, Bundle savedInstanceState) {
    }

    ;


    public View getContentView() {
        return mContentView;
    }

    public Context getApplicationContext() {
        if (mActivity != null) {
            return mActivity.getApplicationContext();
        }
        return null;
//        return SysApplication.getInstance();
    }


    public boolean isRetainInstance = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setRetainInstance(isRetainInstance);


    }


    /**
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
    }

    /**
     *
     */
    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        mActivity = null;
        isActivityCreated = false;
        super.onDetach();

    }

    public void finish() {
        if (isContextEnable()) {
            getActivity().finish();
        }
    }

    public boolean onBackPressed(){
        return false;
    };

    /**
     *
     */
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

    }

    public void saveState() {
        mFragmentState = getFragmentManager().saveFragmentInstanceState(this);
    }


    public void startActivity(Class<?> claz) {
        Intent intent = new Intent(getActivity(), claz);
        startActivity(intent);
//        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public boolean isContextEnable() {
        return !(mActivity == null || !mActivity.isActive() || !isAdded() || isDetached());
    }

    @SuppressWarnings("unchecked")
    final public <E extends View> E findViewById(int id) {
        try {
            return (E) mContentView.findViewById(id);
        } catch (ClassCastException e) {
            throw e;
        }
    }


    /*Set all widget that need to implements OnClick() here*/
    protected void setClickableItems(View... views) {
        if (views != null && views.length > 0) {
            for (View v : views) {
                if (v != null)
                    v.setOnClickListener(mNoDoubleClickListener);
            }
        }
    }

    /*Set all widget that need to implements OnClick() here*/
    protected void setClickableItems(int... residGroup) {
        if (residGroup != null && residGroup.length > 0) {
            for (int resid : residGroup) {
                if (resid != 0) {
                    findViewById(resid).setOnClickListener(mNoDoubleClickListener);
                }
            }
        }
    }

    NoDoubleClickListener mNoDoubleClickListener = new NoDoubleClickListener() {

        @Override
        public void clickInternal(View v) {
            BaseFragment.this.onClick(v);
        }
    };

    public NoDoubleClickListener getOnClick() {
        return mNoDoubleClickListener;
    }

    public void onClick(View v) {

    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (nextAnim > 0) {
            final Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
            if(mAnimationListener !=null){
                anim.setAnimationListener(mAnimationListener);
                mAnimationListener.onAttachToAnim();
            }
            return anim;
        }
        return super.onCreateAnimation(transit,enter,nextAnim);

    }

    private AnimateOnHWLayerIfNeededListener mAnimationListener;

    public void setAnimationListener(AnimateOnHWLayerIfNeededListener l) {
        this.mAnimationListener = l;
    }

    public LaunchMode getLaunchMode(){
        return LaunchMode.standard;
    }
    private boolean isTransparent;
    public boolean isTransparent(){
        return isTransparent;
    }
    public void setIsTransparent(boolean isTransparent){
        this.isTransparent =isTransparent;
    }

    public void onNewIntent(){

    }
    @Override
    public void onPause() {
        super.onPause();
        Logger.e("pause"+getClass().getSimpleName());
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.e("stop"+getClass().getSimpleName());
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.e("start"+getClass().getSimpleName());
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.e("onResume"+getClass().getSimpleName());
    }
    public int[] getAnims(){
        return null;
    }
    public EditText getFocuseEditText(){
        return null;
    }

    protected final PublishSubject<FragmentEvent> lifecycleSubject = PublishSubject.create();
    //监听Frament声明周期，当Fragment销毁后，停止网络请求
    @NonNull
    public <T> Observable.Transformer<T, T> bindUntilEvent(@NonNull final FragmentEvent event) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> sourceObservable) {
                Observable<FragmentEvent> compareLifecycleObservable =
                        lifecycleSubject.takeFirst(new Func1<FragmentEvent, Boolean>() {
                            @Override
                            public Boolean call(FragmentEvent activityLifeCycleEvent) {
                                return activityLifeCycleEvent.equals(event);
                            }
                        });
                return sourceObservable.takeUntil(compareLifecycleObservable);
            }
        };
    }
    public enum FragmentEvent {
        ATTACH,
        CREATE,
        CREATE_VIEW,
        START,
        RESUME,
        PAUSE,
        STOP,
        DESTROY_VIEW,
        DESTROY,
        DETACH
    }

}