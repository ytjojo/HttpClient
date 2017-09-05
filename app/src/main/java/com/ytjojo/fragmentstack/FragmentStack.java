package com.ytjojo.fragmentstack;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;

import com.orhanobut.logger.Logger;
import com.ytjojo.ui.BaseActivity;
import com.ytjojo.ui.BaseFragment;
import com.ytjojo.practice.R;
import com.ytjojo.utils.CollectionUtils;

import java.util.ArrayList;


/**
 * Created by Administrator on 2016/4/15 0015.
 */
public class FragmentStack {
    public static final String BUNDLE_FRAGMENT_TAGS = "KEY_BUNDLE_FRAGMENT_TAGS";
    private final BaseActivity mActivty;
    ArrayList<String> mFragmentTags;
    FragmentManager mFragmentManager;

    private int[] mAnims;
    public int getBackStackCount(){
        if(mFragmentTags==null){
            return 0;

        }
        return mFragmentTags.size();
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

    public void onCreate(Bundle savedInstanceState) {
        mFragmentTags = new ArrayList();
        FragmentTransactionBugFixHack.injectAvailIndicesAutoReverseOrder(mActivty.getSupportFragmentManager());
        initDefaltAnims();
        Class<? extends BaseFragment> clazz = mActivty.getRootFragmentClass();
        initOrRestoreFragment(clazz, savedInstanceState);
    }

    private void initDefaltAnims() {
        if (mAnims == null) {
            mAnims = convertAnimations(FragmentAnim.slide);
        }
    }

    public FragmentStack(BaseActivity activty) {
        this.mActivty = activty;
        this.mFragmentManager = activty.getSupportFragmentManager();
    }

    private BaseFragment buildFragment(Class<? extends BaseFragment> clazz, Bundle bundle) {
        if (bundle == null) {
            return (BaseFragment) Fragment.instantiate(mActivty, clazz.getName());
        } else {
            return (BaseFragment) Fragment.instantiate(mActivty, clazz.getName(), bundle);
        }
    }

    private void initOrRestoreFragment(Class<? extends BaseFragment> rootClazz, Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            clearRecord();
            addFragmentToStack(buildFragment(rootClazz, mActivty.getIntent().getExtras()));
        } else {
            this.onRestoreInstanceState(savedInstanceState);
            if (mFragmentTags != null && !mFragmentTags.isEmpty()) {
                BaseFragment root = (BaseFragment) mFragmentManager.getFragment(savedInstanceState, mFragmentTags.get(0));
                Fragment last = mFragmentManager.getFragment(savedInstanceState, mFragmentTags.get(mFragmentTags.size() - 1));
                if (last.isAdded() && root.isAdded()) {
                    mFragmentManager.beginTransaction().show(last)
                            .commit();
                    return;
                }
            }
            clearRecord();
            addFragmentToStack(buildFragment(rootClazz, mActivty.getIntent().getExtras()));
        }


    }

    private void addToRecorder(BaseFragment f, String tag) {
        if(mFragmentTags.contains(tag)){
            mFragmentTags.remove(tag);
        }
        mFragmentTags.add(tag);
    }

    private void commitAddFragment(BaseFragment to, int[] anim, Fragment from) {
        final String toTag = generateTag(to);
        Logger.e(toTag);
        int count = mFragmentTags.size();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (anim != null && anim.length >= 4 && count >= 1) {
            ft.setCustomAnimations(anim[0], anim[1], anim[2], anim[3]);
        }
        if (count == 0) {
            ft.replace(R.id.fragment_container, to, toTag);
        } else {
            if (to.isAdded() && to.getLaunchMode() == LaunchMode.singleInstance) {
                ft.remove(to);
                ft.add(R.id.fragment_container, to, toTag);


            } else {
                ft.add(R.id.fragment_container, to, toTag);
            }
        }

        if (from != null) {
            ft.hide(from);

        }
        ft.commit();
        addToRecorder(to, toTag);
    }

    public void replaceRootFragment(BaseFragment f) {
        if (mFragmentTags.size() > 0) {
            popToFragment((BaseFragment) mFragmentManager.findFragmentByTag(mFragmentTags.get(0)), 0);
        }
        clearRecord();
        addFragmentToStack(f);

    }

