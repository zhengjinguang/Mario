package com.lemi.controller.lemigameassistance.account.authenticate;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/**
 * @author zhengjinguang@letv.com (shining).
 */
@TargetApi(Build.VERSION_CODES.ECLAIR)
public class AuthenticatorService extends Service {
  @Override
  public IBinder onBind(Intent intent) {
    AccountAuthenticator accountAuthenticator = new AccountAuthenticator(this);
    return accountAuthenticator.getIBinder();
  }
}
