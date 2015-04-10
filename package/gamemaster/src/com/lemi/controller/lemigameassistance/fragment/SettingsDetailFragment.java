package com.lemi.controller.lemigameassistance.fragment;

import android.os.Bundle;
import android.view.View;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.fragment.base.BaseFragment;
import com.lemi.controller.lemigameassistance.manager.SDCardManager;
import com.lemi.controller.lemigameassistance.view.SettingsItemView;
import com.lemi.controller.lemigameassistance.view.SwitcherView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SettingsDetailFragment extends BaseFragment {

  private SettingsItemView cleanManage;
  private SettingsItemView sdCardManage;

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    initView();
    setData();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.settings_detail_fragment;
  }

  private void initView() {
    cleanManage = (SettingsItemView) contentView.findViewById(R.id.settings_detail_clean);
    sdCardManage = (SettingsItemView) contentView.findViewById(R.id.settings_detail_sdcard);
  }

  private void setData() {
    cleanManage.setData(R.string.space_clear, R.string.space_tip, SwitcherView.SwitcherType.CLEAN);
    if (SDCardManager.getInstance().getSdCardSupportStatus()
        == SDCardManager.SDCardSupportStatus.NOT_SUPPORT) {
      sdCardManage.setVisibility(View.GONE);
    } else {
      sdCardManage.setVisibility(View.VISIBLE);
      sdCardManage.setData(R.string.external_sdcard, R.string.sdcard_tip,
          SwitcherView.SwitcherType.EXTERNAL_SD_CARD);
    }
  }

}
