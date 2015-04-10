package com.lemi.mario.accountmanager.net.request;

import com.lemi.mario.accountmanager.config.Constants;

import java.util.Map;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class CheckAuthRequestBuilder extends AccountHttpRequestBuilder {

  private static final String URL = Constants.BASE_URL
      + "/check_auth?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String USER_FILTER = "user_filter";

  private String userFilter;


  public CheckAuthRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public CheckAuthRequestBuilder setUserFilter(String userFilter) {
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
    params.put(USER_FILTER, userFilter);
  }

}
