package com.lemi.controller.lemigameassistance.download;


/**
 * Download info interface.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface DownloadInfo {

  public enum Status {
    SUCCESS(0),
    FAILED(1),
    DELETED(2),
    CANCELED(3),
    PENDING(4),
    PAUSED(5),
    CREATED(6),
    DOWNLOADING(7);

    private final int priority;

    Status(int value) {
      this.priority = value;
    }

    public int getPriority() {
      return this.priority;
    }

  }

  public enum SubStatus {
    // user click pause button
    PAUSED_BY_APP,
    // can't download because media can't be write
    PAUSED_BY_NO_WRITE_PERMISSION,
    // can't download because media has no enough space
    PAUSED_BY_NO_SPACE,
    // can't download because media can't be used
    PAUSED_BY_MEDIA,
    // network unused
    PAUSED_BY_NETWORK,
    // other status
    OTHERS
  }

  public enum ContentType {
    APP, MUSIC, VIDEO, IMAGE, BOOK,
    COMIC, PATCH, MISC, DATA_PACKET, UNKNOWN, PLUGIN, ZIP, UPGRADE
  }

  DownloadInfo.SubStatus getSubStatus();

  long getId();

  String getIdentity();

  long getCurrentBytes();

  long getSpeed();

  long getTotalBytes();

  String getFilePath();

  String getIntermediateFilePath();

  String getTitle();

  String getMimeType();

  Status getStatus();

  Status getPreviousStatus();

  DownloadInfo.ContentType getContentType();

  String getIcon();

  String getMD5();

  String getDownloadUrl();

  boolean isVisible();

  long getSpeedLimit();

  String getStringExtra(String key);

  int getIntExtra(String key);

  long getLongExtra(String key);

  boolean getBooleanExtra(String key, boolean defaultValue);

  long getLastMod();

}
