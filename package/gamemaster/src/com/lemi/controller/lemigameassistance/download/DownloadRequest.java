package com.lemi.controller.lemigameassistance.download;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to build a download request for pheonix download caller.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class DownloadRequest {

  private static final String TAG = DownloadRequest.class.getSimpleName();

  public static final int UNKNOWN_SIZE = -1;

  public final String url;
  public final String title;
  public final String identity;
  public final String extraResource;
  private final Map<String, String> extrasMap;
  public final DownloadInfo.ContentType contentType;
  public final boolean allowInMobile;
  public final long totalBytes;
  public final String description;
  public final String source;
  public final String iconUrl;

  // set FilePath
  public final String folderPath;
  // set FileName
  public final String fileName;

  public final long threshold;
  public final long checkSize;
  public final boolean visible; // whether it can be seen from notification bar and download list
  public long speedLimit = -1;
  public final VerifyType verifyType;
  public final String verifyValue;

  public static enum VerifyType {
    MD5, PF5
  }

  private DownloadRequest(Builder builder) {
    this.allowInMobile = builder.allowInMobile;
    this.checkSize = builder.checkSize;
    this.contentType = builder.contentType;
    this.description = builder.description;
    this.iconUrl = builder.iconUrl;
    this.identity = builder.identity;
    this.source = builder.source;
    this.threshold = builder.threshold;
    this.title = builder.title;
    this.totalBytes = builder.totalBytes;
    this.visible = builder.visible;
    this.fileName = builder.fileName;
    this.folderPath = builder.folderPath;
    this.speedLimit = builder.speedLimit;
    this.verifyType = builder.verifyType;
    this.verifyValue = builder.verifyValue;
    this.url = generateUrl(builder);
    this.extrasMap = builder.extraResourceMap;
    if (builder.extraResourceMap == null) {
      this.extraResource = null;
    } else {
      JSONObject object = new JSONObject(builder.extraResourceMap);
      this.extraResource = object.toString();
    }
  }

  /**
   * Modify url when needed
   * 
   * @param builder
   * @return
   */
  private String generateUrl(Builder builder) {
    return builder.url;
  }

  private Map<String, String> createParameters() {
    Map<String, String> params = new HashMap<String, String>();
    return params;
  }

  private String appendParametersIfNotExists(String url, Map<String, String> params) {
    Uri origin = Uri.parse(url);
    Uri.Builder builder = origin.buildUpon();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      if (origin.getQueryParameter(entry.getKey()) == null) {
        builder.appendQueryParameter(entry.getKey(), entry.getValue());
      }
    }
    return builder.toString();
  }

  public void putExtra(String key, String value) {
    extrasMap.put(key, value);
  }

  public String getExtraValue(String key) {
    return extrasMap != null ? extrasMap.get(key) : null;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String url = null;
    private String title = null;
    private String identity = null;
    private HashMap<String, String> extraResourceMap = null;
    private DownloadInfo.ContentType contentType = null;
    private boolean allowInMobile = false;
    private long totalBytes = -1;
    private String description = null;
    private String source = null;
    private String iconUrl = null;
    private long threshold = 0;
    private long checkSize = 0;
    private boolean visible = true;
    private String fileName;
    private String folderPath;
    private long speedLimit = -1;
    private VerifyType verifyType;
    private String verifyValue;

    /**
     * Sets file name, excluding path.
     * 
     * @param fileName file name
     * @return request builder
     */
    public Builder setFileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    /**
     * Sets destination folder of download file.
     * 
     * @param folderPath folder path
     * @return request builder
     */
    public Builder setFolderPath(String folderPath) {
      this.folderPath = folderPath;
      return this;
    }

    /**
     * Set the download url.
     * 
     * @param url
     * @return builder
     */
    public Builder setUrl(String url) {
      this.url = url;
      return this;
    }

    /**
     * Set the download task's title.
     * 
     * @param title
     * @return builder
     */
    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    /**
     * Set the identity of download task, for instance, package name is a app's identity by now.
     * 
     * @param identity
     * @return builder
     */
    public Builder setIdentity(String identity) {
      this.identity = identity;
      return this;
    }

    /**
     * Set the content type.
     * 
     * @param contentType
     * @return builder
     */
    public Builder setContentType(DownloadInfo.ContentType contentType) {
      this.contentType = contentType;
      return this;
    }

    /**
     * Set if the task is allowed to download in 2g/3g network, false for default.
     * 
     * @param allowInMobile
     * @return builder
     */
    public Builder setAllowInMobile(boolean allowInMobile) {
      this.allowInMobile = allowInMobile;
      return this;
    }


    /**
     * Set the total size of the download task.
     * 
     * @param totalBytes
     * @return builder
     */
    public Builder setTotalBytes(long totalBytes) {
      this.totalBytes = totalBytes;
      return this;
    }


    /**
     * Set the description of the download task, option field.
     * 
     * @param description
     * @return builder
     */
    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }


    /**
     * Set the source of the download task.
     * 
     * @param source
     * @return builder
     */
    public Builder setSource(String source) {
      this.source = source;
      return this;
    }

    /**
     * Set icon url of the download task, option field.
     * 
     * @param iconUrl
     * @return builder
     */
    public Builder setIconUrl(String iconUrl) {
      this.iconUrl = iconUrl;
      return this;
    }

    /**
     * Set the threshold when achieved "something" would happen and wait for ui's confirm.
     * 
     * @param threshold
     * @return builder
     */
    public Builder setThreshold(long threshold) {
      this.threshold = threshold;
      return this;
    }


    /**
     * Set the size when achieved md5 check will start, 0 for default and no check.
     * 
     * @param checkSize
     * @return builder
     */
    public Builder setCheckSize(long checkSize) {
      this.checkSize = checkSize;
      return this;
    }

    /**
     * set visibility of this download task.
     * 
     * @param visible
     * @return builder
     */
    public Builder setVisible(boolean visible) {
      this.visible = visible;
      return this;
    }

    /**
     * Set the extra info if you need.
     * 
     * @param key
     * @param value
     * @return builder
     */
    public Builder putExtraString(String key, String value) {
      if (extraResourceMap == null) {
        extraResourceMap = new HashMap<String, String>();
      }
      extraResourceMap.put(key, value);
      return this;
    }

    public Builder putExtras(Map<String, String> extras) {
      if (extraResourceMap == null) {
        extraResourceMap = new HashMap<String, String>();
      }
      extraResourceMap.putAll(extras);
      return this;
    }


    /**
     * Sets speed limit, in bytes/s.
     * 
     * @param bytesPerSecond speed limit
     * @return builder
     */
    public Builder setSpeedLimit(long bytesPerSecond) {
      speedLimit = bytesPerSecond;
      return this;
    }

    /**
     * set verify download task valid function type and compared value.
     * 
     * @param type
     * @param value
     * @return builder
     */
    public Builder setVerifyInfo(VerifyType type, String value) {
      verifyType = type;
      verifyValue = value;
      return this;
    }

    /**
     * Build a DownloadRequest, null will be returned if build failed.
     * 
     * @return builder
     */
    public DownloadRequest build() {
      if (TextUtils.isEmpty(url)
          || contentType == null) {
        Log.e(
            TAG,
            "Lack of Paramters to build a download request " + " Make sure you have set" + " url: "
                + url + "and Content Type");
        return null;
      }
      return new DownloadRequest(this);
    }
  }

}
