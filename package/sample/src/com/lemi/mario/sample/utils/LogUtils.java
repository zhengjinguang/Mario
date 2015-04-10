package com.lemi.mario.sample.utils;

import android.util.Log;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class LogUtils {

  private static boolean enableLog = true;

  public static void setLogEnabled(boolean enabled) {
    enableLog = enabled;
  }

  public static boolean isLogEnabled() {
    return enableLog;
  }

  public static String getTag(Class<?> clazz) {
    return "LEMI SAMPLE." + clazz.getSimpleName();
  }

  public static void i(String tag, String msg) {
    if (enableLog) {
      Log.i(tag, msg);
    }
  }

  public static void v(String tag, String msg) {
    if (enableLog) {
      Log.v(tag, msg);
    }
  }

  public static void d(String tag, String msg) {
    if (enableLog) {
      Log.d(tag, msg);
    }
  }

  public static void w(String tag, String msg) {
    if (enableLog) {
      Log.w(tag, msg);
    }
  }

  public static void e(String tag, String msg) {
    Log.e(tag, msg);
  }

}
