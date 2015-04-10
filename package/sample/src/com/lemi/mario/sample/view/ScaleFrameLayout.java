package com.lemi.mario.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import com.lemi.mario.sample.R;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ScaleFrameLayout extends FrameLayout {

  public ScaleFrameLayout(Context context) {
    super(context);
  }

  public ScaleFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    final Animation anim_larger;
    final Animation anim_smaller;
    TypedArray array = context.obtainStyledAttributes(attrs,
        R.styleable.ScaleView);// TypedArray是一个数组容器
    float xScale = array.getFloat(R.styleable.ScaleView_xScale, 1.1f);
    float yScale = array.getFloat(R.styleable.ScaleView_yScale, 1.1f);
    /** 设置缩放动画 */
    anim_larger = new ScaleAnimation(1.0f, xScale, 1.0f, yScale,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    anim_larger.setDuration(250);// 设置动画持续时间
    anim_larger.setFillAfter(true);

    anim_smaller = new ScaleAnimation(xScale, 1.0f, yScale, 1.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    anim_smaller.setDuration(250);
    anim_smaller.setFillAfter(true);

    this.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean focus) {
        if (focus) {
          startAnimation(anim_larger);
          view.bringToFront();
        } else {
          startAnimation(anim_smaller);
        }
      }
    });

    array.recycle();
  }
}
