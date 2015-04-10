package com.lemi.mario.image.rpc;

/**
 * Interface to update progress of a procedure.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface ProgressCallback {

  /**
   * Sets progress it changes. Max progress is 100.
   * 
   * @param progress
   */
  void onProgressChanged(int progress);
}
