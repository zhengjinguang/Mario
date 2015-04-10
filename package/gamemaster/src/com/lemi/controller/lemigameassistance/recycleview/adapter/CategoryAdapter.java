package com.lemi.controller.lemigameassistance.recycleview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.recycleview.item.CategoryItem;
import com.lemi.controller.lemigameassistance.recycleview.model.CategoryItemModel;

/**
 * @author zhengjinguang@letv.com (shining)
 */
public class CategoryAdapter
    extends BaseRecycleViewAdapter<CategoryAdapter.CategoryItemViewHolder, CategoryItemModel> {

  public CategoryAdapter() {}

  @Override
  public CategoryItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

    return new CategoryItemViewHolder(CategoryItem.newInstance(viewGroup));
  }

  @Override
  public void onBindViewHolder(CategoryItemViewHolder viewHolder, int position) {
    super.onBindViewHolder(viewHolder, position);

    if (dataList.size() > position && dataList.get(position) != null) {

      if (viewHolder.itemView instanceof CategoryItem) {

        ((CategoryItem) viewHolder.itemView).bind(dataList.get(position));
      }
    }

  }

  public final static class CategoryItemViewHolder extends RecyclerView.ViewHolder {
    public CategoryItemViewHolder(View itemView) {
      super(itemView);
    }
  }
}
