package com.lemi.controller.lemigameassistance.recycleview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.recycleview.item.SubjectDetailItem;
import com.lemi.controller.lemigameassistance.recycleview.model.SubjectDetailItemModel;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SubjectDetailAdapter extends
    BaseRecycleViewAdapter<SubjectDetailAdapter.SubjectItemViewHolder, SubjectDetailItemModel> {

  public SubjectDetailAdapter() {}

  @Override
  public SubjectItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

    return new SubjectItemViewHolder(SubjectDetailItem.newInstance(viewGroup));
  }

  @Override
  public void onBindViewHolder(SubjectItemViewHolder viewHolder, int position) {
    super.onBindViewHolder(viewHolder, position);

    if (dataList.size() > position && dataList.get(position) != null) {

      if (viewHolder.itemView instanceof SubjectDetailItem) {

        ((SubjectDetailItem) viewHolder.itemView).bind(dataList.get(position));
      }
    }

  }

  public final static class SubjectItemViewHolder extends RecyclerView.ViewHolder {
    public SubjectItemViewHolder(View itemView) {
      super(itemView);
    }
  }
}
