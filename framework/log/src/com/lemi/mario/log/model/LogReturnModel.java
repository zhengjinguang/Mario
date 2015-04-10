package com.lemi.mario.log.model;

import java.io.Serializable;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class LogReturnModel implements Serializable {

  private String ret;
  private String reason;

  public String getRet() {
    return ret;
  }

  public String getReason() {
    return reason;
  }
}
