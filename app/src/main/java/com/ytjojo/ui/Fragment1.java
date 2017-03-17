package com.ytjojo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ytjojo.app.RequestPermissionManager;
import com.ytjojo.drawable.LoadingDrawable;
import com.ytjojo.practice.R;
import com.ytjojo.fragmentstack.LaunchMode;
import com.ytjojo.fragmentstack.LaunchModelAnnotaion;


/**
 * Created by Administrator on 2016/4/18 0018.
 */
@LaunchModelAnnotaion(LaunchMode.standard)
public class Fragment1 extends BaseFragment {
    TextView mTextVie;
    TextView mtv_gonext3;
    @Override
    public View getLayout() {
        return null;
    }
    RequestPermissionManager<Fragment1> mFragment1RequestPermissionManager;
    @Override
    public int getLayoutResource() {
        return R.layout.fragment_test;
    }
    @Override
    public void initView(LayoutInflater inflater, View contentView, ViewGroup container, Bundle savedInstanceState) {
        mTextVie= findViewById(R.id.tv_gonext);
        mtv_gonext3 = findViewById(R.id.tv_gonext3);
        mtv_gonext3.setText(getClass().getName());
        ImageView imageView = findViewById(R.id.img_load);
        imageView.setImageDrawable(new LoadingDrawable());
        setClickableItems(mTextVie,findViewById(R.id.tv_gonext2));
        setClickableItems(R.id.tv_setDivision1,R.id.tv_setDivision2,R.id.tv_setDivision3,R.id.tv_setDivision4);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFragment1RequestPermissionManager = new RequestPermissionManager<>(this);
        if(savedInstanceState !=null){
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mFragment1RequestPermissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getContext(),"0000000000000000000000000000000",Toast.LENGTH_LONG).show();
    }

    @Override
    public void loadData() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }


    @Override
    public void onClick(View v) {
        BaseActivity baseActivity= (BaseActivity) getActivity();

        switch (v.getId()){
            case R.id.tv_gonext:

//                mFragment1RequestPermissionManager.requestPermissions(1,"需要拍照",android.R.string.ok,
//                        android.R.string.cancel, Manifest.permission.CAMERA,Manifest.permission.WRITE_CONTACTS);
                baseActivity.getFragmentStack().startFragment(Fragment2.class,null);
                break;
            case R.id.tv_gonext2:
                baseActivity.getFragmentStack().startFragment(Fragment2.class,null);
                break;
            case R.id.tv_setDivision1:
                break;
            case R.id.tv_setDivision2:
                break;
            case R.id.tv_setDivision3:
                break;
            case R.id.tv_setDivision4:
                break;

        }

    }


}
