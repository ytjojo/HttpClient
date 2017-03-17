package com.ytjojo.practice.action;

import com.ytjojo.utils.TextUtils;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/3/20 0020.
 */

public class FluxAction<T> {
    public String type;
    public T data;
    public FluxAction(String type,T data){
        this.type =type;
        this.data =data;
    }
    // 镜头Builder
    public static class Builder {
//        private String mType; // 类型
        private HashMap<String, Object> mData; // 数据

//        // 通过类型创建Builder
//        public Builder with(String type) {
//            if (type == null) {
//                throw new IllegalArgumentException("Type may not be null.");
//            }
//            mType = type;
//
//            return this;
//        }

        // 绑定数据
        public Builder put(String key, Object value) {
            if(mData ==null){
                mData = new HashMap<>();
            }
            if (key == null || value == null) {
                throw new IllegalArgumentException("Key or value may not be null.");
            }
            mData.put(key, value);
            return this;
        }

        // 通过Builder创建Action
        public FluxAction build(String type) {
            if (TextUtils.isEmpty(type)) {
                throw new IllegalArgumentException("At least one key is required.");
            }
            return new FluxAction(type, mData);
        }
    }
}
