package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SubjectListModel extends BaseErrorModel implements Serializable {

  private int count;
  private List<SubjectModel> subjects;

  public int getCount() {
    return count;
  }

  public List<SubjectModel> getSubjects() {
    return subjects;
  }
}
