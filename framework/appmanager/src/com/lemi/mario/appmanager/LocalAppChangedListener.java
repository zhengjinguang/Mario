package com.lemi.mario.appmanager;

import com.lemi.mario.appmanager.error.Error;
import com.lemi.mario.appmanager.model.AppInfo;

/**
 * Created by zhou on 14-12-8.
 */
public interface LocalAppChangedListener {
  void onInstalled(String packageName, AppInfo appInfo);

  void onUninstalled(String packageName);

  void onInstallFailed(String packageName, com.lemi.mario.appmanager.error.Error error);

  void onUninstallFailed(String packageName, Error error);

  void onStartNonRootInstall(String packageName);

  void onStartSilenceInstall(String packageName);

  void onStartUninstall(String packageName);
}
