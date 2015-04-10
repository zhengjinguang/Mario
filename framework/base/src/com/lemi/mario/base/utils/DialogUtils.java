package com.lemi.mario.base.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

/**
 * utils of showing dialog
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class DialogUtils {

  public static void showDialog(final Dialog dialog) {
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        if (dialog != null && !dialog.isShowing()) {
          try {
            dialog.show();
          } catch (Throwable e) {}
        }
      }
    });
  }

  public static void dismissDialog(final Dialog dialog) {
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        if (dialog != null && dialog.isShowing()) {
          try {
            dialog.dismiss();
          } catch (Throwable e) {}
        }
      }
    });
  }

  public static boolean isContextFinished(Context context) {
    return context instanceof Activity && ((Activity) context).isFinishing();
  }
}
