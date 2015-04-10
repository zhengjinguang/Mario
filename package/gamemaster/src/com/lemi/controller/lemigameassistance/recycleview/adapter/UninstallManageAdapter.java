package com.lemi.controller.lemigameassistance.recycleview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.recycleview.item.UninstallItemView;
import com.lemi.controller.lemigameassistance.recycleview.tag.TagConstants;
import com.lemi.controller.lemigameassistance.recycleview.tag.TagHelper;

import java.util.List;

/**
 * @author zhoulei@letv.com (Zhou Lei)
 */
public class UninstallManageAdapter
    extends BaseRecycleViewAdapter<UninstallManageAdapter.UninstallItemViewHolder, GameModel> {

  public UninstallManageAdapter() {}

  public void setData(List<GameModel> dataList) {
    if (dataList == null) {
      throw new IllegalArgumentException("modelData must not be null");
    }
    this.dataList = dataList;
  }

  @Override
  public UninstallItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
    return new UninstallItemViewHolder(UninstallItemView.newInstance(viewGroup));
  }

  @Override
  public void onBindViewHolder(UninstallItemViewHolder viewHolder, int position) {
    super.onBindViewHolder(viewHolder, position);

    if (dataList.size() > position && dataList.get(position) != null) {
      if (viewHolder.itemView instanceof UninstallItemView) {
        /**
         * use this function only in remove or add view
         */
        if (dataList.get(position).equals(viewHolder.itemView.getTag(TagConstants.TAG_MODEL))) {
          ((UninstallItemView) viewHolder.itemView).onlyPositionChange();
          return;
        }
        TagHelper.setModel(viewHolder.itemView, dataList.get(position));
        ((UninstallItemView) viewHolder.itemView).bind(dataList.get(position));
      }
    }

  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }

  public final static class UninstallItemViewHolder extends RecyclerView.ViewHolder {
    public UninstallItemViewHolder(View itemView) {
      super(itemView);
    }
  }
}
