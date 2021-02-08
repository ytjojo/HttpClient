package com.jiulongteng.http.croutines

import com.jiulongteng.http.annotation.RawString
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Url


public interface SuspendService {
    @RawString
    @GET
    suspend fun get(@HeaderMap headers: Map<String, String>, @Url url: String): Response<ResponseBody>
}
