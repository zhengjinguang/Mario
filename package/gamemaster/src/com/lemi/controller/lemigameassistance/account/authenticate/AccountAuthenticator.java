package com.lemi.controller.lemigameassistance.account.authenticate;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.lemi.mario.accountmanager.config.Constants;
import com.lemi.mario.accountmanager.config.UserKeys;
import com.lemi.mario.accountmanager.model.User;

/**
 * @author zhengjinguang@letv.com (shining).
 */
@TargetApi(Build.VERSION_CODES.ECLAIR)
public class AccountAuthenticator extends AbstractAccountAuthenticator {

  private final Context mContext;
  private AccountManager accountManager;

  public AccountAuthenticator(Context context) {
    super(context);
    this.mContext = context;
    accountManager = AccountManager.get(context);
  }

  // used to change account here and other apps get account data.
  @Override
  public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
      String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
    final Intent intent = new Intent();
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
    if (!TextUtils.isEmpty(options.getString(AccountManager.KEY_USERDATA))) {
      changeUserInfo(options);
    } else {
      User getUser = getUserInfo();
      if (getUser != null) {
        intent.putExtra(AccountManager.KEY_USERDATA, getUser);
      }
    }
    final Bundle bundle = new Bundle();
    bundle.putParcelable(AccountManager.KEY_INTENT, intent);
    return bundle;
  }

  public void changeUserInfo(Bundle user) {
    Account account = getLocalAccount();
    if (account == null || user == null) {
      return;
    }
    if (!TextUtils.isEmpty(user.getString(UserKeys.NICK))) {
      accountManager.setUserData(account, UserKeys.NICK, user.getString(UserKeys.NICK));
    }

    if (!TextUtils.isEmpty(user.getString(UserKeys.PHONE))) {
      accountManager.setUserData(account, UserKeys.PHONE, user.getString(UserKeys.PHONE));
    }

    if (!TextUtils.isEmpty(user.getString(UserKeys.EMAIL))) {
      accountManager.setUserData(account, UserKeys.EMAIL, user.getString(UserKeys.EMAIL));
    }

    accountManager.setUserData(account, UserKeys.GENDER, user.getInt(UserKeys.GENDER, 0) + "");
  }

  public Account getLocalAccount() {
    Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
    return accounts == null || accounts.length == 0 ? null : accounts[0];
  }

  public User getUserInfo() {
    Account account = getLocalAccount();
    if (account == null) {
      return null;
    }
    User user = new User();
    user.setNick(accountManager.getUserData(account, UserKeys.NICK));
    if (accountManager
        .getUserData(account, UserKeys.UID) != null) {
      try {
        user.setGender(Integer.parseInt(accountManager
            .getUserData(account, UserKeys.UID)));
      } catch (NumberFormatException e) {
        user.setUid(0);
      }
    }
    else {
      user.setUid(0);
    }
    user.setUdid(accountManager.getUserData(account, UserKeys.UDID));
    user.setEmail(accountManager.getUserData(account, UserKeys.EMAIL));
    user.setPhone(accountManager.getUserData(account, UserKeys.PHONE));
    if (accountManager
        .getUserData(account, UserKeys.GENDER) != null) {
      try {
        user.setGender(Integer.parseInt(accountManager
            .getUserData(account, UserKeys.GENDER)));
      } catch (NumberFormatException e) {
        user.setGender(0);
      }
    }
    else {
      user.setGender(0);
    }
    return user;
  }

  @Override
  public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
      String authTokenType, Bundle options) throws NetworkErrorException {
    // If the caller requested an authToken type we don't support, then
    // return an error
    if (!authTokenType.equals(Constants.AUTHTOKEN_TYPE)) {
      final Bundle result = new Bundle();
      result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
      return result;
    }

    // Extract the username and password from the Account Manager, and ask
    // the server for an appropriate AuthToken.
    final AccountManager am = AccountManager.get(mContext);
    String authToken = am.peekAuthToken(account, authTokenType);

    // If we get an authToken - we return it
    if (!TextUtils.isEmpty(authToken)) {
      final Bundle result = new Bundle();
      result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
      result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
      result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
      return result;
    }

    // If we get here, then we couldn't access the user's password - so we
    // need to re-prompt them for their credentials. We do that by creating
    // an intent to display our AuthenticatorActivity.
    final Intent intent = new Intent();
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

    final Bundle bundle = new Bundle();
    bundle.putParcelable(AccountManager.KEY_INTENT, intent);
    return bundle;
  }


  @Override
  public String getAuthTokenLabel(String authTokenType) {
    return authTokenType + " (Label)";
  }

  @Override
  public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
      String[] features) throws NetworkErrorException {
    final Bundle result = new Bundle();
    result.putBoolean(KEY_BOOLEAN_RESULT, false);
    return result;
  }

  @Override
  public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
    return null;
  }

  @Override
  public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
      Bundle options) throws NetworkErrorException {
    return null;
  }

  @Override
  public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
      String authTokenType, Bundle options) throws NetworkErrorException {
    return null;
  }
}
