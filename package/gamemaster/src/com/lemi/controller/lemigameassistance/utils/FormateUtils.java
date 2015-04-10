package com.lemi.controller.lemigameassistance.utils;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class FormateUtils {
  public final static boolean isValidEmail(CharSequence target) {
    return !TextUtils.isEmpty(target)
        && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
  }

  public final static boolean isValidPhone(CharSequence target) {
    return !TextUtils.isEmpty(target) && Patterns.PHONE.matcher(target).matches();
  }
}
