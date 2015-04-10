package com.lemi.controller.lemigameassistance.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.config.TabConstants.TabId;
import com.lemi.controller.lemigameassistance.focus.view.TabFragment;
import com.lemi.controller.lemigameassistance.fragment.base.BaseFragment;
import com.lemi.controller.lemigameassistance.fragment.tabhost.TabFragmentDelegate;
import com.lemi.controller.lemigameassistance.fragment.tabhost.TabHostFragment;
import com.lemi.controller.lemigameassistance.view.TaggedTab;
import com.lemi.mario.base.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ExploreTabHostFragment extends TabHostFragment {

  @Override
  protected String getInitTabId() {
    return TabId.RECOMMEND;
  }

  @Override
  public List<TabFragmentDelegate> getTabFragmentDelegates() {

    List<TabFragmentDelegate> tabFragments = new ArrayList<TabFragmentDelegate>();

    tabFragments.add(new TabFragmentDelegate(new TaggedTab(TabId.SUBJECT, StringUtil
        .getString(R.string.explore_tab_subject)),
        SubjectFragment.class, null));
    tabFragments.add(new TabFragmentDelegate(new TaggedTab(TabId.RECOMMEND, StringUtil
        .getString(R.string.explore_tab_recommend)),
        RecommendFragment.class, null));
    tabFragments.add(new TabFragmentDelegate(new TaggedTab(TabId.CATEGORY, StringUtil
        .getString(R.string.explore_tab_category)),
        CategoryFragment.class, null));
    tabFragments.add(new TabFragmentDelegate(new TaggedTab(TabId.SETTINGS, StringUtil
        .getString(R.string.explore_tab_settings)),
        SettingsFragment.class, null));

    return tabFragments;
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.explore_tab_layout;
  }

  @Override
  public boolean getUseSmoothScroller() {
    return true;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

      @Override
      public void onPageSelected(int position) {
        setFocusWhenPageScroll(position);
      }

      @Override
      public void onPageScrollStateChanged(int state) {}
    });

  }

  public boolean onTabHostKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_LEFT:
        return handleLeftToFocusTab();
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        return handleRightToFocusTab();
      case KeyEvent.KEYCODE_DPAD_UP:
        return handleUpToFocusTab();
      case KeyEvent.KEYCODE_DPAD_DOWN:
        return handleDownToFocusFragment();
    }
    return false;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_LEFT:
      case KeyEvent.KEYCODE_DPAD_RIGHT:
      case KeyEvent.KEYCODE_DPAD_UP:
      case KeyEvent.KEYCODE_DPAD_DOWN:
        return deliverToCurrentFragment(keyCode, event);
    }
    return super.onKeyDown(keyCode, event);
  }

  private boolean deliverToCurrentFragment(int keyCode, KeyEvent event) {
    Fragment fragment = getCurrentFragment();
    if (fragment instanceof BaseFragment) {
      return ((BaseFragment) fragment).onKeyDown(keyCode, event);
    }
    return false;
  }

  private void setFocusWhenPageScroll(int position) {
    if (getLastFragmentIndex() == position) {
      return;
    }
    if (viewPager != null && viewPager.findFocus() != null) {

      if (position > getLastFragmentIndex()) {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof TabFragment) {
          ((TabFragment) fragment).requestLeftFocus();
        }
      } else {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof TabFragment) {
          ((TabFragment) fragment).requestRightFocus();
        }
      }
    }
  }

  private boolean handleUpToFocusTab() {
    if (viewPager != null && viewPager.findFocus() != null) {
      Fragment fragment = getCurrentFragment();
      if (fragment instanceof TabFragment) {
        if (((TabFragment) fragment).isOnTop(viewPager.findFocus())) {
          getTabStrip().requestTabFocus(getCurrentItem());
          return true;
        }
      }
    }
    return false;
  }

  private boolean handleDownToFocusFragment() {
    if (viewPager == null || viewPager.findFocus() == null) {
      Fragment fragment = getCurrentFragment();
      if (fragment instanceof TabFragment) {
        ((TabFragment) fragment).requestDownFocus();
      }
      return true;
    }
    return false;
  }

  private boolean handleLeftToFocusTab() {
    if (getCurrentItem() == 0) {
      return true;
    }
    return false;
  }

  private boolean handleRightToFocusTab() {
    if (getCurrentItem() == pagerAdapter.getCount() - 1) {
      return true;
    }
    return false;
  }

}
