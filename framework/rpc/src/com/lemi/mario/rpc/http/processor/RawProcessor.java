package com.lemi.mario.rpc.http.processor;

/**
 * 
 * Raw processor to return input directly.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class RawProcessor<T> implements Processor<T, T, Exception> {

  @Override
  public T process(T input) throws Exception {
    return input;
  }
}
