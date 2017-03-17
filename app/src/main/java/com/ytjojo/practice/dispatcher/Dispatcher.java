package com.ytjojo.practice.dispatcher;

import android.content.Context;

import com.ytjojo.practice.action.FluxAction;
import com.ytjojo.practice.store.Store;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/20 0020.
 */
public class Dispatcher {
    private static Dispatcher instance;
    private final List<Store> stores = new ArrayList<>();
    public static Dispatcher getInstance(){
        if(instance ==null){
            synchronized (Dispatcher.class){
                if(instance ==null){
                    instance = new Dispatcher();
                }
            }
        }
        return instance;
    }
    public void register(Object context,final Store store) {
        if (!stores.contains(store)) {
            store.register(context);
            stores.add(store);
        }
    }

    public void unregister(Context context,final Store store) {
        store.unRegister(context);
        stores.remove(store);
    }

    public void dispatch(FluxAction action) {
        post(action);
    }

    private void post(final FluxAction action) {
        for (Store store : stores) {
            store.operateAction(action);
        }
    }
}
