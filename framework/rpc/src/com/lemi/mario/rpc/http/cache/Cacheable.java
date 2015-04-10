package com.lemi.mario.rpc.http.cache;


import com.google.gson.reflect.TypeToken;

/**
 * Interface to mark an object is cacheable.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 * @param <T> cacheable class
 */
public interface Cacheable<T> {

  /**
   * Gets type token of cacheable class.
   * 
   * @return type token
   */
  TypeToken<T> getTypeToken();

  /**
   * Gets cache key of request.
   * 
   * @return cache key
   */
  String getCacheKey();

  /**
   * Gets timeout interval.
   * 
   * @return timeout interval, in ms
   */
  long getTimeoutInterval();
}
