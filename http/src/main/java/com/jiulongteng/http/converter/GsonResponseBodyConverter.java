package com.jiulongteng.http.converter;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.$Gson$Types;
import com.jiulongteng.http.entities.IResult;
import com.jiulongteng.http.entities.StandardResult;
import com.jiulongteng.http.exception.APIException;
import com.jiulongteng.http.exception.ExceptionHandle;
import com.jiulongteng.http.exception.JsonException;
import com.jiulongteng.http.exception.TokenInvalidException;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;
import retrofit2.Converter;

;

final public class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private TypeAdapter<T> adapter;

    private final Gson mGson;

    private final Type type;

    private Class<? extends IResult> boundaryResultClass = StandardResult.class;
    GsonResponseBodyConverter(Gson paramGson, Type paramType) {
        this.mGson = paramGson;
        this.type = paramType;
    }

    public void setBoundaryResultClass(Class<? extends IResult> boundaryResultClass){
        this.boundaryResultClass = boundaryResultClass;
    }
    @Override
    public T convert(ResponseBody responseBody) throws IOException {
        BufferedSource bufferedSource = Okio.buffer((Source) responseBody.source());
        String responseStingBody = bufferedSource.readUtf8();
        bufferedSource.close();
        if (!TextUtils.isEmpty(responseStingBody))
            try {
                if(boundaryResultClass == null){
                    return (T) this.mGson.fromJson(responseStingBody, this.type);
                }
                IResult<JsonElement> entireResult = this.mGson.fromJson(responseStingBody,  $Gson$Types.newParameterizedTypeWithOwner(null, boundaryResultClass, new Type[]{this.type}));
                String message = entireResult.getMessage();
                if (!entireResult.isInvalidToken()) {
                    if (entireResult.isSuccessful()) {
                        if (this.type instanceof Class) {
                            if (this.type == String.class) {
                                JsonElement jsonElement = entireResult.getData();
                                return (T) jsonElement.getAsString();
                            }
                            if (this.type == Object.class) {
                                return null;
                            }
                            if (this.type == Void.class) {
                                return null;
                            }
                            if (JsonElement.class.isAssignableFrom((Class) this.type)) {
                                return (T) mGson.fromJson(responseStingBody, JsonElement.class);
                            }
                            if (IResult.class.isAssignableFrom((Class) type)) {
                                return (T) this.mGson.fromJson(responseStingBody, this.type);
                            }
                        }
                        if (this.type instanceof ParameterizedType && IResult.class.isAssignableFrom((Class) ((ParameterizedType) this.type).getRawType())) {
                            return (T) this.mGson.fromJson(responseStingBody, this.type);
                        }
                        ParameterizedType parameterizedType = $Gson$Types.newParameterizedTypeWithOwner(null, boundaryResultClass, new Type[]{this.type});
                        return (T) ((IResult) this.mGson.fromJson(responseStingBody, parameterizedType)).getData();
                    } else {
                        APIException apiException = new APIException(entireResult.getCode(), message);
                        throw apiException;
                    }

                } else {
                    TokenInvalidException tokenInvalidException = new TokenInvalidException(40400, message);
                    throw tokenInvalidException;
                }

            } catch (TokenInvalidException tokenInvalidException) {
                throw tokenInvalidException;
            } catch (APIException aPIException) {
                throw aPIException;
            } catch (Exception exception) {
                throw new JsonException("json解析失败", exception);
            }
        throw new NullPointerException("response is null");
    }
}
