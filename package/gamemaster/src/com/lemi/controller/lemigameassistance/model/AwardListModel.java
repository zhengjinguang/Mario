package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class AwardListModel extends BaseErrorModel implements Serializable {

  private List<AwardModel> awards;

  public List<AwardModel> getAwards() {
    return awards;
  }
}
