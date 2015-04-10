package com.lemi.controller.lemigameassistance.download;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.mario.download.rpc.DownloadConstants;
import com.lemi.mario.download.rpc.InnerDownloadInfo;

import org.apache.http.util.LangUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Single downloadInfo impl DownloadInfo.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadInfoSingle implements DownloadInfo {
  private final InnerDownloadInfo innerDownloadInfo;
  private HashMap<String, String> extraResource = null;

  public DownloadInfoSingle(InnerDownloadInfo innerDownloadInfo) {
    this.innerDownloadInfo = innerDownloadInfo;
  }

  /**
   * return detail status about download task.
   * 
   * @return sub status
   */
  public SubStatus getSubStatus() {
    switch (innerDownloadInfo.getStatus()) {
      case DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED:
        return SubStatus.PAUSED_BY_MEDIA;
      case DownloadConstants.Status.STATUS_NO_WRITE_PERMISSION:
        return SubStatus.PAUSED_BY_NO_WRITE_PERMISSION;
      case DownloadConstants.Status.STATUS_INSUFFICIENT_SPACE_ERROR:
        return SubStatus.PAUSED_BY_NO_SPACE;
      case DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB:
        return SubStatus.PAUSED_BY_NETWORK;
      case DownloadConstants.Status.STATUS_PAUSED_BY_APP:
        return SubStatus.PAUSED_BY_APP;
      default:
        return SubStatus.OTHERS;
    }
  }

  public long getId() {
    return innerDownloadInfo.mId;
  }

  public String getIdentity() {
    return innerDownloadInfo.mIdentity;
  }

  public long getCurrentBytes() {
    return innerDownloadInfo.mCurrentBytes;
  }

  public long getSpeed() {
    return innerDownloadInfo.getSpeed();
  }

  public long getTotalBytes() {
    return innerDownloadInfo.mTotalBytes;
  }

  public String getFilePath() {
    return innerDownloadInfo.mFilePath;
  }

  public String getIntermediateFilePath() {
    return innerDownloadInfo.getIntermediateFilePath();
  }

  public String getTitle() {
    return innerDownloadInfo.mTitle;
  }

  public String getMimeType() {
    return innerDownloadInfo.mMimeType;
  }

  public Status getStatus() {
    return DataUtils.getDownloadStatus(innerDownloadInfo.getStatus());
  }

  public Status getPreviousStatus() {
    return DataUtils.getDownloadStatus(innerDownloadInfo.previousStatus);
  }

  public ContentType getContentType() {
    return DataUtils.getContentType(innerDownloadInfo.mType);
  }

  public String getIcon() {
    return innerDownloadInfo.mIconUrl;
  }

  public String getMD5() {
    return innerDownloadInfo.mMd5;
  }

  /**
   * @return download url
   */
  public String getDownloadUrl() {
    return innerDownloadInfo.mUri;
  }

  public boolean isVisible() {
    return innerDownloadInfo.mVisible;
  }

  public long getSpeedLimit() {
    return innerDownloadInfo.speedLimit;
  }

  public String getStringExtra(String key) {
    if (extraResource == null) {
      extraResource = getExtraResource();
    }

    if (extraResource == null) {
      return null;
    }
    return extraResource.get(key);
  }

  public int getIntExtra(String key) {
    if (extraResource == null) {
      extraResource = getExtraResource();
    }

    if (extraResource == null) {
      return 0;
    }
    String value = extraResource.get(key);

    if (!TextUtils.isEmpty(value)) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }

    return 0;
  }

  public long getLongExtra(String key) {
    if (extraResource == null) {
      extraResource = getExtraResource();
    }

    if (extraResource == null) {
      return 0L;
    }
    String value = extraResource.get(key);

    if (!TextUtils.isEmpty(value)) {
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }

    return 0L;
  }

  public boolean getBooleanExtra(String key, boolean defaultValue) {
    if (extraResource == null) {
      extraResource = getExtraResource();
    }

    if (extraResource == null) {
      return defaultValue;
    }
    String value = extraResource.get(key);

    if (!TextUtils.isEmpty(value)) {
      return Boolean.parseBoolean(value);
    }

    return defaultValue;
  }

  public long getLastMod() {
    return innerDownloadInfo.getLastMod();
  }


  private HashMap<String, String> getExtraResource() {
    Gson gson = new Gson();
    return gson.fromJson(innerDownloadInfo.mExtras,
        new TypeToken<HashMap<String, String>>() {}.getType());
  }

  public Iterator<DownloadInfoSingle> iterator() {
    // has no left leaf to iterate
    return Arrays.asList(this).iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DownloadInfoSingle single = (DownloadInfoSingle) o;

    if (getId() != single.getId()) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    final int HASH_SEED = 17;
    return LangUtils.hashCode(HASH_SEED, getId());
  }
}
