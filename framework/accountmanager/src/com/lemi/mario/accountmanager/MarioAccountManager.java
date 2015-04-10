package com.lemi.mario.accountmanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.lemi.mario.accountmanager.accountStorage.AccountStorage;
import com.lemi.mario.accountmanager.accountStorage.AccountStorageFactory;
import com.lemi.mario.accountmanager.config.Constants;
import com.lemi.mario.accountmanager.config.ReturnValues;
import com.lemi.mario.accountmanager.model.ChangeUserInfoModel;
import com.lemi.mario.accountmanager.model.LoginModel;
import com.lemi.mario.accountmanager.model.RegisterModel;
import com.lemi.mario.accountmanager.model.User;
import com.lemi.mario.accountmanager.net.AccountHttpHelper;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.udid.UDIDUtil;

/**
 * @author zhengjinguang@letv.com (shining).
 */
@TargetApi(Build.VERSION_CODES.ECLAIR)
public class MarioAccountManager {
  private static final String TAG = "MarioAccountManager";
  private static MarioAccountManager instance;
  private Context appContext;
  private AccountManager accountManager;
  private AccountStorage sharedPrefAccountStorage;
  private AccountStorage systemAccountStorage;
  private AccountDataUpdater accountDataUpdater;
  private AccountStateChangeListener accountStateChangeListener;

  private User user;
  private String authCode;

  private MarioAccountManager() {
    appContext = GlobalConfig.getAppContext();
    sharedPrefAccountStorage =
        AccountStorageFactory.creator(AccountStorageFactory.StorageType.TYPE_SHAREDPREF);
    systemAccountStorage =
        AccountStorageFactory.creator(AccountStorageFactory.StorageType.TYPE_SYSTEM);
    accountDataUpdater = new AccountDataUpdater(sharedPrefAccountStorage, systemAccountStorage);
    accountManager = AccountManager.get(appContext);
    initData();
  }

  public synchronized static MarioAccountManager getInstance() {
    if (instance == null) {
      instance = new MarioAccountManager();
    }
    return instance;
  }

  public void setAccountStateChangeListener(AccountStateChangeListener accountStateChangeListener) {
    this.accountStateChangeListener = accountStateChangeListener;
  }

  public void initData() {
    if (!TextUtils.isEmpty(sharedPrefAccountStorage.getAuth())) {
      authCode = sharedPrefAccountStorage.getAuth();
    } else if (getLocalAccount() != null) {
      accountDataUpdater.runTask(new Runnable() {
        @Override
        public void run() {
          syncAuth(systemAccountStorage.getAuth());
        }
      });
    }
    user = sharedPrefAccountStorage.getUserInfo();
    if (user == null) {
      accountDataUpdater.runTask(new Runnable() {
        @Override
        public void run() {
          syncUserInfo(systemAccountStorage.getUserInfo());
        }
      });
    }
  }

  public Account getLocalAccount() {
    Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
    return accounts == null || accounts.length == 0 ? null : accounts[0];
  }

  public boolean hasSystemAccount() {
    return getLocalAccount() != null;
  }

  public boolean isRegistered() {
    if (isLogined()) {
      return true;
    }
    if (getLocalAccount() != null) {
      return true;
    }
    return false;
  }

  public boolean isLogined() {
    return !TextUtils.isEmpty(authCode);
  }

  public void login() {
    AccountState accountState = AccountState.ERROR;
    AccountError accountError = AccountError.OTHER;
    LoginModel model = AccountHttpHelper.login(UDIDUtil.getUDID(appContext));
    if (model == null) {
      accountError = AccountError.NETWORK_INVALID;
    } else if (model.getRet() == ReturnValues.VALID_RETURN) {
      saveAuth(model.getAuthcode());
      saveUserInfo(model.getUser());
      accountState = AccountState.OK;
    } else if (model.getRet() == ReturnValues.UDID_NONEXIST) {
      accountError = AccountError.NOT_REGISTERED;
    }

    if (accountStateChangeListener != null) {
      if (accountState == AccountState.OK) {
        accountStateChangeListener.onLogin(model.getAuthcode(), model.getUser());
      } else {
        accountStateChangeListener.onLoginError(accountError,
            model == null ? null : model.getReason());
      }
    }
  }

  public void register() {
    AccountState accountState = AccountState.ERROR;
    AccountError accountError = AccountError.NOT_REGISTERED;
    RegisterModel model = AccountHttpHelper.register(UDIDUtil.getUDID(appContext));

    if (model == null) {
      accountError = AccountError.NETWORK_INVALID;
    } else if (model.getRet() == ReturnValues.VALID_RETURN
        || model.getRet() == ReturnValues.UDID_EXIST) {
      accountState = AccountState.OK;
    }
    if (accountStateChangeListener != null) {
      if (accountState == AccountState.OK) {
        accountStateChangeListener.onRegister();
      } else {
        accountStateChangeListener.onRegisterError(accountError,
            model == null ? null : model.getReason());
      }
    }
  }

  public void changeUserInfo(final User newUser) {
    if (newUser == null) {
      return;
    }
    AccountState accountState = AccountState.ERROR;
    AccountError accountError = AccountError.OTHER;
    ChangeUserInfoModel model =
        AccountHttpHelper
            .changeUserInfo(newUser);
    if (model == null) {
      accountError = AccountError.NETWORK_INVALID;
    } else if (model.getRet() == ReturnValues.VALID_RETURN) {
      accountState = AccountState.OK;
      user = newUser;
      accountDataUpdater.updateUserInfo(newUser);
    } else if (model.getRet() == ReturnValues.AUTHCODE_INVALID) {
      accountError = AccountError.AUTH_INVALID;
      logout();
    }
    if (accountStateChangeListener != null) {
      if (accountState == AccountState.OK) {
        accountStateChangeListener.onUserInfoChange(newUser);
      } else {
        accountStateChangeListener.onChangeUserInfoError(accountError,
            model == null ? null : model.getReason());
      }
    }
  }

  public User getUserInfo() {
    return user;
  }

  private void syncUserInfo(User user) {
    if (user != null) {
      this.user = user;
      sharedPrefAccountStorage.saveUserInfo(user);
    }
  }

  public void saveUserInfo(User user) {
    this.user = user;
    accountDataUpdater.updateUserInfo(user);
  }

  public String getAuth() {
    return authCode;
  }

  private void syncAuth(String authCode) {
    if (!TextUtils.isEmpty(authCode)) {
      this.authCode = authCode;
      sharedPrefAccountStorage.saveAuth(authCode);
    }
  }

  public void saveAuth(String authCode) {
    this.authCode = authCode;
    accountDataUpdater.updateAuth(authCode);
  }

  public void logout() {
    authCode = null;
    accountDataUpdater.removeAuth();
    if (accountStateChangeListener != null) {
      accountStateChangeListener.onLogout();
    }
  }

  public static enum AccountState {
    OK, ERROR
  }

  public static enum AccountError {
    NETWORK_INVALID, NOT_REGISTERED, AUTH_INVALID, OTHER
  }

  public interface AccountStateChangeListener {
    void onRegister();

    void onLogin(String authCode, User user);

    void onUserInfoChange(User user);

    void onLogout();

    void onRegisterError(AccountError accountError, String reason);

    void onLoginError(AccountError accountError, String reason);

    void onChangeUserInfoError(AccountError accountError, String reason);

  }
}
