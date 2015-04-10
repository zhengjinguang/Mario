package com.lemi.mario.image;

import android.content.Context;
import android.content.res.Resources;

/**
 * Image manager configurations.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface Config {

  Context getContext();

  String getFileCacheDir();

  Resources getResources();

  int getFileCacheSize();

  int getMemoryCacheSize();

  int getNetworkThreadPoolSize();

  int getLocalThreadPoolSize();

}
