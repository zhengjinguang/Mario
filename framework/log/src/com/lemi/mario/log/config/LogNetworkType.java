package com.lemi.mario.log.config;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public enum LogNetworkType {
  NONE(0),
  WIFI(1),
  MOBILE(2);

  private int intValue;

  private LogNetworkType(int intValue) {
    this.intValue = intValue;
  }

  public int getIntValue() {
    return intValue;
  }
}
