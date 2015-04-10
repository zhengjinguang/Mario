package com.lemi.mario.externalmanager.manager;

import android.content.Context;
import android.content.Intent;

import com.lemi.mario.externalmanager.model.ExternalStorageInfo;
import com.lemi.mario.externalmanager.utils.ExternalStorageUtils;

import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ExternalStorageManager {

  private static ExternalStorageManager instance;

  private List<ExternalStorageInfo> externalStorageInfoList;

  private ExternalStorageManager() {
    updateStorageInfo();
  }

  public static synchronized ExternalStorageManager getInstance() {
    if (instance == null) {
      instance = new ExternalStorageManager();
    }
    return instance;
  }


  public synchronized ExternalStorageInfo getMaxSizeSdcard() {
    return ExternalStorageUtils.getMaxSizeSdcard(externalStorageInfoList);
  }

  public synchronized ExternalStorageInfo getMaxSizeExternalStorage() {
    return ExternalStorageUtils.getMaxSizeExternalStorage(externalStorageInfoList);
  }

  public ExternalStorageInfo getLocalExternalStorage() {
    return ExternalStorageUtils.getLocalExternalStorage();
  }


  public synchronized boolean hasSdcard() {
    return ExternalStorageUtils.hasSdcard(externalStorageInfoList);
  }

  public synchronized boolean hasOtherExternalStorage() {
    return ExternalStorageUtils.hasOtherExternalStorage(externalStorageInfoList);
  }

  public void receiveMountMessage(Context context, Intent intent) {
    if (context != null) {
      updateStorageInfo();
    }
  }

  private synchronized void updateStorageInfo() {
    externalStorageInfoList = ExternalStorageUtils.searchExternalStorageInPossiblePath();
  }

}
