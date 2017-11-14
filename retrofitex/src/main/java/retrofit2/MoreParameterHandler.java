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
    final private String paramName;
    final private Annotation annotation;
    final private int index;

    MoreParameterHandler(String paramName, Type type ,Annotation annotation,int index) {
        this.type = checkNotNull(type, "type == null");
        this.paramName = paramName;
        this.annotation = annotation;
        this.index = index;
    }

    @Override
    void apply(RequestBuilder builder, T value) throws IOException {
        //do nothing
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
    public int getIndex(){
        return  index;
    }
}
