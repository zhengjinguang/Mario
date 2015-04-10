package com.lemi.mario.widget.helper.view;

import android.text.TextUtils;

import com.lemi.mario.widget.helper.config.Constants;

import java.io.File;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class PathUtils {

  public static final String WIDGET_PATH = Constants.SDCARD_PATH + File.separator + "widget";

  public static final String INDEX_FILE_PATH = WIDGET_PATH + File.separator + "widget.data";
  public static final String APK_FILE_FOLDER = WIDGET_PATH + File.separator + "data";
  public static final String POSTER_FILE_FOLDER = WIDGET_PATH + File.separator + "poster";



  public static String getApkPath(String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return null;
    }
    return APK_FILE_FOLDER + File.separator + packageName + Constants.WIDGET_HELPER_DATA_SUFFIX;
  }

  public static String getPosterPath(String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return null;
    }
    return POSTER_FILE_FOLDER + File.separator + packageName + Constants.POSTER_SUFFIX;
  }

}
