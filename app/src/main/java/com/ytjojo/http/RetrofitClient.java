package com.ytjojo.http;

import android.content.Context;
import android.support.v4.util.Pair;
import com.google.gson.JsonObject;
import com.ytjojo.BaseApplication;
import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.domin.response.OrganAddrArea;
import com.ytjojo.domin.vo.LoginResponse;
import com.ytjojo.http.coverter.GsonConverterFactory;
import com.ytjojo.http.https.HttpsDelegate;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.ProxyHandler;
import retrofit2.Retrofit;
import retrofit2.ServiceAndMethod;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.ArrayItem;
import retrofit2.http.Body;
import retrofit2.http.BodyJsonAttr;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;
import rx.Observable;

public class RetrofitClient {
    public static final String BASE_URL="http://192.168.0.46:8080";
    public static volatile  String TOKEN;
    public static  final String TOKEN_HEADER_KEY = "X-Access-Token";
    public static  final String ContentType_JSON = "application/json";
    public static  final String ContentType_FORM = "application/x-www-form-urlencoded; charset=UTF-8";
    public static final int HTTP_RESPONSE_DISK_CACHE_MAX_SIZE=10 * 1024 * 1024;
    private Retrofit retrofit ;
    static OkHttpClient mOkHttpClient;
    public RetrofitClient(Retrofit retrofit){
        this.retrofit = retrofit;
    }
    public void clearCached(){
        try {
            mOkHttpClient.cache().delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void init(Context c){

    }
    public static Builder newBuilder(Context context){
        return new Builder(context);
    }
    public static class Builder{
        Context context;
        String baseUrl;
        HashMap<String,String> headers;
        int writeTimeout;
        int readTimeout;
        int connectTimeout;
        private Pair<SSLSocketFactory, X509TrustManager> sslFactory;

        public Builder(Context context){
           this.context =  context.getApplicationContext();
        }
        public Builder baseUrl(String baseUrl){
            this.baseUrl = baseUrl;
            return this;
        }
        public Builder headers(HashMap<String,String> headers){
            this.headers = headers;
            return this;
        }
        public Builder writeTimeout(int writeTimeout){
            this.writeTimeout = writeTimeout;
            return this;
        }
        public Builder readTimeout(int readTimeout){
            this.readTimeout = readTimeout;
            return this;
        }
        public Builder connectTimeout(int connectTimeout){
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * 信任所有证书,不安全有风险
         *
         * @return
         */
        public Builder unsafeSSLSocketFactory(){
           this.sslFactory =  HttpsDelegate.getUnsafeSslSocketFactory();

            return this;
        }
        /**
         * 使用预埋证书，校验服务端证书（自签名证书）
         * @return
         */
        public Builder unsafeSSLSocketFactory(InputStream[] certificates){
           this.sslFactory =  HttpsDelegate.getUnsafeSslSocketFactory(certificates);
            return this;
        }

        /**
         *  使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
         * @param certificates
         * @param bksFile
         * @param password
         * @return
         */
        public Builder safeSSLSocketFactory(InputStream[] certificates, InputStream bksFile, String password){
           this.sslFactory =  HttpsDelegate.getSslSocketFactory(certificates,bksFile,password);
            return this;
        }
        public RetrofitClient build(){
                Retrofit  retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(OkHttpClientBuilder.builder(context).build())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
            return new RetrofitClient(retrofit);
        }
    }

    private static void create(Context c){
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpClientBuilder.builder(c.getApplicationContext()).build())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    public  <T> T getService(Class<T> service){
        return retrofit.create(service);
    }
    public <T> T getHackedService(Class<T> service){
        return ProxyHandler.create(retrofit,service);
    }


    public void uploadByPartmap(String token,String catalog, int mode, String id, File file, HashMap<String,String> params){

        RequestBody catalogRB = RequestBody.create(null, catalog);
        RequestBody idRB = RequestBody.create(null, id);
        RequestBody modeRB = RequestBody.create(MediaType.parse("text/plain"), token);
        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        for(Map.Entry<String,String> entry: params.entrySet()){
            RequestBody value =RequestBody.create(null, entry.getValue());
            requestBodyMap.put(entry.getKey(),value);
        }
//        RequestBody requestFile =
//                RequestBody.create(MediaType.parse("application/otcet-stream"), file);
        MultipartBody.Part fileBody = MultipartBody.Part.createFormData(
                "file",
                file.getName(),
                RequestBody.create(MediaType.parse("image/*"), file));
        RetrofitClient.getRetrofit(BaseApplication.getInstance()).create(GitApiInterface.class).uploadImage(token,catalogRB,idRB,modeRB,fileBody);
    }
    public void upload(String token, int mode, String id, File file, HashMap<String,String> params){
        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        for(Map.Entry<String,String> entry: params.entrySet()){
            RequestBody value = RequestBody.create(MediaType.parse("multipart/form-body"), entry.getValue());
            requestBodyMap.put(entry.getKey(),value);
        }
        String fileName = "file\"; filename=\"" + file.getName();
        requestBodyMap.put(fileName, RequestBody.create(MediaType.parse("multipart/form-body"), file));
        RetrofitClient.getRetrofit(BaseApplication.getInstance()).create(GitApiInterface.class).uploadImagePartMap(requestBodyMap);
    }
    public interface GitApiInterface {

        @Multipart
        @POST("File/upload")
        Observable<Boolean> uploadImage(
                @Header(RetrofitClient.TOKEN_HEADER_KEY) String token,
                @Part("catalog") RequestBody catalog,
                @Part("doctorId") RequestBody id,
                @Part("mode") RequestBody mode,
                @Part MultipartBody.Part image
        );
        @Multipart
        @POST("File/upload")
        Observable<Boolean> uploadImagePartMap(
                @PartMap Map<String, RequestBody> params
        );
        @POST("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/logon/login")
        Observable<LoginResponse> loginAttr(@BodyJsonAttr("uid") String uid, @BodyJsonAttr("pwd") String pwd, @BodyJsonAttr("rid") String rid, @BodyJsonAttr("forAccessToken") boolean forAccessToken);
        @POST("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/logon/login")
        Observable<LoginResponse> login(@Body LoginRequest request);
        @POST("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/logon/login")
        Observable<LoginResponse.UserRoles> loginRoles(@Body LoginRequest request);
        @POST()
        @Headers({
            "CHACHE_DYNAMIC_KEY:11",
            "CHACHE_DYNAMIC_KEY_GROUP:sdw",
            "CACHEINTERCEPTOR_CACHE_TIME:36000"
        })
        @ServiceAndMethod(method = "getOgranAddrArea",serviceId = "eh.organ")
        Observable<OrganAddrArea> loginWithArray(@Url String url, @ArrayItem int id );

        @POST("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/*.jsonRequest")
        @ServiceAndMethod(method = "getPatientNum",serviceId = "eh.relationDoctor")
        Observable<JsonObject> getPatientNum(@ArrayItem int id );


        @POST("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/*.jsonRequest")
        @Headers({
            "X-Service-Id:eh.relationDoctor",
            "X-Service-Method:getPatientNum"
        })
        Observable<JsonObject> getPatientNumByHeader(@Body ArrayList<Integer> integers);
        @POST("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/*.jsonRequest")
        @Headers({
            "X-Service-Id:eh.unLoginSevice",
            "X-Service-Method:getAddrArea",
            "CHACHE_DYNAMIC_KEY:getAddrArea",
            "CHACHE_DYNAMIC_KEY_GROUP:eh.unLoginSevice",
            "CACHEINTERCEPTOR_CACHE_TIME:60"
        })
        Observable<JsonObject> getAddrArea(@ArrayItem String arg,@ArrayItem int areaCode );
        @GET("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/eh.mpi.dictionary.PatientType.dic?limit=0")
        Observable<JsonObject> getHealthCardTypeDict();


    }
}