    private void stackPop() {
        int count = mFragmentTags.size();

        String tag = mFragmentTags.get(count - 1);
        Fragment toRemove = mFragmentManager.findFragmentByTag(tag);
        Fragment toShow = null;
        if (count >= 2) {
            toShow = mFragmentManager.findFragmentByTag(mFragmentTags.get(count - 2));
        }
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.setCustomAnimations(mAnims[0], mAnims[1], mAnims[2], mAnims[3]);
        ft.remove(toRemove);
        if (toShow != null) {
            ft.show(toShow);
        }
        ft.commit();
        removeTheTailRecord();
    }

    private void clearRecord() {
        mFragmentTags.clear();
    }

    public void startFragmentInternal(BaseFragment from, BaseFragment to) {

        if (to.isAdded() && to.getLaunchMode() != LaunchMode.singleInstance) {
            return;
        }
        Fragment fromToHide = null;
        int[] anims = null;
        if (to.getAnims() != null) {
           int[] custermanim = to.getAnims();
            if(custermanim.length ==4){
                anims = custermanim;
            }else{
                anims = null;
            }
        } else {
            anims = mAnims;
        }
        commitAddFragment(to, anims, fromToHide);
    }

    public void onDestroy() {

    }
    SparseArray<String> mSingleInstanceInexTags =new SparseArray<>();
    public void onSaveInstanceState(Bundle outState) {
        for (String tag : mFragmentTags) {
            BaseFragment fragment = (BaseFragment) mFragmentManager.findFragmentByTag(tag);
            Logger.e(tag);
            if(fragment !=null)
            mFragmentManager.putFragment(outState, tag, fragment);
        }
        outState.putStringArrayList(BUNDLE_FRAGMENT_TAGS, mFragmentTags);

    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mFragmentTags = savedInstanceState.getStringArrayList(BUNDLE_FRAGMENT_TAGS);
        Logger.e(mFragmentTags.toString());
    }

    public boolean onBackPressed() {
        if(CollectionUtils.isEmpty(mFragmentTags)){
            return false;
        }
        int count = mFragmentTags.size();
        String tag = mFragmentTags.get(count - 1);
        Fragment last = mFragmentManager.findFragmentByTag(tag);
        Logger.e(count + tag);
        BaseFragment baseFragment = (BaseFragment) last;
        if(baseFragment ==null){
            return false;
        }
        boolean handle;
        handle = baseFragment.onBackPressed();
        if (!handle) {
            handle =true;
            popback();
        }

        return handle;
    }


    public void startFragment(Class<? extends BaseFragment> clazz){
        startFragment(clazz,null);
    }
    public void startFragment(Class<? extends BaseFragment> clazz, Bundle bundle) {
        LaunchMode launchMode = buildFragment(clazz, bundle).getLaunchMode();
        switch (launchMode) {
            case standard:
                putStandard(clazz, bundle);
                break;
            case singleInstance:
                putSingleInstance(clazz, bundle);
                break;
            case singleTask:
                putSingleTask(clazz, bundle);
                break;
            case singleTop:
                putSingleTop(clazz, bundle);
                break;
        }
    }

    public void onResumeFragments() {

    }

