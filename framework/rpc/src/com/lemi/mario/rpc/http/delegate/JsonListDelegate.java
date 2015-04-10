package com.lemi.mario.rpc.http.delegate;


import com.lemi.mario.rpc.http.processor.Processor;
import com.lemi.mario.rpc.http.request.HttpRequestBuilder;

import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class JsonListDelegate<U extends HttpRequestBuilder, T>
    extends GZipHttpDelegate<U, List<T>> {

  public JsonListDelegate(U builder, Processor<String, List<T>, ? extends Exception> processor) {
    super(builder, processor);
  }
}
