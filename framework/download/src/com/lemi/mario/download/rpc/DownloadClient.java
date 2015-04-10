package com.lemi.mario.download.rpc;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.download.listener.DownloadVerifier;
import com.lemi.mario.download.listener.InnerDownloadListener;
import com.lemi.mario.download.listener.MD5Confirmer;
import com.lemi.mario.download.listener.NetworkStatusStub;
import com.lemi.mario.download.listener.SizeWatcher;
import com.lemi.mario.download.rpc.DownloadConstants.PauseReason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * The agent for download manager and info manager Download API.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadClient {
  private static final String TAG = DownloadClient.class.getSimpleName();
  private final DownloadVerifier verifier;
  private final MD5Confirmer md5Confirmer;
  private final NetworkStatusStub networkStatusStub;
  private final SizeWatcher sizeWatcher;
  private final Map<Long, DownloadTask> downloadTasks = new HashMap<Long, DownloadTask>();
  private final Context context;
  private final ExecutorService visibleExecutorService =
      new CachedThreadPoolExecutorWithCapacity(3, 10 * 1000L, "Download Visible");
  private final ExecutorService invisibleExecutorService =
      new CachedThreadPoolExecutorWithCapacity(2, 10 * 1000L, "Download Invisible");
  private final DatabaseDataController databaseDataController;

  private final Set<String> addTaskLock = new HashSet<String>();


  private final DownloadFinishedListener finishedListener = new DownloadFinishedListener() {

    @Override
    public void onDownloadFinished(long id) {
      synchronized (downloadTasks) {
        downloadTasks.remove(id);
      }
    }
  };

  /**
   * init download data and task controller.
   */
  public DownloadClient(final Context context, final DownloadVerifier verifier,
      final MD5Confirmer md5Confirmer, final NetworkStatusStub networkStatusStub,
      final SizeWatcher sizeWatcher) {
    this.context = context;
    this.verifier = verifier;
    this.md5Confirmer = md5Confirmer;
    this.networkStatusStub = networkStatusStub;
    this.sizeWatcher = sizeWatcher;
    databaseDataController = DatabaseDataController.getInstance(context);
    new Thread(new Runnable() {
      @Override
      public void run() {
        List<DownloadTask> downloadTaskList = new ArrayList<DownloadTask>();
        List<InnerDownloadInfo> runningDownloadInfo = getInterruptedRunningDownloadInfo();
        synchronized (downloadTasks) {
          for (InnerDownloadInfo info : runningDownloadInfo) {
            downloadTaskList.add(createDownloadTask(info));
          }
        }
        for (DownloadTask task : downloadTaskList) {
          task.resume();
        }
      }
    }).start();
  }

  private List<InnerDownloadInfo> getInterruptedRunningDownloadInfo() {
    InnerDownloadFilter.DownloadFilterBuilder builder = InnerDownloadFilter.newBuilder();
    List<Integer> status = new ArrayList<Integer>();
    status.add(DownloadConstants.Status.STATUS_CREATED);
    status.add(DownloadConstants.Status.STATUS_RUNNING);
    status.add(DownloadConstants.Status.STATUS_PENDING);
    status.add(DownloadConstants.Status.STATUS_CREATED);
    status.add(DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB);
    status.add(DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED);
    builder.setStatusFilter(status);
    InnerDownloadFilter filter = builder.build();
    return getDownloadInfos(filter);
  }

  /**
   * Start a download task.
   *
   * <p>
   * NOTE:if this download task has already existed, it will return Failed DownloadInfo which is
   * blank expect DOWNLOAD IDENTITY. And if start success, the return STATUS will be
   * {@link DownloadConstants.Status#STATUS_CREATED}
   * </p>
   *
   * @param request provide download detail info
   * @return downloadLiteInfo , if failed return fake failed downloadInfo
   */
  public InnerDownloadInfo start(InnerDownloadRequest request) {
    // Firstly, check there is no running and paused task
    String identity;
    if (!TextUtils.isEmpty(request.identity)) {
      identity = request.identity;
    } else {
      identity = request.url;
    }
    InnerDownloadInfo downloadInfo;
    try {
      // Add a latch to block tasks with the same identity.
      synchronized (addTaskLock) {
        while (!addTaskLock.add(identity)) {
          try {
            addTaskLock.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      InnerDownloadInfo info = databaseDataController.getDownloadInfoByIdentity(identity);
      // delete ' && info.mType == request.type', because one identity only allows one running task.
      if (info != null
          && (info.getStatus() == DownloadConstants.Status.STATUS_CREATED
              || info.getStatus() == DownloadConstants.Status.STATUS_PENDING
              || info.getStatus() == DownloadConstants.Status.STATUS_RUNNING
              || info.getStatus() == DownloadConstants.Status.STATUS_PAUSED_BY_APP
              || info.getStatus() == DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB
              || info.getStatus() == DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED)) {
        if (info.mVisible == request.visible || !request.visible && info.mVisible) {
          return buildFailedDownloadInfo(request);
        } else {
          // Cancel existing invisible download task
          cancel(info.mId);
        }
      }

      downloadInfo = databaseDataController.addDownloadInfo(request);
    } finally {
      synchronized (addTaskLock) {
        addTaskLock.remove(identity);
        addTaskLock.notifyAll();
      }
    }
    if (downloadInfo != null) {
      InnerDownloadInfo snapshot = downloadInfo.clone();
      DownloadMessageCenter.getInstance().notifyStatusChange(snapshot);
      DownloadTask task = createDownloadTask(downloadInfo);
      task.start();
      return snapshot;
    } else {
      return buildFailedDownloadInfo(request);
    }
  }

  private InnerDownloadInfo buildFailedDownloadInfo(InnerDownloadRequest request) {
    InnerDownloadInfo download = new InnerDownloadInfo();
    download.mIdentity = request.identity;
    return download;
  }

  /**
   * Resume given download id.
   *
   * @param id
   * @return the result of resume operation
   */
  public boolean resume(long id) {
    synchronized (downloadTasks) {
      if (downloadTasks.containsKey(id)) {
        return false;
      }
    }
    DownloadTask task = createDownloadTask(id);
    if (task == null) {
      return false;
    }
    return task.resume();
  }

  /**
   * Resume given download task with argument.
   *
   * @param id
   * @param allowInMobile
   * @return the result of resume operation
   */
  public boolean resume(long id, boolean allowInMobile) {
    synchronized (downloadTasks) {
      if (downloadTasks.containsKey(id)) {
        return false;
      }
    }

    DownloadTask task = createDownloadTask(id);
    if (task == null) {
      return false;
    }
    return task.resume(allowInMobile);
  }

  /**
   * Resume download according to download filter {@link InnerDownloadFilter}.
   *
   * <p>
   * If a download is added to download queue , it means resume success
   * </p>
   *
   * @param filter
   * @return if ALL download task resume successfully
   */
  public boolean resume(InnerDownloadFilter filter) {
    List<InnerDownloadInfo> downloadInfos =
        databaseDataController.getDownloadInfos(filter, 0, 0, null, true);
    boolean result = true;
    List<DownloadTask> tasks = new ArrayList<DownloadTask>();
    synchronized (downloadTasks) {
      for (InnerDownloadInfo downloadInfo : downloadInfos) {
        if (downloadTasks.containsKey(downloadInfo.mId)) {
          Log.w(TAG, "Cannot resume running tasks.");
          continue;
        }
        tasks.add(createDownloadTask(downloadInfo));
      }
    }
    for (DownloadTask task : tasks) {
      result = task.resume() && result;
    }
    return result;
  }

  /**
   * Pause download if a download is running or pending.
   *
   * <p>
   * NOTE: pause id and the pause reason is {@link PauseReason#PAUSE_BY_APP}
   * </p>
   *
   * @param id
   * @return whether success
   */
  public boolean pause(long id) {
    return pause(id, PauseReason.PAUSE_BY_APP);
  }

  private boolean pause(long id, PauseReason reason) {
    DownloadTask task;
    synchronized (downloadTasks) {
      task = downloadTasks.get(id);
    }
    if (task == null) {
      return false;
    }
    return task.pause(reason);
  }

  /**
   * Pause multi threads according to downloadFilter.
   *
   * @param filter
   * @return whether ALL download task resume successfully
   */
  public boolean pause(InnerDownloadFilter filter, PauseReason reason) {
    List<InnerDownloadInfo> downloadInfos =
        databaseDataController.getDownloadInfos(filter, 0, 0, null, true);
    boolean result = true;
    if (downloadInfos != null) {
      for (InnerDownloadInfo download : downloadInfos) {
        result = pause(download.mId, reason) && result;
      }
    }
    return result;
  }

  /**
   * Retry Download if failed.
   *
   * @param id
   * @return whether success
   */
  public boolean retry(long id) {
    return retry(id, null);
  }

  /**
   * Retry download with new URL.
   *
   * <p>
   * NOTE: the first choice is use newUrl to retry. if newUrl is null and currentBytes is bigger
   * than zero, then try to resume. If resume failed, it will set bytesRead to zero, and then at
   * next times, use retry.
   * </p>
   *
   * @param id
   * @param newUrl the new download url to replace the old one with new url.
   * @return whether success
   */
  public boolean retry(long id, String newUrl) {
    synchronized (downloadTasks) {
      if (downloadTasks.containsKey(id)) {
        return false;
      }
    }
    DownloadTask task = createDownloadTask(id);
    if (task == null) {
      return false;
    }
    return task.retry(newUrl);
  }

  /**
   * Cancel download By id.
   *
   * @param id
   * @return whether success
   */
  public synchronized boolean cancel(long id) {
    DownloadTask task;
    synchronized (downloadTasks) {
      task = downloadTasks.get(id);
    }
    if (task != null) {
      return task.cancel();
    }
    InnerDownloadInfo downloadInfo = databaseDataController.getDownloadInfoById(id);
    if (downloadInfo == null) {
      return false;
    }
    boolean success = databaseDataController.removeDownloadTask(id);
    if (!success) {
      return false;
    }

    InnerDownloadInfo snapshot = downloadInfo.clone();
    if (snapshot.getStatus() == DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB
        || snapshot.getStatus() == DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED
        || snapshot.getStatus() == DownloadConstants.Status.STATUS_PAUSED_BY_APP) {
      downloadInfo.setStatus(DownloadConstants.Status.STATUS_CANCELED);
      DownloadMessageCenter.getInstance().notifyStatusChange(downloadInfo);
    }
    snapshot.setStatus(DownloadConstants.Status.STATUS_DELETED);
    DownloadMessageCenter.getInstance().notifyStatusChange(snapshot);
    return true;
  }

  /**
   * return specific downloadInfos for list of identities.
   *
   * <p>
   * NOTE: if no identity is found, it will use NULL to take the position.
   * </p>
   *
   * @param identities
   * @return List of DownloadInfo
   */
  public List<InnerDownloadInfo> getDownloadInfos(List<String> identities) {
    return databaseDataController.getDownloadInfoByIdentities(identities);
  }

  /**
   * return specific downloadInfos for list of ids.
   *
   * <p>
   * NOTE: if no download id is assigned to the identity ,return null. if the identity has more than
   * one download ids, return the LATEST one.
   * </p>
   *
   * @param identity
   * @return DownloadInfo that resId is identity.
   */
  public InnerDownloadInfo getDownloadInfo(String identity) {
    return databaseDataController.getDownloadInfoByIdentity(identity);
  }

  /**
   * Get DownloadInfo from download id.
   *
   * @param id
   * @return DownloadInfo
   */
  public InnerDownloadInfo getDownloadInfo(long id) {
    return databaseDataController.getDownloadInfoById(id);
  }

  /**
   * Get the list of DownloadInfos according to DownloadFilter.
   *
   * <p>
   * NOTE: if the filter contains Status, and the filter argument don't equal
   * {@link DownloadConstants.Status#STATUS_SUCCESS}, all data can be achieved from
   * MemoryDataController
   * </p>
   *
   * @param filter null it will return all results.
   * @return List of the downloadInfo that match the request.
   */
  public List<InnerDownloadInfo> getDownloadInfos(InnerDownloadFilter filter) {
    return databaseDataController.getDownloadInfos(filter, 0, 0, null, true);
  }

  /**
   * Get the capacity which has been cost by all the unfinished download tasks.
   *
   * <p>
   * NOTE: it's not a time-consuming operation
   * </p>
   *
   * @return space which unfinished task need.
   */
  public long getUnfinishedTaskSpaceNeeded() {
    long size = 0L;
    synchronized (downloadTasks) {
      for (DownloadTask downloadTask : downloadTasks.values()) {
        if (downloadTask.isVisible()) {
          size += downloadTask.getSpaceNeeded();
        }
      }
    }
    return size;
  }

  /**
   * Get the size of downloads which match the filter.
   *
   * @param filter
   * @return count
   */
  public int getDownloadCount(InnerDownloadFilter filter) {
    return databaseDataController.getDownloadCount(filter);
  }

  public void setDownloadListener(InnerDownloadListener listener) {
    assert (listener != null);
    DownloadMessageCenter.getInstance().setDownloadListener(listener);
  }

  /**
   * Put argument into extra col_umn and update it in database.
   *
   * @param id
   * @param key
   * @param value
   */
  public void putExtraArgument(long id, String key, String value) {
    if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
      throw new NullPointerException("key is null or value is null");
    }
    HashMap<String, String> map = new HashMap<String, String>();
    map.put(key, value);
    putExtraArgument(id, map);
  }

  /**
   * Put argument into extra col_umn and update it in database.
   *
   * @param id
   * @param values
   */
  public void putExtraArgument(long id, HashMap<String, String> values) {
    DownloadTask downloadTask;
    synchronized (downloadTasks) {
      downloadTask = downloadTasks.get(id);
    }
    if (downloadTask != null) {
      downloadTask.putExtraArgument(id, values);
    } else {
      InnerDownloadInfo innerDownloadInfo = getDownloadInfo(id);
      if (innerDownloadInfo == null) {
        return;
      }
      Gson gson = new Gson();
      HashMap<String, String> extras = gson.fromJson(
          innerDownloadInfo.mExtras, new TypeToken<HashMap<String, String>>() {}.getType());
      if (extras == null) {
        extras = new HashMap<String, String>(0);
      }
      for (Map.Entry<String, String> entry : values.entrySet()) {
        extras.put(entry.getKey(), entry.getValue());
      }
      ContentValues contentValues = new ContentValues();
      contentValues.put(DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_EXTRAS,
          gson.toJson(extras, new TypeToken<HashMap<String, String>>() {}.getType()));
      databaseDataController.updateDownloadInfo(id, contentValues);
    }
  }

  private DownloadTask createDownloadTask(long id) {
    InnerDownloadInfo downloadInfo = databaseDataController.getDownloadInfoById(id);
    if (downloadInfo == null) {
      Log.e(TAG, "There is no download info found.");
      return null;
    }
    return createDownloadTask(downloadInfo);
  }

  private DownloadTask createDownloadTask(InnerDownloadInfo downloadInfo) {
    ExecutorService executor = downloadInfo.mVisible
        ? visibleExecutorService : invisibleExecutorService;
    DownloadTask task = new DownloadTask(
        context, downloadInfo, finishedListener, executor,
        verifier, md5Confirmer, databaseDataController, networkStatusStub, sizeWatcher);
    synchronized (downloadTasks) {
      downloadTasks.put(downloadInfo.mId, task);
    }
    return task;
  }

}
