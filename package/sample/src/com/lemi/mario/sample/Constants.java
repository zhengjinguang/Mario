package com.lemi.mario.sample;

import android.os.Environment;

import java.io.File;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class Constants {

  public static final String SDCARD_PATH = Environment.getExternalStorageDirectory()
      .getAbsolutePath();

  public static final String APP_PATH = "lemi" + File.separator + "mario" + File.separator
      + "sample";
  public static final String ROOT_PATH = SDCARD_PATH + File.separator + APP_PATH;

  // for file size
  public static final long KB = 1024L;
  public static final long GB = KB * KB * KB;
  public static final long MB = KB * KB;

  // for time constants
  public static final long ONE_SECOND = 1000;
  public static final long ONE_MINUTE = ONE_SECOND * 60;
  public static final long ONE_HOUR = ONE_MINUTE * 60;
  public static final long ONE_DAY = ONE_HOUR * 24;

}
