package com.lemi.mario.sample.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SampleReciver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
      // network change
      // Toast.makeText(context, "" + NetworkUtil.getNetworkType(), Toast.LENGTH_LONG).show();
    }
  }

}
