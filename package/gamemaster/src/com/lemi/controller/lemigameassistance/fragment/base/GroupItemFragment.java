package com.lemi.controller.lemigameassistance.fragment.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lemi.controller.lemigameassistance.focus.utils.RecycleFocusUtils;
import com.lemi.controller.lemigameassistance.focus.view.GroupItem;
import com.lemi.controller.lemigameassistance.view.KeepOrderViewGroup;
import com.lemi.mario.base.utils.MainThreadPostUtils;

/**
 * use this class to impl item by page recycleView , which item is group
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class GroupItemFragment<T> extends NetworkAsyncListFragment<T> {

  private static final long SET_FOCUS_DELAY = 50l;
  private static final long INIT_FOCUS_DELAY = 100l;

  /**
   * focus info
   */
  protected int itemFocusLayoutTreeIndex = 0;

  // one item has how many InnerItem
  protected int itemInnerNum = 1;
  // current data list divide to page
  protected int pageNum = 0;

  protected abstract int getItemFocusLayoutTreeIndexLayoutTree();

  @Override
  protected void initConfig() {
    super.initConfig();
    itemFocusLayoutTreeIndex = getItemFocusLayoutTreeIndexLayoutTree();
  }

  @Override
  protected boolean isTop() {
    if (super.isTop()) {
      GroupItem groupItem = getFocusGroupItem();
      if (groupItem != null) {
        if (orientation == LayoutOrientation.HORIZONTAL) {
          return groupItem.isOnLeftEdge(getFocusInnerItem());
        } else {
          return groupItem.isOnTopEdge(getFocusInnerItem());
        }
      }
    }
    return false;
  }

  @Override
  protected boolean isBottom() {
    if (super.isBottom()) {
      GroupItem groupItem = getFocusGroupItem();
      if (groupItem != null) {
        if (orientation == LayoutOrientation.HORIZONTAL) {
          return groupItem.isOnRightEdge(getFocusInnerItem());
        } else {
          return groupItem.isOnBottomEdge(getFocusInnerItem());
        }
      }
    }
    return false;
  }

  @Override
  protected boolean needScroll(PageDirection direction) {
    GroupItem focusGroupItem = getFocusGroupItem();
    if (focusGroupItem != null) {
      if (orientation == LayoutOrientation.HORIZONTAL) {
        if (direction == PageDirection.PRE) {
          if (focusGroupItem.isOnLeftEdge(getFocusInnerItem())) {
            return true;
          }
        } else {
          if (focusGroupItem.isOnRightEdge(getFocusInnerItem())) {
            return true;
          }
        }
      } else {
        if (direction == PageDirection.PRE) {
          if (focusGroupItem.isOnTopEdge(getFocusInnerItem())) {
            return true;
          }
        } else {
          if (focusGroupItem.isOnBottomEdge(getFocusInnerItem())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  protected void requestFocusAfterLeftScroll(final View currentFocusView) {
    GroupItem nextFocusItem = getGroupItem(RecycleFocusUtils.getFocusPosition(recyclerView) - 1);
    if (nextFocusItem != null) {
      GroupItem currentFocusItem = null;
      if (currentFocusView instanceof GroupItem) {
        currentFocusItem = (GroupItem) currentFocusView;
      }
      if (currentFocusItem != null) {
        nextFocusItem.focusAfterLeftScroll(currentFocusItem.getIndex(getFocusInnerItem()));
      } else {
        nextFocusItem.focusAfterLeftScroll(0);
      }
    } else {
      MainThreadPostUtils.postDelayed(new Runnable() {
        @Override
        public void run() {
          requestFocusAfterLeftScroll(currentFocusView);
        }
      }, SET_FOCUS_DELAY);
    }
  }

  @Override
  protected void requestFocusAfterRightScroll(final View currentFocusView) {
    GroupItem nextFocusItem = getGroupItem(RecycleFocusUtils.getFocusPosition(recyclerView) + 1);
    if (nextFocusItem != null) {
      GroupItem currentFocusItem = null;
      if (currentFocusView instanceof GroupItem) {
        currentFocusItem = (GroupItem) currentFocusView;
      }
      if (currentFocusItem != null) {
        nextFocusItem.focusAfterRightScroll(currentFocusItem.getIndex(getFocusInnerItem()));
      } else {
        nextFocusItem.focusAfterRightScroll(0);
      }
    } else {
      MainThreadPostUtils.postDelayed(new Runnable() {
        @Override
        public void run() {
          requestFocusAfterRightScroll(currentFocusView);
        }
      }, SET_FOCUS_DELAY);
    }
  }

  @Override
  protected void requestFocusAfterUpScroll(final View currentFocusView) {
    GroupItem nextFocusItem = getGroupItem(RecycleFocusUtils.getFocusPosition(recyclerView) - 1);
    if (nextFocusItem != null) {
      GroupItem currentFocusItem = null;
      if (currentFocusView instanceof GroupItem) {
        currentFocusItem = (GroupItem) currentFocusView;
      }
      if (currentFocusItem != null) {
        nextFocusItem.focusAfterUpScroll(currentFocusItem.getIndex(getFocusInnerItem()));
      } else {
        nextFocusItem.focusAfterUpScroll(0);
      }
    } else {
      MainThreadPostUtils.postDelayed(new Runnable() {
        @Override
        public void run() {
          requestFocusAfterUpScroll(currentFocusView);
        }
      }, SET_FOCUS_DELAY);
    }
  }


  @Override
  protected void requestFocusAfterDownScroll(final View currentFocusView) {
    GroupItem nextFocusItem = getGroupItem(RecycleFocusUtils.getFocusPosition(recyclerView) + 1);
    if (nextFocusItem != null) {
      GroupItem currentFocusItem = null;
      if (currentFocusView instanceof GroupItem) {
        currentFocusItem = (GroupItem) currentFocusView;
      }
      if (currentFocusItem != null) {
        nextFocusItem.focusAfterDownScroll(currentFocusItem.getIndex(getFocusInnerItem()));
      } else {
        nextFocusItem.focusAfterDownScroll(0);
      }
    } else {
      MainThreadPostUtils.postDelayed(new Runnable() {
        @Override
        public void run() {
          requestFocusAfterDownScroll(currentFocusView);
        }
      }, SET_FOCUS_DELAY);
    }
  }

  protected void initFocus(final int childIndex) {
    if (childIndex < 0) {
      return;
    }
    RecyclerView.ViewHolder child = recyclerView.findViewHolderForPosition(childIndex);
    if (child != null && child.itemView instanceof KeepOrderViewGroup) {
      View view = ((KeepOrderViewGroup) child.itemView).getChildInOriginal(0);
      if (view != null) {
        view.requestFocus();
        return;
      }
    }
    MainThreadPostUtils.postDelayed(new Runnable() {
      @Override
      public void run() {
        initFocus(childIndex);
      }
    }, INIT_FOCUS_DELAY);
  }

  protected GroupItem getFocusGroupItem() {
    View focusChild = recyclerView.getFocusedChild();
    if (focusChild instanceof GroupItem) {
      return (GroupItem) focusChild;
    }
    return null;
  }


  protected GroupItem getGroupItem(int position) {
    if (position < 0 || position >= recycleAdapter.getItemCount()) {
      return null;
    }

    RecyclerView.ViewHolder child = recyclerView.findViewHolderForPosition(position);
    if (child == null) {
      return null;
    }
    if (child.itemView instanceof GroupItem) {
      return (GroupItem) child.itemView;
    }
    return null;
  }


  protected View getFocusInnerItem() {
    View focusItem = recyclerView.findFocus();
    for (int i = 0; i < itemFocusLayoutTreeIndex; i++) {
      if (focusItem == null) {
        return null;
      }
      focusItem = (View) focusItem.getParent();
    }
    return focusItem;
  }


}
