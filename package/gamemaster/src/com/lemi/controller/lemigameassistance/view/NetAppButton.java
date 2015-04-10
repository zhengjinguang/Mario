package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.download.DownloadListener;
import com.lemi.controller.lemigameassistance.download.DownloadManager;
import com.lemi.controller.lemigameassistance.download.DownloadRequestBuilder;
import com.lemi.controller.lemigameassistance.manager.BackgroundScheduleManager;
import com.lemi.controller.lemigameassistance.manager.InstallManager;
import com.lemi.controller.lemigameassistance.manager.ZipManager;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.utils.AppUtils;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.controller.lemigameassistance.utils.PathUtils;
import com.lemi.mario.appmanager.AppManager;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.base.utils.MainThreadPostUtils;

/**
 * Button to operation app status.
 * This Button only trigger download action and refresh UI display .
 * This Button will not schedule download - unzip - install task.
 * download - unzip - install schedule logic is in
 * {@link com.lemi.controller.lemigameassistance.manager.BackgroundScheduleManager}.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class NetAppButton extends Button {

  protected static final int[] STATE_INSTALL = {R.attr.state_install};
  protected static final int[] STATE_DOWNLOADING = {R.attr.state_downloading};
  protected static final int[] STATE_PAUSE = {R.attr.state_pause};
  protected static final int[] STATE_RESUME = {R.attr.state_resume};
  protected static final int[] STATE_RETRY = {R.attr.state_retry};
  protected static final int[] STATE_UNZIPING = {R.attr.state_unziping};
  protected static final int[] STATE_INSTALLING = {R.attr.state_installing};
  protected static final int[] STATE_OPEN = {R.attr.state_open};



  {
    setFocusable(true);
    setFocusableInTouchMode(true);
    setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        NetAppButton.this.onClick();
        if(extraRunnable != null){
          extraRunnable.run();
        }
      }
    });
  }

  /**
   * Action of button.
   */
  public static enum Action {
    INSTALL(R.string.net_button_install, STATE_INSTALL) {
      @Override
      protected void onClick(final NetAppButton button) {
        DataUtils.runAsyncTask(new AsyncTask<Void, Void, DownloadInfo>() {
          @Override
          protected DownloadInfo doInBackground(Void... objects) {
            return DownloadManager.getInstance().getDownloadInfo(button.packageName);
          }

          @Override
          protected void onPostExecute(DownloadInfo info) {
            if (info != null && !TextUtils.isEmpty(info.getIdentity())) {
              button.downloadInfo = info;
              BackgroundScheduleManager.getInstance().setPackageSchedule(info.getIdentity(), info);
            }
            if (info != null
                && FileUtil.exists(info.getFilePath())
                && info.getStatus() == DownloadInfo.Status.SUCCESS) {
              if (info.getContentType() == DownloadInfo.ContentType.ZIP
                  && ZipManager.getInstance().getStatus(info.getIdentity()) != ZipManager.ZipStatus.SUCCESS) {
                startUnzip(button);
                return;
              }
              if (InstallManager.getInstance().getStatus(info.getIdentity()) != InstallManager.InstallStatus.SUCCESS) {
                startInstall(button);
                return;
              }
            } else {
              startDownload(button);
            }

          }
        });
      }
    },
    DOWNLOADING(R.string.net_button_downloading, STATE_DOWNLOADING, false) {
      @Override
      protected void onClick(NetAppButton button) {}
    },
    PAUSE(R.string.net_button_pause, STATE_PAUSE) {
      @Override
      protected void onClick(NetAppButton button) {
        pauseDownload(button);
      }
    },
    RESUME(R.string.net_button_resume, STATE_RESUME) {
      @Override
      protected void onClick(NetAppButton button) {
        resumeDownload(button);
      }
    },
    UNZIPING(R.string.net_button_unziping, STATE_UNZIPING, false) {
      @Override
      protected void onClick(NetAppButton button) {}
    },
    INSTALLING(R.string.net_button_installing, STATE_INSTALLING, false) {
      @Override
      protected void onClick(NetAppButton button) {}
    },
    RETRY(R.string.net_button_retry, STATE_RETRY) {
      @Override
      public void onClick(final NetAppButton button) {
        if (button.downloadInfo != null) {
          DownloadManager.getInstance().retryAsync(button.downloadInfo);
        }
      }
    },
    OPEN(R.string.net_button_open, STATE_OPEN) {
      @Override
      public void onClick(final NetAppButton button) {
        if (!AppManager.getInstance().syncIsAppInstalled(button.packageName)) {
          MainThreadPostUtils.toast(R.string.net_button_app_is_uninstall);
          button.setAction(Action.INSTALL);
          return;
        }
        openApp(button);
      }
    };

    private final int textResId;
    private final boolean enabled;
    private final int[] drawableState;

    private Action(int textResId, int[] drawableState) {
      this(textResId, drawableState, true);
    }

    private Action(int textResId, int[] drawableState, boolean enabled) {
      this.textResId = textResId;
      this.drawableState = drawableState;
      this.enabled = enabled;
    }

    private int[] getDrawableState() {
      return drawableState;
    }

    private void apply(NetAppButton button) {
      button.setText(textResId);
      button.setEnabled(enabled);
    }

    protected abstract void onClick(NetAppButton button);
  }


  private GameModel gameInfo;
  private Action action;

  protected String packageName;
  protected DownloadInfo downloadInfo;
  protected Runnable extraRunnable;

  public NetAppButton(Context context) {
    super(context);
    DownloadManager.getInstance().addVisibleListener(downloadListener);
    ZipManager.getInstance().addListener(unzipListener);
    InstallManager.getInstance().addListener(installListener);
  }

  public NetAppButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    DownloadManager.getInstance().addVisibleListener(downloadListener);
    ZipManager.getInstance().addListener(unzipListener);
    InstallManager.getInstance().addListener(installListener);
  }

  public NetAppButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    DownloadManager.getInstance().addVisibleListener(downloadListener);
    ZipManager.getInstance().addListener(unzipListener);
    InstallManager.getInstance().addListener(installListener);
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (action == null) {
      mergeDrawableStates(drawableState, STATE_INSTALL);
    } else {
      mergeDrawableStates(drawableState, action.getDrawableState());
    }
    return drawableState;
  }

  public void setAction(Action action) {
    if (this.action != action) {
      this.action = action;
      action.apply(this);
      refreshDrawableState();
    }
  }

  public Action getAction() {
    return action;
  }

  public GameModel getGameInfo() {
    return gameInfo;
  }

  private final ZipManager.ZipListener unzipListener = new ZipManager.ZipListener() {
    @Override
    public void onZipStatusChange(String token, ZipManager.ZipStatus status) {
      if (!checkPackageNameValid(token)) {
        return;
      }
      if (status != ZipManager.ZipStatus.SUCCESS) {
        updateUnzipStat(status);
      }
    }

    @Override
    public void onZipProgressChange(String token, int percent) {}
  };

  private final InstallManager.InstallListener installListener =
      new InstallManager.InstallListener() {
        @Override
        public void onInstallStatusChange(String packageName, InstallManager.InstallStatus status) {
          if (!checkPackageNameValid(packageName)) {
            return;
          }
          updateInstallStat(status);
        }
      };

  private final DownloadListener downloadListener = new DownloadListener() {
    @Override
    public void onProgressChanged(DownloadInfo info) {}

    @Override
    public void onStatusChanged(DownloadInfo info) {
      if (info == null || !checkPackageNameValid(info.getIdentity())) {
        return;
      }
      if (info.getStatus() != DownloadInfo.Status.SUCCESS) {
        updateDownloadInfo(info);
      }
    }
  };


  /**
   * Set data of this button, when initializing this button.
   *
   * @param gameInfo app info
   */
  public void setData(GameModel gameInfo) {
    if (gameInfo == null || TextUtils.isEmpty(gameInfo.getPackageName())) {
      throw new IllegalArgumentException(
          "net button must set gameInfo and package can not be null.");
    }
    reset();
    this.gameInfo = gameInfo;
    this.packageName = gameInfo.getPackageName();
    initButtonStat();
  }

  /**
   * Set extra runnable for click listener
   */
  public void setExtraRunnable(Runnable runnable){
    extraRunnable = runnable;
  }

  private void reset() {
    gameInfo = null;
    packageName = null;
    action = null;
    downloadInfo = null;
    setEnabled(true);
  }

  protected void initButtonStat() {
    if (AppManager.getInstance().syncIsAppInstalled(packageName)) {
      setAction(Action.OPEN);
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
    setAction(Action.INSTALL);
    DataUtils.runAsyncTask(new AsyncTask<Void, Void, DownloadInfo>() {
      @Override
      protected DownloadInfo doInBackground(Void... objects) {
        return DownloadManager.getInstance().getDownloadInfo(packageName);
      }

      @Override
      protected void onPostExecute(DownloadInfo info) {
        if (info != null && !TextUtils.isEmpty(info.getIdentity())) {
          BackgroundScheduleManager.getInstance().setPackageSchedule(info.getIdentity(), info);
          updateDownloadInfo(info);
          return;
        }
        setAction(Action.INSTALL);
      }
    });
  }

  protected void updateInstallStat(InstallManager.InstallStatus installStatus) {
    switch (installStatus) {
      case CREATE:
        setAction(Action.INSTALLING);
        break;
      case SILENCE_INSTALLING:
        setAction(Action.INSTALLING);
        break;
      case NORMAL_INSTALLING:
        setAction(Action.INSTALL);
        break;
      case FAILED:
        setAction(Action.INSTALL);
        break;
      case SUCCESS:
        setAction(Action.OPEN);
        break;
      default:
        break;
    }
  }

  protected void updateUnzipStat(ZipManager.ZipStatus unzipStatus) {
    switch (unzipStatus) {
      case CREATE:
        setAction(Action.UNZIPING);
        break;
      case UNZIPPING:
        setAction(Action.UNZIPING);
        break;
      case FAILED:
        setAction(Action.INSTALL);
        break;
      case CANCEL_BY_USER:
        setAction(Action.INSTALL);
        break;
      case SUCCESS:
        setAction(Action.INSTALL);
        break;
      default:
        break;
    }
  }

  protected void updateDownloadInfo(DownloadInfo downloadInfo) {
    this.downloadInfo = downloadInfo;
    switch (downloadInfo.getStatus()) {
      case CREATED:
        setAction(Action.DOWNLOADING);
        break;
      case PENDING:
        break;
      case PAUSED:
        setAction(Action.RESUME);
        break;
      case DOWNLOADING:
        setAction(Action.PAUSE);
        break;
      case SUCCESS:
        setAction(Action.INSTALL);
        break;
      case CANCELED:
        setAction(Action.INSTALL);
        break;
      case FAILED:
        setAction(Action.INSTALL);
        break;
      case DELETED:
        setAction(Action.INSTALL);
        break;
      default:
        break;
    }
  }

  protected void onClick() {
    if (action != null && gameInfo != null) {
      action.onClick(this);
    }
  }


  private static void startDownload(NetAppButton button) {
    DownloadManager.getInstance().startAsync(
        DownloadRequestBuilder.buildDownloadRequest(button.gameInfo));
  }

  private static void startUnzip(NetAppButton button) {
    ZipManager.getInstance().startUnZipGameDataTaskAsync(button.downloadInfo.getIdentity(),
        button.downloadInfo.getFilePath(), PathUtils.getUnzipApkFolderPath(),
        PathUtils.getUnzipDataFolderPath(),
        PathUtils.getApkFullPath(button.downloadInfo.getIdentity()));
  }

  private static void startInstall(NetAppButton button) {
    if (button.downloadInfo.getContentType() == DownloadInfo.ContentType.APP) {
      InstallManager.getInstance().asyncInstallPackage(button.downloadInfo.getIdentity(),
          button.downloadInfo.getFilePath());
    } else {
      InstallManager.getInstance().asyncInstallPackage(button.downloadInfo.getIdentity(),
          PathUtils.getApkFullPath(button.downloadInfo.getIdentity()));
    }
  }

  private static void pauseDownload(NetAppButton button) {
    DownloadManager.getInstance().pauseAsync(button.downloadInfo);
  }

  private static void resumeDownload(NetAppButton button) {
    DownloadManager.getInstance().resumeAsync(button.downloadInfo);
  }

  private static void openApp(NetAppButton button) {
    AppUtils.startApp(button.getContext(), button.packageName);
  }

  private boolean checkPackageNameValid(String packageName) {
    if (TextUtils.isEmpty(NetAppButton.this.packageName)) {
      return false;
    }
    return NetAppButton.this.packageName.equals(packageName);
  }

}
