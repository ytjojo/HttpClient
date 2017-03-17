package retrofit2;

import com.ytjojo.utils.TextUtils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.ytjojo.utils.CollectionUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * Created by Administrator on 2016/10/27 0027.
 */
public class ParameterMerge {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private String serviceId,method;
    public void setServiceIdAndMethod(String serviceId,String method){
        this.serviceId =serviceId;
        this.method = method;
    }
    public RequestBuilder merge(RequestBuilder builder, ArrayList<AttrParameterHandler<?>> handlers) throws IOException {
        if(!TextUtils.isEmpty(serviceId) && !TextUtils.isEmpty(method)){
            mergeArrayBodyJson(builder,handlers);
        }else{
            mergeAttrBodyJson(builder,handlers);
        }
        return builder;
    }
    private void  mergeArrayBodyJson(RequestBuilder builder, ArrayList<AttrParameterHandler<?>> handlers) throws IOException{
        Gson gson = new Gson();
        Buffer buffer = new Buffer();
        Writer jsonWriter = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        JsonWriter writer = gson.newJsonWriter(jsonWriter);
        writer.setSerializeNulls(true);
        writer.beginObject();
        writer.name("serviceId").value(serviceId)
                .name("method").value(method)
                .name("body")
                .beginArray();
        if(!CollectionUtils.isEmpty(handlers)){
            for (AttrParameterHandler<?> handler:handlers) {
                Object item = handler.getValue();
                if (item == null) {
                    writer.nullValue();
                } else {
                    Type type = handler.getType();
                    if(Primitives.isPrimitive(handler.getType())){
//                        Type type =  $Gson$Types.canonicalize(handler.getType());
//                        $Gson$Types.getRawType(type);
                        TypeAdapter<?> typeAdapter = gson.getAdapter(TypeToken.get(type));
                        TypeAdapter<Object> adapter = (TypeAdapter<Object>) typeAdapter;
                        adapter.write(writer,handler.getValue());
                    }else{
                        writer.jsonValue(gson.toJson(item, handler.getType()));
                    }
                }
            }
        }
        writer.endArray()
                .endObject();
        writer.close();
        builder.setBody(RequestBody.create(MEDIA_TYPE, buffer.readByteString()));
    }
    private void  mergeAttrBodyJson(RequestBuilder builder, ArrayList<AttrParameterHandler<?>> handlers) throws IOException{
        Gson gson = new Gson();
        Buffer buffer = new Buffer();
        Writer jsonWriter = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        JsonWriter writer = gson.newJsonWriter(jsonWriter);
        writer.setSerializeNulls(true);
        writer.beginObject();
        if(!CollectionUtils.isEmpty(handlers)){
            for (AttrParameterHandler<?> handler:handlers) {
                Object item = handler.getValue();
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
                        adapter.write(writer,handler.getValue());
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
