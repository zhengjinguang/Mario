package com.lemi.mario.appmanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.lemi.mario.appmanager.error.Error;
import com.lemi.mario.appmanager.utils.AppQueryUtils;
import com.lemi.mario.base.utils.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * Created by zhou on 14-12-8.
 */
class AppInstaller {
  private static AppInstaller instance;
  private Context appContext;
  private static final String APP_INSTALL_PATH = "/data";

  private AppInstaller(Context context) {
    appContext = context.getApplicationContext();
  }

  public static AppInstaller getInstance(Context context) {
    if (context == null || context.getApplicationContext() == null) {
      throw new IllegalArgumentException("context can't be null");
    }
    if (instance == null) {
      instance = new AppInstaller(context);
    }
    return instance;
  }

  public interface InstallerCallback {

    void onInstallFailed(String packageName, Error error);

    void onUninstallFailed(String packageName, Error error);

    void onStartNonRootInstall(String packageName);

    void onStartSilenceInstall(String packageName);

    void onStartUninstall(String packageName);

  }

  public void installApk(final String apkPath, ExecutorService executorService,
      final InstallerCallback installerCallback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        String packageName = AppQueryUtils.getPackageNameByApkFile(apkPath, appContext);

        if (!checkAndInitInstall(apkPath, packageName, installerCallback)) {
          return;
        }

        installerCallback.onStartNonRootInstall(packageName);
        installApk(apkPath, appContext);

      }
    });
  }

  public void silenceInstallApk(final String apkPath, ExecutorService executorService,
      final InstallerCallback installerCallback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        String packageName = AppQueryUtils.getPackageNameByApkFile(apkPath, appContext);

        if (!checkAndInitInstall(apkPath, packageName, installerCallback)) {
          return;
        }

        installerCallback.onStartSilenceInstall(packageName);
        silenceInstallApk(apkPath, appContext);

      }
    });
  }

  public void uninstallApp(final String packageName, ExecutorService executorService,
      final InstallerCallback installerCallback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        if (!checkAndInitUninstall(packageName, installerCallback)) {
          return;
        }
        installerCallback.onStartUninstall(packageName);
        uninstallApk(packageName, appContext);
      }
    });
  }

  private void installApk(String filePath, Context context) {
    if (TextUtils.isEmpty(filePath) || context == null)
      return;

    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setDataAndType(Uri.fromFile(new File(filePath)),
        "application/vnd.android.package-archive");
    context.startActivity(intent);
  }

  /**
   * only available in Letv
   * 
   * @param filePath
   * @param context
   */
  private void silenceInstallApk(String filePath, Context context) {
    if (TextUtils.isEmpty(filePath) || context == null)
      return;

    String packageName = AppQueryUtils.getPackageNameByApkFile(filePath, context);

    if (TextUtils.isEmpty(packageName))
      return;

    Intent intent = new Intent(
        "android.intent.action.INSTALL_PACKAGE_QUIET");
    intent.putExtra("INSTALL_APP_NAME", packageName);
    intent.putExtra("INSTALL_PACKAGE_NAME", packageName);
    intent.putExtra("CLIENT_NAME", packageName);
    intent.setData(Uri.fromFile(new File(filePath)));
    context.sendBroadcast(intent);
  }

  private void uninstallApk(String packageName, Context context) {
    if (TextUtils.isEmpty(packageName) || context == null)
      return;

    Uri packageURI = Uri.parse("package:"
        + packageName);
    Intent intent = new Intent(
        Intent.ACTION_DELETE, packageURI);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  private static boolean canAppInstall(long appSize) {
    if (FileUtil.getAvailableBytes(APP_INSTALL_PATH) >= appSize) {
      return true;
    }
    return false;
  }

  private boolean checkAndInitInstall(String apkPath, String packageName,
      InstallerCallback callback) {
    if (TextUtils.isEmpty(apkPath) || !FileUtil.exists(apkPath)) {
      callback.onInstallFailed(AppManager.PACKAGENAME_UNKNOWN, Error.FILE_NO_EXIST_ERROR);
      return false;
    }

    if (TextUtils.isEmpty(packageName)) {
      callback.onInstallFailed(AppManager.PACKAGENAME_UNKNOWN, Error.PACKAGE_PARSE_ERROR);
      return false;
    }

    return true;
  }

  private boolean checkAndInitUninstall(String packageName, InstallerCallback callback) {

    if (TextUtils.isEmpty(packageName)) {
      callback.onUninstallFailed(AppManager.PACKAGENAME_UNKNOWN, Error.PACKAGE_NAME_ERROR);
      return false;
    }

    boolean isInstalled = AppQueryUtils.isAppInstalled(packageName, appContext);
    if (!isInstalled) {
      callback.onUninstallFailed(packageName, Error.NO_APP_ERROR);
      return false;
    }

    return true;
  }
}
