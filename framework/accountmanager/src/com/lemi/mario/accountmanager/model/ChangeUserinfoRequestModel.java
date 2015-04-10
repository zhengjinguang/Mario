package com.lemi.mario.accountmanager.model;

import com.lemi.mario.accountmanager.model.base.BaseRequestModel;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class ChangeUserinfoRequestModel extends BaseRequestModel {
  private String user_filter;
  private User user;

  public User getUser() {
    return user;
  }

  public ChangeUserinfoRequestModel setUser(User user) {
    this.user = user;
    return this;
  }

  public String getUser_filter() {
    return user_filter;
  }

  public ChangeUserinfoRequestModel setUser_filter(String user_filter) {
    this.user_filter = user_filter;
    return this;
  }

}
