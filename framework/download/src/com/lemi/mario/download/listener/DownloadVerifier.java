package com.lemi.mario.download.listener;


import com.lemi.mario.download.rpc.InnerDownloadInfo;

public interface DownloadVerifier {

  /**
   * Check this file Valid.
   * 
   * @param info
   * @return whether this file is vaild
   */
  public boolean isDownloadFileValid(InnerDownloadInfo info);

}
