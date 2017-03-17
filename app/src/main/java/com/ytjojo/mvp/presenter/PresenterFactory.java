package com.ytjojo.mvp.presenter;

public interface PresenterFactory<T extends Presenter> {
      T create();
  }  