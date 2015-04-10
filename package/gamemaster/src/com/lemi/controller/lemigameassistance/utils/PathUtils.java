package com.lemi.controller.lemigameassistance.utils;

import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.download.DownloadInfo.ContentType;
import com.lemi.mario.externalmanager.manager.ExternalStorageManager;
import com.lemi.mario.externalmanager.model.ExternalStorageInfo;

import java.io.File;

/**
 * this class only provide to download, don't use to other place , because the file in these path
 * maybe delete in {@link com.lemi.controller.lemigameassistance.manager.CleanManager}
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public class PathUtils {

  private static final String APP_INSTALL_PATH = "/data";

  // path
  public static String getDownloadRootPath() {
    return Constants.SDCARD_PATH;
  }

  public static String getDownloadFolderPath(ContentType contentType) {
    return Constants.DOWNLOAD_ROOT_PATH + File.separator + contentType.toString();
  }

  public static String getAppInstallRootPath() {
    return APP_INSTALL_PATH;
  }

  public static String getUnzipApkFolderPath() {
    return Constants.DOWNLOAD_ROOT_PATH + File.separator + ContentType.APP.toString();
  }

  public static String getApkFullPath(String packageName) {
    return Constants.DOWNLOAD_ROOT_PATH + File.separator + ContentType.APP.toString()
        + File.separator + packageName + Constants.APK_SUFFIX;
  }

  public static String getUnzipDataFolderPath() {
    return Constants.SDCARD_PATH;
  }

  public static String getExternalStorageRootPath() {
    ExternalStorageInfo storageInfo =
        ExternalStorageManager.getInstance().getMaxSizeExternalStorage();
    if (storageInfo == null || TextUtils.isEmpty(storageInfo.getPath())) {
      return null;
    }
    return storageInfo.getPath();
  }

  public static String getExternalStorageDownloadPath() {
    return getExternalStorageRootPath() + File.separator + Constants.APP_PATH + File.separator
        + Constants.DOWNLOAD_FOLDER_PATH;
  }

}
