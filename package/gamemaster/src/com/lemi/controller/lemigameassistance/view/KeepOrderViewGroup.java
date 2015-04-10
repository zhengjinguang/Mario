package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.mario.base.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class KeepOrderViewGroup extends ViewGroup {

  protected List<View> originalChildList;

  public KeepOrderViewGroup(Context context) {
    super(context);
  }

  public KeepOrderViewGroup(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public KeepOrderViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }


  @Override
  public void addView(View child) {
    super.addView(child);
  }

  @Override
  public void addView(View child, int index) {
    super.addView(child, index);
  }

  /**
   * change original list in this function because addView(View v) and addView(View child, int
   * index) , will call this method finally
   * 
   * @param child
   * @param index
   * @param params
   */
  @Override
  public void addView(View child, int index, LayoutParams params) {
    super.addView(child, index, params);
    checkAndInitOriginalChildList();
    if (index < 0) {
      index = originalChildList.size();
    }
    originalChildList.add(index, child);
  }

  /**
   * super removeView will not call removeViewAt
   * 
   * @param view
   */
  @Override
  public void removeView(View view) {
    super.removeView(view);
    checkAndInitOriginalChildList();
    originalChildList.remove(view);
  }

  @Override
  public void removeViewAt(int index) {
    super.removeViewAt(index);
    checkAndInitOriginalChildList();
    if (originalChildList.size() > index) {
      originalChildList.remove(index);
    }
  }

  protected void checkAndInitOriginalChildList() {
    if (CollectionUtils.isEmpty(originalChildList)) {
      originalChildList = new ArrayList<>();
    }
  }

  protected boolean checkOriginalChildListValid(List<View> childList) {
    if (CollectionUtils.isEmpty(childList)) {
      return false;
    }
    if (childList.size() != getChildCount()) {
      return false;
    }
    return true;
  }

  protected List<View> getOriginalChildList() {
    originalChildList = new ArrayList<>();
    for (int i = 0; i < getChildCount(); i++) {
      originalChildList.add(getChildAt(i));
    }
    return originalChildList;
  }

  public View getChildInOriginal(int index) {
    View child = null;
    if (index < 0 || index >= getChildCount()) {
      return child;
    }
    if (CollectionUtils.isEmpty(originalChildList) || originalChildList.size() != getChildCount()) {
      child = getChildAt(index);
    } else {
      child = originalChildList.get(index);
    }
    return child;
  }

  public int indexOfOriginalChild(View child) {
    if (CollectionUtils.isEmpty(originalChildList) || originalChildList.size() != getChildCount()) {
      return super.indexOfChild(child);
    } else {
      return originalChildList.indexOf(child);
    }
  }

}
