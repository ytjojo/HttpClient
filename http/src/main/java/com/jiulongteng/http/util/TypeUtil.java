package com.jiulongteng.http.util;

import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.Type;

public class TypeUtil {

    public static boolean isAssignableFrom(Class interfaceClazz,Type type){
        Class rawType = $Gson$Types.getRawType(type);
        return interfaceClazz.isAssignableFrom(rawType);
    }
}
