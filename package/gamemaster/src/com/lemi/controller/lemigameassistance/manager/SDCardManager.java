package com.lemi.controller.lemigameassistance.manager;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.dialog.GameMasterDialog;
import com.lemi.controller.lemigameassistance.utils.ThreadPool;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.DialogUtils;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.StringUtil;
import com.lemi.mario.externalmanager.config.TVConfig;
import com.lemi.mario.externalmanager.manager.ExternalStorageManager;
import com.lemi.mario.externalmanager.manager.MountManager;
import com.lemi.mario.externalmanager.model.ExternalStorageInfo;
import com.lemi.mario.externalmanager.utils.ExternalStorageUtils;
import com.lemi.mario.externalmanager.utils.MountUtils;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SDCardManager {

  private static final int ESTIMATE_CONFIG = 200;

  private static SDCardManager instance;

  private SDCardSupportStatus sdCardSupportStatus;
  private SDCardStatus sdCardStatus;
  private ExternalStorageInfo sdCard;
  private boolean initComplete = false;

  private Dialog removeDialog;
  private Dialog mountDialog;
  private Dialog errorDialog;
  private Dialog processDialog;
  private Dialog successDialog;


  public enum SDCardSupportStatus {
    NOT_SUPPORT, NOT_ENABLED, ENABLED
  }

  public enum SDCardStatus {
    NOT_SUPPORT, NO_SDCARD, NO_MOUNT, DATA_COPY_COMPLETE, ENABLED
  }

  public enum ProcessTask {
    REMOVE, MOUNT, NOT_FOUND
  }

  private enum MountStatus {
    SDCARD_CANNOT_WRITE, SDCARD_NOT_FORMAT, SDCARD_NO_SPACE, SDCARD_READY
  }

  private final Set<WeakReference<SDCardMountListener>> listeners = new HashSet<>();
  private DataCopyCallBack dataCopyCallBack = new DataCopyCallBack() {
    @Override
    public void onCopyComplete(Context context) {
      buildAndShowMountSuccessDialog(context);
    }

    @Override
    public void onCopyFail(Context context) {
      MainThreadPostUtils.toast(R.string.sdcard_copy_failed);
    }
  };

  public static synchronized SDCardManager getInstance() {
    if (instance == null) {
      instance = new SDCardManager();
    }
    return instance;
  }

  private SDCardManager() {
    asyncInitSdCardSupportStatus();
    registerReceiver();
  }

  /**
   * init Manager
   */
  public void init() {}

  public boolean isInitComplete() {
    return initComplete;
  }

  public void setSDCardMountListener(SDCardMountListener sdCardMountListener) {
    if (sdCardMountListener != null) {
      synchronized (listeners) {
        listeners.add(new WeakReference<>(sdCardMountListener));
      }
    }
  }

  public SDCardSupportStatus getSdCardSupportStatus() {
    return sdCardSupportStatus;
  }

  public SDCardStatus getSdCardStatus() {
    return sdCardStatus;
  }

  public void initSdCardStatus() {
    if (sdCardSupportStatus == SDCardSupportStatus.ENABLED) {
      sdCardStatus = SDCardStatus.ENABLED;
    } else if (sdCardSupportStatus == SDCardSupportStatus.NOT_ENABLED) {
      updateSdCardUnMountStatus();
    }
  }

  public void updateSdCardUnMountStatus() {
    sdCard = ExternalStorageManager.getInstance().getMaxSizeSdcard();
    if (sdCard == null) {
      sdCardStatus = SDCardStatus.NO_SDCARD;
    } else {
      sdCardStatus = SDCardStatus.NO_MOUNT;
    }
  }

  public void showAlertDialog(Context context, ProcessTask processTask) {
    if (DialogUtils.isContextFinished(context)) {
      return;
    }
    switch (processTask) {
      case REMOVE:
        buildAndShowRemoveDialog(context);
        break;
      case MOUNT:
        buildAndShowMountDialog(context);
        break;
      case NOT_FOUND:
        buildAndShowNotFoundDialog(context);
        break;
    }
  }



  private void asyncInitSdCardSupportStatus() {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        initSdCardSupportStatus();
        initComplete = true;
      }
    });

  }

  private void initSdCardSupportStatus() {
    if (TVConfig.isIsLetv() && !TVConfig.isIsLetvBox()) {
      if (MountUtils.isSdCardMounted()) {
        sdCardSupportStatus = SDCardSupportStatus.ENABLED;
      } else {
        sdCardSupportStatus = SDCardSupportStatus.NOT_ENABLED;
      }
    } else {
      sdCardSupportStatus = SDCardSupportStatus.NOT_SUPPORT;
    }
  }



  private MountStatus checkSdCardCanMount(ExternalStorageInfo udisk) {
    if (udisk == null) {
      return MountStatus.SDCARD_CANNOT_WRITE;
    }
    if (FileUtil.getUsedBytes(udisk.getPath()) > udisk.getTotalSize() / 100) {
      return MountStatus.SDCARD_NOT_FORMAT;
    }
    ExternalStorageInfo localSD = ExternalStorageManager.getInstance().getLocalExternalStorage();
    if (FileUtil.getAvailableBytes(udisk.getPath()) < FileUtil.getUsedBytes(localSD.getPath())) {
      return MountStatus.SDCARD_NO_SPACE;
    }
    if (!FileUtil.canWrite(udisk.getPath())) {
      return MountStatus.SDCARD_CANNOT_WRITE;
    }
    return MountStatus.SDCARD_READY;
  }

  private void removeSDCardMountByInnerBroadcast() {
    MountManager.getInstance().mountSdCard(MountManager.MountAction.UN_MOUNT,
        MountManager.MountActionType.BROADCAST);
  }

  private void mountSDCardByInnerBroadcast() {
    MountManager.getInstance().mountSdCard(MountManager.MountAction.MOUNT,
        MountManager.MountActionType.BROADCAST);
  }


  private void mountSdCard(Context context) {
    switch (checkSdCardCanMount(sdCard)) {
      case SDCARD_CANNOT_WRITE:
        MainThreadPostUtils.toast(R.string.sdcard_error);
        break;
      case SDCARD_NO_SPACE:
        MainThreadPostUtils.toast(R.string.sdcard_no_space);
        break;
      case SDCARD_NOT_FORMAT:
        MainThreadPostUtils.toast(R.string.sdcard_not_format);
        break;
      case SDCARD_READY:
        buildAndShowDataCopyProcessingDialog(context);
        copyDataToSdCard(context);
        break;

    }
  }

  private void buildAndShowRemoveDialog(final Context context) {
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        if (DialogUtils.isContextFinished(context)) {
          return;
        }
        DialogUtils.dismissDialog(removeDialog);
        removeDialog = new GameMasterDialog.Builder(context)
            .setTitle(R.string.sdcard_rm_title)
            .setMessage(R.string.sdcard_rm_msg)
            .setPositiveButton(R.string.sdcard_stop, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                sdCardStatus = SDCardStatus.NO_SDCARD;
                notifyUserUnMountComplete();
                removeSDCardMountByInnerBroadcast();
              }
            })
            .setNegativeButton(R.string.dialogcancel, null)
            .create();
        DialogUtils.showDialog(removeDialog);
      }
    });
  }

  private void buildAndShowNotFoundDialog(final Context context) {
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        if (DialogUtils.isContextFinished(context)) {
          return;
        }
        DialogUtils.dismissDialog(errorDialog);
        errorDialog = new GameMasterDialog.Builder(context)
            .setMessage(R.string.sdcard_none)
            .setPositiveButton(R.string.dialogok, null)
            .create();
        DialogUtils.showDialog(errorDialog);
      }
    });
  }

  private void buildAndShowMountDialog(final Context context) {
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        if (DialogUtils.isContextFinished(context)) {
          return;
        }
        DialogUtils.dismissDialog(mountDialog);
        mountDialog = new GameMasterDialog.Builder(context, R.style.dialog_big)
            .setTitle(R.string.sdcard_title)
            .setMessage(R.string.sdcard_start_msg)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                mountSdCard(context);
              }
            })
            .setNegativeButton(R.string.cancel, null)
            .create();
        DialogUtils.showDialog(mountDialog);
      }
    });

  }


  private void buildAndShowDataCopyProcessingDialog(final Context context) {
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        long time =
            FileUtil.getUsedBytes(ExternalStorageManager.getInstance().getLocalExternalStorage()
                .getPath())
                / Constants.MB / ESTIMATE_CONFIG;
        if (time == 0) {
          time = 1;
        }
        if (DialogUtils.isContextFinished(context)) {
          return;
        }
        DialogUtils.dismissDialog(processDialog);
        processDialog = new GameMasterDialog.Builder(context, R.style.dialog_progress)
            .setMessage(StringUtil.getString(R.string.sdcard_copying_time, (int) time))
            .create();
        processDialog.setCancelable(false);
        DialogUtils.showDialog(processDialog);
      }
    });

  }

  private void buildAndShowMountSuccessDialog(final Context context) {
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        if (DialogUtils.isContextFinished(context)) {
          return;
        }
        DialogUtils.dismissDialog(successDialog);
        successDialog = new GameMasterDialog.Builder(context)
            .setMessage(R.string.sdcard_using_sucess)
            .setPositiveButton(R.string.dialogok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                if (MountUtils.isSdCardMounted()) {
                  notifyUserMountComplete();
                }
              }
            })
            .create();
        DialogUtils.showDialog(successDialog);
      }
    });

  }

  private void copyDataToSdCard(final Context context) {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        if (sdCard == null) {
          dataCopyFinish();
          if (dataCopyCallBack != null) {
            dataCopyCallBack.onCopyFail(context);
          }
          return;
        }
        if (FileUtil.copyAllFiles(Constants.SDCARD_PATH, sdCard.getPath()) < 0) {
          dataCopyFinish();
          if (dataCopyCallBack != null) {
            dataCopyCallBack.onCopyFail(context);
          }
          return;
        }
        sdCardStatus = SDCardStatus.DATA_COPY_COMPLETE;
        mountSDCardByInnerBroadcast();
        dataCopyFinish();
        if (dataCopyCallBack != null) {
          dataCopyCallBack.onCopyComplete(context);
        }
      }
    });
  }

  private void dataCopyFinish() {
    DialogUtils.dismissDialog(processDialog);
  }

  private void notifyUserMountComplete() {
    invokeAndNotify(new ListenerRunnable() {
      @Override
      public void run(SDCardMountListener listener) {
        listener.onMountComplete();
      }
    });
    MainThreadPostUtils.toast(R.string.sdcard_start_sucess);
    sdCardSupportStatus = SDCardSupportStatus.ENABLED;
  }

  private void notifyUserUnMountComplete() {
    invokeAndNotify(new ListenerRunnable() {
      @Override
      public void run(SDCardMountListener listener) {
        listener.onUnMountComplete();
      }
    });
  }

  private void invokeAndNotify(final ListenerRunnable runnable) {
    synchronized (listeners) {
      Iterator<WeakReference<SDCardMountListener>> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        WeakReference<SDCardMountListener> reference = iterator.next();
        final SDCardMountListener listener = reference.get();
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

  private interface ListenerRunnable {
    void run(SDCardMountListener listener);
  }

  public interface DataCopyCallBack {
    void onCopyComplete(Context context);

    void onCopyFail(Context context);
  }

  public interface SDCardMountListener {
    void onMountComplete();

    void onUnMountComplete();
  }

  private void registerReceiver() {
    IntentFilter sdFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
    sdFilter.addAction(Intent.ACTION_MEDIA_EJECT);
    sdFilter.addDataScheme("file");
    GlobalConfig.getAppContext().registerReceiver(sdCardMountRemoveReceiver, sdFilter);
  }

  private BroadcastReceiver sdCardMountRemoveReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent == null || TextUtils.isEmpty(intent.getAction())) {
        return;
      }
      if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
        if (intent.getData().getPath().contains(ExternalStorageUtils.SDCARD0_NAME)
            || intent.getData().getPath().contains(ExternalStorageUtils.S50_SDCARD_NAME)
            || intent.getData().getPath().contains(ExternalStorageUtils.LOCAL_SDCARD_NAME)) {
          if (sdCardStatus == SDCardStatus.DATA_COPY_COMPLETE) {
            sdCardStatus = SDCardStatus.ENABLED;
            DialogUtils.dismissDialog(successDialog);
            notifyUserMountComplete();
            return;
          }
        }
      } else if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
        if (intent.getData().getPath().contains(ExternalStorageUtils.SDCARD0_NAME)
            || intent.getData().getPath().contains(ExternalStorageUtils.S50_SDCARD_NAME)) {
          if (sdCardStatus == SDCardStatus.NO_MOUNT) {
            sdCardStatus = SDCardStatus.NO_SDCARD;
          }
        }
      }
    }
  };

}
