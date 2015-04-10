package com.lemi.controller.lemigameassistance;


import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.android.volley.toolbox.ByteArrayPool;
import com.lemi.controller.lemigameassistance.account.GameMasterAccountManager;
import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.manager.BackgroundScheduleManager;
import com.lemi.controller.lemigameassistance.manager.SDCardManager;
import com.lemi.controller.lemigameassistance.service.GameMonitorService;
import com.lemi.controller.lemigameassistance.utils.LogHelper;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.externalmanager.config.TVConfig;
import com.lemi.mario.image.Config;
import com.lemi.mario.image.ImageManager;
import com.lemi.mario.image.view.AsyncImageView;

import java.io.File;

public class GameMasterApplication extends Application {

  private static final int BYTE_ARRAY_MAX_SIZE = 128 * 1024; // 128K

  private static Context context;
  private static ImageManager imageManager;
  private static ByteArrayPool byteArrayPool;

  private static final String INIT_THREAD_NAME = "init_thread";


  @Override
  public void onCreate() {
    super.onCreate();
    context = this;
    GlobalConfig.setAppContext(context);
    GlobalConfig.setDebug(BuildConfig.DEBUG);
    GlobalConfig.setAppRootDir(Constants.ROOT_PATH);
    TVConfig.initTVVersion();
    SDCardManager.getInstance().init();
    LogHelper.initLogReporter(context);
    initByteArrayPool();
    initImageManager();
    BackgroundScheduleManager.getInstance();
    GameMasterAccountManager.getInstance();
    GameMonitorService.launch(this);
    // Initiate shared preferences in another thread to avoid blocking UI.
    new Thread(new Runnable() {
      @Override
      public void run() {
        GameMasterPreferences.preLoadPrefs();
      }
    }, INIT_THREAD_NAME).start();
  }

  public static Context getAppContext() {
    return context;
  }


  public static synchronized ImageManager getImageManager() {
    if (imageManager == null) {
      imageManager =
          new com.lemi.mario.image.ImageManager(context, new SampleImageCacheConfig(),
              byteArrayPool);
    }
    return imageManager;
  }


  private static void initByteArrayPool() {
    byteArrayPool = new ByteArrayPool(BYTE_ARRAY_MAX_SIZE);
  }

  private void initImageManager() {
    AsyncImageView.setImageManagerHolder(new AsyncImageView.ImageManagerHolder() {
      @Override
      public com.lemi.mario.image.ImageManager getImageManager() {
        return GameMasterApplication.getImageManager();
      }
    });
  }


  private static final class SampleImageCacheConfig implements Config {

    private static final int MB = 1024 * 1024;
    private static final String IMAGE_CACHE_PATH = Constants.ROOT_PATH + File.separator + "image";
    private static final int IMAGE_NETWORK_THREAD_POOL_SIZE = 3;
    private static final int IMAGE_LOCAL_THREAD_POOL_SIZE = 1;
    private static final float BITMAP_MEMORY_CACHE_SIZE_SCALE_BELOW_64 = 0.05f;
    private static final float BITMAP_MEMORY_CACHE_SIZE_SCALE_ABOVE_64 = 0.1f;
    private static final int BITMAP_MAX_FILE_CACHE_SIZE = 128 * MB; // 128M

    @Override
    public Context getContext() {
      return context;
    }

    @Override
    public String getFileCacheDir() {
      return IMAGE_CACHE_PATH;
    }

    @Override
    public Resources getResources() {
      return null;
    }

    @Override
    public int getFileCacheSize() {
      return BITMAP_MAX_FILE_CACHE_SIZE;
    }

    @Override
    public int getMemoryCacheSize() {
      int memoryClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
          .getMemoryClass();
      if (memoryClass <= 64) {
        return Math.round(memoryClass * MB * BITMAP_MEMORY_CACHE_SIZE_SCALE_BELOW_64);
      } else {
        return Math.round(memoryClass * MB * BITMAP_MEMORY_CACHE_SIZE_SCALE_ABOVE_64);
      }
    }

    @Override
    public int getLocalThreadPoolSize() {
      return IMAGE_LOCAL_THREAD_POOL_SIZE;
    }

    @Override
    public int getNetworkThreadPoolSize() {
      return IMAGE_NETWORK_THREAD_POOL_SIZE;
    }
  }

}
