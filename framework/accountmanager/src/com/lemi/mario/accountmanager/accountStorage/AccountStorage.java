package com.lemi.mario.accountmanager.accountStorage;

import com.lemi.mario.accountmanager.model.User;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public interface AccountStorage {
  void saveAuth(String auth);

  void saveUserInfo(User user);

  String getAuth();

  User getUserInfo();

  void invalidateAuth();
}
