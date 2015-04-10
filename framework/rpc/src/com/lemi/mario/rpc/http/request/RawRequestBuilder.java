package com.lemi.mario.rpc.http.request;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * Raw request builder to return request set by caller.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class RawRequestBuilder implements HttpRequestBuilder {

  private HttpUriRequest request;

  public void setHttpRequest(HttpUriRequest request) {
    this.request = request;
  }

  @Override
  public HttpUriRequest build() {
    return request;
  }

  @Override
  public String getCacheKey() {
    return null;
  }
}
