package com.lemi.controller.lemigameassistance.focus.view;

import android.view.View;

/**
 * use this interface to judge if the child is on the edge of ViewGroup
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface GroupItem {

  boolean isOnTopEdge(View child);

  boolean isOnBottomEdge(View child);

  boolean isOnLeftEdge(View child);

  boolean isOnRightEdge(View child);

  /**
   *
   * @param lastIndex is lastFocus child index in his parent
   */
  void focusAfterUpScroll(int lastIndex);

  void focusAfterDownScroll(int lastIndex);

  void focusAfterLeftScroll(int lastIndex);

  void focusAfterRightScroll(int lastIndex);

  int getIndex(View child);
}
