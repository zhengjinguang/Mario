package com.lemi.mario.sample.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.android.volley.toolbox.ByteArrayPool;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.image.Config;
import com.lemi.mario.image.ImageManager;
import com.lemi.mario.image.view.AsyncImageView;
import com.lemi.mario.sample.BuildConfig;
import com.lemi.mario.sample.Constants;
import com.lemi.mario.sample.utils.LogHelper;

import java.io.File;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SampleApplication extends Application {

  private static final int BYTE_ARRAY_MAX_SIZE = 128 * 1024; // 128K

  private static final String SAMPLE = "sample-lx";
  private static Context context;
  private static ImageManager imageManager;
  private static ByteArrayPool byteArrayPool;


  @Override
  public void onCreate() {
    super.onCreate();
    context = this;

    GlobalConfig.setAppContext(context);
    GlobalConfig.setAppRootDir(SAMPLE);
    LogHelper.initLogReporter(context);
    GlobalConfig.setDebug(BuildConfig.DEBUG);
    initByteArrayPool();
    initImageManager();

  }

  private static void initByteArrayPool() {
    byteArrayPool = new ByteArrayPool(BYTE_ARRAY_MAX_SIZE);
  }

  private void initImageManager() {
    AsyncImageView.setImageManagerHolder(new AsyncImageView.ImageManagerHolder() {
      @Override
      public ImageManager getImageManager() {
        if (imageManager == null) {
          imageManager = new ImageManager(context, new SampleImageCacheConfig(), byteArrayPool);
        }
        return imageManager;
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
    private static final int BITMAP_MAX_FILE_CACHE_SIZE = 64 * MB; // 64M

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
