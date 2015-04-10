package com.lemi.controller.lemigameassistance;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.lemi.mario.base.config.GlobalConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author liuxv@wandoujia.com
 */
public class GameMasterPreferences {


  private static SharedPreferences prefs;

  private static final String PREFS_PATH = "game-master-apps-prefs";

  private static final String KEY_FIRST_INIT = "first_init";
  private static final String KEY_HAS_UPGRADE = "has_upgrade";
  private static final String KEY_IGNORE_VERSION_CODE = "ignore_version_code";
  private static final String KEY_LAST_STARTUP_SID = "last_startup_sid";
  private static final String KEY_LAST_STARTUP_DAY = "last_startup_day";

  private static final ExecutorService pool = Executors.newSingleThreadExecutor();

  public static void preLoadPrefs() {
    getPrefs();
  }

  public static boolean isFirstInit() {
    return getPrefs().getBoolean(KEY_FIRST_INIT, true);
  }

  public static void setFirstInit() {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.putBoolean(KEY_FIRST_INIT, false);
    submit(editor);
  }

  public static boolean hasUpgrade() {
    return getPrefs().getBoolean(KEY_HAS_UPGRADE, true);
  }

  public static void setHasUpgrade(boolean hasUpgrade) {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.putBoolean(KEY_HAS_UPGRADE, hasUpgrade);
    submit(editor);
  }

  public static int getIgnoreVersionCode() {
    return getPrefs().getInt(KEY_IGNORE_VERSION_CODE, 0);
  }

  public static void setIgnoreVersionCode(int versionCode) {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.putInt(KEY_IGNORE_VERSION_CODE, versionCode);
    submit(editor);
  }

  public static void setLastStartUpId(long startUpId) {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.putLong(KEY_LAST_STARTUP_SID, startUpId);
    submit(editor);
  }

  public static long getLastStartUpId() {
    return getPrefs().getLong(KEY_LAST_STARTUP_SID, 0);
  }

  public static void setLastStartUpDay(long startUpDay) {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.putLong(KEY_LAST_STARTUP_DAY, startUpDay);
    submit(editor);
  }

  public static long getLastStartUpDay() {
    return getPrefs().getLong(KEY_LAST_STARTUP_DAY, 0);
  }


  public static void clear() {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.clear();
    submit(editor);
  }

  private static SharedPreferences getPrefs() {
    if (prefs == null) {
      prefs = GlobalConfig.getAppContext()
          .getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE);
    }
    return prefs;
  }


  @TargetApi(Build.VERSION_CODES.GINGERBREAD)
  private static void submit(final SharedPreferences.Editor editor) {
    if (Build.VERSION.SDK_INT > 9) {
      editor.apply();
    } else {
      pool.execute(new Runnable() {

        @Override
        public void run() {
          editor.commit();
        }
      });
    }
  }


}
