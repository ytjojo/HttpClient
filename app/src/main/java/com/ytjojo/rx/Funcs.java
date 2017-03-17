package com.ytjojo.rx;

import rx.functions.Func1;

public final class Funcs {
    private Funcs() {
        throw new AssertionError("No instances.");
    }

    public static <T> Func1<T, Boolean> not(final Func1<T, Boolean> func) {
//        new Func1<T,Boolean>(){
//            @Override
//            public Boolean call(T t) {
//                return !func.call(t);
//            }
//        };
        return value -> !func.call(value);
    }
}