package com.lemi.mario.download.rpc;

import android.text.TextUtils;

import com.lemi.mario.base.utils.FileNameUtil;
import com.lemi.mario.download.rpc.DownloadConstants.ResourceType;

/**
 * Download Request.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class InnerDownloadRequest {

  // download url
  public final String url;
  // the name of download file to display in UI
  public final String title;

  // it's used to mark a download extras. e.g. packageName
  public final String identity;
  // download type
  public final ResourceType type;
  // the extra arguments you need to store
  public final String extras;

  // able to download data in 2/3g
  public final boolean allowedInMobile;
  /**
   * NOTE: if the totalBytes is set, and threshold will have no meaning
   */
  public final long totalBytes;
  // the descprition about this download.
  public final String description;
  // the source where download begin e.g. detail page.
  public final String source;
  // the icon url to display the download.
  public final String iconUrl;


  /**
   * Set assigned folder Path.
   * 
   * <p>
   * NOTE: file path should not contains blank. e.g. 1) /storage/sdcard0/lemi/video/star_war will
   * make a folder named star_war. 2) /storage/sdcard0/lemi/video/star_war/ will also make a folder
   * named star_war
   * </p>
   */
  public final String folderPath;

  /**
   * Set assigned file Name.
   */
  public final String fileName;

  /**
   * the threshold for download size, if download size is more than this , it
   * will use callback to notify manager, and pause the thread to wait user
   * confirm.
   */
  public final long threshold;
  /**
   * if the confirmedSize more than zero, when download size is achieved, the
   * download will be paused to wait md5 checking
   */
  public final long checkSize;
  public final String userAgent;
  public final boolean visible;
  public final long speedLimit;

  public final DownloadConstants.VerifyType verifyType;
  public final String verifyValue;

  public static enum VerifyType {
    MD5, PF5
  }

  private InnerDownloadRequest(Builder builder) {
    this.allowedInMobile = builder.allowedInMobile;
    this.extras = builder.extras;
    this.title = builder.title;
    this.identity = builder.identity;
    this.totalBytes = builder.totalBytes;
    this.type = builder.type;
    this.url = builder.uri;
    this.userAgent = builder.userAgent;
    this.description = builder.description;
    this.threshold = builder.threshold;
    this.checkSize = builder.needCheckSize;
    this.source = builder.source;
    this.iconUrl = builder.iconUrl;
    this.visible = builder.visible;
    this.fileName = builder.fileName;
    this.folderPath = builder.folderPath;
    this.speedLimit = builder.speedLimit;
    if (builder.verifyType != null) {
      switch (builder.verifyType) {
        case MD5:
          this.verifyType = DownloadConstants.VerifyType.MD5;
          break;
        case PF5:
          this.verifyType = DownloadConstants.VerifyType.PF5;
          break;
        default:
          this.verifyType = null;
      }
    } else {
      this.verifyType = null;
    }
    this.verifyValue = builder.verifyValue;
  }

  public static class Builder {
    private final String uri;
    private String title;
    private String identity;
    private ResourceType type = ResourceType.UNKNOWN;
    private String extras;
    private boolean allowedInMobile;
    private String userAgent = "downloadmanager";
    private long totalBytes = DownloadConstants.DEFAULT_DOWNLOAD_TOTALBYTES;
    private String description;
    private long needCheckSize;
    private long threshold;
    private String source;
    private String iconUrl;
    private boolean visible = true;
    private String fileName;
    private String folderPath;
    private long speedLimit = -1;
    private VerifyType verifyType;
    private String verifyValue;

    /**
     * Download Url and Resource Type is necessary for a download task.
     * 
     * @param uri
     * @param type
     */
    public Builder(String uri, ResourceType type) {
      assert (uri != null);
      this.uri = uri;
      this.type = type;
    }

    /**
     * Display title for a download file.
     * Note: if it's not set, the UI will have no title to show.
     * 
     * @param title
     * @return DownloadRequest Builder
     */
    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    /**
     * Set Identity to a download task.
     * 
     * <p>
     * NOTE: the unique identity to download file. e.g. Application -> packageName <br/>
     * <p>
     * 
     * <p>
     * if don't set this argument, identity property will be set to URL automatically.<br/>
     * </p>
     * 
     * @param identity
     * @return DownloadRequest Builder
     */
    public Builder setIdentity(String identity) {
      this.identity = identity;
      return this;
    }

    /**
     * Store extra data in database.
     *
     * @param resource
     * @return DownloadRequest Builder
     */
    public Builder setSerializeExtras(String resource) {
      this.extras = resource;
      return this;
    }

    /**
     * Set the attribute if it's allowed to download in 2/3G.
     * 
     * <p>
     * Default: True
     * </P>
     *
     * @param allowedInMobile
     * @return DownloadRequest Builder
     */
    public Builder setAllowedInMobile(boolean allowedInMobile) {
      this.allowedInMobile = allowedInMobile;
      return this;
    }

    /**
     * Set download Agent.
     * 
     * @param agent
     * @return DownloadRequest Builder
     */
    public Builder setUserAgent(String agent) {
      this.userAgent = agent;
      return this;
    }

    /**
     * Set Download Total Bytes.
     * 
     * @param totalBytes
     * @return DownloadRequest Builder
     */
    public Builder setTotalBytes(long totalBytes) {
      this.totalBytes = totalBytes;
      return this;
    }

    /**
     * Currently it's not used.
     * If you want to have description to set about you download tasks,
     * call this function.
     * 
     * @param description
     * @return DownloadRequest Builder
     */
    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    /**
     * When download size is above this value, download is not allowed.
     * 
     * <p>
     * NOTE:Currently it's not used.
     * </p>
     * 
     * @param threshold
     * @return DownloadRequest Builder
     */
    public Builder setThreshold(long threshold) {
      this.threshold = threshold;
      return this;
    }

    /**
     * If you have the willing to pause download when downloaded size is above this value to do some
     * work.
     * set this value.
     * 
     * @param size
     * @return DownloadRequest Builder
     */
    public Builder setCheckSize(long size) {
      this.needCheckSize = size;
      return this;
    }

    /**
     * Set the source where download begin.
     * 
     * @param source
     * @return DownloadRequest Builder
     */
    public Builder setSource(String source) {
      this.source = source;
      return this;
    }

    /**
     * set extras type (replace old one).
     * 
     * @param resourceType
     * @return DownloadRequest Builder
     */
    public Builder setContentType(ResourceType resourceType) {
      this.type = resourceType;
      return this;
    }

    /**
     * set icon url of current downloading item.
     * 
     * @param iconUrl
     * @return DownloadRequest Builder
     */
    public Builder setIconUrl(String iconUrl) {
      this.iconUrl = iconUrl;
      return this;
    }

    /**
     * set visibility of this download task.
     *
     * @param visible
     * @return
     */
    public Builder setVisible(boolean visible) {
      this.visible = visible;
      return this;
    }

    /**
     * Set destination folder path.
     * 
     * @param folderPath folder path
     * @return builder
     */
    public Builder setFolderPath(String folderPath) {
      if (!TextUtils.isEmpty(folderPath) && !folderPath.endsWith("/")) {
        folderPath += "/";
      }
      this.folderPath = folderPath;
      return this;
    }

    /**
     * Set file name, excluding file path.
     *
     * @param fileName
     * @throws IllegalArgumentException
     * @return builder.
     */
    public Builder setFileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    /**
     * Sets download speed limit, in byte/s.
     *
     * @param speedLimit limit of speed
     * @return builder
     */
    public Builder setSpeedLimit(long speedLimit) {
      this.speedLimit = speedLimit;
      return this;
    }

    /**
     * set verify type
     *
     * @param type
     * @param verifyValue
     * @return builder.
     */
    public Builder setVerifyInfo(VerifyType type, String verifyValue) {
      this.verifyType = type;
      this.verifyValue = verifyValue;
      return this;
    }

    /**
     * Builds download request.
     * 
     * @return DownloadRequest
     */
    public InnerDownloadRequest build() {
      if (TextUtils.isEmpty(identity)) {
        identity = uri;
      }
      if (TextUtils.isEmpty(title)) {
        title = FileNameUtil.getBaseName(uri);
      }
      return new InnerDownloadRequest(this);
    }

  }
}
