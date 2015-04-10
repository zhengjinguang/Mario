package com.lemi.mario.download.utils;

import android.os.Build;

import com.lemi.mario.base.utils.SystemUtil;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class HttpUtils {

  /**
   * Get default http client,
   * it is a standard user-agent of chrome, and based on app's version code.
   * E.g:
   * Mozilla/5.0 (Linux; Android <Version>; <Model>; Lemi <Version>)
   * AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>
   */
  public static String getDefaultHttpClient() {
    return "Mozilla/5.0 (Linux;"
        + " Android " + Build.VERSION.RELEASE + "; "
        + Build.MODEL + "; "
        + "Lemi " + SystemUtil.getFullVersion() + ") "
        + "AppleWebKit/535.19 (KHTML, like Gecko) "
        + "Chrome/18.0.1025.133 Mobile Safari/535.19";
  }
}
