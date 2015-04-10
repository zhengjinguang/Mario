package com.lemi.controller.lemigameassistance.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.fetcher.BaseFetcher;
import com.lemi.controller.lemigameassistance.focus.utils.FocusUtils;
import com.lemi.controller.lemigameassistance.fragment.base.NetworkAsyncListFragment;
import com.lemi.controller.lemigameassistance.manager.MyGameManager;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.recycleview.adapter.BaseRecycleViewAdapter;
import com.lemi.controller.lemigameassistance.recycleview.adapter.UninstallManageAdapter;
import com.lemi.controller.lemigameassistance.recycleview.item.UninstallItemView;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemFocusChangeListener;
import com.lemi.mario.base.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author zhoulei@letv.com (Zhou Lei)
 */
public class UninstallManageFragment extends NetworkAsyncListFragment<GameModel> {

  public static final int LAST_POSITION = -1;

  private static int ITEM_HEIGHT = 0;

  private ImageView emptyView;

  private final ArrayList<GameModel> installedGames = new ArrayList<>();

  private MyGameManager.OnMyGameChangeListener onMyGameChangeListener =
      new MyGameManager.OnMyGameChangeListener() {
        @Override
        public void onMyGameAdd(String packageName, GameModel gameModel) {
          synchronized (installedGames) {
            if (gameModel != null && !installedGames.contains(gameModel)) {
              recycleAdapter.add(gameModel, LAST_POSITION);
            }
          }
          judgeEmptyImageNeedShow();
        }

        @Override
        public void onMyGameRemove(String packageName) {
          if (!TextUtils.isEmpty(packageName)) {
            synchronized (installedGames) {
              for (int i = 0; i < installedGames.size(); i++) {
                GameModel model = installedGames.get(i);
                if (model == null || TextUtils.isEmpty(model.getPackageName())) {
                  continue;
                }
                if (packageName.equals(model.getPackageName())) {
                  setRemoveFocus();
                  recycleAdapter.remove(i);
                }
              }
            }
            judgeEmptyImageNeedShow();
          }
        }
      };


  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    super.onInflated(contentView, savedInstanceState);
    initView();
    initScrollConfig();
    setItemListener();
    initFocus();
    MyGameManager.getInstance().registerMyGameChangeListener(onMyGameChangeListener);
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.uninstall_fragment;
  }

  @Override
  protected BaseRecycleViewAdapter getAdapter() {
    recycleAdapter = new UninstallManageAdapter();
    ((UninstallManageAdapter) recycleAdapter).setData(installedGames);
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
  protected List<GameModel> fetchHttpData(int start, int size) throws ExecutionException {
    return MyGameManager.getInstance().getMyInstalledGames();
  }

  @Override
  protected String getCacheKey() {
    return null;
  }

  @Override
  protected void onFetched(int start, int size, BaseFetcher.ResultList<GameModel> result) {
    installedGames.addAll(result.data);
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

  private void setItemListener() {
    recycleAdapter.setOnItemFocusChangeListener(new OnItemFocusChangeListener() {
      @Override
      public void onItemFocusChange(View view, int position, boolean hasFocus) {
        if (hasFocus) {
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
      if (holder.itemView instanceof UninstallItemView) {
        ((UninstallItemView) holder.itemView).focusButton();
      }
    }
  }

  private void initFocus() {
    FocusUtils.initRecycleViewFocus(recyclerView);
  }

  private void judgeEmptyImageNeedShow() {
    synchronized (installedGames) {
      if (CollectionUtils.isEmpty(installedGames)) {
        emptyView.setVisibility(View.VISIBLE);
      } else {
        emptyView.setVisibility(View.GONE);
      }
    }
  }
}
