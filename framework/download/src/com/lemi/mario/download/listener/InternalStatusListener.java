package com.lemi.mario.download.listener;

/**
 * Internal Status Listener.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 *
 */
public interface InternalStatusListener {

  /**
   * callback to Status Change.
   * 
   * if a download thread is end , in finally function, it will
   * use this callback
   * 
   * @param id
   * @param status
   */
  void onStatusChange(long id, int status);

}
