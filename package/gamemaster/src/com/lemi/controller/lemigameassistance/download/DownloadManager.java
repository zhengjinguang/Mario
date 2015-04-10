package com.lemi.controller.lemigameassistance.download;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.download.DownloadInfo.ContentType;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.controller.lemigameassistance.utils.PathUtils;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.NetworkUtil;
import com.lemi.mario.download.listener.DownloadVerifier;
import com.lemi.mario.download.listener.InnerDownloadListener;
import com.lemi.mario.download.listener.MD5Confirmer;
import com.lemi.mario.download.listener.SizeWatcher;
import com.lemi.mario.download.rpc.DownloadClient;
import com.lemi.mario.download.rpc.DownloadConstants;
import com.lemi.mario.download.rpc.DownloadConstants.ResourceType;
import com.lemi.mario.download.rpc.InnerDownloadFilter;
import com.lemi.mario.download.rpc.InnerDownloadFilter.Operator;
import com.lemi.mario.download.rpc.InnerDownloadInfo;
import com.lemi.mario.download.rpc.InnerDownloadRequest;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Download Manager.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadManager {

  private static final String TAG = DownloadManager.class.getSimpleName();
  private final List<WeakReference<DownloadListener>> visibleDownloadListeners;
  private final List<WeakReference<DownloadListener>> invisibleDownloadListeners;
  private final Handler uiThreadHanlder = new Handler(Looper.getMainLooper());
  private final DownloadClient downloadClient;
  private final ExecutorService executor = Executors.newCachedThreadPool();

  // service used to ensure that when has tasks to be executed, keep service running.
  private boolean downloadServiceStarted = false;

  private final DownloadVerifier verifyListener = null;

  private final MD5Confirmer md5CaclListener = null;


  private final InnerDownloadListener downloadListener = new InnerDownloadListener() {
    @Override
    public void onProgressChanged(final InnerDownloadInfo downloadInfo) {
      // If mTotalBytes is unknown, can't update progress.
      if (downloadInfo.mTotalBytes <= 0) {
        return;
      }

      /* Convert DownloadInfo */
      DownloadInfo info = new DownloadInfoSingle(downloadInfo);

      if (info.isVisible()) {
        notifyProgressChanged(info, visibleDownloadListeners);
      } else {
        notifyProgressChanged(info, invisibleDownloadListeners);
      }
    }

    @Override
    public void onStatusChanged(final InnerDownloadInfo downloadInfo) {

      DownloadInfo info = new DownloadInfoSingle(downloadInfo);

      if (info.isVisible()) {
        notifyDownloadInfoChanged(info, visibleDownloadListeners);
      } else {
        notifyDownloadInfoChanged(info, invisibleDownloadListeners);
      }
    }

    @Override
    public void onConfigSizeAchieved(long id, long configSize) {}

    @Override
    public void onMimeTypeAchieved(long id, String mimeType) {}
  };

  private final SizeWatcher downloadSizeWatcher = new SizeWatcher() {
    @Override
    public void onSizeConfirmed(InnerDownloadInfo downloadInfo) {
      // do nothings.
    }
  };



  private DownloadManager() {
    visibleDownloadListeners = new LinkedList<WeakReference<DownloadListener>>();
    invisibleDownloadListeners = new LinkedList<WeakReference<DownloadListener>>();
    downloadClient =
        new DownloadClient(GlobalConfig.getAppContext(), verifyListener,
            md5CaclListener, new NetworkStatusSubImpl(GlobalConfig.getAppContext()),
            downloadSizeWatcher);
    downloadClient.setDownloadListener(downloadListener);
  }

  private static DownloadManager instance;

  /**
   * Get the instance of DownloadManager.
   * 
   * @return the instance of DownloadManger
   */
  public static synchronized DownloadManager getInstance() {
    if (instance == null) {
      instance = new DownloadManager();
    }
    return instance;
  }

  /**
   * Start service when having tasks to be excuted.
   */
  private void startService() {
    Intent intent = new Intent(GlobalConfig.getAppContext(), DownloadService.class);
    downloadServiceStarted = GlobalConfig.getAppContext().startService(intent) != null;
  }


  private DownloadInfo startInternal(DownloadRequest downloadRequest) {
    checkNonUIThread();
    if (downloadRequest == null) {
      Log.e(TAG, "Download Request is null, pls check if lack of params when build this request");
      return null;
    }
    if (!NetworkUtil.isNetworkConnected(GlobalConfig.getAppContext())) {
      MainThreadPostUtils.toastLong(R.string.open_network_connect);
      return null;
    }
    if (!TextUtils.isEmpty(downloadRequest.folderPath)) {
      if (!DownloadUtils.hasEnoughSizeToDownload(downloadRequest.totalBytes,
          downloadRequest.folderPath)) {
        MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_download_tips);
        return null;
      }
      if (!FileUtil.canWrite(new File(downloadRequest.folderPath))) {
        MainThreadPostUtils.toastLong(R.string.no_write_permission);
        return null;
      }
    } else {
      if (!DownloadUtils.hasEnoughSizeToDownload(downloadRequest.totalBytes,
          PathUtils.getDownloadRootPath())) {
        MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_download_tips);
        return null;
      }
      if (!FileUtil.canWrite(new File(PathUtils.getDownloadRootPath()))) {
        MainThreadPostUtils.toastLong(R.string.no_write_permission);
        return null;
      }
    }

    if (!downloadServiceStarted) {
      startService();
    }
    InnerDownloadRequest request = buildInnerDownloadRequest(downloadRequest);
    return new DownloadInfoSingle(downloadClient.start(request));
  }



  /**
   * Starts a download task in asynchronous mode.
   * 
   * <p>
   * See {@link #startInternal(DownloadRequest)}.
   * </p>
   * 
   * @param downloadRequest download request
   */
  public void startAsync(final DownloadRequest downloadRequest) {
    executor.execute(new Runnable() {

      @Override
      public void run() {
        startInternal(downloadRequest);
      }
    });
  }


  /**
   * Starts a download task.
   * 
   * <p>
   * This a blocking API, so cannot be called on UI thread.
   * </p>
   * 
   * <p>
   * This function provides TWO ways to return result. One is via the return value and the other one
   * is via {@link DownloadListener#onStatusChanged(DownloadInfo)}. Some useful suggestions may help
   * you call it right.
   * </p>
   * 
   * <ul>
   * <li>
   * UI MUST handle result via {@link DownloadListener#onStatusChanged(DownloadInfo)}, since other
   * service, such as PUSH SERVICE may establish a download task in background, and UI can only get
   * this message by callback.</li>
   * <li>
   * Background service can get the result just by return value.</li>
   * </ul>
   * 
   * <li>
   * If you pass in a null request which may be resulted by lack of parameters when building
   * download request, this function will return a null download info and there is no call back
   * then.</li>
   * 
   * @param downloadRequest download request
   * @return the DownloadInfo of your download task
   * 
   */
  public DownloadInfo start(final DownloadRequest downloadRequest) {
    return startInternal(downloadRequest);
  }

  /**
   * Cancels a download task.
   * 
   * <p>
   * This a blocking API, so cannot be called on UI thread.
   * </p>
   * 
   * <p>
   * This function provides TWO ways to return result. One is via the return value and the other one
   * is via {@link DownloadListener#onStatusChanged(DownloadInfo)}.Some useful suggestions may help
   * you call it right.
   * </p>
   * 
   * <ul>
   * <li>
   * UI MUST handle result via {@link DownloadListener#onStatusChanged(DownloadInfo)}, since other
   * service, such as PUSH SERVICE may establish a download task in background, and UI can only get
   * this message by callback.</li>
   * <li>
   * Background service can get the result just by return value.</li>
   * </ul>
   * 
   * <p>
   * No matter your call anywhere, any ways, you can NEVER call it on UI thread.
   * </p>
   * 
   * @param downloadInfo download info
   * @return the result of operation
   */
  public boolean cancel(final DownloadInfo downloadInfo) {
    return cancelInternal(downloadInfo);
  }

  /**
   * Cancel download task.
   * 
   * <p>
   * If downloadInfo has sub tasks, it will also cancel its child task.
   * </p>
   * 
   * @param downloadInfo
   * @return success to cancel all the tasks.
   */
  private boolean cancelInternal(DownloadInfo downloadInfo) {
    checkNonUIThread();
    boolean result = true;
    if (downloadInfo instanceof DownloadInfoSingle) {
      result = downloadClient.cancel(downloadInfo.getId());
      deleteDownloadFile(downloadInfo);
    }
    return result;
  }

  /**
   * Deleted download file.
   * If the download type is APP or PATCH, delete the file no matter it is success or not. Else if
   * the download type is others, delete the file only if the task is not succeeded.
   * 
   * @param downloadInfo download info
   */
  private void deleteDownloadFile(DownloadInfo downloadInfo) {
    if (downloadInfo.getStatus() != DownloadInfo.Status.SUCCESS) {
      if (!TextUtils.isEmpty(downloadInfo.getIntermediateFilePath())) {
        FileUtil.deleteFile(downloadInfo.getIntermediateFilePath());
      }
    } else if (downloadInfo.getContentType() == ContentType.APP
        || downloadInfo.getContentType() == ContentType.PATCH) {
      if (!TextUtils.isEmpty(downloadInfo.getFilePath())) {
        FileUtil.deleteFile(downloadInfo.getFilePath());
      }
    }
  };

  /**
   * Cancels a download task in asynchronous mode.
   * 
   * 
   * @param downloadInfo download info
   */
  public void cancelAsync(final DownloadInfo downloadInfo) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        cancelInternal(downloadInfo);
      }
    });
  }

  private boolean pauseInternal(DownloadInfo downloadInfo) {
    checkNonUIThread();
    boolean result = true;
    if (downloadInfo instanceof DownloadInfoSingle) {
      return downloadClient.pause(downloadInfo.getId());
    }
    return result;
  }

  /**
   * Pauses a download task in asynchronous mode.
   * 
   * 
   * @param downloadInfo download info
   */
  public void pauseAsync(final DownloadInfo downloadInfo) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        pauseInternal(downloadInfo);
      }
    });
  }

  /**
   * Pauses a download task.
   * 
   * <p>
   * This a blocking API, so cannot be called on UI thread.
   * </p>
   * 
   * <p>
   * This function provides TWO ways to return result. One is via the return value and the other one
   * is via {@link DownloadListener#onStatusChanged(DownloadInfo)}.Some useful suggestions may help
   * you call it right.
   * </p>
   * 
   * <ul>
   * <li>
   * UI MUST handle result via {@link DownloadListener#onStatusChanged(DownloadInfo)}, since other
   * service, such as PUSH SERVICE may establish a download task in background, and UI can only get
   * this message by callback.</li>
   * <li>
   * Background service can get the result just by return value.</li>
   * </ul>
   * 
   * <p>
   * No matter your call anywhere, any ways, you can NEVER call it on UI thread.
   * </p>
   * 
   * @param downloadInfo download info
   * @return the result of operation
   */
  public boolean pause(final DownloadInfo downloadInfo) {
    return pauseInternal(downloadInfo);
  }

  private boolean resumeInternal(DownloadInfo downloadInfo) {
    checkNonUIThread();
    if (!NetworkUtil.isNetworkConnected(GlobalConfig.getAppContext())) {
      MainThreadPostUtils.toastLong(R.string.open_network_connect);
      return false;
    }
    if (!TextUtils.isEmpty(downloadInfo.getFilePath())) {
      if (!DownloadUtils.hasEnoughSizeToDownload(
          downloadInfo.getTotalBytes() - downloadInfo.getCurrentBytes(),
          DownloadUtils.getFileFolder(downloadInfo.getFilePath()))) {
        MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_download_tips);
        return false;
      }
      if (!FileUtil.canWrite(new File(DownloadUtils.getFileFolder(downloadInfo.getFilePath())))) {
        MainThreadPostUtils.toastLong(R.string.no_write_permission);
        return false;
      }
    } else {
      if (!DownloadUtils.hasEnoughSizeToDownload(
          downloadInfo.getTotalBytes() - downloadInfo.getCurrentBytes(),
          PathUtils.getDownloadRootPath())) {
        MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_download_tips);
        return false;
      }
      if (!FileUtil.canWrite(new File(PathUtils.getDownloadRootPath()))) {
        MainThreadPostUtils.toastLong(R.string.no_write_permission);
        return false;
      }
    }
    if (!downloadServiceStarted) {
      startService();
    }
    boolean result = true;
    if (downloadInfo instanceof DownloadInfoSingle) {
      return downloadClient.resume(downloadInfo.getId());
    }
    return result;
  }

  /**
   * Resume downloadInfo with allowInMobile argument.
   * 
   * @param downloadInfo
   * @param allowInMobile
   * @return true for success
   */
  private boolean resumeInternal(DownloadInfo downloadInfo, boolean allowInMobile) {
    checkNonUIThread();
    if (!NetworkUtil.isNetworkConnected(GlobalConfig.getAppContext())) {
      MainThreadPostUtils.toastLong(R.string.open_network_connect);
      return false;
    }
    if (!TextUtils.isEmpty(downloadInfo.getFilePath())) {
      if (!DownloadUtils.hasEnoughSizeToDownload(
          downloadInfo.getTotalBytes() - downloadInfo.getCurrentBytes(),
          DownloadUtils.getFileFolder(downloadInfo.getFilePath()))) {
        MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_download_tips);
        return false;
      }
      if (!FileUtil.canWrite(new File(DownloadUtils.getFileFolder(downloadInfo.getFilePath())))) {
        MainThreadPostUtils.toastLong(R.string.no_write_permission);
        return false;
      }
    } else {
      if (!DownloadUtils.hasEnoughSizeToDownload(
          downloadInfo.getTotalBytes() - downloadInfo.getCurrentBytes(),
          PathUtils.getDownloadRootPath())) {
        MainThreadPostUtils.toastLong(R.string.no_enough_storage_to_download_tips);
        return false;
      }
      if (!FileUtil.canWrite(new File(PathUtils.getDownloadRootPath()))) {
        MainThreadPostUtils.toastLong(R.string.no_write_permission);
        return false;
      }
    }
    if (!downloadServiceStarted) {
      startService();
    }
    boolean result = true;
    if (downloadInfo instanceof DownloadInfoSingle) {
      return downloadClient.resume(downloadInfo.getId(), allowInMobile);
    }
    return result;
  }

  /**
   * Resumes a download task.
   * 
   * <p>
   * This a blocking API, so cannot be called on UI thread.
   * </p>
   * 
   * <p>
   * This function provides TWO ways to return result. One is via the return value and the other one
   * is via {@link DownloadListener#onStatusChanged(DownloadInfo)}.Some useful suggestions may help
   * you call it right.
   * </p>
   * 
   * <ul>
   * <li>
   * UI MUST handle result via {@link DownloadListener#onStatusChanged(DownloadInfo)}, since other
   * service, such as PUSH SERVICE may establish a download task in background, and UI can only get
   * this message by callback.
   * <li>
   * Background service can get the result just by return value.</li>
   * </ul>
   * 
   * <p>
   * No matter your call anywhere, any ways, you can NEVER call it on UI thread.
   * </p>
   * 
   * @param downloadInfo download info
   * @return the result of operation
   */
  public boolean resume(final DownloadInfo downloadInfo) {
    return resumeInternal(downloadInfo);
  }

  /**
   * Resumes a download task in asynchronous mode.
   * 
   * 
   * @param downloadInfo download info
   */
  public void resumeAsync(final DownloadInfo downloadInfo) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        resumeInternal(downloadInfo);
      }
    });
  }

  /**
   * Resume a downloadtask with argument allowInMobile.
   * 
   * @param downloadInfo
   * @param allowInMobile
   */
  public void resumeAsync(final DownloadInfo downloadInfo, final boolean allowInMobile) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        resumeInternal(downloadInfo, allowInMobile);
      }
    });
  }

  private boolean retryInternal(DownloadInfo downloadInfo) {
    checkNonUIThread();
    if (!NetworkUtil.isNetworkConnected(GlobalConfig.getAppContext())) {
      MainThreadPostUtils.toastLong(R.string.open_network_connect);
      return false;
    }
    if (!downloadServiceStarted) {
      startService();
    }
    DownloadInfo toNotified = null;
    boolean result = true;
    if (downloadInfo instanceof DownloadInfoSingle) {
      InnerDownloadInfo innerDownloadInfo = downloadClient.getDownloadInfo(downloadInfo.getId());
      if (innerDownloadInfo != null) { // it maybe null when task been deleted.
        innerDownloadInfo.setStatus(DownloadConstants.Status.STATUS_CREATED);
        toNotified = new DownloadInfoSingle(innerDownloadInfo);
        downloadClient.retry(downloadInfo.getId());
      }
    }
    return result;
  }

  /**
   * Retries a download task.
   * 
   * <p>
   * This a blocking API, so cannot be called on UI thread.
   * </p>
   * 
   * <p>
   * This function provides TWO ways to return result. One is via the return value and the other one
   * is via {@link DownloadListener#onStatusChanged(DownloadInfo)}.Some useful suggestions may help
   * you call it right.
   * </p>
   * 
   * <ul>
   * <li>
   * UI MUST handle result via {@link DownloadListener#onStatusChanged(DownloadInfo)}, since other
   * service, such as PUSH SERVICE may establish a download task in background, and UI can only get
   * this message by callback.</li>
   * <li>
   * Background service can get the result just by return value.</li>
   * </ul>
   * 
   * <p>
   * No matter your call anywhere, any ways, you can NEVER call it on UI thread.
   * </p>
   * 
   * @param downloadInfo download info
   * @return the result of operation
   */
  public boolean retry(final DownloadInfo downloadInfo) {
    return retryInternal(downloadInfo);
  }

  /**
   * Retries a download task in asynchronous mode.
   * 
   * 
   * @param downloadInfo download info
   */
  public void retryAsync(final DownloadInfo downloadInfo) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        retryInternal(downloadInfo);
      }
    });
  }

  /**
   * Get DownloadInfo using its identity.
   * 
   * <p>
   * If there is no download task with your identity, it will return null.
   * </p>
   * 
   * <p>
   * This function may result in a database query, which will cost some time.
   * <p>
   * 
   * @param identity identity
   * @return download info
   */
  public DownloadInfo getDownloadInfo(String identity) {
    checkNonUIThread();
    InnerDownloadInfo info = downloadClient.getDownloadInfo(identity);
    if (info != null) {
      return new DownloadInfoSingle(info);
    } else {
      return null;
    }
  }

  /**
   * Get DownloadInfo using its identity.
   * 
   * <p>
   * The return list will have the same number with your identity list. If there is no download task
   * with your identity, it will be set null.
   * </p>
   * <p>
   * This function may result in a database query, which will cost some time.
   * <p>
   * 
   * @param identities identities
   * @return a list of download info
   */
  public List<DownloadInfo> getDownloadInfosByIdentities(List<String> identities) {
    checkNonUIThread();
    List<InnerDownloadInfo> infos = downloadClient.getDownloadInfos(identities);
    if (infos == null || infos.isEmpty()) {
      /* Return null */
      /* Give a Fake ResultList */
      return new ArrayList<DownloadInfo>();
    } else {
      List<DownloadInfo> downloadInfoList = new ArrayList<DownloadInfo>();
      for (InnerDownloadInfo next : infos) {
        downloadInfoList.add(new DownloadInfoSingle(next));
      }
      return downloadInfoList;
    }
  }

  /**
   * Adds a download listener to listen to visible download event.
   * 
   * @param listener hold this reference manually, as a field, etc.
   */
  public void addVisibleListener(DownloadListener listener) {
    addListenerToProperListeners(listener, visibleDownloadListeners);
  }

  /**
   * Adds a download listener to listen to invisible download event.
   * 
   * @param listener hold this reference manually, as a field, etc.
   */
  public void addInvisibleListener(DownloadListener listener) {
    addListenerToProperListeners(listener, invisibleDownloadListeners);
  }

  private void addListenerToProperListeners(DownloadListener listener,
      List<WeakReference<DownloadListener>> to) {
    synchronized (to) {
      // Check to avoid add listener twice. Normally there are not too many listeners, so we check
      // them one by one.
      for (WeakReference<DownloadListener> ref : to) {
        DownloadListener downloadListener = ref.get();
        if (downloadListener != null && downloadListener.equals(listener)) {
          return;
        }
      }
      to.add(new WeakReference<DownloadListener>(listener));
    }
  }

  /**
   * Get the download info list according to the specific filter.
   * 
   * @param filter null if getting full list.
   * @return a list of download info
   */
  public List<DownloadInfo> getDownloadInfoList(DownloadFilter filter) {
    checkNonUIThread();
    InnerDownloadFilter innerFilter = buildInnerDownloadFilter(filter);
    List<InnerDownloadInfo> innerDownloadInfoList =
        downloadClient.getDownloadInfos(innerFilter);
    if (innerDownloadInfoList == null || innerDownloadInfoList.isEmpty()) {
      /* Return null */
      /* Give a Fake ResultList */
      return new ArrayList<DownloadInfo>();
    } else {
      List<DownloadInfo> downloadInfoList = new ArrayList<DownloadInfo>();
      for (InnerDownloadInfo next : innerDownloadInfoList) {
        DownloadInfo downloadInfo = new DownloadInfoSingle(next);
        if (!downloadInfoList.contains(downloadInfo)) {
          downloadInfoList.add(downloadInfo);
        }
      }
      return downloadInfoList;
    }
  }

  /**
   * Get the download task count using a filter, if filter is null, the count of all tasks will be
   * return.
   * 
   * @param filter
   * @return download count
   */
  public int getDownloadCount(DownloadFilter filter) {
    checkNonUIThread();
    return getDownloadInfoList(filter).size();
  }

  private static void checkNonUIThread() {
    if (Looper.getMainLooper() == Looper.myLooper()) {
      throw new IllegalStateException("Cannot call in UI thread.");
    }
  }



  private enum DownloadNetworkStatus {
    NO_NETWORK_ACCESS,
    ONLY_MOBILE_NETWORK_ACCESS,
    WIFI_NETWORK_ACCESS
  }



  private void notifyProgressChanged(final DownloadInfo info,
      List<WeakReference<DownloadListener>> listeners) {
    synchronized (listeners) {
      Iterator<WeakReference<DownloadListener>> iterator =
          listeners.iterator();
      while (iterator.hasNext()) {
        WeakReference<DownloadListener> ref = iterator.next();
        final DownloadListener listener = ref.get();
        if (listener != null) {
          /* Notify the upper layer */
          uiThreadHanlder.post(new Runnable() {
            @Override
            public void run() {
              listener.onProgressChanged(info);
            }
          });
        } else {
          iterator.remove();
        }
      }
    }
  }

  private void notifyDownloadInfoChanged(final DownloadInfo info,
      List<WeakReference<DownloadListener>> listeners) {

    /* Notify Listeners */
    synchronized (listeners) {
      Iterator<WeakReference<DownloadListener>> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        WeakReference<DownloadListener> ref = iterator.next();
        final DownloadListener listener = ref.get();
        if (listener != null) {
          /* Notify the upper layer */
          uiThreadHanlder.post(new Runnable() {
            @Override
            public void run() {
              listener.onStatusChanged(info);
            }
          });
        } else {
          iterator.remove();
        }
      }
    }

  }

  private void autoResumeTasks() {
    final InnerDownloadFilter.DownloadFilterBuilder builder =
        InnerDownloadFilter.newBuilder();
    builder.setStatusFilter(Arrays.asList(DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB));
    executor.execute(new Runnable() {

      @Override
      public void run() {
        downloadClient.resume(builder.build());
      }
    });
  }

  /**
   * update extra values.
   * 
   * @param id
   * @param key
   * @param value
   */
  public void putExtraArgument(long id, String key, String value) {
    downloadClient.putExtraArgument(id, key, value);
  }

  /**
   * Update extras values.
   * 
   * @param id
   * @param map
   */
  public void putExtraArgument(long id, HashMap<String, String> map) {
    downloadClient.putExtraArgument(id, map);
  }

  /**
   * Get Single Download infos.
   * 
   * @param ids
   * @return Single Download infos
   */
  // TODO(qisentang): should implements getting group download info.
  public List<DownloadInfo> getDownloadInfosByIds(List<Long> ids) {
    List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
    for (long id : ids) {
      InnerDownloadInfo innerDownloadInfo = downloadClient.getDownloadInfo(id);
      if (innerDownloadInfo != null) {
        downloadInfos.add(new DownloadInfoSingle(innerDownloadInfo));
      }
    }
    return downloadInfos;
  }

  private static InnerDownloadRequest buildInnerDownloadRequest(DownloadRequest request) {
    if (request == null) {
      Log.d(TAG, "Request is null!");
      return null;
    }

    /* Convert type */
    ResourceType type = DataUtils.getInnerResourceType(request.contentType);
    InnerDownloadRequest.Builder builder = new InnerDownloadRequest.Builder(request.url, type);
    if (!TextUtils.isEmpty(request.identity)) {
      builder.setIdentity(request.identity);
    }
    if (!TextUtils.isEmpty(request.title)) {
      builder.setTitle(request.title);
    }
    if (request.verifyType != null && !TextUtils.isEmpty(request.verifyValue)) {
      switch (request.verifyType) {
        case MD5:
          builder.setVerifyInfo(InnerDownloadRequest.VerifyType.MD5, request.verifyValue);
          break;
        case PF5:
          builder.setVerifyInfo(InnerDownloadRequest.VerifyType.PF5, request.verifyValue);
          break;
        default:
          break;
      }
    }
    return builder.setAllowedInMobile(request.allowInMobile)
        .setCheckSize(request.checkSize)
        .setDescription(request.description)
        .setFileName(request.fileName)
        .setFolderPath(request.folderPath)
        .setSerializeExtras(request.extraResource).setSource(request.source)
        .setThreshold(request.threshold)
        .setTotalBytes(request.totalBytes).setIconUrl(request.iconUrl)
        .setVisible(request.visible)
        .setSpeedLimit(request.speedLimit).build();
  }

  private static InnerDownloadFilter buildInnerDownloadFilter(DownloadFilter filter) {
    if (filter == null) {
      /* No Filter, get all */
      return null;
    } else {
      InnerDownloadFilter.DownloadFilterBuilder builder = InnerDownloadFilter.newBuilder();
      /* Set Type Filter */
      if (filter.getAcceptedType() != null && !filter.getAcceptedType().isEmpty()) {
        List<ResourceType> types = new ArrayList<ResourceType>();
        for (DownloadInfo.ContentType next : filter.getAcceptedType()) {
          types.add(DataUtils.getInnerResourceType(next));
        }
        builder.setTypeFilter(types);
      }

      if (filter.getAcceptedStatus() != null && !filter.getAcceptedStatus().isEmpty()) {
        List<Integer> status = new ArrayList<Integer>();

        for (DownloadInfo.Status next : filter.getAcceptedStatus()) {
          if (DataUtils.getInnerStatusCode(next) != null) {
            status.addAll(DataUtils.getInnerStatusCode(next));
          }
        }
        builder.setStatusFilter(status);
      }

      if (filter.getAcceptedExtra() != null && !filter.getAcceptedExtra().isEmpty()) {
        builder.setExtraFilter(filter.getAcceptedExtra());
      }

      long min = filter.getMinSize();
      long max = filter.getMaxSize();

      if (min == -1 && max > 0) {
        builder.setSizeFilter(Operator.LESS, (int) max);
      } else if (min > 0 && max == -1) {
        builder.setSizeFilter(Operator.MORE, (int) min);
      } else if (min == max && min != -1) {
        builder.setSizeFilter(Operator.EQUAL, (int) min);
      }
      if (filter.getVisible() != null) { // it had been set
        builder.setVisibilityFilter(filter.getVisible().booleanValue());
      }
      return builder.build();
    }
  }

  public long getUnfinishedTaskSpaceNeeded() {
    return downloadClient.getUnfinishedTaskSpaceNeeded();
  }
}
