package retrofit2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import static retrofit2.Utils.checkNotNull;

/**
 * Created by Administrator on 2016/10/23 0023.
 */
public class AttrParameterHandler<T> extends ParameterHandler<T> {
    private final Type type;
//    private final Converter<T, String> converter;
    private String paramName;
    private Object value;

    AttrParameterHandler(String paramName, Type type ) {
        this.type = checkNotNull(type, "type == null");
        this.paramName = paramName;
    }

    @Override
    void apply(RequestBuilder builder, T value) throws IOException {
        this.value = value;
    }
    public Object getValue(){
      return value;

    }

    public Type getType(){
        return type;
    }
    public String getParamName(){
        return paramName;
    }
    public static JSONObject get(String serviceId, String method) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        return jsonObject.put("serviceId", serviceId).put("method", method).put("body", jsonArray);
    }


    public static JSONObject appendArray(JSONObject json, Object value) throws JSONException {
        json.getJSONArray("body").put(value);
        return json;
    }
}
