package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class GetLotteryModel extends BaseErrorModel implements Serializable {

  private String title;
  private String lotteryId;
  private long startTime;
  private long stopTime;
  private String iconUrl;

  public String getTitle() {
    return title;
  }

  public String getLotteryId() {
    return lotteryId;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getStopTime() {
    return stopTime;
  }

  public String getIconUrl() {
    return iconUrl;
  }
}
