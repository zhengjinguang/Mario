package com.lemi.mario.rpc.http.request;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * A builder interface which can get {@link org.apache.http.client.methods.HttpUriRequest} from.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface HttpRequestBuilder {

  /**
   * Builds a HttpUriRequest.
   * 
   * @return HttpUriRequest. Returns null if error happens
   */
  HttpUriRequest build();

  /**
   * Returns cache key if the response is cacheable. Otherwise, return null.
   * 
   * @return
   */
  String getCacheKey();
}
