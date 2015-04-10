package com.lemi.controller.lemigameassistance.mvc.model;

import com.lemi.mario.mvc.BaseModel;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameItemModel implements BaseModel {

  private String name;
  private String iconUrl;

  public String getName() {
    return name;
  }

  public String getIconUrl() {
    return iconUrl;
  }
}
