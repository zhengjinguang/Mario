package com.lemi.controller.lemigameassistance.net.request;

import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.net.base.request.GameMasterHttpRequestBuilder;
import com.lemi.mario.rpc.http.request.AbstractHttpRequestBuilder;

import java.util.Map;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class GetAwardsRequestBuilder extends GameMasterHttpRequestBuilder {

  private static final String URL = Constants.API_V3
      + "/get_uawardinfo?api_version=" + Constants.LOTTERY_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String AUTHCODE = "authcode";

  private String authcode;

  public GetAwardsRequestBuilder() {
    super();
    setMethod(AbstractHttpRequestBuilder.Method.POST);
  }


  @Override
  protected String getUrl() {
    return URL;
  }

  @Override
  protected void setContentParams(Map<String, Object> params) {
    super.setContentParams(params);
    params.put(AUTHCODE, "aaa");
  }

}
