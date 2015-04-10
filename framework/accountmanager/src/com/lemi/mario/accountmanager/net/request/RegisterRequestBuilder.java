package com.lemi.mario.accountmanager.net.request;

import com.lemi.mario.accountmanager.config.Constants;

import java.util.Map;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class RegisterRequestBuilder extends AccountHttpRequestBuilder {

  private static final String URL = Constants.BASE_URL
      + "/register?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String UDID = "udid";

  private String udid;


  public RegisterRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public RegisterRequestBuilder setUdid(String udid) {
    this.udid = udid;
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
  }
}
