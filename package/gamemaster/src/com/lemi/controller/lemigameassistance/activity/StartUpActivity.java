package com.lemi.controller.lemigameassistance.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.lemi.controller.lemigameassistance.fragment.StartUpFragment;
import com.lemi.controller.lemigameassistance.manager.StartupManager;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class StartUpActivity extends BaseFragmentActivity {


  public static void launch(Context context) {
    final Intent intent = new Intent(context, StartUpActivity.class);

    // if context is application context, need set flag.
    if (!(context instanceof Activity)) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StartupManager.getInstance().setStartupShown();
    fragment = new StartUpFragment();
    replaceFragment(fragment);
  }


  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_BACK:
        startExplore();
        return true;
      case KeyEvent.KEYCODE_DPAD_CENTER:
        startExplore();
        break;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        startExplore();
        return true;
      case KeyEvent.KEYCODE_DPAD_LEFT:
        break;
      case KeyEvent.KEYCODE_DPAD_UP:
        break;
      case KeyEvent.KEYCODE_DPAD_DOWN:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  private void startExplore() {
    ExploreActivity.launch(StartUpActivity.this);
    finish();
  }
}
