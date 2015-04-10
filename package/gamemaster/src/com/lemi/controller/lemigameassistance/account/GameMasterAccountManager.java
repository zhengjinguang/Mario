package com.lemi.controller.lemigameassistance.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.mario.accountmanager.MarioAccountManager;
import com.lemi.mario.accountmanager.config.Constants;
import com.lemi.mario.accountmanager.config.UserKeys;
import com.lemi.mario.accountmanager.model.User;
import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.MainThreadPostUtils;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhengjinguang@letv.com (shining).
 */
@TargetApi(Build.VERSION_CODES.ECLAIR)
public class GameMasterAccountManager {

  private static final long THREAD_ALIVE_TIME = 30000l;
  private static final int MAX_THREAD_NUM = 3;
  private static GameMasterAccountManager instance;
  private static Context appContext;
  private final Set<WeakReference<OnUserInfoChangeListener>> userInfoChangeListeners =
      new HashSet<>();
  private final Set<WeakReference<OnLoginListener>> onLoginListeners = new HashSet<>();
  private MarioAccountManager marioAccountManager;
  private AccountState accountState = AccountState.NOT_REGISTERED;
  private MarioAccountManager.AccountStateChangeListener accountStateChangeListener =
      new MarioAccountManager.AccountStateChangeListener() {
        @Override
        public void onRegister() {
          accountState = AccountState.REGISTERED;
          marioAccountManager.login();
        }

        @Override
        public void onLogin(String authCode, User user) {
          accountState = AccountState.LOGINED;
          if (!marioAccountManager.hasSystemAccount()) {
            addNewAccount(authCode, user);
          }
          synchronized (onLoginListeners) {
            if (!CollectionUtils.isEmpty(onLoginListeners)) {
              for (WeakReference<OnLoginListener> listener : onLoginListeners) {
                OnLoginListener onLoginListener = listener.get();
                if (onLoginListener == null) {
                  continue;
                }
                onLoginListener.onSucceed();
              }
            }
          }
        }

        @Override
        public void onUserInfoChange(User user) {
          synchronized (userInfoChangeListeners) {
            if (!CollectionUtils.isEmpty(userInfoChangeListeners)) {
              for (WeakReference<OnUserInfoChangeListener> listener : userInfoChangeListeners) {
                OnUserInfoChangeListener onUserInfoChangeListener = listener.get();
                if (onUserInfoChangeListener == null) {
                  continue;
                }
                onUserInfoChangeListener.onSucceed(user);
              }
            }
          }
        }

        @Override
        public void onLogout() {
          accountState = AccountState.REGISTERED;
        }

        @Override
        public void onRegisterError(MarioAccountManager.AccountError accountError, String reason) {
          accountState = AccountState.NOT_REGISTERED;
          synchronized (onLoginListeners) {
            if (!CollectionUtils.isEmpty(onLoginListeners)) {
              for (WeakReference<OnLoginListener> listener : onLoginListeners) {
                OnLoginListener onLoginListener = listener.get();
                if (onLoginListener == null) {
                  continue;
                }
                onLoginListener.onFail(accountError, reason);
              }
            }
          }
        }

        @Override
        public void onLoginError(MarioAccountManager.AccountError accountError, String reason) {
          accountState = AccountState.REGISTERED;
          synchronized (onLoginListeners) {
            if (!CollectionUtils.isEmpty(onLoginListeners)) {
              for (WeakReference<OnLoginListener> listener : onLoginListeners) {
                OnLoginListener onLoginListener = listener.get();
                if (onLoginListener == null) {
                  continue;
                }
                onLoginListener.onFail(accountError, reason);
              }
            }
          }
        }

        @Override
        public void onChangeUserInfoError(MarioAccountManager.AccountError accountError,
            String reason) {
          if (accountError == MarioAccountManager.AccountError.AUTH_INVALID) {
            accountState = AccountState.REGISTERED;
          }
          synchronized (userInfoChangeListeners) {
            if (!CollectionUtils.isEmpty(userInfoChangeListeners)) {
              for (WeakReference<OnUserInfoChangeListener> listener : userInfoChangeListeners) {
                OnUserInfoChangeListener onUserInfoChangeListener = listener.get();
                if (onUserInfoChangeListener == null) {
                  continue;
                }
                onUserInfoChangeListener.onFail(accountError, reason);
              }
            }
          }
        }

      };
  private AccountManager accountManager;
  private CachedThreadPoolExecutorWithCapacity threadPool;

