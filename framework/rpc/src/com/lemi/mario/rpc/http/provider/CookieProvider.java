package com.lemi.mario.rpc.http.provider;

/**
 * Interface to provide cookie.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface CookieProvider {
  /**
   * Gets default cookie.
   * 
   * @return default cookie
   */
  String getDefaultCookie();
}
