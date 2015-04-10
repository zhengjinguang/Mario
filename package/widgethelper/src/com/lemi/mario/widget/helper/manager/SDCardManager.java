package com.lemi.mario.widget.helper.manager;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.externalmanager.manager.ExternalStorageManager;
import com.lemi.mario.externalmanager.manager.MountManager;
import com.lemi.mario.externalmanager.manager.MountManager.ExternalStorageStatus;
import com.lemi.mario.externalmanager.manager.MountMessageCenter;
import com.lemi.mario.externalmanager.model.ExternalStorageInfo;
import com.lemi.mario.widget.helper.R;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SDCardManager {

  private static SDCardManager instance;

  private SDCardManager() {
    setMountReceiver();
    updateMountTask();
  }

  public enum MountProcessTask {
    CHECK, COPY, RESTART, IDLE, COMPLETE
  }

  public enum ProcessError {
    NOT_SUPPORT, NO_EXTERNAL_STORAGE, NOT_FAT32, CANNOT_WRITE, NO_SPACE, DATA_COPY_FAILED,
    DUPLICATE, ALREADY_COMPLETE
  }

  private MountProcessTask mountProcessTask = MountProcessTask.IDLE;
  private final Set<WeakReference<MountProcessListener>> processListeners = new HashSet<>();

  public static synchronized SDCardManager getInstance() {
    if (instance == null) {
      instance = new SDCardManager();
    }
    return instance;
  }

  public MountProcessTask getMountProcessTask() {
    updateMountTask();
    return mountProcessTask;
  }

  public ExternalStorageInfo getMountCard() {
    return ExternalStorageManager.getInstance().getMaxSizeSdcard();
  }

  public boolean isMountCardFat32(ExternalStorageInfo storageInfo) {
    return MountManager.getInstance().isMountCardFat32(storageInfo);
  }

  public boolean isMountCardWritable(ExternalStorageInfo storageInfo) {
    return MountManager.getInstance().isMountCardWritable(storageInfo);
  }

  public boolean isMountSuccess() {
    return MountManager.getInstance().getMountStatus() == MountManager.MountStatus.MOUNT;
  }

  public void setMountProcessListener(MountProcessListener processListener) {
    if (processListener != null) {
      synchronized (processListeners) {
        processListeners.add(new WeakReference<>(processListener));
      }
    }
  }

  public void startUnMount() {
    MountManager.getInstance().mountSdCard(MountManager.MountAction.UN_MOUNT);
  }

  public void startMount() {
    switch (mountProcessTask) {
      case IDLE:
        checkMountCondition();
        break;
      case CHECK:
        handleTipsError(MountProcessTask.CHECK, ProcessError.DUPLICATE);
        break;
      case COPY:
        handleTipsError(MountProcessTask.COPY, ProcessError.DUPLICATE);
        break;
      case RESTART:
        waitRestart();
        break;
      case COMPLETE:
        handleTipsError(MountProcessTask.COMPLETE, ProcessError.ALREADY_COMPLETE);
        break;
    }
  }

  public void waitForRestart() {
    MountManager.getInstance().loopCheckMountComplete();
  }

  public interface MountProcessListener {
    void receiveMountBroadcast(Intent intent);

    void processTaskChange(MountProcessTask task);

    void processTaskFail(MountProcessTask task, ProcessError error);
  }

  private interface ListenerRunnable<T> {
    void run(T listener);
  }

  private void updateMountTask() {
    MountManager.MountStatus status = MountManager.getInstance().getMountStatus();
    if (status == MountManager.MountStatus.MOUNT) {
      mountProcessTask = MountProcessTask.COMPLETE;
    } else if (status == MountManager.MountStatus.DATA_COPY_COMPLETE) {
      mountProcessTask = MountProcessTask.RESTART;
    } else {
      mountProcessTask = MountProcessTask.IDLE;
    }
  }


  private void invokeAndNotifyProcessStatusChange(
      final ListenerRunnable<MountProcessListener> runnable) {
    synchronized (processListeners) {
      Iterator<WeakReference<MountProcessListener>> iterator = processListeners.iterator();
      while (iterator.hasNext()) {
        WeakReference<MountProcessListener> reference = iterator.next();
        final MountProcessListener listener = reference.get();
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

  private void receiveBroadcast(final Intent intent) {
    invokeAndNotifyProcessStatusChange(new ListenerRunnable<MountProcessListener>() {
      @Override
      public void run(MountProcessListener listener) {
        listener.receiveMountBroadcast(intent);
      }
    });
  }

  private void notifyStatus(final MountProcessTask task) {
    mountProcessTask = task;
    invokeAndNotifyProcessStatusChange(new ListenerRunnable<MountProcessListener>() {
      @Override
      public void run(MountProcessListener listener) {
        listener.processTaskChange(task);
      }
    });
  }

  private void handleTipsError(final MountProcessTask task, final ProcessError error) {
    invokeAndNotifyProcessStatusChange(new ListenerRunnable<MountProcessListener>() {
      @Override
      public void run(MountProcessListener listener) {
        listener.processTaskFail(task, error);
      }
    });
  }

  private void handleError(final MountProcessTask task, final ProcessError error) {
    mountProcessTask = MountProcessTask.IDLE;
    invokeAndNotifyProcessStatusChange(new ListenerRunnable<MountProcessListener>() {
      @Override
      public void run(MountProcessListener listener) {
        listener.processTaskFail(task, error);
      }
    });
  }

  private void checkMountCondition() {
    notifyStatus(MountProcessTask.CHECK);
    if (MountManager.getInstance().getSupportStatus() == MountManager.MountSupportStatus.NOT_SUPPORT) {
      handleError(MountProcessTask.CHECK, ProcessError.NOT_SUPPORT);
      return;
    }
    ExternalStorageStatus externalStorageStatus =
        MountManager.getInstance().getExternalStorageStatus();
    switch (externalStorageStatus) {
      case VALID:
        copyData();
        break;
      case NO_EXTERNAL_STORAGE:
        handleError(MountProcessTask.CHECK, ProcessError.NO_EXTERNAL_STORAGE);
        break;
      case NOT_FAT32:
        handleError(MountProcessTask.CHECK, ProcessError.NOT_FAT32);
        break;
      case CANNOT_WRITE:
        handleError(MountProcessTask.CHECK, ProcessError.CANNOT_WRITE);
        break;
      case NO_SPACE:
        handleError(MountProcessTask.CHECK, ProcessError.NO_SPACE);
        break;
      default:
        break;
    }
  }

  private void copyData() {
    notifyStatus(MountProcessTask.COPY);
    MountManager.getInstance().copyDataToSdCard();
  }

  private void executeMountCommand() {
    MountManager.getInstance().mountSdCard(MountManager.MountAction.MOUNT);
  }

  private void waitRestart() {
    notifyStatus(MountProcessTask.RESTART);
  }

  private void setMountReceiver() {
    MountMessageCenter.getInstance().setMountMessageListener(mountMessageListener);
    MountManager.getInstance().setExternalStorageMountListener(storageMountListener);
    MountManager.getInstance().setDataCopyCallListener(dataCopyCallListener);
  }

  private MountMessageCenter.MountMessageListener mountMessageListener =
      new MountMessageCenter.MountMessageListener() {
        @Override
        public void onMessage(Context context, Intent intent) {
          if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
          }
          receiveBroadcast(intent);
        }
      };

  private MountManager.ExternalStorageMountListener storageMountListener =
      new MountManager.ExternalStorageMountListener() {
        @Override
        public void onMountComplete() {
          if (mountProcessTask == MountProcessTask.RESTART) {
            notifyStatus(MountProcessTask.COMPLETE);
            MainThreadPostUtils.toast(R.string.mount_complete);
          }
        }
      };

  private MountManager.DataCopyCallListener dataCopyCallListener =
      new MountManager.DataCopyCallListener() {
        @Override
        public void onCopyComplete() {
          executeMountCommand();
          waitRestart();
        }

        @Override
        public void onCopyFail() {
          handleError(MountProcessTask.CHECK, ProcessError.DATA_COPY_FAILED);
        }
      };
}
