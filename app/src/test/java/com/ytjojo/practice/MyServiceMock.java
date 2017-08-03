package com.ytjojo.practice;

import com.google.gson.JsonObject;
import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.domin.response.OrganAddrArea;
import com.ytjojo.domin.vo.LoginResponse;
import com.ytjojo.http.GitApiInterface;
import com.ytjojo.http.RetrofitClient;
import java.util.ArrayList;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.ArrayItem;
import retrofit2.http.Body;
import retrofit2.http.BodyJsonAttr;
import retrofit2.http.Header;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;
import retrofit2.mock.BehaviorDelegate;
import rx.Observable;

public class MyServiceMock implements GitApiInterface {
    private final BehaviorDelegate<GitApiInterface> delegate;

    public MyServiceMock(BehaviorDelegate<GitApiInterface> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Observable<Boolean> uploadImage(@Header(RetrofitClient.TOKEN_HEADER_KEY) String token, @Part("catalog") RequestBody catalog, @Part("doctorId") RequestBody id, @Part("mode") RequestBody mode, @Part MultipartBody.Part image) {
        return null;
    }

    @Override
    public Observable<Boolean> uploadImagePartMap(@PartMap Map<String, RequestBody> params) {
        return null;
    }

    @Override
    public Observable<LoginResponse> loginAttr(@BodyJsonAttr("uid") String uid, @BodyJsonAttr("pwd") String pwd, @BodyJsonAttr("rid") String rid, @BodyJsonAttr("forAccessToken") boolean forAccessToken) {
        return null;
    }

    @Override
    public rx.Observable<LoginResponse> login(@Body LoginRequest request) {
        return Observable.just(new LoginResponse(200,"",new LoginResponse.UserRoles()));
    }

    @Override
    public Observable<LoginResponse.UserRoles> loginRoles(@Body LoginRequest request) {
        return null;
    }

    @Override
    public Observable<OrganAddrArea> loginWithArray(@Url String url, @ArrayItem int id) {
        return null;
    }

    @Override public Observable<JsonObject> getPatientNum(@ArrayItem int id) {
        return null;
    }

    @Override public Observable<JsonObject> getPatientNumByHeader(@Body ArrayList<Integer> integers) {
        return null;
    }

    @Override
    public Observable<JsonObject> getAddrArea(@ArrayItem String arg, @ArrayItem int areaCode) {
        return null;
    }

    @Override public Observable<JsonObject> getHealthCardTypeDict() {
        return null;
    }
}