package com.lemi.controller.lemigameassistance.recycleview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemAddListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemClickListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemFocusChangeListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemLongClickListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemRemoveListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemTouchListener;
import com.lemi.controller.lemigameassistance.recycleview.tag.TagHelper;

import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class BaseRecycleViewAdapter<VH extends RecyclerView.ViewHolder, T>
    extends RecyclerView.Adapter<VH> {

  /**
   * equals NO_POSITION in RecyclerView.Adapter
   */
  public static final int LAST_POSITION = -1;

  protected List<T> dataList;

  /**
   * call back listeners
   */
  private OnItemClickListener onItemClickListener;
  private OnItemLongClickListener onItemLongClickListener;
  private OnItemFocusChangeListener onItemFocusChangeListener;
  private OnItemTouchListener onItemTouchListener;
  private OnItemAddListener onItemAddListener;
  private OnItemRemoveListener onItemRemoveListener;


  /**
   * the listener set to view
   */
  private OnItemClickListener clickListener = new OnItemClickListener() {
    @Override
    public void onItemClick(View view, int position) {
      if (onItemClickListener != null) {
        onItemClickListener.onItemClick(view, position);
      }
    }
  };
  private OnItemLongClickListener longClickListener = new OnItemLongClickListener() {
    @Override
    public boolean onItemLongClick(View view, int position) {
      if (onItemLongClickListener != null) {
        onItemLongClickListener.onItemLongClick(view, position);
      }
      return false;
    }
  };
  private OnItemFocusChangeListener focusChangeListener = new OnItemFocusChangeListener() {
    @Override
    public void onItemFocusChange(View view, int position, boolean hasFocus) {
      if (onItemFocusChangeListener != null) {
        onItemFocusChangeListener.onItemFocusChange(view, position, hasFocus);
      }

    }
  };
  private OnItemTouchListener touchListener = new OnItemTouchListener() {
    @Override
    public boolean onItemTouchListener(View view, int position, MotionEvent event) {
      if (onItemTouchListener != null) {
        onItemTouchListener.onItemTouchListener(view, position, event);
      }
      return false;
    }
  };
  private OnItemAddListener itemAddListener = new OnItemAddListener() {
    @Override
    public void onItemAdd(View v, int position) {
      if (onItemAddListener != null) {
        onItemAddListener.onItemAdd(v, position);
      }
    }
  };
  private OnItemRemoveListener itemRemoveListener = new OnItemRemoveListener() {
    @Override
    public void onItemRemove(View v, int position) {
      if (onItemRemoveListener != null) {
        onItemRemoveListener.onItemRemove(v, position);
      }
    }
  };



  /**
   * method area
   */

  public void add(T model, int position) {
    position = position == LAST_POSITION ? getItemCount() : position;
    dataList.add(position, model);
    notifyItemInserted(position);
    notifyItemPositionChange(position);
  }

  public void remove(int position) {
    if (position == LAST_POSITION && getItemCount() > 0) {
      position = getItemCount() - 1;
    }

    if (position > LAST_POSITION && position < getItemCount()) {
      dataList.remove(position);
      notifyItemRemoved(position);
      notifyItemPositionChange(position);

    }
  }

  public void setData(List<T> dataList) {
    if (dataList == null) {
      throw new IllegalArgumentException("modelData must not be null");
    }
    this.dataList = dataList;
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }


  @Override
  public void onBindViewHolder(VH holder, int position) {
    TagHelper.setPosition(holder.itemView, position);
    TagHelper.setOnItemClickListener(holder.itemView, clickListener);
    TagHelper.setOnItemLongClickListener(holder.itemView, longClickListener);
    TagHelper.setOnFocusChangeListener(holder.itemView, focusChangeListener);
    TagHelper.setOnTouchListener(holder.itemView, touchListener);
    TagHelper.setOnAddListener(holder.itemView, itemAddListener);
    TagHelper.setOnRemoveListener(holder.itemView, itemRemoveListener);
  }

  public void setOnItemClickLitener(OnItemClickListener listener) {
    onItemClickListener = listener;
  }

  public void setOnItemLongClickLitener(OnItemLongClickListener listener) {
    onItemLongClickListener = listener;
  }

  public void setOnItemFocusChangeListener(OnItemFocusChangeListener listener) {
    onItemFocusChangeListener = listener;
  }

  public void setOnItemTouchListener(OnItemTouchListener listener) {
    onItemTouchListener = listener;
  }

  public void setOnItemAddListener(OnItemAddListener listener) {
    onItemAddListener = listener;
  }

  public void setOnItemRemoveListener(OnItemRemoveListener listener) {
    onItemRemoveListener = listener;
  }

  private void notifyItemPositionChange(int position) {
    for (int i = position; i < getItemCount(); i++) {
      notifyItemChanged(i);
    }
  }


}
