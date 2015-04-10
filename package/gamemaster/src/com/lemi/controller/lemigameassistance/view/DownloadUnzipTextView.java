package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.download.DownloadListener;
import com.lemi.controller.lemigameassistance.download.DownloadManager;
import com.lemi.controller.lemigameassistance.manager.ZipManager;
import com.lemi.controller.lemigameassistance.utils.AppUtils;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.mario.appmanager.AppManager;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadUnzipTextView extends TextView {

  private static final int[] STATE_DOWNLOAD = {R.attr.state_download};
  private static final int[] STATE_UNZIP = {R.attr.state_unzip};
  private static final int BEGIN_PROGRESS = 0;

  public enum Status {
    INVISIBLE(STATE_DOWNLOAD),
    DOWNLOAD(STATE_DOWNLOAD),
    UNZIP(STATE_UNZIP);

    private Status(int[] drawableState) {
      this.drawableState = drawableState;
    }

    private int[] getDrawableState() {
      return drawableState;
    }

    private final int[] drawableState;
  }

  private Status status;
  private String packageName;


  public DownloadUnzipTextView(Context context) {
    super(context);
    DownloadManager.getInstance().addVisibleListener(downloadListener);
    ZipManager.getInstance().addListener(unzipListener);
  }

  public DownloadUnzipTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    DownloadManager.getInstance().addVisibleListener(downloadListener);
    ZipManager.getInstance().addListener(unzipListener);
  }

  public DownloadUnzipTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    DownloadManager.getInstance().addVisibleListener(downloadListener);
    ZipManager.getInstance().addListener(unzipListener);
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (status == null) {
      mergeDrawableStates(drawableState, STATE_DOWNLOAD);
    } else {
      mergeDrawableStates(drawableState, status.getDrawableState());
    }
    return drawableState;
  }

  public synchronized void setStatus(Status status) {
    if (this.status != status) {
      if (status == Status.INVISIBLE) {
        this.setVisibility(INVISIBLE);
      } else {
        this.setVisibility(VISIBLE);
      }
      this.status = status;
      refreshDrawableState();
    }
  }

  public synchronized Status getStatus() {
    return status;
  }


  public void setData(String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      throw new IllegalArgumentException("progress bar packageName can not be null.");
    }
    reset();
    this.packageName = packageName;
    initStatus();
  }

  private void reset() {
    packageName = null;
    status = null;
  }


  private final ZipManager.ZipListener unzipListener = new ZipManager.ZipListener() {
    @Override
    public void onZipStatusChange(String token, ZipManager.ZipStatus status) {
      if (!checkPackageNameValid(token)) {
        return;
      }
      updateUnzipStat(status);
    }

    @Override
    public void onZipProgressChange(String token, int percent) {
      if (!checkPackageNameValid(token)) {
        return;
      }
      setProgress(percent);
    }
  };

  private final DownloadListener downloadListener = new DownloadListener() {
    @Override
    public void onProgressChanged(DownloadInfo info) {
      if (!checkPackageNameValid(info.getIdentity())) {
        return;
      }
      setProgress(calIntPercent(info.getCurrentBytes(), info.getTotalBytes()));
    }

    @Override
    public void onStatusChanged(DownloadInfo info) {
      if (info == null || !checkPackageNameValid(info.getIdentity())) {
        return;
      }
      updateDownloadInfo(info);
    }
  };

  private void initStatus() {
    if (AppManager.getInstance().syncIsAppInstalled(packageName)) {
      setStatus(Status.INVISIBLE);
      return;
    }
    ZipManager.ZipStatus unzipStatus = ZipManager.getInstance().getStatus(packageName);
    if (unzipStatus != null) {
      updateUnzipStat(unzipStatus);
      return;
    }
    setStatus(Status.INVISIBLE);
    DataUtils.runAsyncTask(new AsyncTask<Void, Void, DownloadInfo>() {
      @Override
      protected DownloadInfo doInBackground(Void... objects) {
        return DownloadManager.getInstance().getDownloadInfo(packageName);
      }

      @Override
      protected void onPostExecute(DownloadInfo info) {
        if (info != null) {
          updateDownloadInfo(info);
          return;
        }
        setStatus(DownloadUnzipTextView.Status.INVISIBLE);
      }
    });
  }

  private void updateUnzipStat(ZipManager.ZipStatus unzipStatus) {
    switch (unzipStatus) {
      case CREATE:
        setStatus(Status.UNZIP);
        setProgress(BEGIN_PROGRESS);
        break;
      case UNZIPPING:
        setStatus(Status.UNZIP);
        break;
      case FAILED:
        setStatus(Status.INVISIBLE);
        break;
      case CANCEL_BY_USER:
        setStatus(Status.INVISIBLE);
        break;
      case SUCCESS:
        setStatus(Status.INVISIBLE);
        break;
      default:
        break;
    }
  }

  private void updateDownloadInfo(DownloadInfo downloadInfo) {
    switch (downloadInfo.getStatus()) {
      case CREATED:
        setStatus(Status.DOWNLOAD);
        setProgress(BEGIN_PROGRESS);
        break;
      case PENDING:
        setStatus(Status.DOWNLOAD);
        break;
      case PAUSED:
        setStatus(Status.DOWNLOAD);
        setProgress(calIntPercent(downloadInfo.getCurrentBytes(), downloadInfo.getTotalBytes()));
        break;
      case DOWNLOADING:
        setStatus(Status.DOWNLOAD);
        break;
      case SUCCESS:
        setStatus(Status.INVISIBLE);
        break;
      case CANCELED:
        setStatus(Status.INVISIBLE);
        break;
      case FAILED:
        setStatus(Status.INVISIBLE);
        break;
      case DELETED:
        setStatus(Status.INVISIBLE);
        break;
      default:
        break;
    }
  }

  private boolean checkPackageNameValid(String packageName) {
    if (TextUtils.isEmpty(DownloadUnzipTextView.this.packageName)) {
      return false;
    }
    return DownloadUnzipTextView.this.packageName.equals(packageName);
  }

  private int calIntPercent(long current, long total) {
    if (total == 0) {
      return 0;
    }
    return (int) (current * 100 / total) > 100 ? 100 : (int) (current * 100 / total);
  }

  private void setProgress(int percent) {
    setText(percent + "%");
  }

}
