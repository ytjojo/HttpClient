/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ytjojo.http.coverter;

import com.ytjojo.utils.TextUtils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.ytjojo.http.ResponseWrapper;
import com.ytjojo.http.exception.APIException;
import com.ytjojo.http.exception.AuthException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Converter;

final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
  public final int RESPONSE_DEFAULT_CODE =-200;
  private final Gson mGson;
  private final Type type;
  private TypeAdapter<T> adapter;
  GsonResponseBodyConverter(Gson gson, Type type) {
    this.mGson = gson;
    this.type = type;
}

  @Override public T convert(ResponseBody responseBody) throws IOException {


    BufferedSource bufferedSource = Okio.buffer(responseBody.source());
    String value = bufferedSource.readUtf8();
    bufferedSource.close();
    if(TextUtils.isEmpty(value)){
      throw new APIException(-3,"response is null");
    }
    try {
      JSONObject response = new JSONObject(value);
      int code = response.optInt("code");
      String msg = response.optString("msg");
      if (code != ResponseWrapper.RESULT_OK) {
        if(code == ResponseWrapper.EXCEPTION_TOKEN_NOTVALID){
          throw new AuthException(code,msg);
        }
        //返回的code不是RESULT_OK时Toast显示msg
        throw new APIException(code, msg, value);
      }
      if (type instanceof Class) {
        if (type == String.class) {
          return (T) value;
        }
        if (type == JSONObject.class) {
          //如果返回结果是JSONObject则无需经过Gson
          return (T) response;
        }
      } else if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (parameterizedType.getRawType() == ResponseWrapper.class) {
          String data = response.optString("body");
          Type dataType = parameterizedType.getActualTypeArguments()[0];
          if (dataType == JSONObject.class) {
            return (T) new ResponseWrapper<>(code, msg, new JSONObject(data));
          }
          return mGson.fromJson(value, type);

        }
      }

//      try {
//        return adapter.fromJson(responseBody.charStream());
//      } finally {
//        responseBody.close();
//      }
      return mGson.fromJson(value, type);
    } catch (JSONException e) {
      //服务端返回的不是JSON，服务端出问题
      throw new APIException(ResponseWrapper.EXCEPTION_CCONVERT_JSON, "", value);
    }

  }
}
