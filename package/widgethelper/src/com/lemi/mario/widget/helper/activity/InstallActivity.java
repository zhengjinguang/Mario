package com.lemi.mario.widget.helper.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lemi.mario.widget.helper.activity.base.BaseFragmentActivity;
import com.lemi.mario.widget.helper.fragment.InstallFragment;
import com.lemi.mario.widget.helper.fragment.MountFragment;
import com.lemi.mario.widget.helper.utils.LogHelper;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class InstallActivity extends BaseFragmentActivity {


  public static void launch(Context context) {
    final Intent intent = new Intent(context, InstallActivity.class);

    // if context is application context, need set flag.
    if (!(context instanceof Activity)) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fragment = new InstallFragment();
    replaceFragment(fragment);
    LogHelper.enterPage(LogHelper.PAGE_MOUNT);
  }
}
