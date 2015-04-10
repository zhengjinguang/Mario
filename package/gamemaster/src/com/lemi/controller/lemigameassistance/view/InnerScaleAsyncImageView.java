package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.focus.anim.ScaleAnimHelper;
import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.image.view.AsyncImageView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class InnerScaleAsyncImageView extends AsyncImageView {

  private ScaleAnimHelper scaleAnimHelper;

  public InnerScaleAsyncImageView(Context context) {
    super(context);
  }

  public InnerScaleAsyncImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    scaleAnimHelper = new ScaleAnimHelper(context, attrs);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    scaleAnimHelper.setFocusListener(this);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    scaleAnimHelper.onLayout(this);
  }

  public static InnerScaleAsyncImageView newInstance(ViewGroup parent) {
    return (InnerScaleAsyncImageView) ViewUtils.newInstance(parent,
        R.layout.inner_scale_aync_image_view);
  }

  public View getView() {
    return this;
  }

}
