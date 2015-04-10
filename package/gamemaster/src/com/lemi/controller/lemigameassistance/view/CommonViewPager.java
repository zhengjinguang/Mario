package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;


/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CommonViewPager extends ViewPager {

  private Context context;
  private boolean isScrollEnabled = true;
  private OnPageChangeListener onPageChangelistener;

  public CommonViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  public CommonViewPager(Context context) {
    super(context);
    initView(context);
  }

  private void initView(Context context) {
    this.context = context;
    super.setOnPageChangeListener(new OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (onPageChangelistener != null) {
          onPageChangelistener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
      }

      @Override
      public void onPageSelected(int position) {
        if (onPageChangelistener != null) {
          onPageChangelistener.onPageSelected(position);
        }
      }

      @Override
      public void onPageScrollStateChanged(final int state) {
        if (onPageChangelistener != null) {
          onPageChangelistener.onPageScrollStateChanged(state);
        }
      }
    });
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    // handle ViewPage default pos is 0, onPageSelected onPageScrollStateChanged not called after
    // inflate
  }


  @Override
  public void setOnPageChangeListener(OnPageChangeListener listener) {
    this.onPageChangelistener = listener;
  }

  public boolean isScrollEnabled() {
    return isScrollEnabled;
  }

  public void setScrollEnabled(boolean isEnabled) {
    this.isScrollEnabled = isEnabled;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (!isScrollEnabled) {
      return false;
    }
    return super.onInterceptTouchEvent(ev);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (!isScrollEnabled) {
      return false;
    }
    return super.onTouchEvent(ev);
  }

  @Override
  protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
    if (v != this && v instanceof ViewPager) {
      return true;
    }
    return super.canScroll(v, checkV, dx, x, y);
  }
}
