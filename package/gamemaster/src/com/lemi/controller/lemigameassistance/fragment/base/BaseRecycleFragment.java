package com.lemi.controller.lemigameassistance.fragment.base;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.focus.listener.OnEdgeListener;
import com.lemi.controller.lemigameassistance.focus.utils.RecycleFocusUtils;
import com.lemi.controller.lemigameassistance.recycleview.adapter.BaseRecycleViewAdapter;

/**
 * impl this fragment if you want use recycle view in you fragment
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class BaseRecycleFragment extends AsyncLoadFragment {

  /**
   * recycle component
   */
  protected RecyclerView recyclerView;
  protected BaseRecycleViewAdapter recycleAdapter;
  protected LinearLayoutManager layoutManager;

  /**
   * page info
   */
  protected enum LayoutOrientation {
    VERTICAL, HORIZONTAL
  }

  protected enum PageDirection {
    PRE, NEXT
  }


  protected LayoutOrientation orientation = LayoutOrientation.HORIZONTAL;

  /**
   * firstIndex , in order to judge if is first child
   */
  protected int firstIndex = 0;

  /**
   * if true , fragment you block key event when at first or last item
   */
  protected boolean blockAtEdge = true;

  /**
   * if true , fragment will drop navi event when scroll status is idle
   */
  protected boolean blockScrolling = false;

  /**
   * edge listener
   */
  private OnEdgeListener onEdgeListener;

  private boolean isScrolling = false;


  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    initRecycleView();
    initConfig();
    setOnScrollListener();
  }

  protected void initRecycleView() {
    recyclerView = (RecyclerView) contentView.findViewById(R.id.recycle_view);
    layoutManager = getLayoutManager();
    recyclerView.setLayoutManager(layoutManager);
    recycleAdapter = getAdapter();
    recyclerView.setAdapter(recycleAdapter);
  }

  protected void initConfig() {
    orientation = getOrientation();
  }

  private void setOnScrollListener() {
    recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
          onScrollStateIdle();
        }
      }
    });
  }

  public void setOnEdgeListener(OnEdgeListener onEdgeListener) {
    this.onEdgeListener = onEdgeListener;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (!checkOrientationValid(keyCode)) {
      return false;
    }
    switch (keyCode) {
    /**
     * handler {@link LayoutOrientation.HORIZONTAL}
     */
      case KeyEvent.KEYCODE_DPAD_LEFT:
        if (checkNeedBlockNavi()) {
          return true;
        }
        if (isTop()) {
          if (onEdgeListener != null && onEdgeListener.onTopEdgeRequest()) {
            return true;
          }
          return blockAtEdge;
        }
        if (needScroll(PageDirection.PRE) && scrollLeft()) {
          startScroll();
          requestFocusAfterLeftScroll(RecycleFocusUtils.getFocusChild(recyclerView));
          return true;
        }
        return false;

      case KeyEvent.KEYCODE_DPAD_RIGHT:
        if (checkNeedBlockNavi()) {
          return true;
        }
        if (isBottom()) {
          if (onEdgeListener != null && onEdgeListener.onBottomEdgeRequest()) {
            return true;
          }
          return blockAtEdge;
        }
        if (needScroll(PageDirection.NEXT) && scrollRight()) {
          startScroll();
          requestFocusAfterRightScroll(RecycleFocusUtils.getFocusChild(recyclerView));
          return true;
        }
        return false;


        /**
         * handler {@link LayoutOrientation.VERTICAL}
         */
      case KeyEvent.KEYCODE_DPAD_UP:
        if (checkNeedBlockNavi()) {
          return true;
        }
        if (isTop()) {
          if (onEdgeListener != null && onEdgeListener.onTopEdgeRequest()) {
            return true;
          }
          return blockAtEdge;
        }
        if (needScroll(PageDirection.PRE) && scrollUp()) {
          startScroll();
          requestFocusAfterUpScroll(RecycleFocusUtils.getFocusChild(recyclerView));
          return true;
        }
        return false;

      case KeyEvent.KEYCODE_DPAD_DOWN:
        if (checkNeedBlockNavi()) {
          return true;
        }
        if (isBottom()) {
          if (onEdgeListener != null && onEdgeListener.onBottomEdgeRequest()) {
            return true;
          }
          return blockAtEdge;
        }
        if (needScroll(PageDirection.NEXT) && scrollDown()) {
          startScroll();
          requestFocusAfterDownScroll(RecycleFocusUtils.getFocusChild(recyclerView));
          return true;
        }
        return false;
    }
    return false;
  }

  protected abstract BaseRecycleViewAdapter getAdapter();

  protected abstract LinearLayoutManager getLayoutManager();

  protected abstract LayoutOrientation getOrientation();


  protected boolean isTop() {
    return RecycleFocusUtils.getFocusPosition(recyclerView) == firstIndex;
  }

  protected boolean isBottom() {
    return RecycleFocusUtils.getFocusPosition(recyclerView) == recycleAdapter.getItemCount() - 1;
  }

  protected boolean needScroll(PageDirection direction) {
    int focusPosition = RecycleFocusUtils.getFocusPosition(recyclerView);
    if (direction == PageDirection.PRE) {
      int firstCompletelyVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition();
      if (firstCompletelyVisiblePosition == -1) {
        return true;
      }
      return focusPosition == firstCompletelyVisiblePosition;
    } else {
      int lastCompletelyVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
      if (lastCompletelyVisiblePosition == -1) {
        return true;
      }
      return focusPosition == lastCompletelyVisiblePosition;
    }
  }

  /**
   * overWrite this function if want scroll custom
   *
   * @return true to avoid recycleView his own scroll
   */
  protected boolean scrollLeft() {
    return false;
  }

  protected boolean scrollRight() {
    return false;
  }

  protected boolean scrollUp() {
    return false;
  }

  protected boolean scrollDown() {
    return false;
  }


  protected void requestFocusAfterLeftScroll(View currentFocusView) {
    focusPre();
  }

  protected void requestFocusAfterRightScroll(View currentFocusView) {
    focusNext();
  }

  protected void requestFocusAfterUpScroll(View currentFocusView) {
    focusPre();
  }

  protected void requestFocusAfterDownScroll(View currentFocusView) {
    focusNext();
  }

  protected void onScrollStateIdle() {
    isScrolling = false;
  }

  protected int getFocusPosition() {
    return RecycleFocusUtils.getFocusPosition(recyclerView);
  }

  private void focusPre() {
    int focusPosition = getFocusPosition() - 1;
    if (focusPosition < 0) {
      focusPosition = 0;
    }
    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForPosition(focusPosition);
    if (holder != null && holder.itemView != null) {
      holder.itemView.requestFocus();
    }
  }

  private void focusNext() {
    int focusPosition = getFocusPosition() + 1;
    if (focusPosition > recycleAdapter.getItemCount() - 1) {
      focusPosition = recycleAdapter.getItemCount() - 1;
    }
    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForPosition(focusPosition);
    if (holder != null && holder.itemView != null) {
      holder.itemView.requestFocus();
    }
  }

  private void startScroll() {
    isScrolling = true;
  }

  private boolean checkNeedBlockNavi() {
    if (blockScrolling && isScrolling) {
      return true;
    }
    return false;
  }

  private boolean checkOrientationValid(int keyCode) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_LEFT:
        return orientation == LayoutOrientation.HORIZONTAL;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        return orientation == LayoutOrientation.HORIZONTAL;
      case KeyEvent.KEYCODE_DPAD_UP:
        return orientation == LayoutOrientation.VERTICAL;
      case KeyEvent.KEYCODE_DPAD_DOWN:
        return orientation == LayoutOrientation.VERTICAL;
    }
    return false;
  }

}
