package com.ytjojo.practice.store;

import android.util.SparseArray;

import com.ytjojo.practice.action.FluxAction;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/3/20 0020.
 */
public abstract class Store {

    public SparseArray<Object> actionHistory=new SparseArray();
    public  void register(Object context){
        EventBus.getDefault().register(context);
    }

    public   void unRegister(Object context){
        EventBus.getDefault().unregister(context);
    }

    /*传入操作类型，然后触发主界面更新 */
    private void emitStoreChange(String operationType) {
        EventBus.getDefault().post(prepareChangeEvent(operationType));
    }

    public abstract StoreChangeEvent prepareChangeEvent(String operationType);

    /*所有逻辑的处理，在实现类中可以简单想象成对应着一个Activity（View）的增删改查的处理 */
    public abstract void onAction(FluxAction action
    );
    public  void operateAction(FluxAction action){
        onAction(action);
        emitStoreChange(action.type);
    }
}
