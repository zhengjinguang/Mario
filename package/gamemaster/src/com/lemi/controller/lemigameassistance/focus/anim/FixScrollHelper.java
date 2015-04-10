package com.lemi.controller.lemigameassistance.focus.anim;

import android.support.v7.widget.RecyclerView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class FixScrollHelper {

  private Boolean needFixDownScroll = false;
  private Boolean needFixUpScroll = false;
  private Boolean needFixRightScroll = false;
  private Boolean needFixLeftScroll = false;

  private final byte[] fixScrollLock = new byte[0];


  public void setNeedFixDownScroll() {
    synchronized (fixScrollLock) {
      needFixDownScroll = true;
    }
  }

  public void setNeedFixUpScroll() {
    synchronized (fixScrollLock) {
      needFixUpScroll = true;
    }
  }

  public void setNeedFixRightScroll() {
    synchronized (fixScrollLock) {
      needFixRightScroll = true;
    }
  }

  public void setNeedFixLeftScroll() {
    synchronized (fixScrollLock) {
      needFixLeftScroll = true;
    }
  }

  public boolean needFixScrollVertical() {
    synchronized (fixScrollLock) {
      return needFixDownScroll || needFixUpScroll;
    }
  }

  public boolean needFixScrollHorizontal() {
    synchronized (fixScrollLock) {
      return needFixLeftScroll || needFixRightScroll;
    }
  }

  public void fixScroll(RecyclerView recyclerView) {
    synchronized (fixScrollLock) {
      if (needFixDownScroll) {
        needFixDownScroll = false;
        recyclerView.smoothScrollBy(0, 1);
      }
      if (needFixUpScroll) {
        needFixUpScroll = false;
        recyclerView.smoothScrollBy(0, -1);
      }
      if (needFixRightScroll) {
        needFixRightScroll = false;
        recyclerView.smoothScrollBy(1, 0);
      }
      if (needFixLeftScroll) {
        needFixLeftScroll = false;
        recyclerView.smoothScrollBy(-1, 0);
      }
    }
  }

}
