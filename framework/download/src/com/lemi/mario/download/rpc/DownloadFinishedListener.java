package com.lemi.mario.download.rpc;

public interface DownloadFinishedListener {

  /**
   * notify download success.
   * 
   * @param id
   */
  public void onDownloadFinished(long id);

}
