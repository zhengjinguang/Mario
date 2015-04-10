package com.lemi.controller.lemigameassistance.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.download.DownloadManager;
import com.lemi.controller.lemigameassistance.download.DownloadUtils;
import com.lemi.controller.lemigameassistance.fetcher.BaseFetcher;
import com.lemi.controller.lemigameassistance.focus.utils.FocusUtils;
import com.lemi.controller.lemigameassistance.fragment.base.NetworkAsyncListFragment;
import com.lemi.controller.lemigameassistance.manager.InstallManager;
import com.lemi.controller.lemigameassistance.manager.ZipManager;
import com.lemi.controller.lemigameassistance.recycleview.adapter.BaseRecycleViewAdapter;
import com.lemi.controller.lemigameassistance.recycleview.adapter.DownloadingManageAdapter;
import com.lemi.controller.lemigameassistance.recycleview.item.DownloadingItemView;
import com.lemi.controller.lemigameassistance.recycleview.item.DownloadingItemView.FocusButton;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemFocusChangeListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemRemoveListener;
import com.lemi.mario.base.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadManageFragment extends NetworkAsyncListFragment<DownloadInfo> {

  private static int ITEM_HEIGHT = 0;

  private ImageView emptyView;

  private ButtonFocusStatus lastFocusButton = new ButtonFocusStatus();


  /**
   * custom model
   */
  private List<DownloadInfo> downloadingList = new ArrayList<>();


  public DownloadManageFragment() {}

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    super.onInflated(contentView, savedInstanceState);
    initView();
    initScrollConfig();
    setItemListener();
    initFocus();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.downloading_manage_fragment;
  }

  @Override
  protected BaseRecycleViewAdapter getAdapter() {
    recycleAdapter = new DownloadingManageAdapter();
    ((DownloadingManageAdapter) recycleAdapter).setData(downloadingList);
    return recycleAdapter;
  }

  @Override
  protected LinearLayoutManager getLayoutManager() {
    layoutManager = new LinearLayoutManager(getActivity());
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    return layoutManager;
  }

  @Override
  protected LayoutOrientation getOrientation() {
    return LayoutOrientation.VERTICAL;
  }

  @Override
  protected List<DownloadInfo> fetchHttpData(int start, int size) throws ExecutionException {
    List<DownloadInfo> downloadInfos = new ArrayList<>();
    downloadInfos.addAll(getDownloadingInfo());
    downloadInfos
        .addAll(getProcessingDownloadInfoInInstallAndZip(getProcessingTokenInInstallAndZip()));
    return downloadInfos;
  }

  @Override
  protected String getCacheKey() {
    return null;
  }

  @Override
  protected void onFetched(int start, int size, BaseFetcher.ResultList<DownloadInfo> result) {
    downloadingList.addAll(result.data);
    recycleAdapter.notifyDataSetChanged();
    judgeEmptyImageNeedShow();
  }

  @Override
  protected void onFailed(int start, ExecutionException e) {}

  @Override
  protected LoadNetworkTimes getLoadNetworkTimes() {
    return LoadNetworkTimes.ONCE;
  }

  private void initView() {
    emptyView = (ImageView) contentView.findViewById(R.id.empty_view);
  }

  private void initScrollConfig() {
    ITEM_HEIGHT = getResources().getDimensionPixelOffset(R.dimen.mario_142dp);
  }

  private void initFocus() {
    FocusUtils.initRecycleViewFocus(recyclerView);
  }

  private void setItemListener() {
    recycleAdapter.setOnItemFocusChangeListener(new OnItemFocusChangeListener() {
      @Override
      public void onItemFocusChange(View view, int position, boolean hasFocus) {
        if (hasFocus) {
          setButtonFocus(view);
          if (position == 0) {
            recyclerView.smoothScrollToPosition(0);
            return;
          } else if (position == recycleAdapter.getItemCount() - 1) {
            recyclerView.smoothScrollToPosition(recycleAdapter.getItemCount() - 1);
            return;
          }
          if (position <= layoutManager.findFirstVisibleItemPosition()) {
            recyclerView.smoothScrollBy(0, -ITEM_HEIGHT);
          } else if (position >= layoutManager.findLastVisibleItemPosition()) {
            recyclerView.smoothScrollBy(0, ITEM_HEIGHT);
          }
        }
      }
    });
    recycleAdapter.setOnItemRemoveListener(new OnItemRemoveListener() {
      @Override
      public void onItemRemove(View v, int position) {
        setRemoveFocus();
        recycleAdapter.remove(position);
      }
    });
  }

  private void setRemoveFocus() {
    int position = getFocusPosition() + 1;
    if (position > recycleAdapter.getItemCount() - 1) {
      position = recycleAdapter.getItemCount() - 1;
      if (position < 0) {
        return;
      }
    }
    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForPosition(position);
    if (holder != null && holder.itemView != null) {
      if (holder.itemView instanceof DownloadingItemView) {
        ((DownloadingItemView) holder.itemView).focusButton(lastFocusButton.focusButton);
      }
    }
  }

  private void setButtonFocus(View view) {
    int currentPosition = getFocusPosition();
    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForPosition(getFocusPosition());
    if (holder != null && holder.itemView != null) {
      if (holder.itemView instanceof DownloadingItemView) {
        FocusButton currentFocusButton =
            ((DownloadingItemView) holder.itemView).getFocusButton(view);
        if (lastFocusButton.position != currentPosition) {
          lastFocusButton.position = currentPosition;
          if (lastFocusButton.focusButton != currentFocusButton) {
            ((DownloadingItemView) holder.itemView).focusButton(lastFocusButton.focusButton);
          }
        } else {
          if (lastFocusButton.focusButton != currentFocusButton) {
            lastFocusButton.focusButton = currentFocusButton;
          }
        }
      }
    }
  }

  private void judgeEmptyImageNeedShow() {
    if (CollectionUtils.isEmpty(downloadingList)) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private List<DownloadInfo> getDownloadingInfo() {
    return DownloadManager.getInstance().getDownloadInfoList(
        DownloadUtils.createVisibleOnGoingDownloadFilterBuilder().build());
  }

  private List<String> getProcessingTokenInInstallAndZip() {
    List<String> tokenList = new ArrayList<>();
    tokenList.addAll(InstallManager.getInstance().getUnFinishedToken());
    tokenList.addAll(ZipManager.getInstance().getUnFinishedToken());
    return tokenList;
  }

  private List<DownloadInfo> getProcessingDownloadInfoInInstallAndZip(List<String> tokenList) {
    return DownloadManager.getInstance().getDownloadInfosByIdentities(tokenList);
  }

  private class ButtonFocusStatus {
    public int position = 0;
    public FocusButton focusButton = FocusButton.NONE;
  }

}
