package com.lemi.mario.accountmanager.accountStorage;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class AccountStorageFactory {
  public static AccountStorage creator(StorageType type) {
    switch (type) {
      case TYPE_SYSTEM:
        return new SystemAccountStorage();
      case TYPE_SHAREDPREF:
        return new SharedPrefAccountStorage();
      default:
        return new SharedPrefAccountStorage();
    }
  }

  public enum StorageType {
    TYPE_SYSTEM, TYPE_SHAREDPREF
  }
}
