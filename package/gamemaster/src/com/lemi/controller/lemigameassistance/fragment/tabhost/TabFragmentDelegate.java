package com.lemi.controller.lemigameassistance.fragment.tabhost;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

/**
 * This class is the delegate of a fragment that need to show on a ViewPager and
 * PagerSlidingTabStrip.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class TabFragmentDelegate {
  private PagerSlidingTabStrip.Tab tab;
  private Class<? extends Fragment> fragmentClaz;
  private Bundle args;

  /**
   * @param tab the tab taht will show on
   *          {@link com.astuetz.viewpager.extensions.PagerSlidingTabStrip}
   * @param fragmentClaz the class of the fragment
   * @param args the args that will passed to the fragment
   */
  public TabFragmentDelegate(PagerSlidingTabStrip.Tab tab,
      Class<? extends Fragment> fragmentClaz, Bundle args) {
    this.tab = tab;
    this.fragmentClaz = fragmentClaz;
    this.args = args;
  }

  /**
   * Get the tab that will show on {@link com.astuetz.viewpager.extensions.PagerSlidingTabStrip}.
   * 
   * @return the tab
   */
  public PagerSlidingTabStrip.Tab getTab() {
    return tab;
  }

  /**
   * Get the class of the fragment.
   * 
   * @return the class
   */
  public Class<? extends Fragment> getFragmentClaz() {
    return fragmentClaz;
  }

  /**
   * Get the args that will passed to the fragment.
   * 
   * @return the args
   */
  public Bundle getArgs() {
    return args;
  }

  /**
   * Set the args that will passed to the fragment.
   * 
   * @return the args
   */
  public void setArgs(Bundle bundle) {
    args = bundle;
  }
}
