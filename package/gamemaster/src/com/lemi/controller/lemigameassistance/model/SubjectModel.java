package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.GameModel.ImageInnerItem;

import java.io.Serializable;
import java.util.List;


/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SubjectModel implements Serializable {

  private long sid;
  private String name;
  private String iconUrl;
  private String description;
  private List<ImageInnerItem> posters;

  public long getSid() {
    return sid;
  }

  public String getName() {
    return name;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public String getDescription() {
    return description;
  }

  public List<ImageInnerItem> getPosters() {
    return posters;
  }
}
