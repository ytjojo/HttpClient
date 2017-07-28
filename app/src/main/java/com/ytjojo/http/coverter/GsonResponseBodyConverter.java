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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.$Gson$Types;
import com.ytjojo.http.ResponseWrapper;
import com.ytjojo.http.exception.APIException;
import com.ytjojo.http.exception.TokenInvalidException;
import com.ytjojo.utils.TextUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Converter;

final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
	public final int RESPONSE_DEFAULT_CODE = -200;
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
		if (TextUtils.isEmpty(value)) {
			throw new APIException(-3, "response is null");
		}

		JsonElement root = mGson.fromJson(value, JsonElement.class);
		JsonObject response = root.getAsJsonObject();

		int code = response.get("code").getAsInt();
		JsonElement msgJE = response.get("msg");
		String msg = msgJE == null ? null : msgJE.getAsString();
		if (code != ResponseWrapper.RESULT_OK) {
			if (code == ResponseWrapper.EXCEPTION_TOKEN_NOTVALID) {
				throw new TokenInvalidException(code, msg);
			} else {
				//返回的code不是RESULT_OK时Toast显示msg
				throw new APIException(code, msg, value);
			}
		}
		if (type instanceof Class) {
			if (type == String.class) {
				return (T) value;
			}
			if (type == JsonObject.class) {
				//如果返回结果是JSONObject则无需经过Gson
				return (T)(response);
			}
			if(!(ResponseWrapper.class.isAssignableFrom((Class<?>) type))){
				Type wrapperType = $Gson$Types.newParameterizedTypeWithOwner(null,ResponseWrapper.class,type);
				ResponseWrapper<?> wrapper = mGson.fromJson(value, wrapperType);
				return (T) wrapper.body;
			}
		}
		return mGson.fromJson(value, type);
	}
}
