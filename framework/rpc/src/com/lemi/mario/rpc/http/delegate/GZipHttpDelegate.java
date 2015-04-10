package com.lemi.mario.rpc.http.delegate;


import com.lemi.mario.rpc.http.processor.GZipHttpResponseProcessor;
import com.lemi.mario.rpc.http.processor.Processor;
import com.lemi.mario.rpc.http.processor.ProcessorConnector;
import com.lemi.mario.rpc.http.request.HttpRequestBuilder;

/**
 * Delegate which can process GZip compressed http response.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 * @param <U> HttpRequestBuilder type
 * @param <T> output type
 */
public class GZipHttpDelegate<U extends HttpRequestBuilder, T> extends HttpDelegate<U, T> {

  private final Processor<String, T, ? extends Exception> contentProcessor;

  public GZipHttpDelegate(U requestBuilder,
      Processor<String, T, ? extends Exception> processor) {
    super(requestBuilder,
        ProcessorConnector.connect(new GZipHttpResponseProcessor(), processor));
    this.contentProcessor = processor;
  }

  public Processor<String, T, ? extends Exception> getContentProcessor() {
    return contentProcessor;
  }
}
