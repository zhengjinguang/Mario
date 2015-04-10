package com.lemi.mario.sample.recyclerview;

import java.io.Serializable;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GridItemModel implements Serializable {

  private String index;
  private String imageUrl;

  public GridItemModel(String index, String imageUrl) {
    this.index = index;
    this.imageUrl = imageUrl;
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}
