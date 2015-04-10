package com.lemi.mario.accountmanager.model;

import com.lemi.mario.accountmanager.model.base.BaseErrorModel;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class LoginModel extends BaseErrorModel {
  private String authcode;
  private User user;

  public String getAuthcode() {
    return authcode;
  }

  public User getUser() {
    return user;
  }

}
