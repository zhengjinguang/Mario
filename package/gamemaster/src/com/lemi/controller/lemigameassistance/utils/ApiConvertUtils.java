package com.lemi.controller.lemigameassistance.utils;

import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.model.CategoryModel;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.recycleview.model.CategoryInfo;

import java.util.ArrayList;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ApiConvertUtils {

  /**
   * use this function to get NetAppButton model ,so it will not need size and data size to judge
   * folder
   * 
   * @param downloadInfo
   * @return
   */
  public static GameModel convertToGameModel(DownloadInfo downloadInfo) {
    return buildDownloadNeedGameModel(downloadInfo.getIdentity(), downloadInfo.getDownloadUrl(),
        downloadInfo.getIcon(), downloadInfo.getTitle(), null, null);
  }

  private static GameModel buildDownloadNeedGameModel(String packageName, String apkUrl,
      String iconUrl, String name, String apkSize, String dataSize) {
    GameModel gameModel = new GameModel();
    gameModel.setPackageName(packageName);
    gameModel.setApkUrl(apkUrl);
    gameModel.setIconUrl(iconUrl);
    gameModel.setName(name);
    gameModel.setApkSize(apkSize);
    gameModel.setRealSize(dataSize);
    return gameModel;
  }

  public static CategoryInfo categoryModelConvertToCategoryInfo(CategoryModel categoryModel) {
    if (categoryModel == null) {
      return null;
    }
    CategoryInfo categoryInfo = new CategoryInfo();
    categoryInfo.setCid(categoryModel.getCid());
    categoryInfo.setName(categoryModel.getName());
    categoryInfo.setIconUrl(categoryModel.getIconUrl());
    categoryInfo.setIconResId(-1);
    categoryInfo.setCount(categoryModel.getCount());
    categoryInfo.setEmpty(false);
    return categoryInfo;
  }

  private static ArrayList<String> parseImages(GameModel model) {
    ArrayList<String> images = new ArrayList<String>();
    if (model != null && model.getImages() != null && model.getImages().size() > 0) {
      for (GameModel.ImageInnerItem item : model.getImages()) {
        if (item != null && !TextUtils.isEmpty(item.getUrl())) {
          images.add(item.getUrl());
        }
      }
    }
    return images;
  }
}
