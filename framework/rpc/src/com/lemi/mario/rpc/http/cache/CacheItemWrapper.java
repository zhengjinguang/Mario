package com.lemi.mario.rpc.http.cache;

/**
 * A wrapper class of cached item.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CacheItemWrapper {
  private long timeout;
  private long lastModificationTime;
  private String content;

  private CacheItemWrapper() {}

  public long getTimeout() {
    return timeout;
  }

  public long getLastModificationTime() {
    return lastModificationTime;
  }

  public String getContent() {
    return content;
  }

  public static CacheItemWrapper from(String content, long timeout, long currentTime) {
    CacheItemWrapper item = new CacheItemWrapper();
    item.content = content;
    item.timeout = timeout;
    item.lastModificationTime = currentTime;
    return item;
  }
}
