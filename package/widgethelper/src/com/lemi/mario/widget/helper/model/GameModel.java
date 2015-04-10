package com.lemi.mario.widget.helper.model;

import java.io.Serializable;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameModel implements Serializable {

  private String posterPath;
  private String gameName;
  private String gameSize;
  private String apkPath;
  private String packageName;
  private boolean hasExternalData;
  private String externalDataPath;

  public GameModel() {}


  public GameModel(String posterPath, String gameName, String gameSize, String apkPath, String packageName, boolean hasExternalData, String externalDataPath) {
    this.posterPath = posterPath;
    this.gameName = gameName;
    this.gameSize = gameSize;
    this.apkPath = apkPath;
    this.packageName = packageName;
    this.hasExternalData = hasExternalData;
    this.externalDataPath = externalDataPath;
  }

  public String getPosterPath() {
    return posterPath;
  }

  public String getGameName() {
    return gameName;
  }

  public String getGameSize() {
    return gameSize;
  }

  public String getApkPath() {
    return apkPath;
  }

  public String getPackageName() {
    return packageName;
  }

  public boolean isHasExternalData() {
    return hasExternalData;
  }

  public String getExternalDataPath() {
    return externalDataPath;
  }
}
