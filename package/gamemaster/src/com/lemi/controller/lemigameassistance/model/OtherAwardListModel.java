package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class OtherAwardListModel extends BaseErrorModel implements Serializable {

  private List<OtherAwardModel> awards;

  public List<OtherAwardModel> getAwards() {
    return awards;
  }
}
