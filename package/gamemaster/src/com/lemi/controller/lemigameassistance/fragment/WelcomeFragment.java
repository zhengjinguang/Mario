package com.lemi.controller.lemigameassistance.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.account.GameMasterAccountManager;
import com.lemi.controller.lemigameassistance.activity.ExploreActivity;
import com.lemi.controller.lemigameassistance.activity.StartUpActivity;
import com.lemi.controller.lemigameassistance.fragment.base.BaseFragment;
import com.lemi.controller.lemigameassistance.manager.StartupManager;
import com.lemi.controller.lemigameassistance.utils.ThreadPool;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.SystemUtil;

import java.util.Random;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class WelcomeFragment extends BaseFragment {

  private static final int START_SECOND = 1500;
  private TextView tipsTextView;
  private TextView versionNameTextView;
  private String tip;
  private String versionName;

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    initView();
    initData();
    loadStartUpActive();
    loadRecommendInfo();
    waitForTips();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.welcome_fragment;
  }

  private void initView() {
    tipsTextView = (TextView) contentView.findViewById(R.id.welcome_tips);
    versionNameTextView = (TextView) contentView.findViewById(R.id.welcome_version_name);
  }

  private void initData() {
    String[] tips = getResources().getStringArray(R.array.tips);
    Random random = new Random();
    tip = tips[random.nextInt(tips.length)];
    versionName = SystemUtil.getVersionName(GlobalConfig.getAppContext());
    setData();
    GameMasterAccountManager.getInstance().startLogin();
  }

  private void setData() {
    tipsTextView.setText(tip);
    versionNameTextView.setText(versionName);
  }


  private void loadStartUpActive() {
    StartupManager.getInstance().asyncLoadStartupInfo();
  }

  private void loadRecommendInfo() {
    StartupManager.getInstance().asyncLoadRecommendInfo();
  }

  private void waitForTips() {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(START_SECOND);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        judgeJumpActivity();
      }
    });
  }

  private void judgeJumpActivity() {
    Context context;
    if (isAdded()) {
      context = getActivity();
    } else {
      context = GlobalConfig.getAppContext();
    }
    if (StartupManager.getInstance().canShowStartup()) {
      StartUpActivity.launch(context);
    } else {
      ExploreActivity.launch(context);
    }
    if (isAdded()) {
      getActivity().finish();
    }

  }
}
