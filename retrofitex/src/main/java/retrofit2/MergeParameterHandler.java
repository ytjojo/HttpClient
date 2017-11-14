package retrofit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.ArrayList;

import okhttp3.MediaType;

/**
 * Created by Administrator on 2017/10/12 0012.
 */

public interface MergeParameterHandler {
    MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    Charset UTF_8 = Charset.forName("UTF-8");
    RequestBuilder merge(RequestBuilder builder, ArrayList<Annotation> annotations, ArrayList<MoreParameterHandler<?>> handlers,Object... args) throws IOException;

}
