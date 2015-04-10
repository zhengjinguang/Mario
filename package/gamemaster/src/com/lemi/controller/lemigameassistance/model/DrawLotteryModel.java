package com.lemi.controller.lemigameassistance.model;


import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class DrawLotteryModel extends BaseErrorModel implements Serializable {

  private String awardName;
  private String exchangeCode;

  public String getAwardName() {
    return awardName;
  }

  public String getExchangeCode() {
    return exchangeCode;
  }

}
