package com.lemi.controller.lemigameassistance.model;

import java.io.Serializable;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class AwardModel implements Serializable {

  private String lotteryName;
  private long awardTime;
  private String awardName;
  private String exchangeCode;

  public String getLotteryName() {
    return lotteryName;
  }

  public long getAwardTime() {
    return awardTime;
  }

  public String getAwardName() {
    return awardName;
  }

  public String getExchangeCode() {
    return exchangeCode;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(awardName);
    builder.append(": ").append(exchangeCode);
    return builder.toString();
  }
}
