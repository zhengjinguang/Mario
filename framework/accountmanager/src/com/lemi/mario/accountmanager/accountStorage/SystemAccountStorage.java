package com.lemi.mario.accountmanager.accountStorage;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.lemi.mario.accountmanager.config.Constants;
import com.lemi.mario.accountmanager.config.UserKeys;
import com.lemi.mario.accountmanager.model.User;
import com.lemi.mario.base.config.GlobalConfig;

import java.io.IOException;

/**
 * @author zhengjinguang@letv.com (shining).
 */
@TargetApi(Build.VERSION_CODES.ECLAIR)
public class SystemAccountStorage implements AccountStorage {

  private AccountManager accountManager;
  private Context appContext;

  public SystemAccountStorage() {
    appContext = GlobalConfig.getAppContext();
    accountManager = AccountManager.get(appContext);
  }

  public Account getLocalAccount() {
    Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
    return accounts == null || accounts.length == 0 ? null : accounts[0];
  }

  @Override
  public void saveAuth(String auth) {
    Account account = getLocalAccount();
    if (account == null) {
      return;
    }
    accountManager.setAuthToken(account, Constants.AUTHTOKEN_TYPE, auth);
  }

  @Override
  public void saveUserInfo(User user) {
    Account account = getLocalAccount();
    if (account == null || user == null) {
      return;
    }
    Bundle bundle = new Bundle();
    bundle.putString(AccountManager.KEY_USERDATA, "change");
    bundle.putString(UserKeys.NICK, user.getNick());
    bundle.putInt(UserKeys.UID, user.getUid());
    bundle.putString(UserKeys.UDID, user.getUdid());
    bundle.putString(UserKeys.PHONE, user.getPhone());
    bundle.putString(UserKeys.EMAIL, user.getEmail());
    bundle.putInt(UserKeys.GENDER, user.getGender());

    accountManager.addAccount(Constants.ACCOUNT_TYPE, Constants.AUTHTOKEN_TYPE, null, bundle,
        null, null,
        null);
  }

  @Override
  public String getAuth() {
    Account account = getLocalAccount();
    if (account == null) {
      return null;
    }
    AccountManagerFuture<Bundle> future =
        accountManager.getAuthToken(account, Constants.AUTHTOKEN_TYPE, null, null, null, null);
    Bundle bnd = null;
    try {
      bnd = future.getResult();
    } catch (OperationCanceledException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuthenticatorException e) {
      e.printStackTrace();
    }
    String authtoken = null;
    if (bnd != null) {
      authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
    }
    return authtoken;
  }

  @Override
  public User getUserInfo() {
    Account account = getLocalAccount();
    if (account == null) {
      return null;
    }
    AccountManagerFuture<Bundle> future =
        accountManager.addAccount(Constants.ACCOUNT_TYPE, Constants.AUTHTOKEN_TYPE, null, null,
            null, null, null);
    Bundle bnd = null;
    try {
      bnd = future.getResult();
    } catch (OperationCanceledException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuthenticatorException e) {
      e.printStackTrace();
    }
    User user = null;
    if (bnd != null) {
      Intent intent = bnd.getParcelable(AccountManager.KEY_INTENT);
      if (intent != null) {
        user = (User) intent.getSerializableExtra(AccountManager.KEY_USERDATA);
      }
    }

    return user;
  }

  @Override
  public void invalidateAuth() {
    Account account = getLocalAccount();
    if (account == null) {
      return;
    }
    accountManager.invalidateAuthToken(Constants.AUTHTOKEN_TYPE, getAuth());
  }

}
