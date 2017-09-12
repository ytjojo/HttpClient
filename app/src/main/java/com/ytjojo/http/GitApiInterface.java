package com.ytjojo.http;

import com.google.gson.JsonObject;
import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.domin.response.OrganAddrArea;
import com.ytjojo.domin.vo.LoginResponse;
import java.util.ArrayList;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.ArrayItem;
import retrofit2.http.Body;
import retrofit2.http.BodyJsonAttr;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.NgariJsonPost;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;
import rx.Observable;

public interface GitApiInterface {

        @Multipart @POST("File/upload")
        Observable<Boolean> uploadImage(
                @Header("X-Access-Token") String token,
                @Part("catalog") RequestBody catalog,
                @Part("doctorId") RequestBody id,
                @Part("mode") RequestBody mode,
                @Part MultipartBody.Part image
        );
        @Multipart
        @POST("File/upload") Observable<Boolean> uploadImagePartMap(
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
        @NgariJsonPost(method = "getOgranAddrArea",serviceId = "eh.organ")
        Observable<OrganAddrArea> loginWithArray(@Url String url, @ArrayItem int id );

        @POST("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/*.jsonRequest")
        @NgariJsonPost(method = "getPatientNum",serviceId = "eh.relationDoctor")
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
        @GET("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/eh.mpi.dictionary.PatientType.dic?limit=0")
        Observable<Void> getHealthCardTypeDict1();


    }