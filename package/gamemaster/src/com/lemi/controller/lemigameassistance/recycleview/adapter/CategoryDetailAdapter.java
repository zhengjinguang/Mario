package com.lemi.controller.lemigameassistance.recycleview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.recycleview.item.CategoryDetailItem;
import com.lemi.controller.lemigameassistance.recycleview.model.CategoryDetailItemModel;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CategoryDetailAdapter extends
    BaseRecycleViewAdapter<CategoryDetailAdapter.CategoryItemViewHolder, CategoryDetailItemModel> {

  public CategoryDetailAdapter() {}

  @Override
  public CategoryItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

    return new CategoryItemViewHolder(CategoryDetailItem.newInstance(viewGroup));
  }

  @Override
  public void onBindViewHolder(CategoryItemViewHolder viewHolder, int position) {
    super.onBindViewHolder(viewHolder, position);

    if (dataList.size() > position && dataList.get(position) != null) {

      if (viewHolder.itemView instanceof CategoryDetailItem) {

        ((CategoryDetailItem) viewHolder.itemView).bind(dataList.get(position));
      }
    }

  }

  public final static class CategoryItemViewHolder extends RecyclerView.ViewHolder {
    public CategoryItemViewHolder(View itemView) {
      super(itemView);
    }
  }
}
