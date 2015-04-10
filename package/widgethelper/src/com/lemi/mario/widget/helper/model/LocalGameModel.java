package com.lemi.mario.widget.helper.model;

import java.io.Serializable;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class LocalGameModel implements Serializable {

  private String gameName;
  private String size;
  private String packageName;
  private boolean hasExternalData;
  private String externalDataPath;


  public String getGameName() {
    return gameName;
  }

  public String getSize() {
    return size;
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
