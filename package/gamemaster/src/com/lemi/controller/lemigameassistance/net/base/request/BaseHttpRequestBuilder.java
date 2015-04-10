package com.lemi.controller.lemigameassistance.net.base.request;

import com.lemi.controller.lemigameassistance.model.base.BaseRequestModel;
import com.lemi.mario.rpc.http.request.AbstractHttpRequestBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class BaseHttpRequestBuilder extends AbstractHttpRequestBuilder {

  public BaseHttpRequestBuilder() {
    super(null);
  }

  public String getRequestUrl() {
    return getUrl();
  }

  /**
   * for api log use
   */
  public Map<String, Object> getRequestParams() {
    Params params = new Params();
    setParams(params);

    return new HashMap<String, Object>(params.getParamMap());
  }

  protected BaseRequestModel getRequestModel() {
    return null;
  }

}
