package com.lemi.controller.lemigameassistance.utils;

import com.lemi.controller.lemigameassistance.GameMasterApplication;
import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.download.DownloadInfo.ContentType;
import com.lemi.controller.lemigameassistance.download.DownloadUtils;
import com.lemi.controller.lemigameassistance.manager.SDCardManager;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.SizeConvertUtil;

import java.text.DecimalFormat;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameDownloadUtils {

  private static final float EXPAND_RATE = 1.1f;
  private static final long APP_DEFAULT_SIZE_IN_ZIP = 50 * Constants.MB;
  private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

  public static String judgeGameDownloadPath(long downloadFileSize, ContentType contentType) {
    return judgeGameDownloadPath(downloadFileSize, 0, contentType);
  }

  public static String judgeGameDownloadPath(long downloadFileSize, long dataFileSize,
      ContentType contentType) {
    String downloadPath = null;
    switch (contentType) {
      case APP:
        if (!canAppInstall((long) (downloadFileSize * EXPAND_RATE))) {
          MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_install_tips);
          break;
        }
        if (DownloadUtils
            .hasEnoughSizeToDownload(downloadFileSize, PathUtils.getDownloadRootPath())) {
          downloadPath = PathUtils.getDownloadFolderPath(contentType);
        } else {
          MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_download_tips);
        }
        break;
      case ZIP:
        if (!canAppInstall(APP_DEFAULT_SIZE_IN_ZIP)) {
          MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_install_tips);
          break;
        }
        if (!canAppDataInstall((long) (dataFileSize * EXPAND_RATE))) {
          if (SDCardManager.getInstance().getSdCardSupportStatus()
                  == SDCardManager.SDCardSupportStatus.NOT_SUPPORT) {
            MainThreadPostUtils.toastLong(GameMasterApplication.getAppContext().getString(
                R.string.add_external_storage, SizeConvertUtil.transByte2MB(dataFileSize)));
            break;
          }
          MainThreadPostUtils.toastLong(R.string.no_enough_storage_unzip_tips);
          break;
        }
        if (FileUtil.getAvailableBytes(PathUtils.getDownloadRootPath()) >= downloadFileSize
            + dataFileSize) {
          downloadPath = PathUtils.getDownloadFolderPath(contentType);
        } else if (FileUtil.getAvailableBytes(PathUtils.getExternalStorageRootPath()) >= downloadFileSize) {
          downloadPath = PathUtils.getExternalStorageDownloadPath();
          MainThreadPostUtils.toastLong(R.string.download_to_external_storage);
        } else {
          MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_download_in_udisk_tips);
        }

        break;
      default:
        if (DownloadUtils
            .hasEnoughSizeToDownload(downloadFileSize + dataFileSize,
                PathUtils.getDownloadRootPath())) {
          downloadPath = PathUtils.getDownloadFolderPath(contentType);
        } else {
          MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_download_tips);
        }
        break;
    }
    return downloadPath;
  }

  private static boolean canAppInstall(long appSize) {
    if (FileUtil.getAvailableBytes(PathUtils.getAppInstallRootPath()) >= appSize) {
      return true;
    }
    return false;
  }

  private static boolean canAppDataInstall(long appSize) {
    if (FileUtil.getAvailableBytes(PathUtils.getUnzipDataFolderPath()) >= appSize) {
      return true;
    }
    return false;
  }
}
