package com.lemi.controller.lemigameassistance.download;

import android.text.TextUtils;

import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.download.utils.StorageUtil;

import java.io.File;

/**
 * Download utility class.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadUtils {

  private DownloadUtils() {}

  /**
   * Check If a download request has enough Size to download.
   * 
   * @return whether having enough size.
   */
  public static synchronized boolean hasEnoughSizeToDownload(long size) {
    return StorageUtil.hasEnoughSize(size);
  }

  public static String getFileFolder(String filePath) {
    if (TextUtils.isEmpty(filePath)) {
      return null;
    }
    File file = new File(filePath);
    file.getParentFile().mkdirs();
    return file.getParentFile().getAbsolutePath();
  }


  /**
   * Check If has enough Size to download to Specific path.
   *
   * @return whether having enough size.
   */
  public static synchronized boolean hasEnoughSizeToDownload(long size, String folderPath) {
    if (FileUtil.getAvailableBytes(folderPath) >= size) {
      return true;
    }
    return false;
  }

  public static int getDownloadingCount() {
    DownloadFilter downloadFilter = createDownloadingTaskBuilder().build();
    return DownloadManager.getInstance().getDownloadCount(downloadFilter);
  }

  public static int getOngoingDownloadCount() {
    return DownloadManager.getInstance().getDownloadCount(
        createOnGoingDownloadFilterBuilder().build());
  }

  private static DownloadFilter.Builder createDownloadingTaskBuilder() {
    return DownloadFilter.newBuilder().setAcceptedStatus(DownloadInfo.Status.CREATED,
        DownloadInfo.Status.DOWNLOADING, DownloadInfo.Status.PENDING);
  }

  public static DownloadFilter.Builder createOnGoingDownloadFilterBuilder() {
    return DownloadFilter.newBuilder()
        .setAcceptedStatus(DownloadInfo.Status.CREATED, DownloadInfo.Status.DOWNLOADING,
            DownloadInfo.Status.PAUSED, DownloadInfo.Status.PENDING);
  }

  public static DownloadFilter.Builder createVisibleOnGoingDownloadFilterBuilder() {
    return createOnGoingDownloadFilterBuilder().setVisible(true);
  }


  public static boolean isTaskRunning(DownloadInfo.Status status) {
    return status == DownloadInfo.Status.CREATED || status == DownloadInfo.Status.DOWNLOADING
        || status == DownloadInfo.Status.PAUSED || status == DownloadInfo.Status.PENDING;
  }

  public static boolean isTaskRunning(DownloadInfo downloadInfo) {
    if (downloadInfo == null) {
      return false;
    }
    DownloadInfo.Status status = downloadInfo.getStatus();
    return status == DownloadInfo.Status.CREATED || status == DownloadInfo.Status.DOWNLOADING
        || status == DownloadInfo.Status.PAUSED || status == DownloadInfo.Status.PENDING;
  }

}
