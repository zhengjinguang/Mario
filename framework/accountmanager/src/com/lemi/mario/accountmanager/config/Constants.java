package com.lemi.mario.accountmanager.config;

import android.os.Environment;

import java.io.File;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class Constants {
  public static final String BASE_URL = "http://tv.lemiplay.com/index.php/api3";
  public static final boolean IS_IN_TEST = true;
  public static final int HTTP_API_VERSION = 3;
  public static final String SDCARD_PATH = Environment.getExternalStorageDirectory()
      .getAbsolutePath();

  public static final String APP_PATH = "lemi" + File.separator + "mario" + File.separator
      + "account";
  public static final String ROOT_PATH = SDCARD_PATH + File.separator + APP_PATH;
  public static final String ACCOUNT_TYPE = "com.lemi.mario.account";

  /**
   * Account name
   */
  public static final String ACCOUNT_NAME = "mario";

  /**
   * Auth token types
   */
  public static final String AUTHTOKEN_TYPE = "Full access";

}
