package com.lemi.mario.download.model.segments;


import com.google.gson.Gson;
import com.lemi.mario.download.model.DownloadUrlInfo;
import com.lemi.mario.download.model.SegmentInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to store download config Info.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class DownloadConfigInfo implements Serializable {

  /**
   * serial Version UID
   */
  private static final long serialVersionUID = 3342084427733310475L;

  // the crc checksum of the whole resouce file.
  private String totalCrc;

  // total size of the resource
  private long totalSize;

  // the signature of resource.
  private String resSign;

  // the segment size given by dservice
  private long segSize;

  // store unused urls if able to retry.
  private List<DownloadUrlInfo> backupUrls = new ArrayList<DownloadUrlInfo>();

  // store download block data.
  private List<DownloadBlockInfo> blocks = new ArrayList<DownloadBlockInfo>();

  private List<SegmentInfo> segInfos = new ArrayList<SegmentInfo>();

  public void setTotalCrc(String totalCrc) {
    this.totalCrc = totalCrc;
  }

  public long getTotalSize() {
    return totalSize;
  }

  public void setTotalSize(long totalSize) {
    this.totalSize = totalSize;
  }

  public void setResSign(String resSign) {
    this.resSign = resSign;
  }

  public long getSegSize() {
    return segSize;
  }

  public void setSegSize(long segSize) {
    this.segSize = segSize;
  }

  public List<DownloadUrlInfo> getBackupUrls() {
    return backupUrls;
  }

  public void setBackupUrls(List<DownloadUrlInfo> backupUrls) {
    this.backupUrls = backupUrls;
  }

  public List<DownloadBlockInfo> getBlocks() {
    return blocks;
  }

  public List<SegmentInfo> getSegInfos() {
    return segInfos;
  }

  public void setSegInfos(List<SegmentInfo> segInfos) {
    this.segInfos = segInfos;
  }

  /* end of getter and setter */

  /**
   * Update block progress
   * 
   * @param blockId
   * @param currentBytes
   */
  public void updateBlockInfo(int blockId, long currentBytes) {
    synchronized (blocks) {
      DownloadBlockInfo block = blocks.get(blockId);
      if (block == null) {
        throw new IllegalStateException("try to update not existed block info");
      }
      block.setCurrentSize(currentBytes);
    }
  }

  /**
   * Convert DownloadConfigInfo object to json string.
   * 
   * @return converted json string.
   */
  public String toJson() {
    Gson gson = new Gson();
    String json = "";
    try {
      // don't know why toJson throws NoSuchMethodException!
      // in this case we return an empty string.
      json = gson.toJson(this);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return json;
  }

  /**
   * Build download config info from JSONString
   * 
   * @param json
   * @return downloadconfig info
   */
  public static DownloadConfigInfo fromJson(String json) {
    Gson gson = new Gson();
    DownloadConfigInfo info = gson.fromJson(json, DownloadConfigInfo.class);
    return info;
  }

}
