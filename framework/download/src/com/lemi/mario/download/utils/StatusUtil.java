package com.lemi.mario.download.utils;

import com.lemi.mario.download.rpc.DownloadConstants;
import com.lemi.mario.download.rpc.DownloadConstants.Status;

public class StatusUtil {

  public static boolean isStatusDeleted(int status) {
    return status == DownloadConstants.Status.STATUS_DELETED;
  }

  /**
   * download error caused by media error.
   * 
   * @param status
   * @return
   */
  public static boolean isStatusMediaError(int status) {
    return status == Status.STATUS_DEVICE_NOT_FOUND_ERROR
        || status == Status.STATUS_INSUFFICIENT_SPACE_ERROR
        || status == Status.STATUS_FILE_ERROR
        || status == Status.STATUS_FILE_HAS_DELETED;
  }

  /**
   * Returns whether the status is a success (i.e. 2xx).
   */
  public static boolean isStatusSuccess(int status) {
    return status == DownloadConstants.Status.STATUS_SUCCESS;
  }

  /**
   * Returns whether the status is an error
   */
  public static boolean isStatusError(int status) {
    return status >= 400;
  }

  public static boolean isStatusSdcardError(int status) {
    return status == Status.STATUS_DEVICE_NOT_FOUND_ERROR
        || status == Status.STATUS_INSUFFICIENT_SPACE_ERROR
        || status == Status.STATUS_FILE_ERROR;
  }

  /**
   * Returns whether the download has completed (either with success or
   * error).
   */
  public static boolean isStatusCompleted(int status) {
    return (status >= Status.STATUS_SUCCESS
        && status <= Status.STATUS_DELETED)
        || isStatusError(status);
  }

  public static boolean isStatusPaused(int status) {
    return status == Status.STATUS_PAUSED_BY_APP
        || status == Status.STATUS_QUEUED_FOR_WIFI_OR_USB
        || status == Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED;
  }

  public static boolean isStatusRunning(int status) {
    return status == Status.STATUS_CREATED
        || status == Status.STATUS_PENDING
        || status == Status.STATUS_RUNNING;
  }

}
