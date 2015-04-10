package com.lemi.controller.lemigameassistance.manager;

import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.download.DownloadListener;
import com.lemi.controller.lemigameassistance.download.DownloadManager;
import com.lemi.controller.lemigameassistance.utils.PathUtils;
import com.lemi.controller.lemigameassistance.utils.ThreadPool;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.MainThreadPostUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Schedule game download - unzip - install task.
 * unzip will not exist in app type.
 * {@link com.lemi.controller.lemigameassistance.download.DownloadInfo.ContentType}
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class BackgroundScheduleManager {

  private static BackgroundScheduleManager instance = new BackgroundScheduleManager();
  private final Map<String, DownloadInfo> scheduleMap = new HashMap<String, DownloadInfo>();

  public static synchronized BackgroundScheduleManager getInstance() {
    if (instance == null) {
      instance = new BackgroundScheduleManager();
    }
    return instance;
  }

  private BackgroundScheduleManager() {
    DownloadManager.getInstance().addVisibleListener(downloadListener);
    ZipManager.getInstance().addListener(unzipListener);
    InstallManager.getInstance().addListener(installListener);
  }

  private final ZipManager.ZipListener unzipListener = new ZipManager.ZipListener() {
    @Override
    public void onZipStatusChange(String token, ZipManager.ZipStatus status) {
      if (TextUtils.isEmpty(token)) {
        return;
      }
      if (status == ZipManager.ZipStatus.SUCCESS) {
        DownloadInfo info = getDownloadInfo(token);
        if (!checkDownloadInfoValid(info)) {
          return;
        }
        startInstall(info);
      } else if (status == ZipManager.ZipStatus.FAILED) {
        DownloadInfo info = getDownloadInfo(token);
        if (!checkDownloadInfoValid(info)) {
          return;
        }
        handlerZipFailedPauseError(info);
      } else if(status == ZipManager.ZipStatus.CANCEL_BY_USER){
        DownloadInfo info = getDownloadInfo(token);
        if (!checkDownloadInfoValid(info)) {
          return;
        }
        handlerCancelByUser(info);
      }
    }

    @Override
    public void onZipProgressChange(String token, int percent) {}
  };

  private final InstallManager.InstallListener installListener =
      new InstallManager.InstallListener() {
        @Override
        public void onInstallStatusChange(String packageName, InstallManager.InstallStatus status) {
          if (TextUtils.isEmpty(packageName)) {
            return;
          }
          if (status == InstallManager.InstallStatus.SUCCESS) {
            checkPackageAndNotify(packageName, true);
            cleanDownloadFileWhenInstallSuccess(packageName);
            removeDownloadInfo(packageName);
          } else if (status == InstallManager.InstallStatus.FAILED) {
            checkPackageAndNotify(packageName, false);
          }
        }
      };

  private final DownloadListener downloadListener = new DownloadListener() {
    @Override
    public void onProgressChanged(DownloadInfo info) {}

    @Override
    public void onStatusChanged(DownloadInfo info) {
      if (info == null || TextUtils.isEmpty(info.getIdentity())) {
        return;
      }
      if (info.getStatus() == DownloadInfo.Status.SUCCESS) {
        setDownloadInfo(info.getIdentity(), info);
        if (info.getContentType() == DownloadInfo.ContentType.APP) {
          startInstall(info);
        } else if (info.getContentType() == DownloadInfo.ContentType.ZIP) {
          startUnzip(info);
        }
      } else if (info.getStatus() == DownloadInfo.Status.PAUSED) {
        handlerDownloadPauseError(info);
      } else if (info.getStatus() == DownloadInfo.Status.FAILED) {
        handlerDownloadFailedError(info);
      } else if(info.getStatus() == DownloadInfo.Status.CANCELED){
        handlerCancelByUser(info);
      }

    }
  };


  public void setPackageSchedule(String packageName, DownloadInfo info) {
    synchronized (scheduleMap) {
      scheduleMap.put(packageName, info);
    }
  }

  private static void startUnzip(DownloadInfo downloadInfo) {
    ZipManager.getInstance().startUnZipGameDataTaskAsync(downloadInfo.getIdentity(),
        downloadInfo.getFilePath(), PathUtils.getUnzipApkFolderPath(),
        PathUtils.getUnzipDataFolderPath(),
        PathUtils.getApkFullPath(downloadInfo.getIdentity()));
  }

  private static void startInstall(DownloadInfo downloadInfo) {
    if (downloadInfo.getContentType() == DownloadInfo.ContentType.APP) {
      InstallManager.getInstance().asyncInstallPackage(downloadInfo.getIdentity(),
          downloadInfo.getFilePath());
    } else {
      InstallManager.getInstance().asyncInstallPackage(downloadInfo.getIdentity(),
          PathUtils.getApkFullPath(downloadInfo.getIdentity()));
    }
  }

  private void removeDownloadInfo(String packageName) {
    synchronized (scheduleMap) {
      scheduleMap.remove(packageName);
    }
  }

  private void setDownloadInfo(String packageName, DownloadInfo downloadInfo) {
    if (TextUtils.isEmpty(packageName)) {
      return;
    }
    synchronized (scheduleMap) {
      scheduleMap.put(packageName, downloadInfo);
    }
  }

  private DownloadInfo getDownloadInfo(String packageName) {
    synchronized (scheduleMap) {
      return scheduleMap.get(packageName);
    }
  }

  private void cleanDownloadFileWhenInstallSuccess(final String packageName) {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        CleanManager.getInstance().cleanDownloadFileByPackageName(packageName);
      }
    });
  }

  private void checkPackageAndNotify(String packageName, boolean isSuccess) {
    DownloadInfo downloadInfo;
    synchronized (scheduleMap) {
      downloadInfo = scheduleMap.get(packageName);
    }
    if (downloadInfo == null || TextUtils.isEmpty(downloadInfo.getTitle())) {
      return;
    }
    String toast;
    if (isSuccess) {
      toast =
          GlobalConfig.getAppContext().getString(R.string.notify_install_sucess,
              downloadInfo.getTitle());
    } else {
      toast =
          GlobalConfig.getAppContext().getString(R.string.notify_install_failed,
              downloadInfo.getTitle());
    }
    MainThreadPostUtils.toast(toast);
  }

  private boolean checkDownloadInfoValid(DownloadInfo downloadInfo) {
    if (downloadInfo == null || TextUtils.isEmpty(downloadInfo.getIdentity())) {
      return false;
    }
    return true;
  }

  private void handlerDownloadPauseError(DownloadInfo downloadInfo) {
    switch (downloadInfo.getSubStatus()) {
      case PAUSED_BY_MEDIA:
        MainThreadPostUtils.toastLong(GlobalConfig.getAppContext().getString(
            R.string.download_error_media_error,
            downloadInfo.getTitle()));
        break;
      case PAUSED_BY_NO_WRITE_PERMISSION:
        MainThreadPostUtils.toastLong(GlobalConfig.getAppContext().getString(
            R.string.download_error_no_write_permission,
            downloadInfo.getTitle()));
        break;
      case PAUSED_BY_NO_SPACE:
        MainThreadPostUtils.toastLong(GlobalConfig.getAppContext().getString(
            R.string.download_error_no_enough_space,
            downloadInfo.getTitle()));
        break;
      case PAUSED_BY_NETWORK:
        MainThreadPostUtils.toastLong(GlobalConfig.getAppContext().getString(
            R.string.download_error_network_error,
            downloadInfo.getTitle()));
        break;
      default:
        break;
    }
  }

  private void handlerDownloadFailedError(DownloadInfo downloadInfo) {
    MainThreadPostUtils.toastLong(GlobalConfig.getAppContext().getString(
        R.string.download_error_failed,
        downloadInfo.getTitle()));
  }

  private void handlerZipFailedPauseError(DownloadInfo downloadInfo) {
    MainThreadPostUtils.toastLong(GlobalConfig.getAppContext().getString(
        R.string.unzip_error_failed,
        downloadInfo.getTitle()));
  }

  private void handlerCancelByUser(DownloadInfo downloadInfo) {
    MainThreadPostUtils.toastLong(GlobalConfig.getAppContext().getString(
            R.string.unzip_cancel_by_user,
            downloadInfo.getTitle()));
  }

}
