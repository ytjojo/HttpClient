package retrofit2;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.ytjojo.http.RetrofitClient;
import com.ytjojo.http.util.CollectionUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.http.ArrayItem;
import retrofit2.http.BodyJsonAttr;
import retrofit2.http.NgariJsonPost;

/**
 * Created by Administrator on 2016/10/27 0027.
 */
public class ParameterMerge {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private String serviceId,method;
    public RequestBuilder merge(RequestBuilder builder,ArrayList<Annotation> annotations, ArrayList<MoreParameterHandler<?>> handlers,Object... args) throws IOException {
        if(handlers ==null ||handlers.isEmpty()){
            return builder;
        }
        if(annotations !=null){
            for(Annotation annotation :annotations){
                if(annotation instanceof NgariJsonPost){
                    NgariJsonPost ngariJsonPost = (NgariJsonPost) annotation;
                    this.method = ngariJsonPost.method();
                    this.serviceId = ngariJsonPost.serviceId();
                }
            }
        }
        if(handlers.get(0).getAnnotation() instanceof ArrayItem){
            mergeArrayBodyJson(builder,handlers,args);
        }else if(handlers.get(0).getAnnotation() instanceof BodyJsonAttr){
            mergeAttrBodyJson(builder,handlers,args);
        }else{
            MergeParameterHandler handler = RetrofitClient.getMergeParameterHandler();
            if(handler != null){
                handler.merge(builder,annotations,handlers,args);
            }
        }
        return builder;
    }
    private void  mergeArrayBodyJson(RequestBuilder builder, ArrayList<MoreParameterHandler<?>> handlers,Object... args) throws IOException{
        Gson gson = new Gson();
        Buffer buffer = new Buffer();
        Writer jsonWriter = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        JsonWriter writer = gson.newJsonWriter(jsonWriter);
        writer.setSerializeNulls(true);
        if(serviceId!=null && method !=null){
            writer.beginObject();
            writer.name("serviceId").value(serviceId)
                .name("method").value(method)
                .name("body");
        }
        writer .beginArray();
        if(!CollectionUtils.isEmpty(handlers)){
            for (MoreParameterHandler<?> handler:handlers) {
                final int index = handler.getIndex();
                final Object item = args[index];
                if (item == null) {
                    writer.nullValue();
                } else {
                    Type type = handler.getType();
                    if(Primitives.isPrimitive(handler.getType())){
//                        Type type =  $Gson$Types.canonicalize(handler.getType());
//                        $Gson$Types.getRawType(type);
                        TypeAdapter<Object> adapter = (TypeAdapter<Object>)  gson.getAdapter(TypeToken.get(type));
                        adapter.write(writer,item);
                    }else{
                        writer.jsonValue(gson.toJson(item, handler.getType()));
                    }
                }
            }
        }
        writer.endArray();
        if(serviceId!=null && method !=null){
            writer.endObject();
        }
        writer.close();
        builder.setBody(RequestBody.create(MEDIA_TYPE, buffer.readByteString()));
    }
    private void  mergeAttrBodyJson(RequestBuilder builder, ArrayList<MoreParameterHandler<?>> handlers,Object... args) throws IOException{
        Gson gson = new Gson();
        Buffer buffer = new Buffer();
        Writer jsonWriter = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        JsonWriter writer = gson.newJsonWriter(jsonWriter);
        writer.setSerializeNulls(true);
        writer.beginObject();
        if(!CollectionUtils.isEmpty(handlers)){
            for (MoreParameterHandler<?> handler:handlers) {
                final int index = handler.getIndex();
                final Object item = args[index];
                writer.name(handler.getParamName());
                if (item == null) {
                    writer.nullValue();
                } else {
                    Type type = handler.getType();
                    if(Primitives.isPrimitive(handler.getType())){
//                        Type type =  $Gson$Types.canonicalize(handler.getType());
//                        $Gson$Types.getRawType(type);
                        TypeAdapter<?> typeAdapter = gson.getAdapter(TypeToken.get(type));
                        TypeAdapter<Object> adapter = (TypeAdapter<Object>) typeAdapter;
                        adapter.write(writer,item);
                    }else{
                        writer.jsonValue(gson.toJson(item, handler.getType()));
                    }
                }
            }
        }
        writer.endObject();
        writer.close();
        builder.setBody(RequestBody.create(MEDIA_TYPE, buffer.readByteString()));
    }
}
