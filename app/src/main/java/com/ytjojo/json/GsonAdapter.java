package com.ytjojo.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.Expose;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ytjojo.utils.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/24 0024.
 */
public class GsonAdapter {

    public static class NullStringToEmptyAdapterFactory<T> implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<T> rawType = (Class<T>) type.getRawType();
            if (rawType != String.class) {
                return null;
            }
            return (TypeAdapter<T>) new StringNullAdapter();
        }
    }

    public static class StringNullAdapter extends TypeAdapter<String> {
        @Override
        public String read(JsonReader reader) throws IOException {
            // TODO Auto-generated method stub
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return "";
            }
            return reader.nextString();
        }

        @Override
        public void write(JsonWriter writer, String value) throws IOException {
            // TODO Auto-generated method stub
            if (value == null) {
                writer.nullValue();
                return;
            }
            writer.value(value);
        }
    }

    public static Gson getDefaultNullStringGson() {
        GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(String.class, new TypeAdapter<String>() {

            @Override
            public void write(JsonWriter out, String value) throws IOException {
                if (value == null) {
                    // out.nullValue();
                    out.value(""); // 序列化时将 null 转为 ""
                } else {
                    out.value(value);
                }
            }

            @Override
            public String read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                // return in.nextString();
                String str = in.nextString();
                if (str.equals("")) { // 反序列化时将 "" 转为 null
                    return null;
                } else {
                    return str;
                }
            }

        });

        return gsonBuilder.create();
    }

    public static class StringConverter implements JsonSerializer<String>,
            JsonDeserializer<String> {
        public JsonElement serialize(String src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            if (src == null) {
                return new JsonPrimitive("");
            } else {
                return new JsonPrimitive(src.toString());
            }
        }

        public String deserialize(JsonElement json, Type typeOfT,
                                  JsonDeserializationContext context)
                throws JsonParseException {
            return json.getAsJsonPrimitive().getAsString();
        }


    }

    public static Gson get() {
        GsonBuilder mBuilder = new GsonBuilder();
        mBuilder.registerTypeAdapter(String.class, new StringConverter());
        return mBuilder.create();
    }

    public static Gson getdeserializeNotNullGson() {
        GsonBuilder gsonBulder = new GsonBuilder();
        gsonBulder.setDateFormat("yyyy-MM-dd");
        gsonBulder.registerTypeAdapter(String.class, new StringNullAdapter());   //所有String类型null替换为字符串“”

        //通过反射获取instanceCreators属性
        try {
            Class builder = (Class) gsonBulder.getClass();
            Field f = builder.getDeclaredField("instanceCreators");
            f.setAccessible(true);
            Map<Type, InstanceCreator<?>> val = (Map<Type, InstanceCreator<?>>) f.get(gsonBulder);//得到此属性的值
            //注册数组的处理器
            gsonBulder.registerTypeAdapterFactory(new CollectionTypeAdapterFactory(new ConstructorConstructor(val)));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return gsonBulder.create();
    }

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String toJson(Gson gson, Object value) {
        return gson.toJson(value);
    }
    public static String toJson(Gson gson, Object value,Type type) {

        return gson.toJson(value,type);
    }

    /**
     * Json字符串 转为指定对象
     *
     * @param json json字符串
     * @param type 对象类型
     * @param <T>  对象类型
     * @return
     * @throws JsonSyntaxException
     */
    public static <T> T toBean(Gson gson, String json, Class<T> type) {
        try {
            Type genType = type.getGenericSuperclass();
            if (genType ==null ||genType instanceof Class) {
                return gson.fromJson(json, type);
            }
            Type knowType = TypeUtils.canonicalize(genType);
            T obj = gson.fromJson(json, knowType);
            return obj;
        } catch (JsonSyntaxException e) {

        }
        return null;
    }

    public static Gson getSerializationExclusionGson(){
       return new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        // 这里作判断，决定要不要排除该字段,return true为排除
                        if ("finalField".equals(f.getName())) return true; //按字段名排除
                        Expose expose = f.getAnnotation(Expose.class);
                        if (expose != null && expose.deserialize() == false) return true; //按注解排除
                        return false;
                    }
                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        // 直接排除某个类 ，return true为排除
//                        return (clazz == int.class || clazz == Integer.class);
                        return false;
                    }
                })
               .addDeserializationExclusionStrategy(new ExclusionStrategy() {
                   @Override
                   public boolean shouldSkipField(FieldAttributes f) {
                       return false;
                   }

                   @Override
                   public boolean shouldSkipClass(Class<?> clazz) {
                       return false;
                   }
               })
                .create();
    }
}