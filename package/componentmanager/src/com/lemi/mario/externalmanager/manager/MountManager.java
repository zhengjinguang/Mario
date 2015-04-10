package com.lemi.mario.externalmanager.manager;

import android.content.Intent;
import android.text.TextUtils;

import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.externalmanager.config.TVConfig;
import com.lemi.mario.externalmanager.model.ExternalStorageInfo;
import com.lemi.mario.externalmanager.socket.MountSocketHelper;
import com.lemi.mario.externalmanager.utils.ExternalStorageUtils;
import com.lemi.mario.externalmanager.utils.MountUtils;
import com.lemi.mario.externalmanager.utils.TagUtils;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class MountManager {

  public static final String MOUNT_ACTION = "sdcard";
  public static final String UN_MOUNT_ACTION = "null";

  private static final String TAG_COPY_COMPLETE = "copy-complete";
  private static final String TAG_COPY_BEGIN = "copy-begin";
  private static final String TAG_LOCAL = "local";

  private static final String LOOP_THREAD_NAME = "loop-thread-name";
  private static final long LOOP_DURATION = 500;

  public enum MountActionType {
    BROADCAST, SOCKET_COMMAND
  }

  public enum MountAction {
    MOUNT, UN_MOUNT
  }

  public enum MountSupportStatus {
    NOT_SUPPORT, SUPPORT
  }

  public enum MountStatus {
    NO_EXTERNAL_STORAGE, NOT_MOUNT, DATA_COPY_COMPLETE, MOUNT
  }

  public enum ExternalStorageStatus {
    NO_EXTERNAL_STORAGE, NOT_FAT32, CANNOT_WRITE, NO_SPACE, VALID
  }

  private static final String ACTION_MOUNTED_SDCARD = "com.lemi.controller.mountpoint";
  private static final String ACTION_EXTRA_SDCARD = "ext_name";
  private static final int EXECUTE_THREAD_NUM = 1;

  private CachedThreadPoolExecutorWithCapacity executeThreadPool;

  private final Set<WeakReference<DataCopyCallListener>> copyListeners = new HashSet<>();
  private final Set<WeakReference<ExternalStorageMountListener>> mountListeners = new HashSet<>();

  private MountSocketHelper mountSocketHelper;

  private static MountManager instance;

  private MountManager() {
    executeThreadPool = new CachedThreadPoolExecutorWithCapacity(EXECUTE_THREAD_NUM);
  }

  public static synchronized MountManager getInstance() {
    if (instance == null) {
      instance = new MountManager();
    }
    return instance;
  }

  public void setDataCopyCallListener(DataCopyCallListener dataCopyCallListener) {
    if (dataCopyCallListener != null) {
      synchronized (copyListeners) {
        copyListeners.add(new WeakReference<>(dataCopyCallListener));
      }
    }
  }

  public void setExternalStorageMountListener(
      ExternalStorageMountListener externalStorageMountListener) {
    if (externalStorageMountListener != null) {
      synchronized (mountListeners) {
        mountListeners.add(new WeakReference<>(externalStorageMountListener));
      }
    }
  }

  public void mountSdCard(MountAction action) {
    mountSdCard(action, MountActionType.SOCKET_COMMAND);
  }

  public void mountSdCard(MountAction action, MountActionType type) {
    if (type == MountActionType.SOCKET_COMMAND) {
      if (mountSocketHelper == null) {
        mountSocketHelper = new MountSocketHelper();
      }
      mountSocketHelper.mountSdCard(action);
    } else {
      mountSdCardByBroadcast(action);
    }
  }

  public MountSupportStatus getSupportStatus() {
    if (TVConfig.isIsLetv() && !TVConfig.isIsLetvBox()) {
      return MountSupportStatus.SUPPORT;
    } else {
      return MountSupportStatus.NOT_SUPPORT;
    }
  }

  public MountStatus getMountStatus() {
    if (MountUtils.isSdCardMounted()) {
      // if local tag file is not exits mount is complete
      if (!TagUtils.isTagFileExistAtLocal(TagUtils.LOCAL_TAG_FILE)) {
        return MountStatus.MOUNT;
      } else {
        return MountStatus.DATA_COPY_COMPLETE;
      }
    } else {
      if (ExternalStorageManager.getInstance().getMaxSizeSdcard() == null) {
        return MountStatus.NO_EXTERNAL_STORAGE;
      } else {
        return MountStatus.NOT_MOUNT;
      }
    }
  }


  public ExternalStorageStatus getExternalStorageStatus() {
    ExternalStorageInfo storageInfo = ExternalStorageManager.getInstance().getMaxSizeSdcard();
    if (storageInfo == null) {
      return ExternalStorageStatus.NO_EXTERNAL_STORAGE;
    }
    if (!FileUtil.canWrite(storageInfo.getPath())) {
      return ExternalStorageStatus.CANNOT_WRITE;
    }
    ExternalStorageInfo localSD = ExternalStorageManager.getInstance().getLocalExternalStorage();
    if (TextUtils.isEmpty(TagUtils.getTagFromExternal(TagUtils.COPY_TAG_FILE)) &&
        FileUtil.getAvailableBytes(storageInfo.getPath()) < FileUtil
            .getUsedBytes(localSD.getPath())) {
      return ExternalStorageStatus.NO_SPACE;
    }
    return ExternalStorageStatus.VALID;
  }

  public void copyDataToSdCard() {
    executeThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        ExternalStorageInfo storageInfo = ExternalStorageManager.getInstance().getMaxSizeSdcard();
        if (storageInfo == null) {
          invokeAndNotifyCopy(new ListenerRunnable<DataCopyCallListener>() {
            @Override
            public void run(DataCopyCallListener listener) {
              listener.onCopyFail();
            }
          });
          return;
        }
        // clear local tag file avoid copy
        TagUtils.clearLocalTagFile(TagUtils.LOCAL_TAG_FILE);
        // Tag use for not-check available size next time
        TagUtils.writeTag(storageInfo.getPath(), TagUtils.COPY_TAG_FILE, TAG_COPY_BEGIN);
        int ret =
            FileUtil.copyAllFiles(ExternalStorageUtils.LOCAL_SDCARD_PATH, storageInfo.getPath());
        if (ret < 0) {
          invokeAndNotifyCopy(new ListenerRunnable<DataCopyCallListener>() {
            @Override
            public void run(DataCopyCallListener listener) {
              listener.onCopyFail();
            }
          });
          return;
        }
        // Tag use for not-check available size next time
        TagUtils.writeTag(storageInfo.getPath(), TagUtils.COPY_TAG_FILE, TAG_COPY_COMPLETE);
        // Tag use check sd-card is mount
        TagUtils.writeTag(ExternalStorageUtils.LOCAL_SDCARD_PATH, TagUtils.LOCAL_TAG_FILE,
            TAG_LOCAL);
        invokeAndNotifyCopy(new ListenerRunnable<DataCopyCallListener>() {
          @Override
          public void run(DataCopyCallListener listener) {
            listener.onCopyComplete();
          }
        });
        loopCheckMountComplete();
      }
    });
  }

  public boolean isMountSDCard(String name) {
    if (ExternalStorageUtils.SDCARD0_NAME.equals(name)
        || ExternalStorageUtils.S50_SDCARD_NAME.equals(name)
        || ExternalStorageUtils.LOCAL_SDCARD_NAME.equals(name)) {
      return true;
    }
    return false;
  }

  public boolean isMountCardWritable(ExternalStorageInfo storageInfo) {
    if (storageInfo != null && FileUtil.canWrite(storageInfo.getPath())) {
      return true;
    }
    return false;
  }

  public boolean isMountCardFat32(ExternalStorageInfo storageInfo) {
    if (storageInfo != null && FileUtil.canWrite(storageInfo.getPath())) {
      return true;
    }
    return false;
  }

  public interface DataCopyCallListener {
    void onCopyComplete();

    void onCopyFail();
  }

  public interface ExternalStorageMountListener {
    void onMountComplete();
  }

  private void invokeAndNotifyCopy(final ListenerRunnable<DataCopyCallListener> runnable) {
    synchronized (copyListeners) {
      Iterator<WeakReference<DataCopyCallListener>> iterator = copyListeners.iterator();
      while (iterator.hasNext()) {
        WeakReference<DataCopyCallListener> reference = iterator.next();
        final DataCopyCallListener listener = reference.get();
        if (listener == null) {
          iterator.remove();
        } else {
          MainThreadPostUtils.post(new Runnable() {
            @Override
            public void run() {
              runnable.run(listener);
            }
          });
        }
      }
    }
  }

  private void invokeAndNotifyMount(final ListenerRunnable<ExternalStorageMountListener> runnable) {
    synchronized (mountListeners) {
      Iterator<WeakReference<ExternalStorageMountListener>> iterator = mountListeners.iterator();
      while (iterator.hasNext()) {
        WeakReference<ExternalStorageMountListener> reference = iterator.next();
        final ExternalStorageMountListener listener = reference.get();
        if (listener == null) {
          iterator.remove();
        } else {
          MainThreadPostUtils.post(new Runnable() {
            @Override
            public void run() {
              runnable.run(listener);
            }
          });
        }
      }
    }
  }

  private interface ListenerRunnable<T> {
    void run(T listener);
  }

  private void mountSdCardByBroadcast(MountAction action) {
    String command;
    if (action == MountAction.MOUNT) {
      command = MOUNT_ACTION;
    } else {
      command = UN_MOUNT_ACTION;
    }
    Intent intent = new Intent();
    intent.setAction(ACTION_MOUNTED_SDCARD);
    intent.putExtra(ACTION_EXTRA_SDCARD, command);
    GlobalConfig.getAppContext().sendBroadcast(intent);
  }

  /**
   * because mount complete can not receive broadcast, so use loop check
   */
  public void loopCheckMountComplete() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          if (getMountStatus() == MountStatus.MOUNT) {
            invokeAndNotifyMount(new ListenerRunnable<ExternalStorageMountListener>() {
              @Override
              public void run(ExternalStorageMountListener listener) {
                listener.onMountComplete();
              }
            });
            break;
          } else {
            try {
              Thread.sleep(LOOP_DURATION);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }, LOOP_THREAD_NAME).start();
  }

}
