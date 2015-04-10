package com.lemi.mario.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;

import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.sample.R;


public class ScaleImageButton extends ImageButton {

  public ScaleImageButton(Context context) {
    this(context, null);
  }

  public ScaleImageButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    final Animation anim_larger;
    final Animation anim_smaller;
    final Animation anim_larger_parent;
    final Animation anim_smaller_parent;
    TypedArray array = context.obtainStyledAttributes(attrs,
        R.styleable.ScaleView);// TypedArray是一个数组容器
    float xScale = array.getFloat(R.styleable.ScaleView_xScale, 1.1f);
    float yScale = array.getFloat(R.styleable.ScaleView_yScale, 1.1f);
    float xScaleParent = array.getFloat(R.styleable.ScaleView_xScaleParent, 1.1f);
    float yScaleParent = array.getFloat(R.styleable.ScaleView_yScaleParent, 1.1f);
    /** 设置缩放动画 */
    anim_larger = new ScaleAnimation(1.0f, xScale, 1.0f, yScale,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    anim_larger.setDuration(250);// 设置动画持续时间
    anim_larger.setFillAfter(true);

    anim_smaller = new ScaleAnimation(xScaleParent, 1.0f, yScaleParent, 1.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    anim_smaller.setDuration(250);
    anim_smaller.setFillAfter(true);

    anim_larger_parent = new ScaleAnimation(1.0f, xScaleParent, 1.0f, yScaleParent,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    anim_larger_parent.setDuration(250);// 设置动画持续时间
    anim_larger_parent.setFillAfter(true);

    anim_smaller_parent = new ScaleAnimation(xScale, 1.0f, yScale, 1.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    anim_smaller_parent.setDuration(250);
    anim_smaller_parent.setFillAfter(true);

    this.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean focus) {
        View parentView = (View) view.getParent();
        if (focus) {
          startAnimation(anim_larger);
          parentView.startAnimation(anim_larger_parent);
          parentView.bringToFront();
        } else {
          startAnimation(anim_smaller);
          parentView.startAnimation(anim_smaller_parent);
        }
      }
    });

    array.recycle();
  }

  public static ScaleImageButton newInstance(ViewGroup parent) {
    return (ScaleImageButton) ViewUtils.newInstance(parent, R.layout.scale_image_little);
  }

  public static ScaleImageButton newInstanceLarge(ViewGroup parent) {
    return (ScaleImageButton) ViewUtils.newInstance(parent, R.layout.scale_image_large);
  }

  public static ScaleImageButton newInstanceNormal(ViewGroup parent) {
    return (ScaleImageButton) ViewUtils.newInstance(parent, R.layout.scale_image_normal);
  }

  public View getView() {
    return this;
  }

}
