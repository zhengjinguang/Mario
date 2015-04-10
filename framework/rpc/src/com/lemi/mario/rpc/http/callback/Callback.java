package com.lemi.mario.rpc.http.callback;

/**
 * Callback class.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 * @param <V> result type
 * @param <E> error type
 */
public interface Callback<V, E extends Exception> {
  /**
   * Gets call when result arrives.
   * 
   * @param result result
   */
  void onSuccess(V result);

  /**
   * Gets called when error happens.
   * 
   * @param e error
   */
  void onError(E e);
}
