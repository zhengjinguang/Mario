package com.lemi.controller.lemigameassistance.fragment.tabhost;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip.Tab;
import com.lemi.controller.lemigameassistance.config.TabConstants.TabId;
import com.lemi.controller.lemigameassistance.fragment.base.AsyncLoadFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * FragmentPagerAdapter for GameMaster, with ability to control the loading of fragment by the
 * scroll
 * state of ViewPager.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class TabFragmentPagerAdapter extends PagerAdapter implements
    PagerSlidingTabStrip.TabProvider {
  private static final String TAG = TabFragmentPagerAdapter.class.getSimpleName();
  private static final String KEY_STATE = "state";
  private static final String KEY_PREFIX_FRAGMENT = "f";
  private final Context context;
  private final FragmentManager fragmentManager;
  private final List<TabFragmentDelegate> tabFragmentDelegates =
      new ArrayList<TabFragmentDelegate>();
  private FragmentTransaction curTransaction = null;
  private SparseArray<Fragment> fragments = new SparseArray<Fragment>();
  private SparseArray<Fragment.SavedState> savedStates = new SparseArray<Fragment.SavedState>();
  private SparseArray<Bundle> fragmentArgs = new SparseArray<Bundle>();
  private Fragment currentPrimaryItem = null;
  private boolean allowLoading = true;

  /**
   * @param context context
   * @param fm FragmentManager
   */
  public TabFragmentPagerAdapter(Context context, FragmentManager fm) {
    this.fragmentManager = fm;
    this.context = context;
  }

  /**
   * Get the Fragment instance at the position if exist. May be null.
   * 
   * @param position position
   * @return the fragment if it's loaded in memory, null otherwise.
   */
  public Fragment getFragment(int position) {
    return fragments.get(position);
  }

  /**
   * @param delegates the list of {@link TabFragmentDelegate}
   */
  public void setFragments(List<TabFragmentDelegate> delegates) {
    this.tabFragmentDelegates.clear();
    appendFragments(delegates);
  }

  /**
   * Add a list of fragment at the end of tabs.
   * 
   * @param delegates the new tabs
   */
  public void appendFragments(List<TabFragmentDelegate> delegates) {
    if (delegates == null) {
      throw new RuntimeException("delegates should not be null for setFragments()");
    }
    int oldSize = tabFragmentDelegates.size();
    int newSize = oldSize + delegates.size();
    for (int i = oldSize; i < newSize; ++i) {
      TabFragmentDelegate delegate = delegates.get(i - oldSize);
      fragmentArgs.put(i, delegate.getArgs());
    }
    tabFragmentDelegates.addAll(delegates);
    notifyDataSetChanged();

  }

  /**
   * Add a fragment at the end of tabs.
   * 
   * @param delegate the new tab
   */
  public void appendFragment(TabFragmentDelegate delegate) {
    List<TabFragmentDelegate> delegateList = new ArrayList<TabFragmentDelegate>();
    delegateList.add(delegate);
    appendFragments(delegateList);
  }

  /**
   * Set the bundle args that will pass to fragment. If there is an existing bundle,
   * the new bundle will be merged into it.
   * 
   * @param position the position of the fragment
   * @param bundle the args
   */
  public void setFragmentArgs(int position, Bundle bundle) {
    if (bundle == null) {
      return;
    }
    Bundle args = fragmentArgs.get(position);
    if (args == null) {
      args = bundle;
    } else {
      args.putAll(bundle);
    }
    fragmentArgs.put(position, args);
    Fragment fragment = getFragment(position);
    if (fragment instanceof MessageReceiver) {
      ((MessageReceiver) fragment).onMessageReceived(args);
    }
  }

  @Override
  public int getCount() {
    return tabFragmentDelegates.size();
  }

  /**
   * Create the Fragment associated with a specified position.
   * 
   * @param position the position of the fragment
   * @return the new fragment
   */
  private Fragment newItem(int position) {
    Fragment fragment =
        Fragment.instantiate(context,
            tabFragmentDelegates.get(position).getFragmentClaz().getName(),
            fragmentArgs.get(position));
    if (fragment instanceof AsyncLoadFragment) {
      ((AsyncLoadFragment) fragment).setAllowLoading(allowLoading);
    }
    return fragment;
  }

  @Override
  public Fragment instantiateItem(ViewGroup container, int position) {
    // If we already have this item instantiated, there is nothing
    // to do. This can happen when we are restoring the entire pager
    // from its saved state, where the fragment manager has already
    // taken care of restoring the fragments we previously had instantiated.
    Fragment fragment = fragments.get(position);
    if (fragment != null) {
      return fragment;
    }

    if (curTransaction == null) {
      curTransaction = fragmentManager.beginTransaction();
    }

    fragment = newItem(position);
    Fragment.SavedState savedState = savedStates.get(position);
    if (savedState != null) {
      fragment.setInitialSavedState(savedState);
    }
    fragment.setMenuVisibility(false);
    fragment.setUserVisibleHint(false);
    fragments.put(position, fragment);
    curTransaction.add(container.getId(), fragment);

    return fragment;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    Fragment fragment = (Fragment) object;

    if (curTransaction == null) {
      curTransaction = fragmentManager.beginTransaction();
    }
    savedStates.put(position, fragmentManager.saveFragmentInstanceState(fragment));
    fragments.remove(position);

    curTransaction.remove(fragment);
  }

  @Override
  public void setPrimaryItem(ViewGroup container, int position, Object object) {
    Fragment fragment = (Fragment) object;
    if (fragment != currentPrimaryItem) {
      if (currentPrimaryItem != null) {
        currentPrimaryItem.setMenuVisibility(false);
        currentPrimaryItem.setUserVisibleHint(false);
      }
      if (fragment != null) {
        fragment.setMenuVisibility(true);
        fragment.setUserVisibleHint(true);
      }
      currentPrimaryItem = fragment;
    }
  }

  public Fragment getCurrentFragment() {
    return currentPrimaryItem;
  }

  @Override
  public void startUpdate(ViewGroup container) {}

  @Override
  public void finishUpdate(ViewGroup container) {
    if (curTransaction != null) {
      curTransaction.commitAllowingStateLoss();
      curTransaction = null;
      fragmentManager.executePendingTransactions();
    }
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return ((Fragment) object).getView() == view;
  }

  @Override
  public Parcelable saveState() {
    Bundle state = null;
    if (savedStates.size() > 0) {
      state = new Bundle();
      state.putSparseParcelableArray(KEY_STATE, savedStates);
    }
    for (int i = 0; i < fragments.size(); i++) {
      Fragment f = fragments.get(fragments.keyAt(i));
      if (f != null) {
        if (state == null) {
          state = new Bundle();
        }
        String key = KEY_PREFIX_FRAGMENT + fragments.keyAt(i);
        fragmentManager.putFragment(state, key, f);
      }
    }
    return state;
  }

  @Override
  public void restoreState(Parcelable state, ClassLoader loader) {
    if (state != null) {
      Bundle bundle = (Bundle) state;
      bundle.setClassLoader(loader);
      SparseArray<Fragment.SavedState> savedStates = bundle.getSparseParcelableArray("states");
      this.savedStates.clear();
      fragments.clear();
      if (savedStates != null) {
        this.savedStates = savedStates;
      }
      Iterable<String> keys = bundle.keySet();
      for (String key : keys) {
        if (key.startsWith(KEY_PREFIX_FRAGMENT)) {
          int position = Integer.parseInt(key.substring(1));
          Fragment f = fragmentManager.getFragment(bundle, key);
          if (f != null) {
            f.setMenuVisibility(false);
            fragments.put(position, f);
          }
        }
      }
    }
  }

  @Override
  public PagerSlidingTabStrip.Tab getTab(int position) {
    if (tabFragmentDelegates == null || tabFragmentDelegates.isEmpty()) {
      return null;
    }
    if (position < 0 || position >= tabFragmentDelegates.size()) {
      return null;
    }
    return tabFragmentDelegates.get(position).getTab();
  }

  @Override
  public Tab getTab(String id) {
    if (tabFragmentDelegates == null || TextUtils.isEmpty(id)) {
      return null;
    }
    for (TabFragmentDelegate d : tabFragmentDelegates) {
      if (d != null && d.getTab() != null && id.equals(d.getTab().getId())) {
        return d.getTab();
      }
    }
    return null;
  }

  @Override
  public int getTabPositionById(String id) {
    if (tabFragmentDelegates == null || TextUtils.isEmpty(id)) {
      return -1;
    }
    for (int i = 0; i < tabFragmentDelegates.size(); i++) {
      TabFragmentDelegate d = tabFragmentDelegates.get(i);
      if (d != null && d.getTab() != null && id.equals(d.getTab().getId())) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public String getTabIdByPosition(int position) {
    Tab tab = getTab(position);
    return tab == null ? TabId.NONE : tab.getId() == null ? TabId.NONE : tab.getId();
  }

  /**
   * Should only be called by the holder of ViewPager when the scroll state of the ViewPager
   * changed.
   *
   * @param state the new scroll State
   * @see android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING
   * @see android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING
   * @see android.support.v4.view.ViewPager.SCROLL_STATE_IDLE
   */
  public final void notifyScrollStateChanged(int state) {
    boolean allowLoading = true;
    switch (state) {
      case ViewPager.SCROLL_STATE_DRAGGING:
      case ViewPager.SCROLL_STATE_SETTLING:
        allowLoading = false;
        break;
      case ViewPager.SCROLL_STATE_IDLE:
        allowLoading = true;
      default:
        break;
    }
    setAllowLoading(allowLoading);
  }

  public final void setAllowLoading(boolean allowLoading) {
    if (this.allowLoading != allowLoading) {
      this.allowLoading = allowLoading;
      for (int i = 0; i < fragments.size(); ++i) {
        Fragment fragment = fragments.valueAt(i);
        if (fragment instanceof AsyncLoadFragment) {
          ((AsyncLoadFragment) fragment).setAllowLoading(allowLoading);
        }
      }
    }
  }
}
