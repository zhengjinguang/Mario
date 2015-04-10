package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.focus.anim.ScaleAnimHelper;
import com.lemi.mario.base.utils.ViewUtils;

/**
 * inner scale view , this class can scale it's parent when focus
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public class InnerScaleImageView extends ImageView {

  private ScaleAnimHelper scaleAnimHelper;

  public InnerScaleImageView(Context context) {
    super(context);
  }

  public InnerScaleImageView(Context context, AttributeSet attrs) {
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

  public static InnerScaleImageView newInstance(ViewGroup parent) {
    return (InnerScaleImageView) ViewUtils.newInstance(parent, R.layout.inner_scale_image_view);
  }

  public View getView() {
    return this;
  }

}
