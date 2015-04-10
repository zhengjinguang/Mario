package com.lemi.controller.lemigameassistance.download;

/**
 * Download Listener.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface DownloadListener {

  /**
   * Gets called when download progress changed, on UI thread.
   * 
   * @param info download info
   */
  void onProgressChanged(DownloadInfo info);

  /**
   * Gets called when download status changed, on UI thread.
   * 
   * @param info download info. If your task was created failed, this info will contain an identity
   *          and an error status.
   */
  void onStatusChanged(DownloadInfo info);


}
