package com.lemi.controller.lemigameassistance.focus.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;

import com.lemi.mario.base.utils.MainThreadPostUtils;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class FocusUtils {

  private static final long INIT_FOCUS_DELAY = 100l;

  /**
   * make sure the recycle view must have child later ,or it will make memory leak when postDelayed
   *
   */
  public static void initRecycleViewFocus(final RecyclerView recyclerView) {
    RecyclerView.ViewHolder child = recyclerView.findViewHolderForPosition(0);
    if (child != null && child.itemView != null) {
      child.itemView.requestFocus();
      return;
    }
    MainThreadPostUtils.postDelayed(new Runnable() {
      @Override
      public void run() {
        initRecycleViewFocus(recyclerView);
      }
    }, INIT_FOCUS_DELAY);
  }


  public static View getParent(View child, int treeIndex) {
    if (treeIndex == 0) {
      return child;
    }
    if (child == null) {
      return null;
    }
    View parent = (View) child.getParent();
    return getParent(parent, treeIndex - 1);
  }

  public static boolean canFocusValid(View view) {
    if (view == null) {
      return false;
    }
    if (view.getHeight() == 0 || view.getWidth() == 0) {
      return false;
    }
    return true;
  }


  public static boolean isParentFocus(View view) {
    if (view == null) {
      return false;
    }
    if (view.isFocused()) {
      return true;
    }
    ViewParent parent = view.getParent();
    if (parent instanceof View) {
      return isParentFocus((View) parent);
    }
    return false;
  }

}
