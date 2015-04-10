package com.lemi.controller.lemigameassistance.config;

import android.os.Environment;

import java.io.File;

public class Constants {
  // mario path
  public static final String SDCARD_PATH = Environment.getExternalStorageDirectory()
      .getAbsolutePath();

  public static final String APP_PATH = "lemi" + File.separator + "mario" + File.separator
      + "gamemaster";
  public static final String ROOT_PATH = SDCARD_PATH + File.separator + APP_PATH;

  public static final String DOWNLOAD_FOLDER_PATH = "download";

  public static final String DOWNLOAD_ROOT_PATH = ROOT_PATH + File.separator + DOWNLOAD_FOLDER_PATH;

  // for file size
  public static final long KB = 1024L;
  public static final long GB = KB * KB * KB;
  public static final long MB = KB * KB;

  // for time constants
  public static final long ONE_SECOND = 1000;
  public static final long ONE_MINUTE = ONE_SECOND * 60;
  public static final long ONE_HOUR = ONE_MINUTE * 60;
  public static final long ONE_DAY = ONE_HOUR * 24;


  // rpc request
  public static final String BASE_HOST = "http://tv.lemiplay.com/index.php";
  public static final String API_V3 = BASE_HOST + "/api3";

  public static final boolean IS_IN_TEST = false;
  public static final int HTTP_API_VERSION = 2;
  public static final int LOTTERY_API_VERSION = 3;
  // suffix
  public static final String APK_SUFFIX = ".apk";
  public static final String ZIP_SUFFIX = ".zip";

  // my game
  public static final String MY_GAME = "MYGAME";
  public static final String HOT_GAME = "HOT";
  public static final String NEW_GAME = "NEW";

  // lotteryId
  public static final String LOTTERY_ID = "DUMPLING";
  public static final String USERID_PREFIX = "LM1586";
  public static final String USERID_OHTER_PREFIX = "LM****";
}
