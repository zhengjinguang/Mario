package com.lemi.controller.lemigameassistance.fragment.base;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.fetcher.BaseFetcher;
import com.lemi.controller.lemigameassistance.fetcher.FetchHelper;
import com.lemi.controller.lemigameassistance.focus.listener.OnEdgeListener;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.controller.lemigameassistance.view.TipsView;
import com.lemi.mario.base.utils.MainThreadPostUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * AsyncLoadFragment that can handle network load/load more/pre load and show network tips.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class NetworkAsyncListFragment<T> extends BaseRecycleFragment {

  private static final int DEFAULT_INDEX = 0;

  private final byte[] loadMoreLock = new byte[0];

  protected enum LoadNetworkTimes {
    ONCE, MORE
  }

  private enum LoadMoreType {
    PRE_LOAD, LOAD_MORE, NONE
  }

  protected TipsView tipsView;
  protected ProgressBar progressBar;

  private View focusView;

  private LoadNetworkTimes loadNetworkTimes;
  private LoadMoreType loadMoreType = LoadMoreType.NONE;

  /**
   * fetcher component
   */
  private FetchHelper<T> fetchHelper;
  private BaseFetcher<T> fetcher = new BaseFetcher<T>() {
    @Override
    protected List<T> fetchHttpData(int start, int size) throws ExecutionException {
      return NetworkAsyncListFragment.this.fetchHttpData(start, size);
    }

    @Override
    protected String getCacheKey() {
      return NetworkAsyncListFragment.this.getCacheKey();
    }
  };
  private BaseFetcher.Callback<T> fetchCallback =
      new BaseFetcher.Callback<T>() {
        @Override
        public void onFetched(int start, int size, BaseFetcher.ResultList<T> result) {
          if (start == 0) {
            tipsView.changeTipsStatus(TipsView.TipsStatus.GONE);
          } else {
            hideProgressBar();
            resetLoadMoreStatus();
          }
          NetworkAsyncListFragment.this.onFetched(start, size, result);
        }

        @Override
        public void onFailed(int start, ExecutionException e) {
          if (start == 0) {
            tipsView.changeTipsStatus(TipsView.TipsStatus.FAILED);
          } else {
            hideProgressBar();
            toastWhenLoadMoreFailed();
            resetLoadMoreStatus();
          }
          NetworkAsyncListFragment.this.onFailed(start, e);
        }
      };

  /**
   * async task
   */
  private NetworkAsyncTask networkAsyncTask;

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    super.onInflated(contentView, savedInstanceState);
    initLoadMethod();
    initTipsView(contentView);
    initProgressBar(contentView);
    setEdgeListener();
  }

  private void initTipsView(View contentView) {
    tipsView = (TipsView) contentView.findViewById(R.id.tips_view);
    tipsView.setOnRefreshListener(mOnRefreshListener);
  }

  private void initProgressBar(View contentView) {
    if (loadNetworkTimes == LoadNetworkTimes.MORE) {
      progressBar = (ProgressBar) contentView.findViewById(R.id.load_more_progress);
    }
  }

  private void initLoadMethod() {
    loadNetworkTimes = getLoadNetworkTimes();
    if (loadNetworkTimes == LoadNetworkTimes.MORE) {
      fetchHelper = new FetchHelper<T>(fetcher, fetchCallback);
    } else {
      // async task init at execute
      networkAsyncTask = null;
    }
  }

  private void setEdgeListener() {
    setOnEdgeListener(new OnEdgeListener() {
      @Override
      public boolean onTopEdgeRequest() {
        return false;
      }

      @Override
      public boolean onBottomEdgeRequest() {
        checkAndLoadMore();
        return false;
      }
    });
  }


  private void checkAndLoadMore() {
    if (checkNeedLoadMore()) {
      loadMore();
    }
  }

  private void checkAndPreLoad() {
    if (checkNeedPreLoad()) {
      preLoad();
    }
  }

  private void loadMore() {
    if (loadNetworkTimes == LoadNetworkTimes.MORE) {
      showProgressBar();
      setLoadMoreStatus(LoadMoreType.LOAD_MORE);
      fetchHelper.fetchMore();
    }
  }

  private void preLoad() {
    if (loadNetworkTimes == LoadNetworkTimes.MORE) {
      setLoadMoreStatus(LoadMoreType.PRE_LOAD);
      fetchHelper.fetchMore();
    }
  }

  private void setLoadMoreStatus(LoadMoreType type) {
    synchronized (loadMoreLock) {
      loadMoreType = type;
    }
  }

  private void resetLoadMoreStatus() {
    synchronized (loadMoreLock) {
      loadMoreType = LoadMoreType.NONE;
    }
  }

  private void showProgressBar() {
    synchronized (loadMoreLock) {
      focusView = recyclerView.findFocus();
      progressBar.setVisibility(View.VISIBLE);
      progressBar.requestFocus();
    }
  }

  private void hideProgressBar() {
    synchronized (loadMoreLock) {
      if (loadMoreType == LoadMoreType.LOAD_MORE) {
        if (focusView != null) {
          focusView.requestFocus();
        }
      }
      progressBar.setVisibility(View.GONE);
    }
  }

  private void toastWhenLoadMoreFailed() {
    synchronized (loadMoreLock) {
      if (loadMoreType == LoadMoreType.LOAD_MORE) {
        toastFailed();
      }
    }
  }


  @Override
  protected void onPrepareLoading() {
    tipsView.changeTipsStatus(TipsView.TipsStatus.LOADING);
  }

  @Override
  protected void onStartLoading() {
    if (loadNetworkTimes == LoadNetworkTimes.MORE) {
      fetchHelper.fetch();
    } else {
      if (networkAsyncTask != null) {
        DataUtils.stopAsyncTask(networkAsyncTask);
      }
      networkAsyncTask = new NetworkAsyncTask();
      DataUtils.runAsyncTask(networkAsyncTask);
    }
  }


  protected boolean checkNeedLoadMore() {
    if (loadNetworkTimes == LoadNetworkTimes.MORE) {
      return true;
    } else {
      return false;
    }
  }

  protected boolean checkNeedPreLoad() {
    return false;
  }

  @Override
  protected void onScrollStateIdle() {
    super.onScrollStateIdle();
    checkAndPreLoad();
  }

  protected void toastFailed() {
    MainThreadPostUtils.toast(R.string.category_detail_load_more);
  }

  /**
   * Fetches data from network.
   *
   * @param start the request start index
   * @param size if size < 0 means fetch all
   * @return return result list, can be null
   */
  protected abstract List<T> fetchHttpData(int start, int size) throws ExecutionException;


  /**
   * if only load data once return null.
   * this key only used for load more
   *
   */
  protected abstract String getCacheKey();

  /**
   * Gets called when result is fetched.
   *
   * @param start start
   * @param size request size
   * @param result result list, at lease an empty list, never null
   */
  protected abstract void onFetched(int start, int size, BaseFetcher.ResultList<T> result);

  protected abstract void onFailed(int start, ExecutionException e);

  /**
   * need load more when at page last
   */
  protected abstract LoadNetworkTimes getLoadNetworkTimes();



  private final class NetworkAsyncTask extends AsyncTask<Void, Void, List<T>> {

    @Override
    protected List<T> doInBackground(Void... voids) {
      List<T> model = null;
      try {
        model = NetworkAsyncListFragment.this.fetchHttpData(DEFAULT_INDEX, DEFAULT_INDEX);
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
      return model;
    }

    @Override
    protected void onPostExecute(List<T> models) {
      super.onPostExecute(models);
      if (models != null) {
        NetworkAsyncListFragment.this.onFetched(DEFAULT_INDEX, DEFAULT_INDEX,
            new BaseFetcher.ResultList<T>(models));
        tipsView.changeTipsStatus(TipsView.TipsStatus.GONE);
      } else {
        NetworkAsyncListFragment.this.onFailed(DEFAULT_INDEX,
            new ExecutionException(new IllegalStateException("data size is null")));
        tipsView.changeTipsStatus(TipsView.TipsStatus.FAILED);
      }
    }
  }

  TipsView.OnRefreshListener mOnRefreshListener = new TipsView.OnRefreshListener() {
    @Override
    public void onRefresh() {
      NetworkAsyncListFragment.this.onStartLoading();
    }
  };

}
