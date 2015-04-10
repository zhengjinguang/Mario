package com.lemi.controller.lemigameassistance.recycleview.tag;

import android.view.View;

import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemAddListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemClickListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemFocusChangeListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemLongClickListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemRemoveListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemTouchListener;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class TagHelper {

  public static void setPosition(View view, int position) {
    view.setTag(TagConstants.TAG_POSITION, position);
  }

  public static void setModel(View view, Object model) {
    view.setTag(TagConstants.TAG_MODEL, model);
  }

  public static void setOnItemClickListener(View view, OnItemClickListener onClickListener) {
    view.setTag(TagConstants.TAG_CLICK_LISTENER, onClickListener);
  }

  public static void setOnItemLongClickListener(View view,
      OnItemLongClickListener onLongClickListener) {
    view.setTag(TagConstants.TAG_LONG_CLICK_LISTENER, onLongClickListener);
  }

  public static void setOnFocusChangeListener(View view,
      OnItemFocusChangeListener onFocusChangeListener) {
    view.setTag(TagConstants.TAG_FOCUS_CHANGE_LISTENER, onFocusChangeListener);
  }

  public static void setOnTouchListener(View view, OnItemTouchListener onTouchListener) {
    view.setTag(TagConstants.TAG_TOUCH_LISTENER, onTouchListener);
  }

  public static void setOnAddListener(View view, OnItemAddListener onItemAddListener) {
    view.setTag(TagConstants.TAG_ADD_LISTENER, onItemAddListener);
  }

  public static void setOnRemoveListener(View view, OnItemRemoveListener onItemRemoveListener) {
    view.setTag(TagConstants.TAG_REMOVE_LISTENER, onItemRemoveListener);
  }

  public static int getPosition(View view) {
    return (int) view.getTag(TagConstants.TAG_POSITION);
  }

  public static OnItemClickListener getOnItemClickListener(View view) {
    return (OnItemClickListener) view.getTag(TagConstants.TAG_CLICK_LISTENER);
  }

  public static OnItemLongClickListener getOnItemLongClickListener(View view) {
    return (OnItemLongClickListener) view.getTag(TagConstants.TAG_LONG_CLICK_LISTENER);
  }

  public static OnItemFocusChangeListener getOnFocusChangeListener(View view) {
    return (OnItemFocusChangeListener) view.getTag(TagConstants.TAG_FOCUS_CHANGE_LISTENER);
  }

  public static OnItemTouchListener getOnTouchListener(View view) {
    return (OnItemTouchListener) view.getTag(TagConstants.TAG_TOUCH_LISTENER);
  }

  public static OnItemAddListener getOnAddListener(View view) {
    return (OnItemAddListener) view.getTag(TagConstants.TAG_ADD_LISTENER);
  }

  public static OnItemRemoveListener getOnRemoveListener(View view) {
    return (OnItemRemoveListener) view.getTag(TagConstants.TAG_REMOVE_LISTENER);
  }

}
