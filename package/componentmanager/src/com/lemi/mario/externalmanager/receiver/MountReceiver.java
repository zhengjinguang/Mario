package com.lemi.mario.externalmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lemi.mario.externalmanager.manager.MountMessageCenter;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class MountReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    MountMessageCenter.getInstance().handleMountMessage(context, intent);
  }

}
