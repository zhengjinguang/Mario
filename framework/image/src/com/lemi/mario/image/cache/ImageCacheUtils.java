package com.lemi.mario.image.cache;

import android.graphics.Bitmap;

import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;

import java.util.concurrent.Executor;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public final class ImageCacheUtils {
  private static final Executor EXECUTOR =
      new CachedThreadPoolExecutorWithCapacity(1, 60 * 1000L);

  private ImageCacheUtils() {}

  /**
   * Saves image to cache in asynchronous mode.
   */
  public static void cacheImage(final ImageCache cache, final String key, final Bitmap bitmap) {
    EXECUTOR.execute(new Runnable() {
      @Override
      public void run() {
        cache.put(key, bitmap);
      }
    });
  }
}
