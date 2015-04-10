package com.lemi.mario.download.listener;


import com.lemi.mario.download.rpc.InnerDownloadInfo;

/**
 * internal download listener for download manager
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public interface InnerDownloadListener {

  /**
   * the status will always be running, and it happens when progress changed
   */
  public void onProgressChanged(InnerDownloadInfo downloadInfo);

  /**
   * when the status is changed
   */
  public void onStatusChanged(InnerDownloadInfo downloadInfo);

  /**
   * need to check size
   */
  public void onConfigSizeAchieved(long id, long configSize);

  /**
   * when mimetype is achived, if mimeType is wrong, download manager should cancel this download
   */
  public void onMimeTypeAchieved(long id, String mimeType);

}
