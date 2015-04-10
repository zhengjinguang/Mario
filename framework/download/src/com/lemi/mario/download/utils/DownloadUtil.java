package com.lemi.mario.download.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import com.lemi.mario.download.model.segments.DownloadBlockInfo;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadUtil {

  // if memory size is below 50M, use three threads.
  private static final int MINIMUM_MEMORYSIZE_USE_LARGE_THREADS = 60;

  private static final int SMALL_THREADS_RUN_SAME_TIME = 3;

  private static final int LARGE_THREADS_RUN_SAME_TIME = 6;

  private static int maxRunningTheadsCount = -1;

  /**
   * Get current free memory size.
   *
   * @param context
   * @return currentMemorySize.
   */
  public static int getCurrentFreeMemorySize(Context context) {
    int currentMemorySize = 0;
    ActivityManager activityManager = (ActivityManager) (context)
        .getSystemService(Activity.ACTIVITY_SERVICE);
    if (activityManager != null) {
      currentMemorySize = activityManager.getMemoryClass();
    }
    return currentMemorySize;
  }

  /**
   * Get Max running thread count.
   * 
   * <p>
   * If the size is set once, it won't be change in app life circle.
   * </p>
   * 
   * @return
   */
  public static int getMaxRunningThreadCount(Context context) {
    // TODO assign to chunyu, if memory is low, use no more than 3 threads at one time.
    if (maxRunningTheadsCount == -1) {
      int maxMemorySize = 16;
      ActivityManager activityManager = (ActivityManager) (context)
          .getSystemService(Activity.ACTIVITY_SERVICE);
      if (activityManager != null) {
        maxMemorySize = activityManager.getMemoryClass();
      }

      maxRunningTheadsCount = maxMemorySize <= MINIMUM_MEMORYSIZE_USE_LARGE_THREADS
          ? SMALL_THREADS_RUN_SAME_TIME
          : LARGE_THREADS_RUN_SAME_TIME;
    }
    return maxRunningTheadsCount;
  }

  public static boolean isBlockDownloadFinished(DownloadBlockInfo block) {
    // achieve the data [1,400], currentSize is 400, startPos is 0, endPos is 399.
    return (block.getCurrentSize() + block.getStartPos() == (block.getEndPos() + 1));
  }

}
