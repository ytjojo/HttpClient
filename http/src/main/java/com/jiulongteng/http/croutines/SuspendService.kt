package com.jiulongteng.http.croutines

import com.jiulongteng.http.annotation.RawString
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


public interface SuspendService {
    @RawString
    @GET
    suspend fun get(@HeaderMap headers: Map<String, String>, @Url url: String): Response<ResponseBody>

    @RawString
    @Multipart
    @POST
    suspend fun multipartPost(@HeaderMap headers: Map<String, String>, @Url url: String, @Part partList: List<MultipartBody.Part>): Response<ResponseBody>

    @RawString
    @POST
    suspend fun post(@HeaderMap headers: Map<String, String>, @Url url: String): Response<ResponseBody>

    @RawString
    @POST
    suspend fun post(@HeaderMap headers: Map<String, String>, @Url url: String, @Body body: Any): Response<ResponseBody>

    @POST
    @RawString
    suspend fun upload(@HeaderMap headers: Map<String, String>, @Url url: String, @Body multipartBody: MultipartBody): Response<ResponseBody>
}
