package com.lemi.controller.lemigameassistance.focus.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class RecycleFocusUtils {


  public static View getFocusChild(RecyclerView recyclerView) {
    if (recyclerView != null) {
      return recyclerView.getFocusedChild();
    }
    return null;
  }

  public static int getFocusPosition(RecyclerView recyclerView) {
    View focusChild = getFocusChild(recyclerView);
    if (focusChild == null) {
      return -1;
    }
    return recyclerView.getChildPosition(focusChild);
  }

}
