package com.lemi.controller.lemigameassistance.utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ViewPagerUtils {

  public static void changeScrollerSpeed(ViewPager viewPager) {
    try {
      Field mScroller;
      mScroller = ViewPager.class.getDeclaredField("mScroller");
      mScroller.setAccessible(true);
      Interpolator sInterpolator = new AccelerateDecelerateInterpolator();
      FixedScroller scroller = new FixedScroller(viewPager.getContext(),
          sInterpolator);
      mScroller.set(viewPager, scroller);
    } catch (NoSuchFieldException e) {

    } catch (IllegalArgumentException e) {

    } catch (IllegalAccessException e) {

    }
  }

  private static class FixedScroller extends Scroller {

    private int mDuration = 500;

    public FixedScroller(Context context) {
      super(context);
    }

    public FixedScroller(Context context, Interpolator interpolator) {
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

  }

}
