package com.lemi.mario.accountmanager.model.base;

import java.io.Serializable;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class BaseErrorModel implements Serializable {

  private int ret;
  private String reason;

  public BaseErrorModel() {}

  public BaseErrorModel(int ret, String reason) {
    this.ret = ret;
    this.reason = reason;
  }

  public int getRet() {
    return ret;
  }

  public void setRet(int ret) {
    this.ret = ret;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
