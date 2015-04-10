package com.lemi.controller.lemigameassistance.fragment.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * All fragment need to async loading data should override this. This class will make sure that
 * if you put your fragment into a ViewPager, your fragment will load data after the scrolling of
 * the ViewPager is finished rather than still scrolling, so the UI will not lag when scrolling.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class AsyncLoadFragment extends BaseFragment {
  private boolean allowLoading = true;
  private boolean pendingToLoad = false;

  @Override
  public final void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (!needToLoadData()) {
      return;
    }
    onPrepareLoading();
    // We need to post it to make sure scroll state of ViewPager is changed before requestLoad,
    // so we can check the param allowLoading
    contentView.post(new Runnable() {
      @Override
      public void run() {
        // Because we post this, so we need to check if this fragment is not attached
        if (!isAdded()) {
          return;
        }
        if (allowLoading) {
          onStartLoading();
        } else {
          pendingToLoad = true;
        }
      }
    });
  }

  /**
   * Called before loading, subclass should override this if sometimes it needn't to load
   * data. For example, if the network is disconnected and you need network to load data,
   * then you should show network disconnect tip here and return false.
   *
   * @return true if need to load, false otherwise
   */
  protected boolean needToLoadData() {
    return isInflated;
  }

  /**
   * Called before loading, you should show loading tips here.
   * <p>
   * <b>Do not launch to load data here.</b>
   * </p>
   */
  protected abstract void onPrepareLoading();

  /**
   * Called after onPrepareLoading, you should do actual loading here, like starting the fetcher.
   */
  protected abstract void onStartLoading();

  /**
   * This will request loading data, and needToLoadData will not being checked.
   * Normally you don't need to call this, unless you need to manually refresh your UI.
   */
  protected final void requestLoad() {
    if (!needToLoadData()) {
      return;
    }
    onPrepareLoading();
    if (allowLoading) {
      onStartLoading();
    } else {
      pendingToLoad = true;
    }
  }

  /**
   * This is used to notify the fragment that if it should load data.
   *
   * @param allowLoading whether the fragment should launch loading
   */
  public final void setAllowLoading(boolean allowLoading) {
    this.allowLoading = allowLoading;
    if (allowLoading && pendingToLoad) {
      pendingToLoad = false;
      requestLoad();
    }
  }

}
