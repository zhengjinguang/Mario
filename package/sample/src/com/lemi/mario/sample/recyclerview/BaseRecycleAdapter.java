package com.lemi.mario.sample.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.mario.sample.view.RecycleItem;

import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class BaseRecycleAdapter extends RecyclerView.Adapter<BaseRecycleAdapter.GridItemViewHolder> {

  private List<GridItemModel> dataList;

  public BaseRecycleAdapter(List<GridItemModel> dataList) {
    if (dataList == null) {
      throw new IllegalArgumentException("modelData must not be null");
    }
    this.dataList = dataList;
  }

  @Override
  public GridItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

    return new GridItemViewHolder(RecycleItem.newInstance(viewGroup));
  }

  @Override
  public void onBindViewHolder(GridItemViewHolder viewHolder, int position) {


    if (dataList.size() > position && dataList.get(position) != null) {

      if (viewHolder.itemView instanceof RecycleItem) {
        ((RecycleItem) viewHolder.itemView).bind(position, dataList.get(position).getImageUrl());
      }

    }

  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }

  public final static class GridItemViewHolder extends RecyclerView.ViewHolder {

    public GridItemViewHolder(View itemView) {
      super(itemView);
    }
  }
}
