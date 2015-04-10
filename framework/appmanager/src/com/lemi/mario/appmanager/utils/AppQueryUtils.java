package com.lemi.mario.appmanager.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import com.lemi.mario.appmanager.filter.AppInfoFilter;
import com.lemi.mario.appmanager.model.AppInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhou on 14-12-1.
 */
public class AppQueryUtils {

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static AppInfo getAppByApkFile(String apkPath, Context context,
      Set<AppInfoFilter.Filter> filters) {
    if (TextUtils.isEmpty(apkPath) || context == null) {
      return null;
    }

    PackageManager pm = context.getPackageManager();
    PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, getPackageInfoFlag(filters));
    ApplicationInfo applicationInfo = packageInfo.applicationInfo;

    return createAppInfo(pm, packageInfo, applicationInfo, apkPath, context, filters);
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static AppInfo getAppByPackageName(String packageName, Context context,
      Set<AppInfoFilter.Filter> filters) {
    if (TextUtils.isEmpty(packageName) || context == null) {
      return null;
    }

    PackageManager pm = context.getPackageManager();
    try {
      ApplicationInfo applicationInfo =
          pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
      PackageInfo packageInfo =
          pm.getPackageInfo(applicationInfo.packageName, getPackageInfoFlag(filters));
      return createAppInfo(pm, packageInfo, applicationInfo, applicationInfo.sourceDir, context,
          filters);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    return null;
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static Map<String, AppInfo> getAllInstalledApp(Context context,
      Set<AppInfoFilter.Filter> filters) {
    if (context == null) {
      return null;
    }

    PackageManager pm = context.getPackageManager();
    List<ApplicationInfo> applicationInfos =
        pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

    Map<String, AppInfo> appInfos = new HashMap<String, AppInfo>();
    for (int i = 0; i < applicationInfos.size(); i++) {
      PackageInfo packageInfo = null;
      try {
        packageInfo =
            pm.getPackageInfo(applicationInfos.get(i).packageName, getPackageInfoFlag(filters));
        AppInfo appInfo =
            createAppInfo(pm, packageInfo, applicationInfos.get(i),
                applicationInfos.get(i).sourceDir,
                context, filters);
        if (appInfo == null) {
          continue;
        }
        appInfos.put(appInfo.getPackageName(), appInfo);
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
    }
    return appInfos;
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static List<String> getAllInstalledAppPackageName(Context context) {
    List<String> packageNameList = new ArrayList<String>();

    if (context == null) {
      return packageNameList;
    }

    PackageManager pm = context.getPackageManager();
    List<ApplicationInfo> applicationInfos =
        pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

    for (int i = 0; i < applicationInfos.size(); i++) {
      if (applicationInfos.get(i) == null || TextUtils.isEmpty(applicationInfos.get(i).packageName)) {
        continue;
      }
      packageNameList.add(applicationInfos.get(i).packageName);
    }

    return packageNameList;
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static Map<String, AppInfo> getAllRunningApp(Context context,
      Set<AppInfoFilter.Filter> filters) {
    if (context == null) {
      return null;
    }

    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

    if (runningAppProcesses == null || runningAppProcesses.size() <= 0) {
      return null;
    }

    Map<String, AppInfo> appInfos = new HashMap<String, AppInfo>();

    for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
      for (String runningPackageName : runningAppProcessInfo.pkgList) {
        if (appInfos.containsKey(runningPackageName)) {
          continue;
        }
        appInfos.put(runningPackageName, getAppByPackageName(runningPackageName, context, filters));
      }
    }

    return appInfos;
  }

  public static AppInfo getForegroundAppInfo(Context context, Set<AppInfoFilter.Filter> filters) {
    if (context == null) {
      return null;
    }

    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

    if (am == null) {
      return null;
    }

    return getAppByPackageName(am.getRunningTasks(1).get(0).topActivity.getPackageName(), context,
        filters);
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static String getVersionName(String packageName, Context context) {
    if (TextUtils.isEmpty(packageName) || context == null) {
      return null;
    }

    PackageManager pm = context.getPackageManager();
    try {
      PackageInfo packageInfo =
          pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
      return packageInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static String getPackageNameByApkFile(String apkPath, Context context) {
    if (TextUtils.isEmpty(apkPath) || context == null) {
      return null;
    }

    PackageManager pm = context.getPackageManager();
    PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, 0);
    if (packageInfo == null) {
      return null;
    }
    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
    if (applicationInfo == null || applicationInfo.packageName == null) {
      return null;
    }

    return applicationInfo.packageName;
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static Signature[] getSignatures(String packageName, Context context) {
    if (TextUtils.isEmpty(packageName) || context == null) {
      return null;
    }

    PackageManager pm = context.getPackageManager();
    try {
      PackageInfo packageInfo =
          pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES
              | PackageManager.GET_SIGNATURES);
      return packageInfo.signatures;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static String[] getPermissions(String packageName, Context context) {
    if (TextUtils.isEmpty(packageName) || context == null) {
      return null;
    }

    PackageManager pm = context.getPackageManager();
    try {
      PackageInfo packageInfo =
          pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES
              | PackageManager.GET_PERMISSIONS);
      return packageInfo.requestedPermissions;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static int getVersionCode(String packageName, Context context) {
    if (TextUtils.isEmpty(packageName) || context == null) {
      return 0;
    }

    PackageManager pm = context.getPackageManager();
    try {
      PackageInfo packageInfo =
          pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
      return packageInfo.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      return 0;
    }
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static boolean isAppInstalled(String packageName, Context context) {
    if (TextUtils.isEmpty(packageName) || context == null) {
      return false;
    }

    ApplicationInfo info = null;
    try {
      info = context.getPackageManager().getApplicationInfo(packageName,
          PackageManager.GET_UNINSTALLED_PACKAGES);
    } catch (PackageManager.NameNotFoundException e) {
      info = null;
    }
    if (info != null) {
      return true;
    }
    return false;
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static boolean isAppRunning(String packageName, Context context) {
    if (TextUtils.isEmpty(packageName) || context == null) {
      return false;
    }

    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

    for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
      for (String runningPackageName : runningAppProcessInfo.pkgList) {
        if (runningPackageName.equals(packageName)) {
          return true;
        }
      }
    }

    return false;
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static boolean isSystemApp(String packageName, Context context) {
    if (TextUtils.isEmpty(packageName) || context == null) {
      return false;
    }

    PackageManager pm = context.getPackageManager();

    try {
      ApplicationInfo applicationInfo =
          pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);

      return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    return false;
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static Drawable getAppIcon(String packageName, Context context) {
    if (TextUtils.isEmpty(packageName) || context == null) {
      return null;
    }

    PackageManager pm = context.getPackageManager();
    ApplicationInfo applicationInfo;
    try {
      applicationInfo =
          pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
    } catch (PackageManager.NameNotFoundException e) {
      applicationInfo = null;
      e.printStackTrace();
    }
    if (applicationInfo == null) {
      return null;
    }

    return applicationInfo.loadIcon(pm);
  }

  private static int getPackageInfoFlag(Set<AppInfoFilter.Filter> filters) {
    int flag = 0;
    if (filters == null) {
      return flag;
    }
    if (filters.contains(AppInfoFilter.Filter.PERMISSIONS)) {
      flag = flag | PackageManager.GET_PERMISSIONS;
    }
    if (filters.contains(AppInfoFilter.Filter.SIGNATURES)) {
      flag = flag | PackageManager.GET_SIGNATURES;
    }
    return flag;
  }

  @TargetApi(Build.VERSION_CODES.DONUT)
  private static AppInfo createAppInfo(PackageManager pm, PackageInfo packageInfo,
      ApplicationInfo applicationInfo
      , String apkPath, Context context, Set<AppInfoFilter.Filter> filters) {
    if (pm == null || packageInfo == null || applicationInfo == null
        || TextUtils.isEmpty(apkPath) || context == null || filters == null || filters.size() <= 0) {
      return null;
    }

    AppInfo appInfo = new AppInfo();

    if (filters.contains(AppInfoFilter.Filter.ICON)) {
      appInfo.setIcon(applicationInfo.loadIcon(pm));
    }
    if (filters.contains(AppInfoFilter.Filter.APP_NAME)) {
      appInfo.setAppName(pm.getApplicationLabel(applicationInfo).toString());
    }
    if (filters.contains(AppInfoFilter.Filter.PACKAGE_NAME)) {
      appInfo.setPackageName(applicationInfo.packageName);
    }
    if (filters.contains(AppInfoFilter.Filter.VERSION_NAME)) {
      appInfo.setVersionName(packageInfo.versionName);
    }
    if (filters.contains(AppInfoFilter.Filter.VERSION_CODE)) {
      appInfo.setVersionCode(packageInfo.versionCode);
    }
    if (filters.contains(AppInfoFilter.Filter.PERMISSIONS)) {
      appInfo.setPermissions(packageInfo.requestedPermissions);
    }
    if (filters.contains(AppInfoFilter.Filter.SIGNATURES)) {
      appInfo.setSignatures(packageInfo.signatures);
    }
    if (filters.contains(AppInfoFilter.Filter.SOURCE_DIR)) {
      appInfo.setSourceDir(applicationInfo.sourceDir);
    }
    if (filters.contains(AppInfoFilter.Filter.DATA_DIR)) {
      appInfo.setDataDir(applicationInfo.dataDir);
    }
    if (filters.contains(AppInfoFilter.Filter.TARGET_SDK_VERSION)) {
      appInfo.setTargetSdkVersion(applicationInfo.targetSdkVersion);
    }
    if (filters.contains(AppInfoFilter.Filter.APK_SIZE)) {
      appInfo.setApkSize(new File(apkPath).length());
    }
    if (filters.contains(AppInfoFilter.Filter.IS_INSTALLED)) {
      appInfo.setIsInstalled(isAppInstalled(appInfo.getPackageName(), context));
    }
    if (filters.contains(AppInfoFilter.Filter.IS_SYSTEM_APP)) {
      appInfo.setIsSystemApp((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }
    return appInfo;
  }
}
