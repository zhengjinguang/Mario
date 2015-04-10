package com.lemi.controller.lemigameassistance.utils;

import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.download.DownloadInfo.ContentType;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SuffixUtils {


  /**
   * check suffix
   * 
   * @param path path,fileName,url etc..
   * @return
   */
  public static ContentType getContentTypeBySuffix(String path) {
    if (TextUtils.isEmpty(path)) {
      return ContentType.UNKNOWN;
    }

    if (path.endsWith(".apk")) {
      return ContentType.APP;
    } else if (path.endsWith(".zip")) {
      return ContentType.ZIP;
    } else if (path.endsWith(".mp3") || path.endsWith(".acc")) {
      return ContentType.MUSIC;
    } else if (path.endsWith(".jpg")) {
      return ContentType.IMAGE;
    } else if (path.endsWith(".epub")) {
      return ContentType.BOOK;
    } else if (path.endsWith(".buka")) {
      return ContentType.COMIC;
    } else if (path.endsWith(".patch")) {
      return ContentType.PATCH;
    }

    return ContentType.UNKNOWN;
  }

}
