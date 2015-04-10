package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameListModel extends BaseErrorModel implements Serializable {

  private List<GameModel> games;


  public List<GameModel> getGames() {
    return games;
  }

}
