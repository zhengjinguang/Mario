package com.lemi.mario.accountmanager.net.request;


import com.lemi.mario.accountmanager.model.base.BaseRequestModel;
import com.lemi.mario.rpc.http.request.AbstractHttpRequestBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class BaseHttpRequestBuilder extends AbstractHttpRequestBuilder {

  public String getRequestUrl() {
    return getUrl();
  }

  public Map<String, Object> getRequestParams() {
    Params params = new Params();
    setParams(params);

    return new HashMap<String, Object>(params.getParamMap());
  }

  protected BaseRequestModel getRequestModel() {
    return null;
  }

}
