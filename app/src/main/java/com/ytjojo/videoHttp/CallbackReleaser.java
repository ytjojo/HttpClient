package com.ytjojo.videoHttp;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/12/3 0003.
 */
public class CallbackReleaser {
    ArrayList<ResponseDispatcher> mDispatchers;
    public void add(ResponseDispatcher dispatcher){
        if(mDispatchers ==null){
            mDispatchers = new ArrayList<>();
        }
        mDispatchers.add(dispatcher);
    }
    public void onRelease() {
        if (mDispatchers != null) {
            for (ResponseDispatcher item : mDispatchers) {
                item.removeCallback();
            }
        }
    }
    public void cancelAll(){
        if(mDispatchers !=null){
            for(ResponseDispatcher item:mDispatchers){
                item.cancel();
            }
        }
    }
}
