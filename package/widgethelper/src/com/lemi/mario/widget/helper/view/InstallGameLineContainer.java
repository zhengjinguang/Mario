package com.lemi.mario.widget.helper.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.widget.helper.R;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class InstallGameLineContainer extends LinearLayout {
  public InstallGameLineContainer(Context context) {
    super(context);
  }

  public InstallGameLineContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public InstallGameLineContainer(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }


  public static InstallGameLineContainer newInstance(ViewGroup parent) {
    return (InstallGameLineContainer) ViewUtils.newInstance(parent, R.layout.install_line_container);
  }

  public static InstallGameLineContainer newInstance(Context context) {
    return (InstallGameLineContainer) ViewUtils.newInstance(context, R.layout.install_line_container);
  }

}
