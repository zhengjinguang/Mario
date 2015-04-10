package com.lemi.mario.accountmanager.net.filter;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public enum UserFilter {

  ALL(
      "nick,uid,udid,phone,email,gender"),

  LITE("nick");

  private final String filter;

  private UserFilter(String filter) {
    this.filter = filter;
  }

  public String getFilter() {
    return filter;
  }

}
