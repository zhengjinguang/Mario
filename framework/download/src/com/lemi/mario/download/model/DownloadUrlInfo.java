package com.lemi.mario.download.model;

import org.apache.http.util.LangUtils;

import java.io.Serializable;
import java.util.List;

public class DownloadUrlInfo implements Serializable {

  /**
   * serial Version UID
   */
  private static final long serialVersionUID = -5047401112760994112L;

  private String url;
  private int type;
  private String host;
  private String refer;
  private String cookie;
  private String toast;
  private long weight;
  private List<ClipInfo> clipinfo;
  private long size;

  public List<ClipInfo> getClipinfo() {
    return clipinfo;
  }

  public String getUrl() {
    return url;
  }

  public int getType() {
    return type;
  }

  public String getHost() {
    return host;
  }

  public String getRefer() {
    return refer;
  }

  public String getCookie() {
    return cookie;
  }

  public long getWeight() {
    return weight;
  }

  public String getToast() {
    return toast;
  }

  public long getSize() {
    return size;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof DownloadUrlInfo)) {
      return false;
    }
    return LangUtils.equals(((DownloadUrlInfo) o).url, url);
  }
}
