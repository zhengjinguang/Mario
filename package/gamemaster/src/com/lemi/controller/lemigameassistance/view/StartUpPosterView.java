package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.image.view.AsyncImageView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class StartUpPosterView extends AsyncImageView {

  public StartUpPosterView(Context context) {
    super(context);
  }

  public StartUpPosterView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public StartUpPosterView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public static StartUpPosterView newInstance(ViewGroup parent) {
    return (StartUpPosterView) ViewUtils.newInstance(parent, R.layout.start_up_item);
  }

  public static StartUpPosterView newInstance(Context context) {
    return (StartUpPosterView) ViewUtils.newInstance(context, R.layout.start_up_item);
  }


  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
  }

  public void setData(String path) {
    loadLocalImageImmediate(path);
  }

  public View getView() {
    return this;
  }
}
