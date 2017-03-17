package com.ytjojo.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.ytjojo.practice.R;
import com.ytjojo.fragmentstack.LaunchMode;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2016/4/18 0018.
 */
public class Fragment3 extends BaseFragment {
    TextView mTextVie;
    TextView mtv_gonext3;
    @Override
    public View getLayout() {
        return null;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_test;
    }

    @Override
    public void initView(LayoutInflater inflater, View contentView, ViewGroup container, Bundle savedInstanceState) {
        mTextVie= findViewById(R.id.tv_gonext);
        mtv_gonext3 = findViewById(R.id.tv_gonext3);
        mtv_gonext3.setText(getClass().getName());
        setClickableItems(mTextVie);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState !=null){
        }
    }

    @Override
    public void loadData() {

    }

    @Override
    public LaunchMode getLaunchMode() {
        return LaunchMode.singleInstance;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        try {
            final Field field = getClass().getDeclaredField("mIndex");
            field.setAccessible(true);
            int index =field.getInt(this);
            int count =((BaseActivity) getActivity()).getFragmentStack().getBackStackCount();
            Logger.e("index=============================================" + index);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        BaseActivity baseActivity= (BaseActivity) getActivity();
        try {
            Field field = null;
            field = Fragment.class.getDeclaredField("mIndex");
            field.setAccessible(true);
            int index =field.getInt(this);
            Logger.e("index=============================================" + index);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        switch (v.getId()){
            case R.id.tv_gonext:
                baseActivity.getFragmentStack().startFragment(Fragment1.class,null);
                break;
            case R.id.tv_gonext2:
                break;
        }

    }
}