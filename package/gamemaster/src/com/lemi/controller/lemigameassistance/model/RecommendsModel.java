package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class RecommendsModel extends BaseErrorModel implements Serializable {

  private List<GameModel> games;
  private List<SubjectModel> subjects;

  public List<GameModel> getGames() {
    return games;
  }

  public List<SubjectModel> getSubjects() {
    return subjects;
  }
}
