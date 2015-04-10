package com.lemi.controller.lemigameassistance.recycleview.model;

import java.io.Serializable;

/**
 * @author zhengjinguang@letv.com (shining)
 */
public class CategoryInfo implements Serializable {

  private String cid;
  private String name;
  private String iconUrl;
  private int count;

  private int iconResId;
  private boolean isEmpty;

  public void setCid(String cid) {
    this.cid = cid;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public void setIconResId(int iconResId) {
    this.iconResId = iconResId;
  }

  public void setEmpty(boolean isEmpty) {
    this.isEmpty = isEmpty;
  }

  public int getIconResId() {
    return iconResId;
  }

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

  public boolean isEmpty() {
    return isEmpty;
  }

}
