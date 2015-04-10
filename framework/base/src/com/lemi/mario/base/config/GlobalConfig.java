package com.lemi.mario.base.config;

import android.content.Context;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GlobalConfig {

  private static Context appContext;
  private static String rootDir = "lemi";
  private static boolean debug = false;

  public static void setAppContext(Context context) {
    appContext = context;
  }

  public static Context getAppContext() {
    return appContext;
  }

  public static void setAppRootDir(String dir) {
    rootDir = dir;
  }

  public static String getAppRootDir() {
    return rootDir;
  }

  /**
   * This should only be called in main android project, like in Application,
   * because in Gradle build system the BuildConfig.DEBUG in library project is always false.
   * 
   * @param debug true if in debug mode, false otherwise.
   */
  public static void setDebug(boolean debug) {
    GlobalConfig.debug = debug;
  }

  /**
   * Because in Gradle build system the BuildConfig.DEBUG in library project is always false,
   * and this will not be fixed in short-term, so we need to use this instead of
   * BuildConfig.DEBUG in library project.
   * 
   * @return true if in debug mode, false otherwise.
   */
  public static boolean isDebug() {
    return debug;
  }

}
