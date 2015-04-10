package com.lemi.mario.widget.helper.view;

import com.lemi.mario.widget.helper.model.GameModel;
import com.lemi.mario.widget.helper.model.LocalGameModel;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ApiUtils {


  public static GameModel convertToGameModel(LocalGameModel localGameModel) {
    return new GameModel(
        PathUtils.getPosterPath(localGameModel.getPackageName()),
        localGameModel.getGameName(),
        localGameModel.getSize(),
        PathUtils.getApkPath(localGameModel.getPackageName()),
        localGameModel.getPackageName(),
        localGameModel.isHasExternalData(),
        localGameModel.getExternalDataPath());
  }

}
