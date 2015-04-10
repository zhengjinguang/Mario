package com.lemi.controller.lemigameassistance.utils;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.download.DownloadInfo.ContentType;
import com.lemi.mario.download.rpc.DownloadConstants;
import com.lemi.mario.download.rpc.DownloadConstants.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * This class provides some utility functions to manipulate data.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DataUtils {

  private static final String TAG = DataUtils.class.getSimpleName();
  private static final int MAX_PROGRESS = 100;
  private static final float K = 1024f;

  private DataUtils() {}

  /**
   * Stops an asynchronous task.
   */
  public static void stopAsyncTask(AsyncTask<?, ?, ?> asyncTask) {
    if (asyncTask != null && asyncTask.getStatus() != Status.FINISHED) {
      asyncTask.cancel(true);
    }
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public static <T> void runAsyncTask(AsyncTask<T, ?, ?> asyncTask, T... params) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
      } else {
        asyncTask.execute(params);
      }
    } catch (RejectedExecutionException e) {
      // Avoid add too many task, limit is 128
      e.printStackTrace();
    }
  }

  /**
   * Judge if this task is just created.
   *
   * @return true for created
   */
  public static boolean isCreated(DownloadInfo.Status status) {
    return status == DownloadInfo.Status.CREATED;
  }

  /**
   * Judge if this task is in download process.
   *
   * @return true for processing
   */
  public static boolean isInProcessing(DownloadInfo.Status status) {
    return status == DownloadInfo.Status.DOWNLOADING || status == DownloadInfo.Status.PAUSED;
  }

  /**
   * Judge if this task is finished.
   *
   * @return true for finished
   */
  public static boolean isFinished(DownloadInfo.Status status) {
    return status == DownloadInfo.Status.FAILED || status == DownloadInfo.Status.SUCCESS;
  }

  /**
   * Convert Download Status to Inner Download Status Code.
   *
   * @param status
   * @return inner status
   */
  public static List<Integer> getInnerStatusCode(DownloadInfo.Status status) {
    if (status == null) {
      Log.d(TAG, "null status");
      return null;
    }
    List<Integer> statuses = new ArrayList<Integer>();
    // TODO Chris make use the range of each kind of status
    switch (status) {
      case CREATED:
        statuses.add(DownloadConstants.Status.STATUS_CREATED);
        break;
      case DELETED:
        statuses.add(DownloadConstants.Status.STATUS_DELETED);
        break;
      case CANCELED:
        statuses.add(DownloadConstants.Status.STATUS_CANCELED);
      case DOWNLOADING:
        statuses.add(DownloadConstants.Status.STATUS_RUNNING);
        break;
      case PAUSED:
        statuses.add(DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED);
        statuses.add(DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB);
        statuses.add(DownloadConstants.Status.STATUS_PAUSED_BY_APP);
        break;
      case SUCCESS:
        statuses.add(DownloadConstants.Status.STATUS_SUCCESS);
        break;
      case FAILED:
        statuses.add(DownloadConstants.Status.STATUS_UNKNOWN_ERROR);
        statuses.add(DownloadConstants.Status.STATUS_OTHER_VERIFY_ERROR);
        statuses.add(DownloadConstants.Status.STATUS_FILE_LENGTH_VERIFY_ERROR);
        statuses.add(DownloadConstants.Status.STATUS_CRC_VERIFY_ERROR);
        statuses.add(DownloadConstants.Status.STATUS_MD5_VERIFY_ERROR);
        statuses.add(DownloadConstants.Status.STATUS_PF5_VERIFY_ERROR);
        statuses.add(DownloadConstants.Status.STATUS_FILE_ERROR);
        statuses.add(DownloadConstants.Status.STATUS_FILE_HAS_DELETED);
        statuses.add(DownloadConstants.Status.STATUS_INSUFFICIENT_SPACE_ERROR);
        statuses.add(DownloadConstants.Status.STATUS_DEVICE_NOT_FOUND_ERROR);
        statuses.add(DownloadConstants.Status.STATUS_CLIP_INFO_ERROR);
        statuses.add(DownloadConstants.Status.STATUS_CONTENT_LENGTH_MISMATCH);
        statuses.add(DownloadConstants.Status.STATUS_RESOLVE_REDIRECT_URL_FAILED);
        statuses.add(DownloadConstants.Status.STATUS_ACTUALLY_SIZE_UNKNOWN);
        statuses.add(DownloadConstants.Status.STATUS_REACHED_MAX_RETRIED_TIMES);
        statuses.add(DownloadConstants.Status.STATUS_DOWNLOADED_BYTES_OVERFLOW);
        statuses.add(DownloadConstants.Status.STATUS_TOO_MANY_REDIRECTS);
        statuses.add(DownloadConstants.Status.STATUS_SERVER_ERROR);
        break;
      case PENDING:
        statuses.add(DownloadConstants.Status.STATUS_PENDING);
        break;
      default:
        Log.d(TAG, "Catch an unknown status type");
    }
    return statuses;
  }

  /**
   * Convert inner download status code to download status.
   *
   * @param innerDownloadStatusCode
   * @return download status
   */
  public static DownloadInfo.Status getDownloadStatus(int innerDownloadStatusCode) {
    // TODO
    /* There is some duplicate case, they will be added after qisen correct them */
    switch (innerDownloadStatusCode) {
      case DownloadConstants.Status.STATUS_RUNNING:
        return DownloadInfo.Status.DOWNLOADING;
      case DownloadConstants.Status.STATUS_CREATED:
        /* Return Created */
        return DownloadInfo.Status.CREATED;
      case DownloadConstants.Status.STATUS_DELETED:
        return DownloadInfo.Status.DELETED;
      case DownloadConstants.Status.STATUS_CANCELED:
        return DownloadInfo.Status.CANCELED;
      case DownloadConstants.Status.STATUS_PENDING:
        return DownloadInfo.Status.PENDING;
      case DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED:
      case DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB:
      case DownloadConstants.Status.STATUS_PAUSED_BY_APP:
      case DownloadConstants.Status.STATUS_NO_WRITE_PERMISSION:
      case DownloadConstants.Status.STATUS_INSUFFICIENT_SPACE_ERROR:
        return DownloadInfo.Status.PAUSED;
      case DownloadConstants.Status.STATUS_SUCCESS:
        return DownloadInfo.Status.SUCCESS;
      default:
        return DownloadInfo.Status.FAILED;

    }
  }

  /**
   * convert the inner resource type to content type.
   *
   * @param type
   * @return content type
   */
  public static DownloadInfo.ContentType getContentType(ResourceType type) {
    if (type == null) {
      Log.d(TAG, "catch a null resource type");
      return ContentType.UNKNOWN;
    }

    switch (type) {
      case APP:
        return ContentType.APP;
      case VIDEO:
        return ContentType.VIDEO;
      case COMIC:
        return ContentType.COMIC;
      case IMAGE:
        return ContentType.IMAGE;
      case MISC:
        return ContentType.MISC;
      case MUSIC:
        return ContentType.MUSIC;
      case PATCH:
        return ContentType.PATCH;
      case EBOOK:
        return ContentType.BOOK;
      case DATA_PACKET:
        return ContentType.DATA_PACKET;
      case PLUGIN:
        return ContentType.PLUGIN;
      case ZIP:
        return ContentType.ZIP;
      case UPGRADE:
        return ContentType.UPGRADE;
      default:
        return ContentType.UNKNOWN;
    }
  }

  /**
   * convert content type to inner resource type.
   *
   * @param type
   * @return inner resource type
   */
  public static ResourceType getInnerResourceType(DownloadInfo.ContentType type) {

    if (type == null) {
      Log.d(TAG, "catch a null content type");
      return ResourceType.UNKNOWN;
    }
    switch (type) {
      case APP:
        return ResourceType.APP;
      case MUSIC:
        return ResourceType.MUSIC;
      case VIDEO:
        return ResourceType.VIDEO;
      case IMAGE:
        return ResourceType.IMAGE;
      case BOOK:
        return ResourceType.EBOOK;
      case COMIC:
        return ResourceType.COMIC;
      case PATCH:
        return ResourceType.PATCH;
      case MISC:
        return ResourceType.MISC;
      case DATA_PACKET:
        return ResourceType.DATA_PACKET;
      case PLUGIN:
        return ResourceType.PLUGIN;
      case ZIP:
        return ResourceType.ZIP;
      case UPGRADE:
        return ResourceType.UPGRADE;
      case UNKNOWN:
      default:
        return ResourceType.UNKNOWN;
    }
  }

  public static String formatVersion(String versionName) {
    String formattedVersion = versionName;
    if (TextUtils.isEmpty(versionName)) {
      return formattedVersion;
    }
    if (formattedVersion.startsWith("V") || formattedVersion.startsWith("v")) {
      formattedVersion = formattedVersion.substring(1);
    }
    formattedVersion = "v" + formattedVersion;
    return formattedVersion;
  }

}
