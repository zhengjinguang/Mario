package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SubjectContentModel extends BaseErrorModel implements Serializable {

  private SubjectModel subject;
  private List<GameModel> games;

  public SubjectModel getSubject() {
    return subject;
  }

  public List<GameModel> getGames() {
    return games;
  }
}
