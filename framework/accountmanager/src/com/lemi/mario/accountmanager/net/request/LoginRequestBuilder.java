package com.lemi.mario.accountmanager.net.request;

import com.lemi.mario.accountmanager.config.Constants;

import java.util.Map;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class LoginRequestBuilder extends AccountHttpRequestBuilder {

  private static final String URL = Constants.BASE_URL
      + "/login?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String UDID = "udid";
  private static final String USER_FILTER = "user_filter";
  private String udid;
  private String userFilter;


  public LoginRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public LoginRequestBuilder setUdid(String udid) {
    this.udid = udid;
    return this;
  }

  public LoginRequestBuilder setUserFilter(String userFilter) {
    this.userFilter = userFilter;
    return this;
  }

  @Override
  protected String getUrl() {
    return URL;
  }

  @Override
  protected void setContentParams(Map<String, Object> params) {
    super.setContentParams(params);
    params.put(UDID, udid);
    params.put(USER_FILTER, userFilter);
  }
}
