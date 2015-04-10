package com.lemi.mario.rpc.http.delegate;


import com.lemi.mario.rpc.http.processor.EmptyResponseProcessor;
import com.lemi.mario.rpc.http.request.HttpRequestBuilder;

/**
 * Delegate to submit request.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 * @param <U> request builder
 */
public class SubmitDelegate<U extends HttpRequestBuilder> extends HttpDelegate<U, Void> {

  public SubmitDelegate(U requestBuilder) {
    super(requestBuilder, new EmptyResponseProcessor());
  }

}
