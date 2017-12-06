package retrofit2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyHandler {


    @SuppressWarnings("unchecked")
    public static <T> T create(Retrofit retrofit, final Class<T> service) {
        Utils.validateServiceInterface(service);
        if (retrofit.validateEagerly) {
            eagerlyValidateMethods(retrofit, service);
        }
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    private final Platform platform = Platform.get();

                    @Override
                    public Object invoke(Object proxy, Method method, Object... args)
                            throws Throwable {
                        // If the method is a method from Object then defer to normal invocation.
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        if (platform.isDefaultMethod(method)) {
                            return platform.invokeDefaultMethod(method, service, proxy, args);
                        }
                        ServiceMethodHack<Object, Object> serviceMethod =
                                (ServiceMethodHack<Object, Object>) ServiceMethodCache.getInstance(retrofit).loadServiceMethod(method);
                        OkHttpCallHack<Object> okHttpCall = new OkHttpCallHack<>(serviceMethod, args);
                        return serviceMethod.callAdapter.adapt(okHttpCall);
                    }
                });
    }

    private static void eagerlyValidateMethods(Retrofit retrofit, Class<?> service) {
        Platform platform = Platform.get();
        for (Method method : service.getDeclaredMethods()) {
            if (!platform.isDefaultMethod(method)) {
                ServiceMethodCache.getInstance(retrofit).loadServiceMethod(method);
            }
        }
    }
}