package com.lemi.controller.lemigameassistance.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.lemi.controller.lemigameassistance.config.Intents;
import com.lemi.controller.lemigameassistance.fragment.SubjectDetailFragment;
import com.lemi.controller.lemigameassistance.utils.LogHelper;

/**
 * @author zhoulei@letv.com (Zhou Lei)
 */
public class SubjectDetailActivity extends BaseTitleFragmentActivity {
  private long sid;
  private String subjectName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleIntent(getIntent());
    LogHelper.subjectDetailClick(subjectName);
    Bundle bundle = new Bundle();
    bundle.putLong(Intents.INTENT_EXTRA_SID, sid);

    fragment = new SubjectDetailFragment();
    fragment.setArguments(bundle);
    replaceFragment(fragment);
  }

  @Override
  protected String getTitleText() {
    return subjectName;
  }

  public static void launch(Context context, long sid, String subjectName) {
    if (context == null) {
      throw new IllegalArgumentException("context and packageName can not be null");
    }
    Intent intent = new Intent(context, SubjectDetailActivity.class);
    if (!(context instanceof Activity)) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.putExtra(Intents.INTENT_EXTRA_SID, sid);
    intent.putExtra(Intents.INTENT_EXTRA_SUBJECTNAME, subjectName);
    context.startActivity(intent);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
    super.onNewIntent(intent);
  }


  private void handleIntent(Intent intent) {
    if (intent != null) {
      sid = intent.getLongExtra(Intents.INTENT_EXTRA_SID, -1l);
      subjectName = intent.getStringExtra(Intents.INTENT_EXTRA_SUBJECTNAME);
      setTitleText(subjectName);
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
