package com.ytjojo.practice;

import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.domin.vo.LoginResponse;
import com.ytjojo.http.RetrofitClient;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.mock.BehaviorDelegate;
import retrofit2.mock.MockRetrofit;
import retrofit2.mock.NetworkBehavior;
import rx.observers.TestSubscriber;

/**
 * Created by Administrator on 2016/11/6 0006.
 */
public class MyserviceTest {
    private final NetworkBehavior behavior = NetworkBehavior.create();
    private final rx.observers.TestSubscriber<LoginResponse> testSubscriber = TestSubscriber.create();
    private RetrofitClient.GitApiInterface mockService;

    @Before
    public void setUp() throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl("http://example.com").build();

        MockRetrofit mockRetrofit = new MockRetrofit.Builder(retrofit)
                .networkBehavior(behavior).build();

        final BehaviorDelegate<RetrofitClient.GitApiInterface> delegate = mockRetrofit.create(RetrofitClient.GitApiInterface.class);

        mockService = new MyServiceMock(delegate);
    }

    @Test
    public void testSuccessResponse() throws Exception {
        givenNetworkFailurePercentIs(0);

        mockService.login(new LoginRequest()).subscribe(testSubscriber);

        testSubscriber.assertValue(new LoginResponse(200,"",new LoginResponse.UserRoles()));
        testSubscriber.assertCompleted();
    }

    @Test
    public void testFailureResponse() throws Exception {
        givenNetworkFailurePercentIs(100);

        mockService.login(new LoginRequest()).subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertError(IOException.class);
    }

    private void givenNetworkFailurePercentIs(int failurePercent) {
        behavior.setDelay(0, TimeUnit.SECONDS);
        behavior.setVariancePercent(0);
        behavior.setFailurePercent(failurePercent);
    }
}
