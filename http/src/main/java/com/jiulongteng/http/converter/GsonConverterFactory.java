package com.jiulongteng.http.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.jiulongteng.http.annotation.RawString;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public final class GsonConverterFactory extends Converter.Factory {
    private static Gson GSONINTANCE;

    static final StringResponseBodyConverter INSTANCE = new StringResponseBodyConverter();

    private final Gson gson;

    private GsonConverterFactory(Gson paramGson) {
        if (paramGson != null) {
            this.gson = paramGson;
            return;
        }
        throw new NullPointerException("gson == null");
    }

    public static GsonConverterFactory create() {
        return create(getGson());
    }

    public static GsonConverterFactory create(Gson paramGson) {
        return new GsonConverterFactory(paramGson);
    }

    public static Gson getGson() {
        if (GSONINTANCE == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.enableComplexMapKeySerialization().serializeNulls();
            GSONINTANCE = gsonBuilder.create();
        }
        return GSONINTANCE;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> typeAdapter = this.gson.getAdapter(TypeToken.get(type));
        return new GsonRequestBodyConverter(this.gson, typeAdapter);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type paramType, Annotation[] annotations, Retrofit retrofit) {
        if (annotations != null && annotations.length > 0) {
            int length = annotations.length;
            for (byte i = 0; i < length; i++) {
                if (annotations[i].annotationType().equals(RawString.class)) {
                    return INSTANCE;
                }
            }
        }
        return new GsonResponseBodyConverter(this.gson, paramType);
    }

    static final class StringResponseBodyConverter implements Converter<ResponseBody, String> {
        public String convert(ResponseBody responseBody) throws IOException {
            return responseBody.string();
        }
    }
}
