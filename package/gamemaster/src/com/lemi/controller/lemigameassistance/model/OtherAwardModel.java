package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.config.Constants;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class OtherAwardModel {
  private int uid;
  private String lotteryName;
  private long awardTime;
  private String awardName;

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public String getLotteryName() {
    return lotteryName;
  }

  public void setLotteryName(String lotteryName) {
    this.lotteryName = lotteryName;
  }

  public long getAwardTime() {
    return awardTime;
  }

  public void setAwardTime(long awardTime) {
    this.awardTime = awardTime;
  }

  public String getAwardName() {
    return awardName;
  }

  public void setAwardName(String awardName) {
    this.awardName = awardName;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(Constants.USERID_OHTER_PREFIX);
    builder.append(uid).append(": ").append(awardName).append("; ");
    return builder.toString();
  }

}
