package com.ytjojo.practice.request;

import com.ytjojo.domin.vo.LoginResponse;
import com.ytjojo.practice.BuildConfig;
import com.ytjojo.practice.TestSchedulerRule;
import com.ytjojo.http.RetrofitClient;
import com.ytjojo.ui.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import rx.Observable;

/**
 * Created by Administrator on 2016/11/4 0004.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 21,constants = BuildConfig.class,manifest = Config.NONE)
public class LoginRequestTest {
    MainActivity activity;

    @Rule
    public final TestSchedulerRule testSchedulerRule = new TestSchedulerRule();
    @Mock
    private RetrofitClient.GitApiInterface mockApi;
    @Captor
    private ArgumentCaptor<Observable<LoginResponse>> cb;
    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class);
        activity = controller.get();
        activity.setApi(mockApi);
        controller.create();
        RetrofitClient.getDefault();

    }
    @Test
    public void testLogin(){
//        Mockito.verify(mockApi).repositories(Mockito.anyString(), cb.capture());
//
//        List<Repository> testRepos = new ArrayList<Repository>();
//        testRepos.add(new Repository("rails", "ruby", new Owner("dhh")));
//        testRepos.add(new Repository("android", "java", new Owner("google")));
//
//        cb.getValue().success(testRepos, null);
//
//        assertThat(activity.getListAdapter()).hasCount(2);
    }
}
