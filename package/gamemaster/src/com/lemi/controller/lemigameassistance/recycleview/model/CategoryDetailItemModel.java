package com.lemi.controller.lemigameassistance.recycleview.model;

import com.lemi.controller.lemigameassistance.model.GameModel;

import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CategoryDetailItemModel {

  private List<GameModel> categoryItemModel;

  public CategoryDetailItemModel(List<GameModel> categoryItemModel) {
    this.categoryItemModel = categoryItemModel;
  }

  public List<GameModel> getCategoryItemModel() {
    return categoryItemModel;
  }

  public void setCategoryItemModel(List<GameModel> categoryItemModel) {
    this.categoryItemModel = categoryItemModel;
  }
}
