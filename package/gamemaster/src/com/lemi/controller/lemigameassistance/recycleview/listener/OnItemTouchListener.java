package com.lemi.controller.lemigameassistance.recycleview.listener;

import android.view.MotionEvent;
import android.view.View;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface OnItemTouchListener {
  boolean onItemTouchListener(View view, int position, MotionEvent event);
}
