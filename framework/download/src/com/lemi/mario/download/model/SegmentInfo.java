package com.lemi.mario.download.model;

import java.io.Serializable;

public class SegmentInfo implements Serializable {

  /**
   * serial Version UID
   */
  private static final long serialVersionUID = 2833776197684679160L;

  private long seq;
  private String md5;
  private String crc;

  public long getSeq() {
    return seq;
  }

  public String getMd5() {
    return md5;
  }

  public String getCrc() {
    return crc;
  }

}
