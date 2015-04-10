package com.lemi.controller.lemigameassistance.download;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.lemi.mario.base.concurrent.RestrictGivenPeriodExecutor;

/**
 * Temporarily the Usage of the service is to keep downloadClient alive.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class DownloadService extends Service {

  private static final String TAG = DownloadService.class.getSimpleName();

  private static final long INTERNAL_TIME = 3000; // ms

  private Runnable checkRunningCountTask = new Runnable() {
    @Override
    public void run() {
      if (DownloadUtils.getDownloadingCount() == 0) {
        stopSelf();
      }
    }
  };

  private RestrictGivenPeriodExecutor restrictGivenPeriodExecutor =
      new RestrictGivenPeriodExecutor(INTERNAL_TIME);

  /**
   * Visible download listener.
   * Only watching visible downloading tasks.
   */
  private DownloadListener downloadListener = new DownloadListener() {
    @Override
    public void onProgressChanged(DownloadInfo info) {
      // do nothing momentarily.
    }

    @Override
    public void onStatusChanged(DownloadInfo info) {
      if (info.getStatus() == DownloadInfo.Status.CANCELED
          || info.getStatus() == DownloadInfo.Status.SUCCESS
          || info.getStatus() == DownloadInfo.Status.FAILED) {
        restrictGivenPeriodExecutor.execute(checkRunningCountTask);
      }
    }
  };

  @Override
  public IBinder onBind(Intent arg0) {
    // do nothing momentarily.
    return null;
  }

  /**
   * When service starts
   * it will check all downloads which should been
   * to download.
   */
  @Override
  public void onCreate() {
    Log.d(TAG, "download service created.");
    restrictGivenPeriodExecutor.execute(checkRunningCountTask);
    DownloadManager.getInstance().addVisibleListener(downloadListener);
    DownloadManager.getInstance().addInvisibleListener(downloadListener);
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "download service destroyed.");
    restrictGivenPeriodExecutor.shutdown();
  }

}
