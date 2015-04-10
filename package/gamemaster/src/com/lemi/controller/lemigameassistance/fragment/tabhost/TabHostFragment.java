package com.lemi.controller.lemigameassistance.fragment.tabhost;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.config.TabConstants.TabId;
import com.lemi.controller.lemigameassistance.fragment.base.BaseFragment;
import com.lemi.controller.lemigameassistance.helper.ViewPagerHelper;
import com.lemi.controller.lemigameassistance.view.CommonViewPager;
import com.lemi.mario.base.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that is to be the tab host of several fragments.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class TabHostFragment extends BaseFragment {

  public static final String LAST_SELECTED_ITEM_POS = "last_selected_item_pos";

  private PagerSlidingTabStrip tabStrip;
  protected CommonViewPager viewPager;
  protected TabFragmentPagerAdapter pagerAdapter;
  private int currentFragmentIndex;
  private int lastFragmentIndex;
  private ViewPager.OnPageChangeListener onPageChangeListener =
      new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
          if (delegateOnPageChangeListener != null) {
            delegateOnPageChangeListener.onPageScrolled(position, positionOffset,
                positionOffsetPixels);
          }
        }

        @Override
        public void onPageSelected(int position) {
          currentFragmentIndex = position;
          if (delegateOnPageChangeListener != null) {
            delegateOnPageChangeListener.onPageSelected(position);
          }
          lastFragmentIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
          pagerAdapter.notifyScrollStateChanged(state);
          if (delegateOnPageChangeListener != null) {
            delegateOnPageChangeListener.onPageScrollStateChanged(state);
          }
        }
      };
  private ViewPager.OnPageChangeListener delegateOnPageChangeListener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  protected int getLayoutResId() {
    return R.layout.common_tab_layout;
  }

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    tabStrip = (PagerSlidingTabStrip) contentView.findViewById(R.id.tabs);
    viewPager = (CommonViewPager) contentView.findViewById(R.id.common_view_pager);
    pagerAdapter = new TabFragmentPagerAdapter(getActivity(), getChildFragmentManager());
    final List<TabFragmentDelegate> fragmentDelegates = getTabFragmentDelegates();
    viewPager.setAdapter(pagerAdapter);
    if (getUseSmoothScroller()) {
      ViewPagerHelper.changeViewPageScroller(getActivity(), viewPager);
    }
    if (!CollectionUtils.isEmpty(fragmentDelegates)) {
      pagerAdapter.setFragments(getTabFragmentDelegates());
      pagerAdapter.notifyDataSetChanged();
      currentFragmentIndex = getInitTabIndex();
      lastFragmentIndex = currentFragmentIndex;
      if (getArguments() != null && getArguments().containsKey(LAST_SELECTED_ITEM_POS)) {
        viewPager.setCurrentItem(getArguments().getInt(LAST_SELECTED_ITEM_POS), false);
      } else {
        viewPager.setCurrentItem(currentFragmentIndex);
      }
    }
    tabStrip.setViewPager(viewPager);
    tabStrip.setOnPageChangeListener(onPageChangeListener);
  }

  protected void setAllowLoading(boolean allowLoading) {
    pagerAdapter.setAllowLoading(allowLoading);
  }

  public void setScrollEnabled(boolean isEnabled) {
    viewPager.setScrollEnabled(isEnabled);
    tabStrip.setAllTabEnabled(isEnabled);
  }

  protected void setOffScreenPageLimit(int limit) {
    viewPager.setOffscreenPageLimit(limit);
  }

  public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
    this.delegateOnPageChangeListener = listener;
  }

  private int getInitTabIndex() {
    if (getInitTabId() != null && pagerAdapter != null) {
      int position = getTabPosition(getInitTabId());
      if (position >= 0) {
        return position;
      }
    }
    return 0;
  }

  public int getCurrentFragmentIndex() {
    return currentFragmentIndex;
  }

  public int getLastFragmentIndex() {
    return lastFragmentIndex;
  }

  /**
   * Get the tab id at the specified position
   * 
   * @param position
   * @return tabId
   */
  protected String getTabId(int position) {
    return pagerAdapter.getTabIdByPosition(position);
  }

  /**
   * Get the tab position by the specified tab id.
   * 
   * @param tabId
   * @return position
   */
  protected int getTabPosition(String tabId) {
    return pagerAdapter.getTabPositionById(tabId);
  }

  /**
   * Get the initial selected tab, sub class should override this to change the default position,
   * this method will call in {@link TabHostFragment#onActivityCreated(android.os.Bundle)}.
   * 
   * @return the default selected tab index
   */
  protected String getInitTabId() {
    return TabId.NONE;
  }

  /**
   * Move the specific tab, it should only be called AFTER the initialization of this fragment,
   * if you just want to set the default selected page,
   * just override {@link TabHostFragment#getInitTabId()}.
   * 
   * @param position the position of the fragment
   * @param args the arguments need to be passed to this fragment
   */
  public void selectTab(int position, Bundle args) {
    // TODO don't allow to call this function before onActivityCreated() or add NPE change on
    // pagerAdapter.
    pagerAdapter.setFragmentArgs(position, args);
    viewPager.setCurrentItem(position, false);
  }

  public void selectTab(String id, Bundle args) {
    int index = pagerAdapter.getTabPositionById(id);
    if (index >= 0) {
      selectTab(pagerAdapter.getTabPositionById(id), args);
    }
  }

  /**
   * Just switch page of the viewpager. Do nothing with the fragments.
   * see more {@link TabFragmentPagerAdapter#setFragmentArgs(int, android.os.Bundle)}
   * 
   * @param position, index of viewpager
   */
  public void selectTabWithoutNotify(int position) {
    viewPager.setCurrentItem(position, false);
  }

  /**
   * Just switch page of the viewpager.
   * 
   * @param id the tabId
   */
  public void selectTabWithoutNotify(String id) {
    selectTabWithoutNotify(pagerAdapter.getTabPositionById(id));
  }

  /**
   * Set the arguments for fragment in selected position
   * 
   * @param position the position of the fragment
   * @param args the arguments need to be passed to this fragment
   */
  public void setTabArgs(int position, Bundle args) {
    pagerAdapter.setFragmentArgs(position, args);
  }

  public void setTabArgs(String tabId, Bundle args) {
    int index = pagerAdapter.getTabPositionById(tabId);
    if (index >= 0) {
      pagerAdapter.setFragmentArgs(index, args);
    }
  }

  public int getCurrentItem() {
    if (viewPager != null) {
      return viewPager.getCurrentItem();
    }
    return getInitTabIndex();
  }

  /**
   * Get the entire view of the fragment.
   * 
   * @return the content view
   */
  public View getContentView() {
    return contentView;
  }

  /**
   * Get the tab strip
   * 
   * @return the tab strip
   */
  public PagerSlidingTabStrip getTabStrip() {
    return tabStrip;
  }

  /**
   * Get the tabs that need to be shown in ViewPager and PagerSlidingTabStrip.
   * 
   * @return the list of {@link TabFragmentDelegate}
   */
  public abstract List<TabFragmentDelegate> getTabFragmentDelegates();


  public abstract boolean getUseSmoothScroller();


  public Fragment getFragment(int position) {
    return pagerAdapter.getFragment(position);
  }

  public Fragment getCurrentFragment() {
    return getFragment(getCurrentItem());
  }

  public List<Fragment> getAliveFragments() {
    final List<Fragment> fragments = new ArrayList<Fragment>();
    final int currentIndex = viewPager.getCurrentItem();
    fragments.add(getFragment(currentIndex));
    for (int i = 1; i <= viewPager.getOffscreenPageLimit(); i++) {
      if (currentIndex + i < pagerAdapter.getCount()) {
        fragments.add(getFragment(currentIndex + i));
      }
      if (currentIndex - i >= 0) {
        fragments.add(getFragment(currentIndex - i));
      }
    }
    return fragments;
  }

  public boolean isDefaultFragment(Fragment f) {
    return pagerAdapter.getFragment(getInitTabIndex()) == f;
  }

  public void appendFragment(List<TabFragmentDelegate> delegates) {
    pagerAdapter.appendFragments(delegates);
    tabStrip.notifyDataSetChanged();
  }

  public void setFragments(List<TabFragmentDelegate> delegates) {
    pagerAdapter.setFragments(delegates);
    tabStrip.notifyDataSetChanged();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putInt(LAST_SELECTED_ITEM_POS, getCurrentItem());
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onViewStateRestored(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      int lastItemPos = savedInstanceState.getInt(LAST_SELECTED_ITEM_POS, -1);
      if (lastItemPos != -1) {
        selectTab(lastItemPos, savedInstanceState);
      }
    }
    super.onViewStateRestored(savedInstanceState);
  }
}
