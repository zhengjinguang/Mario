package com.lemi.mario.externalmanager.model;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ExternalStorageInfo {

  private String path;
  private long totalSize = 0;

  public ExternalStorageInfo() {}

  public ExternalStorageInfo(String path, long availableSize) {
    this.path = path;
    this.totalSize = availableSize;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public long getTotalSize() {
    return totalSize;
  }

  public void setTotalSize(long totalSize) {
    this.totalSize = totalSize;
  }
}
