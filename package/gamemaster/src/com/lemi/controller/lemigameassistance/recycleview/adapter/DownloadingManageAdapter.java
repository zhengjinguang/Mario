package com.lemi.controller.lemigameassistance.recycleview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.recycleview.item.DownloadingItemView;
import com.lemi.controller.lemigameassistance.recycleview.tag.TagConstants;
import com.lemi.controller.lemigameassistance.recycleview.tag.TagHelper;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadingManageAdapter extends
    BaseRecycleViewAdapter<DownloadingManageAdapter.DownloadingItemViewHolder, DownloadInfo> {

  public DownloadingManageAdapter() {}

  @Override
  public DownloadingItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

    return new DownloadingItemViewHolder(DownloadingItemView.newInstance(viewGroup));
  }

  @Override
  public void onBindViewHolder(DownloadingItemViewHolder viewHolder, int position) {
    super.onBindViewHolder(viewHolder, position);

    if (dataList.size() > position && dataList.get(position) != null) {

      if (viewHolder.itemView instanceof DownloadingItemView) {
        /**
         * use this function only in remove or add view
         */
        if (dataList.get(position).equals(viewHolder.itemView.getTag(TagConstants.TAG_MODEL))) {
          ((DownloadingItemView) viewHolder.itemView).onlyPositionChange();
          return;
        }
        TagHelper.setModel(viewHolder.itemView, dataList.get(position));
        ((DownloadingItemView) viewHolder.itemView).bind(dataList.get(position));
      }
    }

  }

  public final static class DownloadingItemViewHolder extends RecyclerView.ViewHolder {
    public DownloadingItemViewHolder(View itemView) {
      super(itemView);
    }
  }
}
