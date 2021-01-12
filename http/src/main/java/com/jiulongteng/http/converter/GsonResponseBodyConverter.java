package com.jiulongteng.http.converter;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.$Gson$Types;
import com.jiulongteng.http.entities.IResult;
import com.jiulongteng.http.entities.StandardResult;
import com.jiulongteng.http.exception.APIException;
import com.jiulongteng.http.exception.JsonException;
import com.jiulongteng.http.exception.TokenInvalidException;;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;
import retrofit2.Converter;

final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private TypeAdapter<T> adapter;

    private final Gson mGson;

    private final Type type;

    GsonResponseBodyConverter(Gson paramGson, Type paramType) {
        this.mGson = paramGson;
        this.type = paramType;
    }

    public T convert(ResponseBody responseBody) throws IOException {
        BufferedSource bufferedSource = Okio.buffer((Source) responseBody.source());
        String responseStingBody = bufferedSource.readUtf8();
        bufferedSource.close();
        if (!TextUtils.isEmpty(responseStingBody))
            try {
                JsonObject jsonObject = ((JsonElement) this.mGson.fromJson(responseStingBody, JsonElement.class)).getAsJsonObject();
                int code = jsonObject.get("code").getAsInt();
                JsonElement messageElement = jsonObject.get("message");

                if (code != 40400) {
                    if (code == 0) {
                        if (this.type instanceof Class) {
                            if (this.type == String.class) {
                                JsonElement jsonElement = jsonObject.get("data");
                                return (T) jsonElement.getAsString();
                            }
                            if (this.type == Object.class){
                                return null;
                            }
                            if (this.type == Void.class){
                                return null;
                            }
                            if (JsonElement.class.isAssignableFrom((Class) this.type)){
                                return (T) jsonObject;
                            }
                            if (this.type instanceof ParameterizedType && IResult.class.isAssignableFrom((Class) type)){
                                return (T) this.mGson.fromJson(jsonObject, this.type);
                            }
                        }
                        if (this.type instanceof ParameterizedType && IResult.class.isAssignableFrom((Class) ((ParameterizedType) this.type).getRawType())){
                            return (T) this.mGson.fromJson(jsonObject, this.type);
                        }
                        ParameterizedType parameterizedType = $Gson$Types.newParameterizedTypeWithOwner(null, StandardResult.class, new Type[]{this.type});
                        return (T) ((StandardResult) this.mGson.fromJson(jsonObject, parameterizedType)).data;
                    }else {
                        APIException apiException = new APIException(code,messageElement.getAsString());
                        throw apiException;
                    }

                }else {
                    TokenInvalidException tokenInvalidException = new TokenInvalidException(40400, messageElement.getAsString());
                    throw tokenInvalidException;
                }

            } catch (TokenInvalidException tokenInvalidException) {
                throw tokenInvalidException;
            } catch (APIException aPIException) {
                throw aPIException;
            } catch (Exception exception) {
                throw new JsonException("json解析失败", exception);
            }
        throw new APIException(-3, "response is null");
    }
}
