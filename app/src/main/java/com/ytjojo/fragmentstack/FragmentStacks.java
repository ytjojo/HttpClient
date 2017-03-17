package com.ytjojo.fragmentstack;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.orhanobut.logger.Logger;
import com.ytjojo.ui.BaseActivity;
import com.ytjojo.ui.BaseFragment;
import com.ytjojo.practice.R;
import com.ytjojo.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by Administrator on 2016/8/22 0022.
 */
public class FragmentStacks {
    public static final String BUNDLE_FRAGMENT_STATS = "KEY_BUNDLE_FRAGMENT_STATES";
    private SavedState mSavedState;
    BaseActivity mActivity;
    FragmentManager mFragmentManager;
    InputMethodManager mInputMethodManager;
    Handler mHandler;
    private int mContainnerId;
    private int[] mAnims;
    ViewGroup mFragmentContainer;
    View mDirtyView;

    public FragmentStacks(BaseActivity activity, int containnerId) {
        this.canCommit = true;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mActivity = activity;
        this.mContainnerId = containnerId;
        this.mFragmentContainer = (ViewGroup) mActivity.findViewById(containnerId);
        this.mFragmentManager = mActivity.getSupportFragmentManager();
        this.mInputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void onCreate(Bundle savedInstanceState, Bundle rootFragmentArguments) {
        FragmentTransactionBugFixHack.injectAvailIndicesAutoReverseOrder(mActivity.getSupportFragmentManager());
        initDefaltAnims();
        if (savedInstanceState == null) {
            mSavedState = new SavedState();
            startRootFragment(rootFragmentArguments == null ? mActivity.getIntent().getExtras() : rootFragmentArguments);

        } else {
            mSavedState = savedInstanceState.getParcelable(BUNDLE_FRAGMENT_STATS);
            if (mSavedState != null && !CollectionUtils.isEmpty(mSavedState.mFragmentBackTags)) {
//                Fragment last = mFragmentManager.getFragment(savedInstanceState, mSavedState.mFragmentBackTags.get(size - 1));
                BaseFragment root = (BaseFragment) mFragmentManager.findFragmentByTag(mSavedState.mFragmentBackTags.get(0));
                Fragment topFragment = getTopFragement();
                if (topFragment.isAdded() && root.isAdded()) {
                    mFragmentManager.beginTransaction().show(topFragment)
                            .commit();
                    return;
                }
            }
            clearState();
            startRootFragment(rootFragmentArguments == null ? mActivity.getIntent().getExtras() : rootFragmentArguments);
        }
    }
    public void onDestroy(){

    }
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BUNDLE_FRAGMENT_STATS, mSavedState);
        canCommit = false;
        Logger.e("-------------------------------------不能提交");
    }

    private boolean canCommit;

    public void onResumeFragments() {
        canCommit = true;
        if(!CollectionUtils.isEmpty(mSavedState.mActionStates)){
            for(ActionState actionState: mSavedState.mActionStates){
                switch (actionState.mActionType){
                    case ActionState.ActionType_POP:
                        popbackImmediate();
                        break;
                    case ActionState.ActionType_POP_TO:
                        popToFragment(actionState.mPopToTag,actionState.includeSelf);
                        break;
                    case ActionState.ActionType_START:
                        ArrayList<Class<? extends BaseFragment>> classes = new ArrayList<>();
                        for(String className:actionState.mToStartFragments){
                            try {
                                Class<?extends BaseFragment> clz= (Class<? extends BaseFragment>) Class.forName(className);
                                classes.add(clz);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }

                        startFragments(actionState.mToRemoveFragmentTags,classes,actionState.mArguments);
                        break;
                    case ActionState.ActionType_START_FOR_RESULT:
                        break;
                    case ActionState.ActionType_REMOVE:
                        break;
                }
            }
            mSavedState.mActionStates.clear();
        }

    }

    public boolean onBackPressed() {
        if (CollectionUtils.isEmpty(mSavedState.mFragmentBackTags)) {
            return false;
        }
        int count = mSavedState.mFragmentBackTags.size();
        String tag = mSavedState.mFragmentBackTags.get(count - 1);
        BaseFragment baseFragment = (BaseFragment) mFragmentManager.findFragmentByTag(tag);
        Logger.e(count + tag);
        if (baseFragment == null) {
            return false;
        }
        boolean handle;
        handle = baseFragment.onBackPressed();
        if (!handle) {
            handle = true;
            popWithAnim();
        }

        return handle;
    }

    public void popWithAnim() {
        int count = mSavedState.mFragmentBackTags.size();
        if (count <= 1) {
            mActivity.finish();//当是最初的fragment时候调用这个方法会退出
            return;
        }
        if(!canCommit){
            ActionState actionState = new ActionState();
            actionState.mActionType = ActionState.ActionType_POP;
            mSavedState.mActionStates.add(actionState);
            return;

        }
        hideSoftKeyBoard();
        BaseFragment top = getTopFragement();
        View topView = top.getView();
        mDirtyView = topView;
        popbackImmediate();
        animPop();
    }
    private void animPop(){
        if(mDirtyView ==null){
            return;
        }
        mFragmentContainer.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {

            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                int childCount = mFragmentContainer.getChildCount();
                if(childCount == getBackStackCount()){
                    if(mCallBack !=null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(mCallBack !=null)
                                mCallBack.onTransationFinish();
                            }
                        });

                    }
                    mFragmentContainer.setOnHierarchyChangeListener(null);
                    if(mAnims == null){
                        mDirtyView = null;
                        return;
                    }
                    View curTop = mFragmentContainer.getChildAt(childCount- 1);
                    mFragmentContainer.addView(mDirtyView);
                    mDirtyView.setVisibility(View.VISIBLE);
                    Animation popExit = AnimationUtils.loadAnimation(mActivity, mAnims[1]);
                    Animation popEnter = AnimationUtils.loadAnimation(mActivity, mAnims[2]);
                    popEnter.setAnimationListener(new AnimateOnHWLayerIfNeededListener(curTop, popEnter));
                    popExit.setAnimationListener(new AnimateOnHWLayerIfNeededListener(mDirtyView, popExit, new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mFragmentContainer.removeView(mDirtyView);
                                    mDirtyView = null;
                                    getTopFragement().setUserVisibleHint(true);
                                    showSoftKeyBoard();
                                    if(mCallBack != null){
                                        mCallBack.onAnimFinish();
                                        mCallBack = null;
                                    }
                                }
                            });

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    }));
                    mDirtyView.startAnimation(popExit);
                    curTop.startAnimation(popEnter);
                }
            }
        });

    }


    public void popbackImmediate() {
        int count = mSavedState.mFragmentBackTags.size();
        if (count <= 1) {
            mActivity.finish();//当是最初的fragment时候调用这个方法会退出
            return;
        }
        if(!canCommit&& CollectionUtils.isEmpty( mSavedState.mActionStates)){
            ActionState actionState = new ActionState();
            actionState.mActionType = ActionState.ActionType_POP;
            mSavedState.mActionStates.add(actionState);
        }
        String tag = mSavedState.mFragmentBackTags.get(count - 1);
        Fragment toRemove = mFragmentManager.findFragmentByTag(tag);
        Fragment toShow = null;
        if (count >= 2) {
            toShow = mFragmentManager.findFragmentByTag(mSavedState.mFragmentBackTags.get(count - 2));
        }
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.remove(toRemove);
        if (toShow != null) {
            ft.show(toShow);
        }
        ft.commit();
        removeTheTailRecord();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showSoftKeyBoard();
            }
        });
    }

    private void removeTheTailRecord() {
        if (!mSavedState.mFragmentBackTags.isEmpty()) {
            mSavedState.mFragmentBackTags.remove(mSavedState.mFragmentBackTags.size() - 1);
        }

    }

    private void clearState() {
        mSavedState.mFragmentBackTags.clear();
        mSavedState.mActionStates.clear();
    }

    private void initDefaltAnims() {
        if (mAnims == null) {
            mAnims = convertAnimations(FragmentAnim.slide);
        }
    }

    private BaseFragment createFragment(String className, Bundle bundle) {
        if (bundle == null) {
            return (BaseFragment) Fragment.instantiate(mActivity, className);
        } else {
            return (BaseFragment) Fragment.instantiate(mActivity, className, bundle);
        }
    }

    private String generateTag(String className, LaunchMode launchMode) {
        if (launchMode == LaunchMode.singleInstance || launchMode == LaunchMode.singleTask) {
            return className + "_0";
        }
        int index = 0;
        for (String tag : mSavedState.mFragmentBackTags) {
            BaseFragment f = (BaseFragment) mFragmentManager.findFragmentByTag(tag);
            if (f.getClass().getName().contains(className)) {
                index++;
            }
        }
        return className + "_" + index;

    }

    public void startMultFragment(ArrayList<Class<? extends BaseFragment>> classes, ArrayList<Bundle> args) {
        if (classes.size() != args.size()) {
            throw new IllegalArgumentException("classes args size not same");
        }
        startFragments(null,classes,args);
    }

    public void startWithPopMult(Class<? extends BaseFragment> clazz, Bundle args,ArrayList<Class<? extends BaseFragment>> toRemoveClasses){
        int count = getBackStackCount();
        if(count ==0){
            return ;
        }
        BaseFragment topFragment = getTopFragement();
        mDirtyView =topFragment.getView();
        boolean canRomove = toRemoveClasses.contains(topFragment.getClass());
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ListIterator<String> iterator= mSavedState.mFragmentBackTags.listIterator();
        ArrayList<String> toRemoveTags = new ArrayList<>();
        while (iterator.hasNext()){
            String tag = iterator.next();
            Fragment f = mFragmentManager.findFragmentByTag(tag);
            if(toRemoveClasses.contains(f.getClass())){
                toRemoveTags.add(tag);
                ft.remove(f);
                iterator.remove();
            }
        }
        add(clazz,ft,topFragment,args);
        ft.commit();
        postAddAnim(canRomove);
    }
    public void startWithPop(Class<? extends BaseFragment> clazz, Bundle args) {
        int count = getBackStackCount();
        if(count ==0){
            return ;
        }
        BaseFragment topFragment = getTopFragement();
        String topTag = mSavedState.mFragmentBackTags.get(count -1);
        mDirtyView = topFragment.getView();
        FragmentTransaction ft= mFragmentManager.beginTransaction().remove(topFragment);
        add(clazz,ft,topFragment,args);
        ft.commit();
        postAddAnim(false);
    }

    private void add(Class<? extends BaseFragment> clazz, FragmentTransaction ft, BaseFragment topFragment, Bundle args){
        LaunchMode launchMode = getLaunchMode(clazz,null);
        String tag = generateTag(clazz.getName(),launchMode);
        BaseFragment toAdd = (BaseFragment) mFragmentManager.findFragmentByTag(tag);
        switch (launchMode){
            case standard:
                break;
            case singleInstance:
                if (toAdd != null) {
                    detachSingleInsanceFragment(toAdd);
                }
                break;
            case singleTask:
                if(toAdd !=null){
                    popToFragment(ft, tag, false);
                }
                break;
            case singleTop:
                if(topFragment != toAdd){
                    toAdd =null;
                }
                break;
        }
        if(toAdd ==null){
            toAdd = createFragment(clazz.getName(),args);
            ft.add(mContainnerId,toAdd,tag);
            mSavedState.mFragmentBackTags.add(tag);
        }else{
            if(launchMode == LaunchMode.singleInstance){
                ft.add(mContainnerId,toAdd,tag);
                mSavedState.mFragmentBackTags.add(tag);
            }
        }
    }

    public void removeRangeFragments(Class<? extends BaseFragment> start, Class<? extends BaseFragment> end) {
        int count = mSavedState.mFragmentBackTags.size();
        if (count >= 1) {
            if(!canCommit){
                ActionState actionState = new ActionState();
                actionState.mActionType = ActionState.ActionType_REMOVE;
                mSavedState.mActionStates.add(actionState);
                return;
            }
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            if(start ==end){
                String className = start.getName();
                for(int i=0;i<count -1; i++){
                    String tag = className+"_"+i;
                    Fragment f = mFragmentManager.findFragmentByTag(tag);
                    if(f != null){
                        ft.remove(f);
                        mSavedState.mFragmentBackTags.remove(tag);
                    }else{
                        break;
                    }
                }
            }else {
                boolean isRemove = false;
                int startIndex = 0;
                int endIndex = 0;
                for (int i = count - 1; i >= 1; i--) {
                    String tag = mSavedState.mFragmentBackTags.get(i);
                    Fragment f = mFragmentManager.findFragmentByTag(tag);
                    if (f.getClass() == end) {
                        isRemove = true;
                        endIndex = i;
                    }
                    if(isRemove){
                        ft.remove(f);
                    }else if( f.getClass() == start) {
                        startIndex = i;
                        break;
                    }
                }
                if ( endIndex > startIndex) {
                    mSavedState.mFragmentBackTags.subList(startIndex + 1, endIndex+1).clear();
                }
            }
            ft.commit();

        }

    }

    public void popToFragment(Class<? extends BaseFragment> to, boolean includeSelf) {
        popToFragment(to.getName() + "_0", includeSelf);
    }

    public void popToFragment(String tag, boolean includeSelf) {
        if(!canCommit){
            ActionState actionState = new ActionState();
            actionState.mActionType = ActionState.ActionType_POP_TO;
            actionState.mPopToTag = tag;
            mSavedState.mActionStates.add(actionState);
            return;


        }
        BaseFragment topFragement = getTopFragement();
        mDirtyView = topFragement.getView();
        final FragmentTransaction ft = mFragmentManager.beginTransaction();
        popToFragment(ft, tag, includeSelf);
        ft.commit();
        animPop();

    }

    public void popToRootFragment() {
        popToFragment(mSavedState.mFragmentBackTags.get(0), false);
    }
    public void startFragment(Class<? extends BaseFragment> clazz,Bundle args){
        ArrayList<Class<? extends BaseFragment>> startClass = new ArrayList<>();
        startClass.add(clazz);
        ArrayList<Bundle> bundles = new ArrayList<>();
        bundles.add(args);
        startFragments(null, startClass,bundles);
    }
    public void startFragment(Class<? extends BaseFragment> clazz){
        startFragment(clazz,null);
    }
    private void startFragments(ArrayList<String> toRemoveTags, ArrayList<Class<? extends BaseFragment>> startClasses, ArrayList<Bundle> fragmentArgs) {
        if (mSavedState.mActionStates.size() > 0) {
            return;
        }
        hideSoftKeyBoard();
        if(!canCommit){
            ActionState actionState = new ActionState();
            actionState.mActionType = ActionState.ActionType_START;
            actionState.mArguments = fragmentArgs;
            actionState.mToRemoveFragmentTags = toRemoveTags;
            actionState.mToStartFragments = new ArrayList<>();
            for (Class<? extends BaseFragment> clazz:startClasses){
                actionState.mToStartFragments.add(clazz.getName());
            }
            mSavedState.mActionStates.add(actionState);
            return;
        }

        int stackSize = mSavedState.mFragmentBackTags.size();
        if (stackSize > 0) {
            BaseFragment topFragment = getTopFragement();
            String topTag = mSavedState.mFragmentBackTags.get(stackSize -1);
            topFragment.setUserVisibleHint(false);
            mDirtyView = topFragment.getView();
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            boolean canRemove = false;
            if (!CollectionUtils.isEmpty(toRemoveTags)) {
                if(toRemoveTags.get(toRemoveTags.size()-1).equals(topTag)){
                    canRemove = true;
                }
                for (String tag : toRemoveTags) {
                    ft.remove(mFragmentManager.findFragmentByTag(tag));
                    mSavedState.mFragmentBackTags.remove(tag);
                }
            }
            for (Class<? extends BaseFragment> clazz : startClasses) {

              add(clazz,ft,topFragment,fragmentArgs.get(startClasses.indexOf(clazz)));
            }
            ft.commit();
            postAddAnim(canRemove);
        }
    }
    private void logTags(){
        Logger.e("  检查 ---------存在" );
        for (String tag: mSavedState.mFragmentBackTags){
            boolean exist = mFragmentManager.findFragmentByTag(tag)==null? false:true;
            Logger.e(tag +"  存在" + exist);

        }

    }


    private void postAddAnim(boolean remove){
        if(mDirtyView ==null){
            return;
        }
        mFragmentContainer.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if(mFragmentContainer.getChildCount() == getBackStackCount()){
                    mFragmentContainer.setOnHierarchyChangeListener(null);
                    if(mCallBack !=null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(mCallBack !=null){
                                    mCallBack.onTransationFinish();
                                }
                            }
                        });
                    }
                    if( mAnims ==null){
                        mDirtyView = null;
                        return;
                    }
                    mFragmentContainer.removeView(mDirtyView);
                    mFragmentContainer.addView(mDirtyView,mFragmentContainer.getChildCount() -1);
                    if(mFragmentContainer.getChildCount() ==1){
                    }else{
                        if(mDirtyView.getParent() == null)
                        mFragmentContainer.addView(mDirtyView,mFragmentContainer.getChildCount() -1);
                    }
                    mDirtyView.setVisibility(View.VISIBLE);
                    View enterView =child;
                    animAdd(enterView, mDirtyView,remove);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {


            }
        });
    }
    private void animAdd(View enter,View exit,boolean removeExitView){
        Logger.e(enter +"" + exit);
        if (enter!= null) {
            final View v = enter;
            Animation anim = AnimationUtils.loadAnimation(mActivity, mAnims[0]);
            anim.setAnimationListener(new AnimateOnHWLayerIfNeededListener(v, anim));
            v.startAnimation(anim);
        } else {
            Logger.e("fragment.getView() == null");
        }
        if (exit != null) {
            final View v =exit;
            Animation anim = AnimationUtils.loadAnimation(mActivity, mAnims[3]);
            anim.setAnimationListener(new AnimateOnHWLayerIfNeededListener(v, anim, new Animation.AnimationListener() {
             @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    showSoftKeyBoard();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getTopFragement().setUserVisibleHint(true);
                        }
                    });
                    if(removeExitView){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mFragmentContainer.removeView(exit);
                                mDirtyView =null;
                            }
                        });
                    }else{
                        mDirtyView =null;
                    }

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            }));
            v.startAnimation(anim);
        } else {
            Logger.e("fragment.getView() == null");
        }
    }

    private BaseFragment getTopFragement() {
        if (CollectionUtils.isEmpty(mSavedState.mFragmentBackTags)) {
            return null;
        }
        int size = mSavedState.mFragmentBackTags.size();
        return (BaseFragment) mFragmentManager.findFragmentByTag(mSavedState.mFragmentBackTags.get(size - 1));

    }

    private BaseFragment getTopPreFragment() {
        if (CollectionUtils.isEmpty(mSavedState.mFragmentBackTags)) {
            return null;
        }
        int size = mSavedState.mFragmentBackTags.size();
        if (size == 1) {
            return null;
        }
        return (BaseFragment) mFragmentManager.findFragmentByTag(mSavedState.mFragmentBackTags.get(size - 2));
    }

    private void startRootFragment(Bundle args) {
        mSavedState.mFragmentBackTags.clear();
        String rootTag = mActivity.getRootFragmentClass().getName() + "_0";
        FragmentTransaction ft = mFragmentManager.beginTransaction().replace(mContainnerId, createFragment(mActivity.getRootFragmentClass().getName(), args), rootTag);
        mSavedState.mFragmentBackTags.add(rootTag);
        ft.commit();

    }

    public FragmentTransaction popToFragment(FragmentTransaction ft, String toTag, boolean inludeSelf) {
        int count = mSavedState.mFragmentBackTags.size();
        if (count >= 1) {
            int index = -200;
            Fragment toFragment = mFragmentManager.findFragmentByTag(toTag);
            if(toFragment ==null){
                return ft;
            }
            for (int i = count - 1; i >= 1; i--) {
                String tag = mSavedState.mFragmentBackTags.get(i);
                Fragment f = mFragmentManager.findFragmentByTag(tag);
                if (tag == toTag) {
                    if (inludeSelf) {
                        index = i - 1;
                        ft.remove(f);
                        break;
                    } else {
                        index = i;
                        break;
                    }
                } else {
                    ft.remove(f);
                }

            }
            if (index + 1 >= 0) {
                mSavedState.mFragmentBackTags.subList(index + 1, count).clear();
            }
        }
        return ft;
    }

    private LaunchMode getLaunchMode(Class<? extends BaseFragment> clazz, String className) {
        Class<? extends BaseFragment> fragmentClass = clazz;
        if (fragmentClass == null) {
            try {
                fragmentClass = (Class<? extends BaseFragment>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                return LaunchMode.standard;
            } catch (ClassCastException e) {
                return LaunchMode.standard;
            }
        }
        if (fragmentClass.isAnnotationPresent(LaunchModelAnnotaion.class)) {
            LaunchModelAnnotaion launchmodel = fragmentClass.getAnnotation(LaunchModelAnnotaion.class);
            return launchmodel.value();
        }
        return LaunchMode.standard;
    }

    public int getBackStackCount() {
        if (mSavedState.mFragmentBackTags == null) {
            return 0;
        }
        return mSavedState.mFragmentBackTags.size();
    }

    private void hideSoftKeyBoard() {
        int count = mSavedState.mFragmentBackTags.size();
        BaseFragment from = null;
        if (count >= 1) {
            from = (BaseFragment) mFragmentManager.findFragmentByTag(mSavedState.mFragmentBackTags.get(count - 1));
            if (from.getView() != null) {
                from.getView().clearFocus();
                mInputMethodManager.hideSoftInputFromWindow(from.getView().getWindowToken(), 0);
            }

        }
    }

    private void showSoftKeyBoard() {
        BaseFragment fragment = getTopFragement();
        if(fragment ==null){
            return;
        }
        EditText et = fragment.getFocuseEditText();
        if (fragment.getView() != null && et != null) {
            fragment.getView().post(new Runnable() {
                @Override
                public void run() {
                    if (!mInputMethodManager.isActive()) {
                        mInputMethodManager.showSoftInput(et, InputMethodManager.RESULT_UNCHANGED_SHOWN);
//                        mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
            });

        }
    }

    private void commiteAndClearActionState(FragmentTransaction ft) {
        ft.commit();
        mSavedState.mActionStates.clear();
    }

    private boolean isValidFragment(BaseFragment fragment) {
        if (fragment.isAdded()) {
            return false;
        }
        return true;
    }

    private void detachSingleInsanceFragment(BaseFragment fragment) {
        mSavedState.mFragmentBackTags.remove(fragment.getClass().getName() + "_0");
        mFragmentManager.beginTransaction().remove(fragment).commit();
        mFragmentManager.executePendingTransactions();
        Logger.e(fragment.isAdded() + "" + fragment.isDetached() + fragment.isRemoving() + "--------------" + mFragmentManager.findFragmentByTag(fragment.getClass().getName() + "_0"));

    }


    private void callOnNewIntent(BaseFragment fragment) {
        fragment.onNewIntent();
    }


    /**
     * 动画转化，根据枚举类返回int数组
     *
     * @param coreAnim 动画枚举
     * @return 转化后的动画数组
     */
    public static int[] convertAnimations(FragmentAnim coreAnim) {
        if (coreAnim == FragmentAnim.present) {
            int[] animations = {R.anim.push_in_up, R.anim.push_out_down, R.anim.push_no_ani, R.anim.push_no_ani};
            return animations;
        } else if (coreAnim == FragmentAnim.fade) {
            int[] animations = {R.anim.alpha_in, R.anim.alpha_out, R.anim.alpha_in, R.anim.alpha_out};
            return animations;
        } else if (coreAnim == FragmentAnim.slide) {
            int[] animations = {R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_left};
            return animations;
        } else if (coreAnim == FragmentAnim.zoom) {
            int[] animations = {R.anim.zoom_in, R.anim.zoom_out, R.anim.zoom_in, R.anim.zoom_out};
            return animations;
        }
        return null;
    }
    public TransationCallBack mCallBack;
    public void setTransationCallBack(TransationCallBack c){
        if(!canCommit){
            return;
        }
        this.mCallBack = c;
    }
    public interface TransationCallBack{
        void onTransationFinish();
        void onAnimFinish();
    }
}
