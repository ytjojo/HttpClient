package com.ytjojo.practice;

import org.junit.Test;

import retrofit2.Retrofit;

/**
 * Created by Administrator on 2016/11/6 0006.
 */
public class MockRetrofitTest {
    @Test
    public void testMockRetrofit(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.baidu.com")
                .build();

        // Create a MockRetrofit object with a NetworkBehavior which manages the fake behavior of calls.
//        NetworkBehavior behavior = NetworkBehavior.create();
//        MockRetrofit mockRetrofit = new MockRetrofit.Builder(retrofit)
//                .networkBehavior(behavior)
//                .build();
//
//        BehaviorDelegate<GitHub> delegate = mockRetrofit.create(GitHub.class);
    }

}
