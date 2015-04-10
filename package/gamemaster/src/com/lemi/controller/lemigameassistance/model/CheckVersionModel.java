package com.lemi.controller.lemigameassistance.model;

import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;

import java.io.Serializable;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CheckVersionModel extends BaseErrorModel implements Serializable {

  private int versionCode;
  private String changeNote;
  private String apkUrl;

  public int getVersionCode() {
    return versionCode;
  }

  public String getChangeNote() {
    return changeNote;
  }

  public String getApkUrl() {
    return apkUrl;
  }
}
