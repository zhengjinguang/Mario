package com.lemi.controller.lemigameassistance.net.request;


import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.net.base.request.GameMasterHttpRequestBuilder;

import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetStartupRequestBuilder extends GameMasterHttpRequestBuilder {

  private static final String URL = Constants.API_V3
      + "/get_startup?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String STARTUP_FILTER = "startupFilter";

  private String startupFilter;

  public GetStartupRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public GetStartupRequestBuilder setStartupFilter(String startupFilter) {
    this.startupFilter = startupFilter;
    return this;
  }

  @Override
  protected String getUrl() {
    return URL;
  }

  @Override
  protected void setContentParams(Map<String, Object> params) {
    super.setContentParams(params);
    params.put(STARTUP_FILTER, startupFilter);
  }

}
