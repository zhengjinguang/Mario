package com.lemi.mario.widget.helper.fragment.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class BaseFragment extends Fragment {

  protected View contentView;
  protected boolean isInflated;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    if (contentView == null) {
      contentView = inflater.inflate(getLayoutResId(), container, false);
    } else {
      ViewGroup parent = (ViewGroup) contentView.getParent();
      if (parent != null) {
        parent.removeView(contentView);
      }
    }
    return contentView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (contentView != null) {
      onInflated(contentView, savedInstanceState);
      isInflated = true;
    }
  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return false;
  }

  /**
   * Called when the Fragment is inflated
   *
   * @param contentView
   * @param savedInstanceState
   */
  protected abstract void onInflated(View contentView, Bundle savedInstanceState);

  /**
   * @return the layout resource id of the fragment content
   */
  protected abstract int getLayoutResId();


  /**
   * Get the root view of the fragment.
   *
   * @deprecated use {@link android.support.v4.app.Fragment#getView()} instead,
   *             but need to check that getView will return null after onDetached, so be careful.
   * @return the root view
   */
  public View getContentView() {
    return contentView;
  }
}
