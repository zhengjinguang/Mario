package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class AlwaysMarqueeTextView extends TextView {
  private static final int INDEFINITELY = -1;

  public AlwaysMarqueeTextView(Context context) {
    super(context);
  }

  public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setMarqueeRepeatLimit(INDEFINITELY);
    setEllipsize(TextUtils.TruncateAt.MARQUEE);
    setSingleLine();
  }

  public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setMarqueeRepeatLimit(INDEFINITELY);
    setEllipsize(TextUtils.TruncateAt.MARQUEE);
    setSingleLine();
  }

  @Override
  public boolean isFocused() {
    return true;
  }
}
