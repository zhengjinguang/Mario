package com.lemi.controller.lemigameassistance.recycleview.model;

import com.lemi.controller.lemigameassistance.model.CategoryModel;
import com.lemi.controller.lemigameassistance.model.GameModel;

import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class MyGameModel extends GameModel {

  public MyGameModel(long gid, String name, String apkUrl, long onlineTime, String apkSize,
      String realSize, long downloadCount, List<CategoryModel> category, int playMode,
      int operationMode, int personMode, String author, String versionName, String star,
      String language, String packageName, String notice, String description, String iconUrl,
      List<ImageInnerItem> images, List<ImageInnerItem> posters) {
    super(gid, name, apkUrl, onlineTime, apkSize, realSize, downloadCount, category, playMode,
        operationMode, personMode, author, versionName, star, language, packageName, notice,
        description, iconUrl, images, posters);
  }
}
