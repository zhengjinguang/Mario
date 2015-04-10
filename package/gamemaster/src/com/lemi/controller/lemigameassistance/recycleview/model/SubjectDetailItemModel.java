package com.lemi.controller.lemigameassistance.recycleview.model;

import com.lemi.controller.lemigameassistance.model.GameModel;

import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SubjectDetailItemModel {

  private List<GameModel> subjectItemModel;

  public SubjectDetailItemModel(List<GameModel> subjectItemModel) {
    this.subjectItemModel = subjectItemModel;
  }

  public List<GameModel> getSubjectItemModel() {
    return subjectItemModel;
  }

  public void setSubjectItemModel(List<GameModel> subjectItemModel) {
    this.subjectItemModel = subjectItemModel;
  }
}
