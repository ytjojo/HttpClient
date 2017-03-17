package com.ytjojo.practice.store;

/**
 * Created by Administrator on 2016/3/20 0020.
 */
public class StoreChangeEvent {
    private String operationType;
    public String getOperationType() {
        return operationType;
    }

    public StoreChangeEvent(String operationType){
        this.operationType=operationType;
    }
}
