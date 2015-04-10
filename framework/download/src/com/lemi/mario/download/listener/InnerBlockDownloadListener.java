package com.lemi.mario.download.listener;


import com.lemi.mario.download.rpc.BlockDownloadTask;

/**
 * The inner listener is used to watch the status or progress changes of download block, and
 * notify the upper layer.
 *
 * @author liuxu5@letv.com (Liu Xu)
 *
 */
public interface InnerBlockDownloadListener {

  /**
   * When status has been changed.
   *
   * @param segId
   * @param status
   */
  void onBlockStatusChange(int segId, BlockDownloadTask.BlockStatus status);

  /**
   * When download progress has been updated.
   *
   * @param segId
   * @param currentBytes
   * @param data received bytes
   * @param length byte length
   */
  void onBlockProgressChange(int segId, long currentBytes, byte[] data, int length);

  /**
   * When file is confirmed.
   *
   * @param segId
   * @param filePath the whole file path and file name
   * @return intermediate file
   */
  String onFilePathDeterminated(int segId, String filePath);

  /**
   * When download begins, download size is not achieved.
   * If size is confirmed, upper layer will receive this message.
   *
   * @param segId
   * @param totalSize
   */
  void onTotalSizeConfirmed(int segId, long totalSize);

  /**
   * downloaded time has been changed.
   *
   * @param segId
   * @param deltaDuration
   */
  void onDurationIncreased(int segId, long deltaDuration);

  /**
   * response details contains etag info.
   * notify etag info has changed.
   *
   * @param blockId
   * @param etag
   */
  void onETagChanged(int blockId, String etag);

}
