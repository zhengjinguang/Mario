package com.lemi.mario.appmanager.filter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhou on 14-12-8.
 */
public class AppInfoFilter {

  public enum Filter {
    ICON,
    APP_NAME,
    PACKAGE_NAME,
    VERSION_NAME,
    SOURCE_DIR,
    DATA_DIR,
    VERSION_CODE,
    TARGET_SDK_VERSION,
    PERMISSIONS,
    SIGNATURES,
    APK_SIZE,
    IS_SYSTEM_APP,
    IS_INSTALLED
  }

  public static Set<Filter> ALL;
  public static Set<Filter> RECOMMEND;
  public static Set<Filter> SIMPLE;
  public static Set<Filter> ONLY_PACKAGENAME;

  static {
    ALL = new HashSet<Filter>();
    ALL.add(Filter.ICON);
    ALL.add(Filter.APP_NAME);
    ALL.add(Filter.PACKAGE_NAME);
    ALL.add(Filter.VERSION_NAME);
    ALL.add(Filter.SOURCE_DIR);
    ALL.add(Filter.DATA_DIR);
    ALL.add(Filter.VERSION_CODE);
    ALL.add(Filter.TARGET_SDK_VERSION);
    ALL.add(Filter.PERMISSIONS);
    ALL.add(Filter.SIGNATURES);
    ALL.add(Filter.APK_SIZE);
    ALL.add(Filter.IS_SYSTEM_APP);
    ALL.add(Filter.IS_INSTALLED);

    RECOMMEND = new HashSet<Filter>();
    RECOMMEND.add(Filter.APP_NAME);
    RECOMMEND.add(Filter.PACKAGE_NAME);
    RECOMMEND.add(Filter.VERSION_NAME);
    RECOMMEND.add(Filter.SOURCE_DIR);
    RECOMMEND.add(Filter.DATA_DIR);
    RECOMMEND.add(Filter.VERSION_CODE);
    RECOMMEND.add(Filter.TARGET_SDK_VERSION);
    RECOMMEND.add(Filter.PERMISSIONS);
    RECOMMEND.add(Filter.SIGNATURES);
    RECOMMEND.add(Filter.APK_SIZE);
    RECOMMEND.add(Filter.IS_SYSTEM_APP);
    RECOMMEND.add(Filter.IS_INSTALLED);

    SIMPLE = new HashSet<Filter>();
    SIMPLE.add(Filter.APP_NAME);
    SIMPLE.add(Filter.PACKAGE_NAME);
    SIMPLE.add(Filter.VERSION_CODE);
    SIMPLE.add(Filter.VERSION_NAME);
    SIMPLE.add(Filter.APK_SIZE);

    ONLY_PACKAGENAME = new HashSet<Filter>();
    ONLY_PACKAGENAME.add(Filter.PACKAGE_NAME);
  }
}
