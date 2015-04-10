package com.lemi.controller.lemigameassistance.focus.anim;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.focus.utils.FocusUtils;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ScaleAnimHelper {

  private static final float DEFAULT_SCALE_SIZE = 1.1F;
  private static final int DEFAULT_PARENT_INDEX = 0;
  private static final int DURATION = 250;

  private AnimStatus status = AnimStatus.NONE;
  private final byte[] statusLock = new byte[0];

  private Animation animLarger;
  private Animation animSmaller;

  /**
   * if this view need itself to bringToFront this value is 0;if need getParent() to bringToFront
   * this value is 1;if need getParent().getParent() to bringToFront this value is 2, then so on...
   */
  private int needBringToFrontParentTreeIndex;

  public ScaleAnimHelper(Context context, AttributeSet attrs) {
    TypedArray array = context.obtainStyledAttributes(attrs,
        R.styleable.InnerScaleImageView);
    needBringToFrontParentTreeIndex =
        array.getInteger(R.styleable.InnerScaleImageView_needBringToFrontParentTreeIndex,
            DEFAULT_PARENT_INDEX);
    TypedArray arrayScale = context.obtainStyledAttributes(attrs,
        R.styleable.ScaleView);
    float xScale = arrayScale.getFloat(R.styleable.ScaleView_scaleInX, DEFAULT_SCALE_SIZE);
    float yScale = arrayScale.getFloat(R.styleable.ScaleView_scaleInY, DEFAULT_SCALE_SIZE);

    animLarger = new ScaleAnimation(1.0f, xScale, 1.0f, yScale,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    animLarger.setDuration(DURATION);
    animLarger.setFillAfter(true);
    animSmaller = new ScaleAnimation(xScale, 1.0f, yScale, 1.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    animSmaller.setDuration(DURATION);
    animSmaller.setFillAfter(true);

    array.recycle();
    arrayScale.recycle();
  }

  public void setFocusListener(final View focusView) {
    if (focusView != null) {
      focusView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
          if (hasFocus) {
            if (animLarger != null) {
              if (FocusUtils.canFocusValid(focusView)) {
                focusView.startAnimation(animLarger);
                judgeViewTreeAndBringToFront(focusView, needBringToFrontParentTreeIndex);
              } else {
                synchronized (statusLock) {
                  status = AnimStatus.PENDING;
                }
              }
            }
          } else {
            if (animSmaller != null) {
              focusView.startAnimation(animSmaller);
            }
          }
        }
      });
    }
  }

  public void onLayout(View focusView) {
    synchronized (statusLock) {
      if (status == AnimStatus.PENDING && FocusUtils.canFocusValid(focusView)) {
        focusView.startAnimation(animLarger);
        judgeViewTreeAndBringToFront(focusView, needBringToFrontParentTreeIndex);
        status = AnimStatus.NONE;
      }
    }
  }

  public void judgeViewTreeAndBringToFront(View currentView, int parentIndex) {
    View parent = FocusUtils.getParent(currentView, parentIndex);
    if (parent != null) {
      parent.bringToFront();
      ViewParent parentView = parent.getParent();
      if (parentView instanceof View) {
        ((View) parentView).invalidate();
      }
    }

  }

}
