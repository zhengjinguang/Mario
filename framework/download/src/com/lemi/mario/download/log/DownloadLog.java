package com.lemi.mario.download.log;

import android.util.Log;

/**
 * Download Log used to trace download details.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadLog {

  private DownloadLog() {}

  public static final String TAG = "download";

  /**
   * logcat v.
   *
   * @param log message
   */
  public static void v(String log) {
    Log.v(TAG, log);
  }

  /**
   * logcat d.
   *
   * @param log message
   */
  public static void d(String log) {
    Log.d(TAG, log);
  }

  /**
   * logcat e.
   *
   * @param log message
   */
  public static void e(String log) {
    Log.e(TAG, log);
  }

  /**
   * logcat e.
   *
   * @param throwable throwable.
   * @param log message
   */
  public static void e(Throwable throwable, String log) {
    Log.d(TAG, log, throwable);
  }

  /**
   * logcat w.
   *
   * @param log message
   */
  public static void w(String log) {
    Log.w(TAG, log);
  }

  /**
   * logcat i.
   *
   * @param log message
   */
  public static void i(String log) {
    Log.i(TAG, log);
  }

  /**
   * logcat level.
   */
  public enum LogLevel {
    I, V, D, W, E
  }

}
