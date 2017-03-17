package com.ytjojo.practice;

import com.google.gson.Gson;
import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.domin.vo.LoginResponse;
import com.ytjojo.http.RetrofitClient;
import com.ytjojo.http.coverter.GsonConverterFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.Scanner;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.functions.Action1;

import static java.util.UUID.randomUUID;
/**
 * Created by Administrator on 2016/11/6 0006.
 */
public class ServiceTestBase {
    protected static final int NUMBER_NEGATIVE_ONE = -1;
    protected static final int NUMBER_ZERO = 0;
    protected static final int NUMBER_ONE = 1;
    protected static final int DENSITY_LDPI = 36;
    protected static final int DENSITY_MDPI = 48;
    protected static final int DENSITY_HDPI = 72;
    protected static final int DENSITY_XHDPI = 96;
    protected static final int DENSITY_XXHDPI = 144;
    protected static final int DENSITY_XXXHDPI = 192;
    protected static final String STRING_EMPTY = "";
    protected static final String STRING_NULL = null;
    protected static final String STRING_UNIQUE = randomUUID().toString();
    protected static final String STRING_UNIQUE2 = randomUUID().toString() + randomUUID().toString();
    protected static final String STRING_UNIQUE3 = randomUUID().toString();
    protected static final Integer INTEGER_RANDOM = new Random().nextInt();
    protected static final Integer INTEGER_RANDOM_POSITIVE = new Random().nextInt(Integer.SIZE - 1);
    protected static final Long LONG_RANDOM = new Random().nextLong();
    protected static final Float FLOAT_RANDOM = new Random().nextFloat();
    protected static final Double DOUBLE_RANDOM = new Random().nextDouble();
    Retrofit mRetrofit;
    private void sendMockMessages(String fileName) throws Exception {
        final InputStream stream = getClass().getResourceAsStream(fileName);
        final String mockResponse = new Scanner(stream, Charset.defaultCharset().name())
                .useDelimiter("\\A").next();

        server.enqueue(new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(mockResponse));

        stream.close();
    }
    private Retrofit.Builder getRetrofit(String endPoint) {
        return new Retrofit.Builder()
                .baseUrl(endPoint)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .client(new OkHttpClient());
    }
    @Rule
    public final MockWebServer server = new MockWebServer();
    protected String mockEndPoint;

    @Before
    public void setUp() throws Exception {
        mockEndPoint = server.url("/").toString();
        mRetrofit = getRetrofit(mockEndPoint).build();
    }

    @After
    public void tearDown() throws Exception {

        server.shutdown();
    }
    @Test
    public void testLogin() throws Exception {
        // Response
        sendMockMessages("/login.json");
        RetrofitClient.GitApiInterface gitApiInterface = mRetrofit.create(RetrofitClient.GitApiInterface.class);
        gitApiInterface.login(new LoginRequest()).subscribe(new Action1<LoginResponse>() {
            @Override
            public void call(LoginResponse response) {

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

            }
        });
    }
}
