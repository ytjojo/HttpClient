package retrofit2;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2016/10/29 0029.
 */
public class ServiceMethodCach {
    private static volatile ServiceMethodCach instance;
    private ServiceMethodCach(Retrofit retrofit){
        this.mRetrofit = retrofit;
    }
    public static ServiceMethodCach getInstance(Retrofit retrofit){
        if(instance ==null){
            synchronized (ServiceMethodCach.class){
                if(instance ==null){
                    instance = new ServiceMethodCach(retrofit);
                }
            }
        }
        return instance;
    }
    private final Map<Method, ServiceMethodHack<?, ?>> serviceMethodCache = new ConcurrentHashMap<>();
    Retrofit mRetrofit;
    ServiceMethodHack<?, ?> loadServiceMethod(Method method) {
        ServiceMethodHack<?, ?> result = serviceMethodCache.get(method);
        if (result != null) return result;
        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new ServiceMethodHack.Builder<>(mRetrofit, method).build();
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }
}
