package com.ytjojo.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ytjojo.practice.R;
import com.ytjojo.fragmentstack.LaunchMode;
import com.ytjojo.widget.StickyNavLayout;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/4/18 0018.
 */
public class Fragment2 extends BaseFragment {
    ViewPager pager = null;
    PagerTabStrip tabStrip = null;
    ArrayList<View> viewContainter = new ArrayList<View>();
    ArrayList<String> titleContainer = new ArrayList<String>();
    public String TAG = "tag";

    @Override
    public View getLayout() {
        return null;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.viewpage_layout;
    }

    @Override
    public void initView(LayoutInflater inflater, View contentView, ViewGroup container, Bundle savedInstanceState) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setClickableItems(R.id.tv_setDivision1,R.id.tv_setDivision2,R.id.tv_setDivision3,R.id.tv_setDivision4);
        pager = (ViewPager) this.findViewById(R.id.viewpager);
        tabStrip = (PagerTabStrip) this.findViewById(R.id.tabstrip);
        //取消tab下面的长横线
        tabStrip.setDrawFullUnderline(false);
        //设置tab的背景色
        tabStrip.setBackgroundColor(Color.parseColor("#e1e1e1"));
        //设置当前tab页签的下划线颜色
        tabStrip.setTabIndicatorColor(this.getResources().getColor(R.color.red));
        tabStrip.setTextSpacing(200);

        View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.tab1, null);
        View view2 = LayoutInflater.from(getContext()).inflate(R.layout.tab2, null);
        View view3 = LayoutInflater.from(getContext()).inflate(R.layout.tab3, null);
        //viewpager开始添加view
        viewContainter.add(view1);
        viewContainter.add(view2);
        viewContainter.add(view3);
        Intent intent =new Intent();

        //页签项
        titleContainer.add("网易新闻");
        titleContainer.add("网易体育");
        titleContainer.add("网易财经");
        pager.setAdapter(new StickyNavLayout.CurrentPagerAdapter(viewContainter,titleContainer));

    }

    @Override
    public void loadData() {

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_gonext:
                BaseActivity baseActivity= (BaseActivity) getActivity();
                baseActivity.getFragmentStack().startFragment(Fragment3.class,null);
                break;
            case R.id.tv_gonext3:

                break;
            case R.id.tv_setDivision1:
                ((BaseActivity)getActivity()).getFragmentStack().startFragment(Fragment3.class,null);
                break;
            case R.id.tv_setDivision2:
                break;
            case R.id.tv_setDivision3:
                break;
            case R.id.tv_setDivision4:
                break;

        }

    }

    @Override
    public LaunchMode getLaunchMode() {
        return LaunchMode.standard;
    }
}