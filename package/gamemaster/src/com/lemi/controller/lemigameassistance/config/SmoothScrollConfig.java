package com.lemi.controller.lemigameassistance.config;

import android.widget.AbsListView;

import java.lang.ref.WeakReference;

/**
 * config of let listView scroll smooth
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SmoothScrollConfig {
  private SmoothScrollConfig() {}

  /**
   * set each item in listView layer type to hardware type
   * (enable sometime will cause draw fail)
   */
  public static final boolean ENABLE_CARD_ITEM_HARDWARE = false;

  /**
   * don't render image while listView is scrolling
   */
  public static final boolean PAUSE_IMAGE_RENDER_WHILE_SCROLLING = true;

  public static boolean isListViewScrolling = false;

  private static WeakReference<AbsListView> currentListView;

  public static void setListViewScrolling(boolean scrolling) {
    isListViewScrolling = scrolling;
    if (PAUSE_IMAGE_RENDER_WHILE_SCROLLING && !isListViewScrolling && currentListView != null) {
      final AbsListView listView = currentListView.get();
      if (listView != null) {
        listView.invalidate();
      }
    }
  }

  public static void setCurrentListView(AbsListView listView) {
    if (listView == null) {
      isListViewScrolling = false;

      if (currentListView != null) {
        currentListView.clear();
        currentListView = null;
      }
    } else {
      currentListView = new WeakReference<AbsListView>(listView);
    }
  }
}
