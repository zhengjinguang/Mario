package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;
import java.util.List;


/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class StartupModel extends BaseErrorModel implements Serializable {

  private long sid;
  private String name;
  private long startTime;
  private long stopTime;
  private String description;
  private List<GameModel.ImageInnerItem> posters;

  public long getSid() {
    return sid;
  }

  public String getName() {
    return name;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getStopTime() {
    return stopTime;
  }

  public String getDescription() {
    return description;
  }

  public List<GameModel.ImageInnerItem> getPosters() {
    return posters;
  }

}
