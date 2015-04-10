package com.lemi.mario.download.model.segments;

import java.io.Serializable;

/**
 * This class is to store download blocks.
 * 
 * <p>
 * NOTE: each download thread will read and write this block info.
 * </p>
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class DownloadBlockInfo implements Serializable {

  /**
   * serial Version Id
   */
  private static final long serialVersionUID = -5352617512880199531L;
  private static final String WDJ_CDN_HOST = "dservice.wdjcdn.com";

  private int blockId;
  private long startPos;
  private long endPos;
  private long currentSize;
  private int usedUrlIndex;

  public DownloadBlockInfo(int blockId, long startPos,
      long endPos, long currentSize, int usedUrlIndex) {
    this.blockId = blockId;
    this.startPos = startPos;
    this.endPos = endPos;
    this.currentSize = currentSize;
    this.usedUrlIndex = usedUrlIndex;
  }

  private DownloadBlockInfo() {
    // used for gson
  }

  public void setEndPos(long endPos) {
    this.endPos = endPos;
  }

  public int getBlockId() {
    return blockId;
  }

  public long getStartPos() {
    return startPos;
  }

  public long getEndPos() {
    return endPos;
  }

  public long getCurrentSize() {
    return currentSize;
  }

  public int getUsedUrlIndex() {
    return usedUrlIndex;
  }

  public void setUsedUrlIndex(int usedUrlIndex) {
    this.usedUrlIndex = usedUrlIndex;
  }

  public void setCurrentSize(long currentSize) {
    this.currentSize = currentSize;
  }
}
