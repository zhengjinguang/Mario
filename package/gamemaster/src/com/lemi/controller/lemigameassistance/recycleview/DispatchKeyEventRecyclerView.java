package com.lemi.controller.lemigameassistance.recycleview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DispatchKeyEventRecyclerView extends RecyclerView {

  public DispatchKeyEventRecyclerView(Context context) {
    super(context);
  }

  public DispatchKeyEventRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public DispatchKeyEventRecyclerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    return super.dispatchKeyEvent(event);
  }

}
