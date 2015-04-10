package com.lemi.mario.accountmanager;

import android.annotation.TargetApi;
import android.os.Build;

import com.lemi.mario.accountmanager.accountStorage.AccountStorage;
import com.lemi.mario.accountmanager.model.User;
import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengjinguang@letv.com (shining).
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class AccountDataUpdater {
  private static final long THREAD_ALIVE_TIME = 30000l;
  private static final int MAX_THREAD_NUM = 3;
  private final List<AccountStorage> accountStorageList = new ArrayList<>();
  private CachedThreadPoolExecutorWithCapacity threadPool;

  public AccountDataUpdater(AccountStorage... accountStorage) {
    synchronized (accountStorageList) {
      for (int i = 0; i < accountStorage.length; i++) {
        accountStorageList.add(accountStorage[i]);
      }
    }
    threadPool = new CachedThreadPoolExecutorWithCapacity(MAX_THREAD_NUM, THREAD_ALIVE_TIME);
  }

  public void updateAuth(final String auth) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        synchronized (accountStorageList) {
          for (int i = 0; i < accountStorageList.size(); i++) {
            if (accountStorageList.get(i) != null) {
              accountStorageList.get(i).saveAuth(auth);
            }
          }
        }
      }
    });
  }

  public void updateUserInfo(final User user) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        synchronized (accountStorageList) {
          for (int i = 0; i < accountStorageList.size(); i++) {
            if (accountStorageList.get(i) != null) {
              accountStorageList.get(i).saveUserInfo(user);
            }
          }
        }
      }
    });
  }

  public void removeAuth() {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        synchronized (accountStorageList) {
          for (int i = 0; i < accountStorageList.size(); i++) {
            if (accountStorageList.get(i) != null) {
              accountStorageList.get(i).invalidateAuth();
            }
          }
        }
      }
    });
  }

  public void runTask(Runnable runnable) {
    threadPool.execute(runnable);
  }
}
