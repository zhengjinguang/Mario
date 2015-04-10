package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.mario.base.utils.ViewUtils;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SettingsItemView extends LinearLayout {

  private TextView settingsName;
  private TextView settingsMessage;
  private SwitcherView switcherView;


  public SettingsItemView(Context context) {
    super(context);
  }

  public SettingsItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SettingsItemView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public static SettingsItemView newInstance(ViewGroup parent) {
    return (SettingsItemView) ViewUtils.newInstance(parent, R.layout.settings_item_layout);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    settingsName = (TextView) findViewById(R.id.settings_detail_name);
    settingsMessage = (TextView) findViewById(R.id.settings_detail_message);
    switcherView = (SwitcherView) findViewById(R.id.settings_detail_switcher);
  }


  public void setData(int nameResId, int messageResId, SwitcherView.SwitcherType switcherType) {
    settingsName.setText(nameResId);
    settingsMessage.setText(messageResId);
    switcherView.setData(switcherType);
  }

}
