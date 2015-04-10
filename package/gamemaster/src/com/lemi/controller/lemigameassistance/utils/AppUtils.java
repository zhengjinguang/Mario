package com.lemi.controller.lemigameassistance.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.lemi.mario.base.config.GlobalConfig;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class AppUtils {

  private static final int LOAD_PACKAGE_INFO_RETRY_TIMES = 3;

  public static boolean isInstalled(Context context, String packageName) {
    return getPackageInfo(context, packageName, 0) != null;
  }

  public static void startApp(Context context, String packageName) {
    Intent intent = context.getPackageManager()
        .getLaunchIntentForPackage(packageName);
    context.startActivity(intent);
  }

  public static PackageInfo getPackageInfo(Context context, String packageName, int flag) {
    for (int i = 0; i < LOAD_PACKAGE_INFO_RETRY_TIMES; ++i) {
      PackageManager packageManager = context.getPackageManager();
      try {
        return packageManager.getPackageInfo(packageName, flag);
      } catch (PackageManager.NameNotFoundException e) {
        break;
      } catch (RuntimeException e) {
        // In some ROM, there will be a PackageManager has died exception. So we catch it here.
        e.printStackTrace();
      }
    }
    return null;
  }


  public static boolean isSystemApp(String package_name) {
    ApplicationInfo info = null;
    try {
      info = GlobalConfig.getAppContext().getPackageManager().getApplicationInfo(
          package_name, PackageManager.GET_UNINSTALLED_PACKAGES);
    } catch (PackageManager.NameNotFoundException e) {
      info = null;
    }
    if (info == null) {
      return false;
    }
    if ((info.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
      return true;
    return false;
  }
}
