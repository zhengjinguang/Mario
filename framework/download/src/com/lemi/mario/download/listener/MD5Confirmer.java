package com.lemi.mario.download.listener;


import com.lemi.mario.download.rpc.InnerDownloadInfo;

/**
 * Download Task is need to check MD5.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface MD5Confirmer {

  /**
   * If return true, download task will calc md5.
   *
   * <p>
   * NOTE: download library will try its best to calc md5, but if application break off when calcing
   * md5, download library won't calc md5 again. You can do this work yourself.
   * </p>
   *
   * @param info
   * @return if need to calc checksum.
   */
  public boolean needToCalculateMd5(InnerDownloadInfo info);
}
