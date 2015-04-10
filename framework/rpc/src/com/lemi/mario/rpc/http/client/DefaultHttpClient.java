package com.lemi.mario.rpc.http.client;


import com.lemi.mario.base.http.HttpClientWrapper;

/**
 * Default http client.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DefaultHttpClient extends HttpClientWrapper {

  private static final int SOCKET_TIMEOUT_MS = 60 * 1000;
  private static final int CONNECTION_TIMEOUT_MS = 30 * 1000;

  public DefaultHttpClient() {
    super(HttpClientFactory.newInstance(SOCKET_TIMEOUT_MS, CONNECTION_TIMEOUT_MS));
  }
}
