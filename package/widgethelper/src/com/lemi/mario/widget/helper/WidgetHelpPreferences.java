package com.lemi.mario.widget.helper;

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
public class WidgetHelpPreferences {


  private static SharedPreferences prefs;

  private static final String PREFS_PATH = "widget-helper-apps-prefs";

  private static final String KEY_FIRST_MOUNT = "first_mount";

  private static final ExecutorService pool = Executors.newSingleThreadExecutor();

  public static void preLoadPrefs() {
    getPrefs();
  }

  public static boolean isFirstMount() {
    return getPrefs().getBoolean(KEY_FIRST_MOUNT, true);
  }

  public static void setFirstMount() {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.putBoolean(KEY_FIRST_MOUNT, false);
    submit(editor);
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
