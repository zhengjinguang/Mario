package com.lemi.controller.lemigameassistance.recycleview.model;

import java.util.List;

/**
 * @author zhengjinguang@letv.com (shining)
 */
public class CategoryItemModel {
  private List<CategoryInfo> categoryInfo;

  public CategoryItemModel(List<CategoryInfo> categoryInfo) {
    this.categoryInfo = categoryInfo;
  }

  public List<CategoryInfo> getCategoryItemModel() {
    return categoryInfo;
  }
}
