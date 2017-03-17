package com.ytjojo.practice;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.ytjojo.domin.vo.User;
import com.ytjojo.utils.Interception;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;

import okhttp3.MediaType;
import okio.Buffer;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    Gson gson;
    @Before
    public void setUp(){
        gson =new Gson();

    }
    @Test(expected = JSONException.class)
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        Json json= create(Json.class);
        ArrayList<User> arr = new ArrayList<>();
        User user = new User(3);
        user.setEmail("ytjojo@163.com");
        arr.add(user);
        User user1 = new User(4);
        user1.setEmail("ytjojo@163.com");
        arr.add(user1);
        json.convert(arr);
    }
    public interface Json{
        String convert(ArrayList<User> value);
    }
    @SuppressWarnings("unchecked")
    private <T> T create(Class<T> clazz){
        return (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new Interception.InterceptionHandler<T>() {
            @Override
            public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
                Type[] generic = method.getGenericParameterTypes();
                return gson(generic,args);
            }
        });
    }
    @Test
    @Ignore("not implemented yet")
    public void testFactorial() {
    }
    private String gson( Type[] types,Object[] args) throws IOException{
        Gson gson = new Gson();
        Buffer buffer = new Buffer();
        Writer jsonWriter = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        JsonWriter writer = gson.newJsonWriter(jsonWriter);
        writer.setSerializeNulls(true);
        writer.beginObject();
        int count = types.length;
        for (int i=0;i <count;i++) {
            Object item = args[i];
            writer.name("abc");
            if (item == null) {
                writer.nullValue();
            } else {
                Type type =types[i];
                if(Primitives.isPrimitive(type)){
//                        Type type =  $Gson$Types.canonicalize(handler.getType());
//                        $Gson$Types.getRawType(type);
                    TypeAdapter<?> typeAdapter = gson.getAdapter(TypeToken.get(type));
                    TypeAdapter<Object> adapter = (TypeAdapter<Object>) typeAdapter;
                    adapter.write(writer,args[i]);
                }else{
                    writer.jsonValue(gson.toJson(item, type));
                }
            }
        }
        writer.endObject();
        writer.close();
        String result=  buffer.readUtf8();
        System.out.println(result);
        return result;
    }
}