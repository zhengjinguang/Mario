package com.lemi.mario.image.cache;

import android.graphics.Bitmap;

/**
 * Interface for image cache.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface ImageCache {

  /**
   * Gets image from cache, according to image key.
   * 
   * @param key image key
   * @return {@link android.graphics.Bitmap}, can be null if there is no such image or some error
   *         happens
   */
  Bitmap get(String key);

  /**
   * Puts image to cache.
   * 
   * @param key image key
   * @param bitmap image bitmap
   */
  void put(String key, Bitmap bitmap);

  /**
   * Checks whether image exists.
   * 
   * @param key image key
   * @return exist or not
   */
  boolean exists(String key);

  /**
   * Removes all images from cache.
   */
  void clear();

  /**
   * Returns the total bytes used in cache.
   * 
   * @return size
   */
  long size();

}
