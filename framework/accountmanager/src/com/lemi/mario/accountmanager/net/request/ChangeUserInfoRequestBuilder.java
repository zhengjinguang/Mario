package com.lemi.mario.accountmanager.net.request;

import com.lemi.mario.accountmanager.config.Constants;
import com.lemi.mario.accountmanager.model.ChangeUserinfoRequestModel;
import com.lemi.mario.accountmanager.model.User;
import com.lemi.mario.accountmanager.model.base.BaseRequestModel;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class ChangeUserInfoRequestBuilder extends AccountHttpRequestBuilder {

  private static final String URL = Constants.BASE_URL
      + "/change_userinfo?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;
  private User user;

  public ChangeUserInfoRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public ChangeUserInfoRequestBuilder setUser(User user) {
    this.user = user;
    return this;
  }


  @Override
  protected String getUrl() {
    return URL;
  }

  @Override
  protected BaseRequestModel getRequestModel() {
    return new ChangeUserinfoRequestModel().setUser(user);
  }

}
