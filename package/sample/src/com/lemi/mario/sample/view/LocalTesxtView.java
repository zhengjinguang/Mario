package com.lemi.mario.sample.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.sample.R;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class LocalTesxtView extends TextView {

  public LocalTesxtView(Context context) {
    super(context);
  }

  public LocalTesxtView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public static LocalTesxtView newInstance(ViewGroup parent) {
    return (LocalTesxtView) ViewUtils.newInstance(parent, R.layout.loacl_textview);
  }


}
