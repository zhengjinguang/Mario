package com.lemi.controller.lemigameassistance.helper;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ViewPagerHelper {

  public static void changeViewPageScroller(Context context, ViewPager mViewPager) {
    try {
      Field mField = ViewPager.class.getDeclaredField("mScroller");
      mField.setAccessible(true);
      FixedSpeedScroller scroller;
      scroller = new FixedSpeedScroller(context, new AccelerateDecelerateInterpolator());
      mField.set(mViewPager, scroller);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  static class FixedSpeedScroller extends Scroller {
    private int mDuration = 800;

    public FixedSpeedScroller(Context context) {
      super(context);
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator) {
      super(context, interpolator);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy,
        int duration) {
      // Ignore received duration, use fixed one instead
      super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
      // Ignore received duration, use fixed one instead
      super.startScroll(startX, startY, dx, dy, mDuration);
    }

    public void setmDuration(int time) {
      mDuration = time;
    }

    public int getmDuration() {
      return mDuration;
    }

  };

}
