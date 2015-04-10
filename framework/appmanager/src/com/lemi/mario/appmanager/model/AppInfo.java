package com.lemi.mario.appmanager.model;

import android.content.pm.Signature;
import android.graphics.drawable.Drawable;

/**
 * Created by zhou on 14-12-1.
 */
public class AppInfo {
  private Drawable icon;

  private String appName;
  private String packageName;
  private String versionName;
  private String sourceDir;
  private String dataDir;

  private int versionCode;
  private int targetSdkVersion;

  private String[] permissions;
  private Signature[] signatures;

  private long apkSize;

  private boolean isSystemApp;
  private boolean isInstalled;

  public void setIcon(Drawable icon) {
      this.icon = icon;
  }

  public Drawable getIcon() {
      return icon;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getAppName() {
    return appName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setSignatures(Signature[] signatures) {
    this.signatures = signatures;
  }

  public Signature[] getSignatures() {
    return signatures;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  public String getVersionName() {
    return versionName;
  }

  public void setSourceDir(String sourceDir) {
    this.sourceDir = sourceDir;
  }

  public String getSourceDir() {
    return sourceDir;
  }

  public void setDataDir(String dataDir) {
    this.dataDir = dataDir;
  }

  public String getDataDir() {
    return dataDir;
  }

  public void setVersionCode(int versionCode) {
    this.versionCode = versionCode;
  }

  public int getVersionCode() {
    return versionCode;
  }

  public void setTargetSdkVersion(int targetSdkVersion) {
    this.targetSdkVersion = targetSdkVersion;
  }

  public int getTargetSdkVersion() {
    return targetSdkVersion;
  }

  public void setPermissions(String[] permissions) {
    this.permissions = permissions;
  }

  public String[] getPermissions() {
    return permissions;
  }

  public void setApkSize(long apkSize) {
    this.apkSize = apkSize;
  }

  public long getApkSize() {
    return apkSize;
  }

  public void setIsSystemApp(boolean isSystemApp) {
    this.isSystemApp = isSystemApp;
  }

  public boolean isSystemApp() {
    return isSystemApp;
  }

  public void setIsInstalled(boolean isInstalled) {
    this.isInstalled = isInstalled;
  }

  public boolean isInstalled() {
    return isInstalled;
  }
}
