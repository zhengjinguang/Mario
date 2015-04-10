package com.lemi.mario.rpc.http.delegate;


import com.lemi.mario.rpc.http.processor.Processor;
import com.lemi.mario.rpc.http.request.HttpRequestBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.concurrent.ExecutionException;

/**
 * Class to bind request builder and result processor.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 * @param <T> result type
 */
public class HttpDelegate<U extends HttpRequestBuilder, T>
    implements ApiDelegate<T, ExecutionException> {

  private final U requestBuilder;
  private final Processor<HttpResponse, T, ExecutionException> processor;
  private HttpUriRequest request;

  public HttpDelegate(U requestBuilder,
      Processor<HttpResponse, T, ExecutionException> processor) {
    this.requestBuilder = requestBuilder;
    this.processor = processor;
  }

  /**
   * Get the HttpUriRequest, if it's already created, just return the previous one, else,
   * create a new one.
   */
  @Override
  public HttpUriRequest getHttpRequest() {
    if (request == null) {
      request = requestBuilder.build();
    }
    return request;
  }

  public HttpUriRequest rebuildHttpRequest() {
    request = requestBuilder.build();
    return request;
  }

  @Override
  public T processResponse(HttpResponse httpResponse) throws ExecutionException {
    return processor.process(httpResponse);
  }

  public U getRequestBuilder() {
    return requestBuilder;
  }
}
