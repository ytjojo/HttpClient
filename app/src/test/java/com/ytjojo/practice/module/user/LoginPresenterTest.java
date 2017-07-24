package com.ytjojo.practice.module.user;

import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.http.RetrofitClient;
import com.ytjojo.module.user.LoginPresenter;
import com.ytjojo.module.user.NetworkCallback;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
/**
 * Created by Administrator on 2016/11/3 0003.
 */
public class LoginPresenterTest {

    @Test
    public void testLogin(){
        RetrofitClient.GitApiInterface gitApiInterface= Mockito.mock(RetrofitClient.GitApiInterface.class);
        LoginPresenter loginPresenter =new LoginPresenter();
        loginPresenter.setApiService(gitApiInterface);
        loginPresenter.login("18668436182","27483X");
        LoginRequest request = new LoginRequest();
        request.uid="18668436182";
        request.pwd = "27483X";
        Mockito.verify(gitApiInterface).login(request);

    }

    @Test
    public void testVerify(){
        LoginPresenter presenter = Mockito.mock(LoginPresenter.class);
        Assert.assertEquals(false,presenter.verify("",""));
        Mockito.when(presenter.verify(anyString(),anyString())).thenReturn(true);
        Assert.assertEquals(true,presenter.verify("",""));
    }
    @Test
    public void testPostRequest(){
        LoginPresenter presenter = Mockito.mock(LoginPresenter.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                NetworkCallback callback = (NetworkCallback) args[2];
                callback.onFailure(500,"server error");
                return 500;
            }
        }).when(presenter).postRequest(anyString(),anyString(),any(NetworkCallback.class));
        presenter.postRequest("ytjojo","123456",Mockito.mock(NetworkCallback.class));
    }
    @Test
    public void testSyp(){
        LoginPresenter presenter = Mockito.spy(LoginPresenter.class);
        boolean result=  presenter.verify("","");
        Assert.assertEquals(false,result);
        result = presenter.verify("ytjojo","123456");
        Assert.assertEquals(true,result);
        Mockito.when(presenter.verify(anyString(),anyString())).thenReturn(true);
        result = presenter.verify("","123");
        Assert.assertEquals(true,result);
        Mockito.verify(presenter,Mockito.times(1)).verify("","");


    }


}
