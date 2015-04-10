package com.lemi.mario.download.rpc;

import com.lemi.mario.download.model.SegmentInfo;
import com.lemi.mario.download.rpc.DownloadConstants.ResourceType;

import java.util.List;

/**
 * This class handles all the resource used to download.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class InnerBlockDownloadRequest {

  // Bytes which download thread has downloaded.
  public final long currentBytes;

  // url to download resource.
  public final String downloadUrl;

  // Full file path, including folder and file name.
  // If file is null, need to decide where to store file when downloading.
  public final String filePath;

  // Folder path
  public final String folderPath;

  public final String title;

  // download block id
  public final int blockId;

  // where begin to download files
  public final long startPos;

  // where download should be end.
  public long endPos;

  // download user agent.
  public final String userAgent;

  // download mime type
  public final String mimeType;

  // the time that download task has cost.
  public final long duration;

  // download task id.
  public final long id;

  // whether can download file from 2/3G
  public final boolean allowInMobile;

  // what the type of download file is.
  public final ResourceType type;

  // used in resume case.
  public final String eTag;

  // failed times
  public final int numFailed;

  // has been using multi threads.
  public boolean downloadWithMultiSegments;

  // crc checksum stored in here.
  public List<SegmentInfo> crcInfos;

  public final long segSize;

  public final long speedLimit;

  /**
   * Build downloadNecessity with default arguments.
   * 
   * @param info
   */
  public InnerBlockDownloadRequest(InnerDownloadInfo info) {
    id = info.mId;
    currentBytes = info.mCurrentBytes;
    downloadUrl = info.mUri;
    filePath = info.getIntermediateFilePath();
    folderPath = info.mFolderPath;
    title = info.mTitle;
    startPos = 0L;
    if (info.mTotalBytes > 0) {
      endPos = info.mTotalBytes - 1;
      assert (startPos + currentBytes - 1 <= endPos);
    }
    userAgent = info.mUserAgent;
    mimeType = info.mMimeType;
    duration = info.mDuration;
    allowInMobile = info.mAllowInMobile;
    type = info.mType;
    eTag = info.mETag;
    numFailed = info.mNumFailed;
    segSize = info.mTotalBytes;
    // default to 0
    blockId = 0;
    speedLimit = info.speedLimit;
  }


  /**
   * Build downloadNecessity with start and end argument.
   * 
   * @param info innerdownloadInfo.
   * @param downloadUrl download url.
   * @param blockIndex download block index.
   * @param start where to start writing file.
   * @param end where to end writing file.
   * @param current how much bytes has been downloaded.
   * @param crcInfos crc verify infos.
   * @param segSize seg size.
   * @param speedLimit speed limit to download.
   * @param blockNum num of download blocks.
   */
  public InnerBlockDownloadRequest(InnerDownloadInfo info, String downloadUrl, int blockIndex,
      long start, long end, long current, List<SegmentInfo> crcInfos, long segSize,
      long speedLimit, int blockNum) {
    id = info.mId;
    currentBytes = current;
    this.downloadUrl = downloadUrl;
    this.segSize = segSize;
    filePath = info.getIntermediateFilePath();
    folderPath = info.mFolderPath;
    title = info.mTitle;
    startPos = start;
    endPos = end;
    userAgent = info.mUserAgent;
    mimeType = info.mMimeType;
    duration = info.mDuration;
    allowInMobile = info.mAllowInMobile;
    type = info.mType;
    eTag = info.mETag;
    numFailed = info.mNumFailed;
    blockId = blockIndex;
    // If only has one block, it means download serially.
    downloadWithMultiSegments = (blockNum > 1);
    this.crcInfos = crcInfos;
    this.speedLimit = speedLimit;
    if (startPos >= 0 && currentBytes >= 0 && endPos > 0) {
      assert (startPos + currentBytes - 1 <= endPos);
    }
  }
}
