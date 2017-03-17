package com.ytjojo.practice;

import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.domin.response.OrganAddrArea;
import com.ytjojo.domin.vo.LoginResponse;
import com.ytjojo.http.RetrofitClient;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.ArrayItem;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.JsonAttr;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;
import retrofit2.mock.BehaviorDelegate;
import rx.Observable;

public class MyServiceMock implements RetrofitClient.GitApiInterface {
    private final BehaviorDelegate<RetrofitClient.GitApiInterface> delegate;

    public MyServiceMock(BehaviorDelegate<RetrofitClient.GitApiInterface> delegate) {
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
    public Observable<LoginResponse> loginAttr(@JsonAttr("uid") String uid, @JsonAttr("pwd") String pwd, @JsonAttr("rid") String rid, @JsonAttr("forAccessToken") boolean forAccessToken) {
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
}