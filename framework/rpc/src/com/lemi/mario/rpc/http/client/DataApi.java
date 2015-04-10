package com.lemi.mario.rpc.http.client;

import android.os.Handler;

import com.lemi.mario.rpc.http.callback.Callback;
import com.lemi.mario.rpc.http.delegate.ApiDelegate;

import org.apache.http.HttpHost;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Data interface to access network.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface DataApi {
  /**
   * Executes request.
   * 
   * @param delegate delegate
   * @return result
   * @throws java.util.concurrent.ExecutionException when error happens
   */
  <T, E extends Exception> T execute(ApiDelegate<T, E> delegate) throws ExecutionException;

  /**
   * Executes request asynchronously.
   * 
   * @param delegate delegate
   * @param callback callback being called asynchronously on UI thread, can be null
   * @return future
   */
  <T, E extends Exception> Future<T> executeAsync(ApiDelegate<T, E> delegate,
      Callback<T, ExecutionException> callback);

  /**
   * Executes request asynchronously.
   * 
   * @param delegate delegate
   * @param callback callback being called asynchronously, can be null
   * @param handler on which thread callback is called. If null, call back on UI thread
   * @return future
   */
  <T, E extends Exception> Future<T> executeAsync(ApiDelegate<T, E> delegate,
      Callback<T, ExecutionException> callback,
      Handler handler);

  /**
   * Makes data api be able to work. Calling it for more than once take no further effect. If not
   * start, any request will cause an {@link java.util.concurrent.ExecutionException}.
   */
  void start();

  /**
   * Shuts down to make all running and further requests stop.
   */
  void shutdown();

  /**
   * Set http proxy host.
   * 
   * @param host
   */
  void setProxyHttpHost(HttpHost host);
}