    public void popToFragment(BaseFragment fragment, int flags) {

        int count = mFragmentTags.size();
        if (count >= 1) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            int index = -200;
            for (int i = count - 1; i >= 1; i--) {
                String tag = mFragmentTags.get(i);
                Fragment f = mFragmentManager.findFragmentByTag(tag);
                if (f == fragment) {
                    if (flags == FragmentManager.POP_BACK_STACK_INCLUSIVE) {
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
            ft.commit();
            if (index + 1 >= 0) {
                mFragmentTags.subList(index + 1, mFragmentTags.size()).clear();
            }
        }
    }

    public void popToRootFragment() {
        int count = mFragmentTags.size();
        if (count <= 1) {
            return;
        } else {
            if (count >= 2) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();

                for (int i = count - 1; i >= 1; i--) {
                    String tag = mFragmentTags.get(i);
                    Fragment f = mFragmentManager.findFragmentByTag(tag);
                    ft.remove(f);
                }
                ft.commit();
                String rootTag = mFragmentTags.get(0);
                mFragmentTags.clear();
                mFragmentTags.add(rootTag);
            }

        }
    }

    private void removeTheTailRecord() {
        if (!mFragmentTags.isEmpty())
            mFragmentTags.remove(mFragmentTags.size() - 1);
    }

    public void popback() {
        int count = mFragmentTags.size();
        if (count <= 1) {
            mActivty.finish();//当是最初的fragment时候调用这个方法会退出
            return;
        }
        stackPop();

    }


    private String generateTag(BaseFragment fragment) {
        if (fragment.getLaunchMode() == LaunchMode.singleInstance || fragment.getLaunchMode() == LaunchMode.singleTask) {
            return fragment.getClass().getName() + "_0";
        }
        int index = 0;
        for (String tag : mFragmentTags) {
            BaseFragment f = (BaseFragment) mFragmentManager.findFragmentByTag(tag);
            if (f.getClass() == fragment.getClass()) {
                index++;
            }
        }
        return fragment.getClass().getName() + "_" + index;

    }

    private boolean isContainClass(Class<? extends BaseFragment> clazz) {
        for (String tag : mFragmentTags) {
            Fragment f = mFragmentManager.findFragmentByTag(tag);
            if (f !=null && f.getClass() == clazz) {
                return true;
            }
        }
        return false;
    }

    public void putStandard(Class<? extends BaseFragment> clazz, Bundle b) {

        addFragmentToStack(buildFragment(clazz, b));
    }

    public boolean putSingleTop(Class<? extends BaseFragment> clazz, Bundle b) {

        if (mFragmentTags.isEmpty()) {

            addFragmentToStack(buildFragment(clazz, b));
            return false;
        } else {
            int size = mFragmentTags.size();
            BaseFragment last = (BaseFragment) mFragmentManager.findFragmentByTag(mFragmentTags.get(size - 1));
            if (last.getClass().getName().equals(clazz.getName())) {
                callOnNewIntent(last);
                return true;
            } else {
                addFragmentToStack(buildFragment(clazz, b));
                return false;
            }
        }
    }

    private void callOnNewIntent(BaseFragment fragment) {
        fragment.onNewIntent();
    }

    private void addFragmentToStack(BaseFragment fragment) {
        int count = mFragmentTags.size();
        BaseFragment from = null;
        if (count >= 1) {
            from = (BaseFragment) mFragmentManager.findFragmentByTag(mFragmentTags.get(count - 1));
            if (from.getView() != null) {
                from.getView().clearFocus();
            }
        }
        startFragmentInternal(from, fragment);
    }

    public boolean putSingleTask(Class<? extends BaseFragment> clazz, Bundle b) {

        if (isContainClass(clazz)) {
            BaseFragment fragment = (BaseFragment) mFragmentManager.findFragmentByTag(clazz.getName() + "_0");
            popToFragment(fragment, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            if (b != null) {
                fragment.setArguments(b);
            }
            callOnNewIntent(fragment);
            return true;
        } else {
            addFragmentToStack(buildFragment(clazz, b));
        }

        return false;

    }

    public void putSingleInstance(Class<? extends BaseFragment> clazz, Bundle b) {
        if (isContainClass(clazz)) {
            final BaseFragment fragment = (BaseFragment) mFragmentManager.findFragmentByTag(clazz.getName() + "_0");
            detachSingleInsanceFragment(fragment);

            if (b != null) {
                fragment.setArguments(b);
            }
            addFragmentToStack(fragment);
            callOnNewIntent(fragment);
        } else {
            addFragmentToStack(buildFragment(clazz, b));
        }
    }

    private void detachSingleInsanceFragment(BaseFragment fragment) {
        mFragmentTags.remove(fragment.getClass().getName() + "_0");
        mFragmentManager.beginTransaction().remove(fragment).commit();
        mFragmentManager.executePendingTransactions();
        Logger.e(fragment.isAdded() + "" + fragment.isDetached() + fragment.isRemoving() + "--------------" + mFragmentManager.findFragmentByTag(fragment.getClass().getName() + "_0"));

    }

}
