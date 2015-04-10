package com.lemi.controller.lemigameassistance.manager;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.GameMasterPreferences;
import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.dialog.GameMasterDialog;
import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.download.DownloadListener;
import com.lemi.controller.lemigameassistance.download.DownloadManager;
import com.lemi.controller.lemigameassistance.download.DownloadRequestBuilder;
import com.lemi.controller.lemigameassistance.model.CheckVersionModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.utils.ThreadPool;
import com.lemi.mario.appmanager.AppManager;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.DialogUtils;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.SystemUtil;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class UpgradeManager {

  public static final String GAME_MASTER_UPGRADE_SELF_ID = "GameMaster";

  private static UpgradeManager instance;

  private Dialog versionAlertDialog;

  private DownloadListener downloadListener = new DownloadListener() {
    @Override
    public void onProgressChanged(DownloadInfo info) {}

    @Override
    public void onStatusChanged(DownloadInfo info) {
      if (info != null && GAME_MASTER_UPGRADE_SELF_ID.equals(info.getIdentity())) {
        if (info.getStatus() == DownloadInfo.Status.SUCCESS) {
          install(info.getFilePath());
        } else if (info.getStatus() == DownloadInfo.Status.FAILED) {
          MainThreadPostUtils.toast(R.string.updating_download_failed);
        }
      }
    }
  };

  public static synchronized UpgradeManager getInstance() {
    if (instance == null) {
      instance = new UpgradeManager();
    }
    return instance;
  }

  private UpgradeManager() {
    DownloadManager.getInstance().addInvisibleListener(downloadListener);
  }


  /**
   * check update in background and store in preference ,it will show dialog in
   * {@link com.lemi.controller.lemigameassistance.activity.ExploreActivity} onCreate()
   */
  public void checkUpgradeBackGround(final boolean isForceCheck) {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        checkUpgrade(isForceCheck);
      }
    });
  }

  /**
   * check update and show process dialog and alert dialog
   * 
   * @param isForceCheck if true will ignore the ignore version
   */
  public void checkUpgradeAndShowDialog(final Context context, final UpgradeCallback callback,
      final boolean isForceCheck) {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        final CheckVersionModel versionModel = checkUpgrade(isForceCheck);
        if (versionModel != null) {
          if (callback != null) {
            MainThreadPostUtils.post(new Runnable() {
              @Override
              public void run() {
                callback.onUpgrade(versionModel);
              }
            });
          }
          buildAndShowAlertDialog(context, versionModel);
        } else {
          if (callback != null) {
            MainThreadPostUtils.post(new Runnable() {
              @Override
              public void run() {
                callback.onNoUpgrade();
              }
            });
          }
          MainThreadPostUtils.toast(R.string.version_newest);
        }
      }
    });
  }

  public void buildAndShowAlertDialog(final Context context, final CheckVersionModel versionModel) {
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        if (DialogUtils.isContextFinished(context)) {
          return;
        }
        DialogUtils.dismissDialog(versionAlertDialog);
        versionAlertDialog = new GameMasterDialog.Builder(context)
            .setMessage(R.string.new_version_dialog)
            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                upgrade(versionModel);
                MainThreadPostUtils.toast(R.string.updating);
              }
            })
            .setNegativeButton(R.string.dialog_cancel, null)
            .create();
        DialogUtils.showDialog(versionAlertDialog);
      }
    });
  }

  public void buildAndShowAlertDialog(final Context context, final CheckVersionModel versionModel,
      final Runnable positiveRunnable, final Runnable negativeRunnable) {
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        if (DialogUtils.isContextFinished(context)) {
          return;
        }
        DialogUtils.dismissDialog(versionAlertDialog);
        versionAlertDialog = new GameMasterDialog.Builder(context)
            .setMessage(R.string.new_version_dialog)
            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                upgrade(versionModel);
                MainThreadPostUtils.toast(R.string.updating);
                if (positiveRunnable != null) {
                  positiveRunnable.run();
                }
              }
            })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                if (negativeRunnable != null) {
                  negativeRunnable.run();
                }
              }
            })
            .create();
        DialogUtils.showDialog(versionAlertDialog);
      }
    });
  }

  public CheckVersionModel checkUpgrade(boolean isForceCheck) {
    int baseVersion = getBaseVersion(isForceCheck);
    CheckVersionModel versionModel = GameMasterHttpHelper.checkVersion(baseVersion);
    if (checkVersionModelValid(versionModel)) {
      GameMasterPreferences.setHasUpgrade(true);
      return versionModel;
    }
    GameMasterPreferences.setHasUpgrade(false);
    return null;
  }

  private void upgrade(CheckVersionModel versionModel) {
    DownloadManager.getInstance().startAsync(
        DownloadRequestBuilder.buildDownloadRequest(versionModel));
  }

  private void install(String path) {
    if (TextUtils.isEmpty(path)) {
      return;
    }
    AppManager.getInstance().installApk(path);
  }

  private int getBaseVersion(boolean isForceCheck) {
    int currentVersionCode;
    int ignoreVersion;
    if (isForceCheck) {
      ignoreVersion = 0;
    } else {
      ignoreVersion = GameMasterPreferences.getIgnoreVersionCode();
    }
    currentVersionCode = SystemUtil.getVersionCode(GlobalConfig.getAppContext());

    return currentVersionCode > ignoreVersion ? currentVersionCode : ignoreVersion;
  }

  private boolean checkVersionModelValid(CheckVersionModel versionModel) {
    if (versionModel == null || TextUtils.isEmpty(versionModel.getApkUrl())) {
      return false;
    }
    return true;
  }


  public interface UpgradeCallback {
    void onNoUpgrade();

    void onUpgrade(CheckVersionModel versionModel);
  }

}
