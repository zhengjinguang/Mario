package com.lemi.controller.lemigameassistance.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;

/**
 * @author zhoulei@letv.com (Zhou Lei)
 */
public abstract class BaseTitleFragmentActivity extends BaseFragmentActivity {
  private TextView titleTextView;

  @Override
  protected int getLayoutId() {
    return R.layout.base_title_fragment_activity;
  }

  @Override
  protected void onCreate(Bundle onSaveInstanceState) {
    super.onCreate(onSaveInstanceState);
    titleTextView = (TextView) findViewById(R.id.title_text);
    if (!TextUtils.isEmpty(getTitleText())) {
      titleTextView.setText(getTitleText());
    }
  }

  protected void setTitleText(String title) {
    if (TextUtils.isEmpty(title)) {
      return;
    }
    titleTextView.setText(title);
  }

  protected abstract String getTitleText();

}
