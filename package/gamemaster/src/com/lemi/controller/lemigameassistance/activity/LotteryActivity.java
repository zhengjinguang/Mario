package com.lemi.controller.lemigameassistance.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lemi.controller.lemigameassistance.config.Intents;
import com.lemi.controller.lemigameassistance.fragment.LotteryFragment;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class LotteryActivity extends BaseFragmentActivity {
  private long lotteryStopTime;
  private long lotteryStartTime;

  public static void launch(Context context, long lotteryStartTime, long lotteryStopTime) {
    if (context == null) {
      throw new IllegalArgumentException("context can not be null");
    }
    Intent intent = new Intent(context, LotteryActivity.class);
    if (!(context instanceof Activity)) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.putExtra(Intents.INTENT_EXTRA_LOTTERY_START_TIME, lotteryStartTime);
    intent.putExtra(Intents.INTENT_EXTRA_LOTTERY_STOP_TIME, lotteryStopTime);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleIntent(getIntent());
    Bundle bundle = new Bundle();
    bundle.putLong(Intents.INTENT_EXTRA_LOTTERY_START_TIME, lotteryStartTime);
    bundle.putLong(Intents.INTENT_EXTRA_LOTTERY_STOP_TIME, lotteryStopTime);
    fragment = new LotteryFragment();
    fragment.setArguments(bundle);
    replaceFragment(fragment);
  }

  private void handleIntent(Intent intent) {
    if (intent != null) {
      lotteryStartTime = intent.getLongExtra(Intents.INTENT_EXTRA_LOTTERY_START_TIME, 0l);
      lotteryStopTime = intent.getLongExtra(Intents.INTENT_EXTRA_LOTTERY_STOP_TIME, 0l);
    }
  }
}
