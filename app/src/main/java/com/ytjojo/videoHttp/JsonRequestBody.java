package com.ytjojo.videoHttp;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2016/12/1 0001.
 */
public class JsonRequestBody {
    public static final MediaType JSON=MediaType.parse("application/json; charset=utf-8");
   public final static ObjectMapper mapper = new ObjectMapper();
    static {
        SimpleDateFormat fmt = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(fmt);

        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);//����ת���ַ�
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);//��������
//        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static Builder newBuilder(){
        return new Builder();
    }
    public static class Builder{
        String serviceId;
        String method;
        ArrayList<ValueHolder<?>> mTypeValues = new ArrayList<>();

        public Builder serviceId(String serviceId){
            this.serviceId = serviceId;
            return this;
        }
        public Builder method(String method){
            this.method = method;
            return this;
        }

        public <T> Builder anyItem(T value){
            mTypeValues.add(new ValueHolder(value));
            return this;
        }
        public RequestBody body(){
//            JavaType javaType = mapper.getTypeFactory().constructType(type);
//            ObjectWriter writer =  mapper.writerFor(javaType);
//            writer.writeValueAsString("sdw");
            RequestBody requestBody =null;
            try {
                StringWriter sw = new StringWriter();
                JsonGenerator generator = mapper.getFactory().createGenerator(sw);
                generator.writeStartObject();
                generator.writeStringField("serviceId", serviceId);
                generator.writeStringField("method", method);
                generator.writeFieldName("body");
                generator.writeStartArray();
                for(ValueHolder<?> typeValue:mTypeValues){
                    generator.writeObject(typeValue.value);

                }
                generator.writeEndArray();
                generator.writeEndObject();
                generator.close();
                String json = sw.toString();
                System.out.println(json);
                requestBody = RequestBody.create(JSON, json);

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return requestBody;

        }

        public <T> ResponseDispatcher post(TypeReference<ResponseWraper<T>> reference){
            return new ResponseDispatcher(body()).responseType(reference);
        }
    }
    public static class ValueHolder<T>{
        T value;
        public ValueHolder(T value){
            this.value = value;
        }

    }
}
