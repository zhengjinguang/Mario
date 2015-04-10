package com.lemi.mario.download.model;

import java.io.Serializable;

public class ClipInfo implements Serializable {
  private String start;
  private String end;

  public String getStart() {
    return start;
  }

  public String getEnd() {
    return end;
  }
}
