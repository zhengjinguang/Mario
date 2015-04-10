package com.lemi.controller.lemigameassistance.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.lemi.controller.lemigameassistance.GameMasterPreferences;
import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.fragment.ExploreTabHostFragment;
import com.lemi.controller.lemigameassistance.manager.UpgradeManager;
import com.lemi.controller.lemigameassistance.model.CheckVersionModel;
import com.lemi.controller.lemigameassistance.utils.ThreadPool;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.TimeUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ExploreActivity extends BaseFragmentActivity {

  private static final long EXIT_TIME_INTERVAL = 3000l;

  private long lastClickBackTime = 0l;

  public static void launch(Context context) {
    final Intent intent = new Intent(context, ExploreActivity.class);

    // if context is application context, need set flag.
    if (!(context instanceof Activity)) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    fragment = new ExploreTabHostFragment();
    replaceFragment(fragment);

    checkUpgrade();
  }

  @Override
  public void onResume() {
    super.onResume();
    MobclickAgent.onResume(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    MobclickAgent.onPause(this);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.explore_activity;
  }

  private void checkUpgrade() {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        final CheckVersionModel versionModel = UpgradeManager.getInstance().checkUpgrade(false);
        if (versionModel != null) {
          UpgradeManager.getInstance().buildAndShowAlertDialog(ExploreActivity.this, versionModel,
              null, new Runnable() {
                @Override
                public void run() {
                  GameMasterPreferences.setIgnoreVersionCode(versionModel.getVersionCode());
                }
              });
        }
      }
    });
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      long nowTime = TimeUtils.getCurrentTime();
      if (nowTime - lastClickBackTime > EXIT_TIME_INTERVAL) {
        lastClickBackTime = nowTime;
        MainThreadPostUtils.toast(R.string.app_exit_msg);
        return true;
      } else {
        finish();
        return true;
      }
    }
    if (fragment instanceof ExploreTabHostFragment) {
      if (((ExploreTabHostFragment) fragment).onTabHostKeyDown(keyCode, event)) {
        return true;
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
      if (fragment.onKeyDown(event.getKeyCode(), event)) {
        return true;
      }
    }
    return super.dispatchKeyEvent(event);
  }
}
