package com.lemi.controller.lemigameassistance.download;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for filter the download info as you want.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class DownloadFilter {
  private static final String TAG = DownloadFilter.class.getSimpleName();

  private final List<DownloadInfo.ContentType> acceptedType;
  private final List<DownloadInfo.Status> acceptedStatus;
  private final List<Pair<String, String>> acceptedExtras;
  private final long maxSize;
  private final long minSize;
  private final Boolean visible;

  public static Builder newBuilder() {
    return new Builder();
  }

  private DownloadFilter(Builder builder) {
    acceptedType = builder.acceptedType;
    acceptedStatus = builder.acceptedStatus;
    maxSize = builder.maxSize;
    minSize = builder.minSize;
    visible = builder.visible;
    acceptedExtras = builder.acceptedExtras;
  }

  public List<DownloadInfo.ContentType> getAcceptedType() {
    return acceptedType;
  }

  public List<DownloadInfo.Status> getAcceptedStatus() {
    return acceptedStatus;
  }

  public List<Pair<String, String>> getAcceptedExtra() {
    return acceptedExtras;
  }

  public long getMaxSize() {
    return maxSize;
  }

  public long getMinSize() {
    return minSize;
  }

  public Boolean getVisible() {
    return visible;
  }

  public static class Builder {
    private List<DownloadInfo.ContentType> acceptedType;
    private List<DownloadInfo.Status> acceptedStatus;
    private List<Pair<String, String>> acceptedExtras;
    private long maxSize = -1L;
    private long minSize = -1L;
    private Boolean visible;// if not set, both visible tasks and invisible tasks will be returned

    public Builder setAcceptedContentType(DownloadInfo.ContentType... types) {
      if (types != null && types.length >= 0) {
        acceptedType = new ArrayList<DownloadInfo.ContentType>();
        for (DownloadInfo.ContentType next : types) {
          acceptedType.add(next);
        }
      }

      return this;
    }

    public Builder setAcceptedStatus(DownloadInfo.Status... statuses) {
      if (statuses != null && statuses.length >= 0) {
        acceptedStatus = new ArrayList<DownloadInfo.Status>();
        for (DownloadInfo.Status next : statuses) {
          acceptedStatus.add(next);
        }
      }

      return this;
    }

    public Builder setAcceptedExtra(Pair<String, String>... pairs) {
      if (pairs != null && pairs.length >= 0) {
        acceptedExtras = new ArrayList<Pair<String, String>>();
        for (Pair<String, String> pair : pairs) {
          if (!TextUtils.isEmpty(pair.first) && !TextUtils.isEmpty(pair.second)) {
            acceptedExtras.add(pair);
          }
        }
      }
      return this;
    }

    public Builder setVisible(boolean visible) {
      this.visible = visible;
      return this;
    }

    public DownloadFilter build() {
      if (minSize != -1 && maxSize != -1 && minSize > maxSize) {
        Log.d(TAG, "wrong size range, check it again");
        return null;
      }

      return new DownloadFilter(this);
    }
  }

}
