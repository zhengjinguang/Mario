package com.lemi.mario.externalmanager.utils;

import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.externalmanager.manager.ExternalStorageManager;
import com.lemi.mario.externalmanager.model.ExternalStorageInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class TagUtils {

  private static final String TAG_DIR = "MountTag";
  public static final String COPY_TAG_FILE = TAG_DIR + File.separator + "mountTag";
  public static final String LOCAL_TAG_FILE = TAG_DIR + File.separator + "localTag";

  public static void writeTag(String rootPath, String tagFileName, String tag) {
    if (!FileUtil.exists(rootPath) || !FileUtil.canWrite(rootPath)) {
      return;
    }
    try {
      File dirFile = new File(rootPath + File.separator + TAG_DIR);
      if (!dirFile.exists()) {
        dirFile.mkdirs();
      }
      FileWriter fileWriter = new FileWriter(rootPath + File.separator + tagFileName);
      fileWriter.write(tag + "\n");
      fileWriter.flush();
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getTagFromLocal(String tagFileName) {
    return FileUtil.readFileFirstLine(ExternalStorageUtils.LOCAL_SDCARD_PATH + File.separator
        + tagFileName);
  }

  public static String getTagFromExternal(String tagFileName) {
    ExternalStorageInfo externalStorageInfo =
        ExternalStorageManager.getInstance().getMaxSizeSdcard();
    if (externalStorageInfo == null) {
      return null;
    }
    return FileUtil.readFileFirstLine(externalStorageInfo.getPath() + File.separator + tagFileName);
  }

  public static void clearLocalTagFile(String tagFileName) {
    FileUtil.deleteFile(ExternalStorageUtils.LOCAL_SDCARD_PATH + File.separator + tagFileName);
  }

  public static boolean isTagFileExistAtLocal(String tagFileName) {
    return FileUtil.exists(ExternalStorageUtils.LOCAL_SDCARD_PATH + File.separator + tagFileName);
  }

  public static boolean isTagFileExistAtExternal(String tagFileName) {
    ExternalStorageInfo externalStorageInfo =
        ExternalStorageManager.getInstance().getMaxSizeSdcard();
    if (externalStorageInfo == null) {
      return false;
    }
    return FileUtil.exists(externalStorageInfo.getPath() + File.separator + tagFileName);
  }

}
