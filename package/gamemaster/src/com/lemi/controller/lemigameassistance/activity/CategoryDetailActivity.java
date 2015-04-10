package com.lemi.controller.lemigameassistance.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.lemi.controller.lemigameassistance.config.Intents;
import com.lemi.controller.lemigameassistance.fragment.CategoryDetailFragment;

/**
 * @author zhoulei@letv.com (Zhou Lei)
 */
public class CategoryDetailActivity extends BaseTitleFragmentActivity {
  private String cid;
  private String categoryName;
  private int categoryCount;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleIntent(getIntent());

    Bundle bundle = new Bundle();
    bundle.putString(Intents.INTENT_EXTRA_CATEGORY_ID, cid);
    bundle.putInt(Intents.INTENT_EXTRA_CATEGORY_COUNT, categoryCount);

    fragment = new CategoryDetailFragment();
    fragment.setArguments(bundle);
    replaceFragment(fragment);
  }

  @Override
  protected String getTitleText() {
    return categoryName;
  }

  public static void launch(Context context, String cid, String categoryName, int count) {
    if (context == null) {
      throw new IllegalArgumentException("context and packageName can not be null");
    }
    Intent intent = new Intent(context, CategoryDetailActivity.class);
    if (!(context instanceof Activity)) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.putExtra(Intents.INTENT_EXTRA_CATEGORY_ID, cid);
    intent.putExtra(Intents.INTENT_EXTRA_CATEGORY_NAME, categoryName);
    intent.putExtra(Intents.INTENT_EXTRA_CATEGORY_COUNT, count);
    context.startActivity(intent);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
    super.onNewIntent(intent);
  }


  private void handleIntent(Intent intent) {
    if (intent != null) {
      cid = intent.getStringExtra(Intents.INTENT_EXTRA_CATEGORY_ID);
      categoryName = intent.getStringExtra(Intents.INTENT_EXTRA_CATEGORY_NAME);
      categoryCount = intent.getIntExtra(Intents.INTENT_EXTRA_CATEGORY_COUNT, 0);
      setTitleText(categoryName);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
      finish();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
