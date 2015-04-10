package com.lemi.controller.lemigameassistance.net.base.processor;


import com.google.gson.Gson;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class LemiGsonFactory {
  private static Gson gson;

  static {
    gson = new Gson();
  }

  private LemiGsonFactory() {}

  public static Gson getGson() {
    return gson;
  }

}
