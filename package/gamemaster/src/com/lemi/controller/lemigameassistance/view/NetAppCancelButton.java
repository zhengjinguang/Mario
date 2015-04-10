package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.download.DownloadManager;
import com.lemi.controller.lemigameassistance.manager.BackgroundScheduleManager;
import com.lemi.controller.lemigameassistance.manager.CleanManager;
import com.lemi.controller.lemigameassistance.manager.InstallManager;
import com.lemi.controller.lemigameassistance.manager.ZipManager;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.controller.lemigameassistance.utils.ThreadPool;
import com.lemi.mario.appmanager.AppManager;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class NetAppCancelButton extends NetAppButton {

  /**
   * Action of button.
   */
  public static enum Action {
    CANCEL_DOWNLOAD(R.string.download_manage_cancel, STATE_DOWNLOADING) {
      @Override
      protected void onClick(final NetAppButton button) {
        cancelDownload(button);
      }
    },
    CANCEL_UNZIP(R.string.download_manage_cancel, STATE_UNZIPING) {
      @Override
      protected void onClick(final NetAppButton button) {
        cancelUnzip(button);
        cleanFiles(button);
      }
    },
    INVISIBLE(R.string.download_manage_cancel, STATE_OPEN) {
      @Override
      public void onClick(final NetAppButton button) {}
    };

    private final int textResId;
    private final int[] drawableState;

    private Action(int textResId, int[] drawableState) {
      this.textResId = textResId;
      this.drawableState = drawableState;
    }

    private int[] getDrawableState() {
      return drawableState;
    }

    private void apply(NetAppButton button) {
      button.setText(textResId);
    }

    protected abstract void onClick(NetAppButton button);
  }

  public NetAppCancelButton(Context context) {
    super(context);
  }

  public NetAppCancelButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  private Action action;

  public void setAction(Action action) {
    if (this.action != action) {
      if (action == Action.INVISIBLE) {
        setVisibility(INVISIBLE);
      } else {
        setVisibility(VISIBLE);
      }
      this.action = action;
      action.apply(this);
      refreshDrawableState();
    }
  }

  protected void onClick() {
    if (action != null) {
      action.onClick(this);
    }
  }

  @Override
  protected void initButtonStat() {
    if (AppManager.getInstance().syncIsAppInstalled(packageName)) {
      setAction(Action.INVISIBLE);
      return;
    }
    InstallManager.InstallStatus installStatus =
        InstallManager.getInstance().getStatus(packageName);
    if (installStatus != null) {
      updateInstallStat(installStatus);
      return;
    }
    ZipManager.ZipStatus unzipStatus =
        ZipManager.getInstance().getStatus(packageName);
    if (unzipStatus != null) {
      updateUnzipStat(unzipStatus);
      return;
    }
    setAction(Action.CANCEL_DOWNLOAD);
    DataUtils.runAsyncTask(new AsyncTask<Void, Void, DownloadInfo>() {
      @Override
      protected DownloadInfo doInBackground(Void... objects) {
        return DownloadManager.getInstance().getDownloadInfo(packageName);
      }

      @Override
      protected void onPostExecute(DownloadInfo info) {
        if (info != null && !TextUtils.isEmpty(info.getIdentity())) {
          updateDownloadInfo(info);
          return;
        }
        setAction(Action.CANCEL_DOWNLOAD);
      }
    });
  }


  @Override
  protected void updateDownloadInfo(DownloadInfo downloadInfo) {
    this.downloadInfo = downloadInfo;
    setAction(Action.CANCEL_DOWNLOAD);
  }


  @Override
  protected void updateUnzipStat(ZipManager.ZipStatus unzipStatus) {
    switch (unzipStatus) {
      case CREATE:
      case UNZIPPING:
      case FAILED:
        setAction(Action.CANCEL_UNZIP);
        break;
      case CANCEL_BY_USER:
        setAction(Action.INVISIBLE);
        break;
      case SUCCESS:
        setAction(Action.INVISIBLE);
        break;
      default:
        break;
    }
  }


  @Override
  protected void updateInstallStat(InstallManager.InstallStatus installStatus) {
    setAction(Action.INVISIBLE);
  }

  private static void cancelDownload(final NetAppButton button) {
    if(button.downloadInfo != null){
      DownloadManager.getInstance().cancelAsync(button.downloadInfo);
      return;
    }
    DataUtils.runAsyncTask(new AsyncTask<Void, Void, DownloadInfo>() {
      @Override
      protected DownloadInfo doInBackground(Void... objects) {
        return DownloadManager.getInstance().getDownloadInfo(button.packageName);
      }

      @Override
      protected void onPostExecute(DownloadInfo info) {
        if (info != null && !TextUtils.isEmpty(info.getIdentity())) {
          button.downloadInfo = info;
          DownloadManager.getInstance().cancelAsync(button.downloadInfo);
          return;
        }
      }
    });
  }

  private static void cancelUnzip(NetAppButton button) {
    ZipManager.getInstance().cancel(button.packageName);
  }

  private static void cleanFiles(final NetAppButton button) {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        CleanManager.getInstance().cleanDownloadFileByPackageName(button.packageName);
      }
    });
  }

}
