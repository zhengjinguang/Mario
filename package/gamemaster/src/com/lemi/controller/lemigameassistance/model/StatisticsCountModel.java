package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class StatisticsCountModel extends BaseErrorModel implements Serializable {

  public static enum StatisticsType implements Serializable {
    DOWNLOAD, INSTALLED, STARTUP
  }

  private long count;
  private StatisticsType type;

  public long getCount() {
    return count;
  }

  public StatisticsType getType() {
    return type;
  }
}
