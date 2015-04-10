package com.lemi.controller.lemigameassistance.focus.view;

import android.view.View;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface TabFragment {

  void requestLeftFocus();

  void requestRightFocus();

  void requestDownFocus();

  boolean isOnTop(View view);

}
