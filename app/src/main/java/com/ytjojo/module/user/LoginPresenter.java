package com.ytjojo.module.user;

import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.http.GitApiInterface;
import com.ytjojo.mvp.presenter.BasePresenter;
import com.ytjojo.utils.TextUtils;

/**
 * Created by Administrator on 2016/11/2 0002.
 */
public class LoginPresenter extends BasePresenter<LoginView> {

    GitApiInterface mGitApiInterface;
    public void login(String userName,String password){
        LoginRequest request = new LoginRequest();
        request.pwd = password;
        request.uid = userName;
        mGitApiInterface.login(request);
    }
    public void setApiService(GitApiInterface service) {
        this.mGitApiInterface = service;
    }

    public boolean verify(String userName,String passWord){
        if(!TextUtils.isEmpty(userName) &&!TextUtils.isEmpty(passWord)&& userName.trim().length()>=2&&passWord.trim().length()>=6){
            return true;
        }
        return false;
    }
    public void postRequest(String userName,String password,NetworkCallback callback){
        callback.onSuccess("success");
    }
}
