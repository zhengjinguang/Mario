package com.lemi.controller.lemigameassistance.model;

import java.io.Serializable;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CategoryModel implements Serializable {

  private String cid;
  private String name;
  private String iconUrl;
  private int count;

  public String getCid() {
    return cid;
  }

  public String getName() {
    return name;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public int getCount() {
    return count;
  }
}
