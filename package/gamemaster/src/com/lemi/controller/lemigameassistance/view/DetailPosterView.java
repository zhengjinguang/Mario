package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.image.view.AsyncImageView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DetailPosterView extends AsyncImageView {

  public DetailPosterView(Context context) {
    super(context);
  }

  public DetailPosterView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public DetailPosterView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public static DetailPosterView newInstance(ViewGroup parent) {
    return (DetailPosterView) ViewUtils.newInstance(parent, R.layout.detail_poster_image_view);
  }

}
