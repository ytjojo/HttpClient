package com.ytjojo.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Interception {

    private interface Intercepted {
    }

    public static abstract class InterceptionHandler<T> implements
            InvocationHandler {
        private T mDelegatee;

        @Override
        public Object invoke(Object obj, Method method, Object[] args)
                throws Throwable {
            Object obj2 = null;
            try {
                obj2 = method.invoke(delegatee(), args);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                throw e3.getTargetException();
            }
            return obj2;
        }

        protected T delegatee() {
            return this.mDelegatee;
        }

        void setDelegatee(T t) {
            this.mDelegatee = t;
        }
    }

    public static Object proxy(Object obj, Class cls,
                               InterceptionHandler interceptionHandler)
            throws IllegalArgumentException {
        if (obj instanceof Intercepted) {
            return obj;
        }
        interceptionHandler.setDelegatee(obj);
        return Proxy.newProxyInstance(Interception.class.getClassLoader(),
                new Class[]{cls, Intercepted.class}, interceptionHandler);
    }

    public static Object proxy(Object obj,
                               InterceptionHandler invocationHandler, Class<?>... interfaces)
            throws IllegalArgumentException {
        invocationHandler.setDelegatee(obj);
        return Proxy.newProxyInstance(Interception.class.getClassLoader(),
                interfaces, invocationHandler);
    }

    private Interception() {
    }
}