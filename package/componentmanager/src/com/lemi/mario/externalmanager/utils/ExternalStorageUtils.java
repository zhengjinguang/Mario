package com.lemi.mario.externalmanager.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.externalmanager.model.ExternalStorageInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * use this helper for get user added external storage
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ExternalStorageUtils {

  public static final String SDCARD0_NAME = "sdcard0";
  public static final String SDCARD1_NAME = "sdcard1";
  public static final String LOCAL_SDCARD_NAME = "sdcard";
  public static final String LOCAL_SDCARD_PATH = Environment.getExternalStorageDirectory()
      .getAbsolutePath();

  /**
   * letv special card
   */
  public static final String S50_SDCARD_NAME = "mmcblka";

  public static final String ROOT_PATH = "/mnt";
  public static final String EXTERNAL_ROOT_PATH = "/storage/external_storage";

  /**
   * value is 1G size.
   * this value is a experience value , we assume external storage (such as SD-CARD U-STORAGE) will
   * have 1G available size
   */
  private static final long MIN_SIZE = 1024 * 1024 * 1024l;


  public static List<ExternalStorageInfo> searchExternalStorageInPossiblePath() {
    List<ExternalStorageInfo> storageInfos = new ArrayList<>();
    storageInfos.addAll(searchExternalStorage(ROOT_PATH));
    storageInfos.addAll(searchExternalStorage(EXTERNAL_ROOT_PATH));
    return storageInfos;
  }

  public static List<ExternalStorageInfo> searchExternalStorage(String path) {
    List<ExternalStorageInfo> storageInfos = new ArrayList<>();
    if (TextUtils.isEmpty(path)) {
      return storageInfos;
    }
    File searchRootFile = new File(path);
    if (!searchRootFile.exists()) {
      return storageInfos;
    }

    File[] files = searchRootFile.listFiles();
    if (files == null || files.length <= 0) {
      return storageInfos;
    }

    for (File file : files) {
      if (file.isDirectory()) {
        // exclude sdcard, because this is system default external path
        if (LOCAL_SDCARD_NAME.equals(file.getName())) {
          continue;
        }

        long size = FileUtil.getAllBytes(file.getAbsolutePath());
        if (size >= MIN_SIZE) {
          ExternalStorageInfo externalStorageInfo =
              new ExternalStorageInfo(file.getAbsolutePath(), size);
          storageInfos.add(externalStorageInfo);
          continue;
        }

        // find in next level for particular path
        File[] childFiles = file.listFiles();
        if (childFiles == null || childFiles.length <= 0) {
          continue;
        }

        for (File childFile : childFiles) {
          if (childFile.isDirectory()) {
            size = FileUtil.getAllBytes(childFile.getAbsolutePath());
            if (size >= MIN_SIZE) {
              ExternalStorageInfo externalStorageInfo =
                  new ExternalStorageInfo(childFile.getAbsolutePath(), size);
              storageInfos.add(externalStorageInfo);
            }
          }
        }

      }
    }

    return storageInfos;
  }


  public static ExternalStorageInfo getMaxSizeSdcard(List<ExternalStorageInfo> storageInfos) {
    if (CollectionUtils.isEmpty(storageInfos)) {
      return null;
    }
    ExternalStorageInfo maxStorageInfo = null;
    for (ExternalStorageInfo storageInfo : storageInfos) {
      if (storageInfo == null || TextUtils.isEmpty(storageInfo.getPath())) {
        continue;
      }
      if (isSdcard(storageInfo.getPath())) {
        if (maxStorageInfo == null || maxStorageInfo.getTotalSize() < storageInfo.getTotalSize()) {
          maxStorageInfo = storageInfo;
        }
      }
    }

    return maxStorageInfo;
  }

  public static ExternalStorageInfo getMaxSizeExternalStorage(List<ExternalStorageInfo> storageInfos) {
    if (CollectionUtils.isEmpty(storageInfos)) {
      return null;
    }
    ExternalStorageInfo maxStorageInfo = null;
    for (ExternalStorageInfo storageInfo : storageInfos) {
      if (storageInfo == null || TextUtils.isEmpty(storageInfo.getPath())) {
        continue;
      }
      if (maxStorageInfo == null || maxStorageInfo.getTotalSize() < storageInfo.getTotalSize()) {
        maxStorageInfo = storageInfo;
      }
    }

    return maxStorageInfo;
  }

  public static ExternalStorageInfo getLocalExternalStorage() {
    return new ExternalStorageInfo(LOCAL_SDCARD_PATH, FileUtil.getAllBytes(LOCAL_SDCARD_PATH));
  }


  public static boolean hasSdcard(List<ExternalStorageInfo> storageInfos) {
    if (CollectionUtils.isEmpty(storageInfos)) {
      return false;
    }
    for (ExternalStorageInfo storageInfo : storageInfos) {
      if (isSdcard(storageInfo.getPath())) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasOtherExternalStorage(List<ExternalStorageInfo> storageInfos) {
    if (CollectionUtils.isEmpty(storageInfos)) {
      return false;
    }
    for (ExternalStorageInfo storageInfo : storageInfos) {
      if (TextUtils.isEmpty(storageInfo.getPath())) {
        continue;
      }
      if (!isSdcard(storageInfo.getPath())) {
        return true;
      }
    }
    return false;
  }


  public static boolean isSdcard(String name) {
    if (TextUtils.isEmpty(name)) {
      return false;
    }
    if (name.contains(SDCARD0_NAME) || name.contains(SDCARD1_NAME)
        || name.contains(S50_SDCARD_NAME)) {
      return true;
    }
    return false;
  }

}
