package com.lemi.mario.download.rpc;

/**
 * This class is used to record download data need to update.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class DownloadData {
  private Long currentBytes;
  private Integer status;
  private Boolean allowInMobile;
  private String filePath;
  private String newUri;
  private Integer visibility;
  private long id;

  private DownloadData(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public Integer getVisibility() {
    return visibility;
  }

  public String getNewUri() {
    return newUri;
  }

  public Long getCurrentBytes() {
    return currentBytes;
  }

  public Integer getStatus() {
    return status;
  }

  public Boolean getAllowInMobile() {
    return allowInMobile;
  }

  public String getFilePath() {
    return filePath;
  }

  public static class DownloadDataBuilder {
    private String filePath;
    private Long currentBytes;
    private Integer status;
    private Boolean allowInMobile;
    private String newUri;
    private Integer visibility;
    private long id;

    DownloadDataBuilder(long id) {
      this.id = id;
    }

    public void setFilePath(String filePath) {
      this.filePath = filePath;
    }

    public void setCurrentBytes(long bytes) {
      this.currentBytes = bytes;
    }

    public void setStatus(int status) {
      this.status = status;
    }

    public void setAllowInMobile(boolean allow) {
      this.allowInMobile = allow;
    }

    public void setNewUri(String newUri) {
      this.newUri = newUri;
    }

    public void setVisibility(int visibility) {
      this.visibility = visibility;
    }

    public DownloadData build() {
      DownloadData data = new DownloadData(id);
      data.id = this.id;
      data.filePath = this.filePath;
      data.currentBytes = this.currentBytes;
      data.status = this.status;
      data.allowInMobile = this.allowInMobile;
      data.newUri = this.newUri;
      data.visibility = this.visibility;
      return data;
    }
  }
}