  private GameMasterAccountManager() {
    appContext = GlobalConfig.getAppContext();
    accountManager = AccountManager.get(appContext);
    marioAccountManager = MarioAccountManager.getInstance();
    marioAccountManager.setAccountStateChangeListener(accountStateChangeListener);
    threadPool = new CachedThreadPoolExecutorWithCapacity(MAX_THREAD_NUM, THREAD_ALIVE_TIME);
  }

  public synchronized static GameMasterAccountManager getInstance() {
    if (instance == null) {
      instance = new GameMasterAccountManager();
    }
    return instance;
  }

  public boolean isLogined() {
    if (!TextUtils.isEmpty(getAuth())) {
      accountState = AccountState.LOGINED;
    }
    return accountState == AccountState.LOGINED;
  }

  public void startLogin() {
    if (marioAccountManager.isLogined()) {
      accountState = AccountState.LOGINED;
      synchronized (onLoginListeners) {
        if (!CollectionUtils.isEmpty(onLoginListeners)) {
          for (WeakReference<OnLoginListener> listener : onLoginListeners) {
            OnLoginListener onLoginListener = listener.get();
            if (onLoginListener == null) {
              continue;
            }
            onLoginListener.onSucceed();
          }
        }
      }
      return;
    }
    loginThroughNet();
  }

  private void loginThroughNet() {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        if (marioAccountManager.isRegistered() || accountState == AccountState.REGISTERED) {
          accountState = AccountState.REGISTERED;
          marioAccountManager.login();
        } else {
          marioAccountManager.register();
        }
      }
    });
  }

  public void logout() {
    marioAccountManager.logout();
  }

  public void registerUserInfoChangeListener(OnUserInfoChangeListener listener) {
    synchronized (userInfoChangeListeners) {
      userInfoChangeListeners.add(new WeakReference<>(listener));
    }
  }

  public void unRegisterUserInfoChangeListener(OnUserInfoChangeListener onUserinfoChangeListener) {
    synchronized (userInfoChangeListeners) {
      if (!CollectionUtils.isEmpty(userInfoChangeListeners)) {
        for (WeakReference<OnUserInfoChangeListener> listener : userInfoChangeListeners) {
          if (onUserinfoChangeListener == listener.get()) {
            userInfoChangeListeners.remove(listener);
            return;
          }
        }
      }
    }
  }

  public void unRegisterOnLoginListener(OnLoginListener onLoginListener) {
    synchronized (onLoginListeners) {
      if (!CollectionUtils.isEmpty(onLoginListeners)) {
        for (WeakReference<OnLoginListener> listener : onLoginListeners) {
          if (onLoginListener == listener.get()) {
            userInfoChangeListeners.remove(listener);
            return;
          }
        }
      }
    }
  }

  public void registerOnLoginListener(OnLoginListener onLoginListener) {
    synchronized (onLoginListeners) {
      onLoginListeners.add(new WeakReference<>(onLoginListener));
    }
  }

  public User getUserInfo() {
    return marioAccountManager.getUserInfo();
  }

  public String getAuth() {
    return marioAccountManager.getAuth();
  }

  public void asyncChangeUserInfo(final User user) {
    DataUtils.runAsyncTask(new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object... params) {
        marioAccountManager.changeUserInfo(user);
        return null;
      }
    });
  }

  private void addNewAccount(String authcode, User user) {
    if (user == null) {
      return;
    }
    final Account account = new Account(Constants.ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
    Bundle bundle = new Bundle();
    bundle.putString(UserKeys.NICK, user.getNick());
    bundle.putInt(UserKeys.UID, user.getUid());
    bundle.putString(UserKeys.UDID, user.getUdid());
    bundle.putString(UserKeys.PHONE, user.getPhone());
    bundle.putString(UserKeys.EMAIL, user.getEmail());
    bundle.putInt(UserKeys.GENDER, user.getGender());

    accountManager.addAccountExplicitly(account, "", bundle);
    accountManager.setAuthToken(account, Constants.AUTHTOKEN_TYPE, authcode);
  }

  public enum AccountState {
    NOT_REGISTERED, REGISTERED, LOGINED
  }

  public interface OnUserInfoChangeListener {
    void onSucceed(User newUser);

    void onFail(MarioAccountManager.AccountError accountError, String reason);
  }

  public interface OnLoginListener {
    void onSucceed();

    void onFail(MarioAccountManager.AccountError accountError, String reason);
  }
}
