package com.lemi.mario.accountmanager.net.processor;


import com.google.gson.Gson;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class AccountGsonFactory {
  private static Gson gson;

  static {
    gson = new Gson();
  }

  private AccountGsonFactory() {}

  public static Gson getGson() {
    return gson;
  }

}
