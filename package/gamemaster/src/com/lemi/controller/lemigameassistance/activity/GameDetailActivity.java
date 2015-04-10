package com.lemi.controller.lemigameassistance.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.config.Intents;
import com.lemi.controller.lemigameassistance.fragment.GameDetailFragment;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameDetailActivity extends BaseTitleFragmentActivity {

  private String packageName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleIntent(getIntent());

    Bundle bundle = new Bundle();
    bundle.putString(Intents.INTENT_EXTRA_PACKAGENAME, packageName);
    fragment = new GameDetailFragment();
    fragment.setArguments(bundle);

    replaceFragment(fragment);

  }

  @Override
  protected String getTitleText() {
    return getString(R.string.game_detail_title);
  }

  public static void launch(Context context, String packageName) {
    if (context == null || packageName == null) {
      throw new IllegalArgumentException("context and packageName can not be null");
    }
    Intent intent = new Intent(context, GameDetailActivity.class);
    // if context is application context, need set flag.
    if (!(context instanceof Activity)) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.putExtra(Intents.INTENT_EXTRA_PACKAGENAME, packageName);
    context.startActivity(intent);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
    super.onNewIntent(intent);
  }


  private void handleIntent(Intent intent) {
    if (intent != null) {
      packageName = intent.getStringExtra(Intents.INTENT_EXTRA_PACKAGENAME);
    }
  }
}
