package retrofit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static retrofit2.Utils.checkNotNull;

/**
 * Created by Administrator on 2016/10/23 0023.
 */
public class MoreParameterHandler<T> extends ParameterHandler<T> {
    private final Type type;
//    private final Converter<T, String> converter;
    private String paramName;
    private Object value;
    private Annotation annotation;

    MoreParameterHandler(String paramName, Type type ,Annotation annotation) {
        this.type = checkNotNull(type, "type == null");
        this.paramName = paramName;
        this.annotation = annotation;
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
    public Annotation getAnnotation(){
        return annotation;
    }
}
