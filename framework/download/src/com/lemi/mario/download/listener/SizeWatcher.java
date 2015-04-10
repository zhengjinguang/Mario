package com.lemi.mario.download.listener;


import com.lemi.mario.download.rpc.InnerDownloadInfo;

/**
 * Watch download size.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface SizeWatcher {

  /**
   * Notify upper layer that size has been confirmed.
   *
   * @param downloadInfo
   */
  public void onSizeConfirmed(InnerDownloadInfo downloadInfo);

}
