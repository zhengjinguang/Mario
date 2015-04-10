package com.lemi.controller.lemigameassistance.manager;

import android.os.Looper;
import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.download.DownloadManager;
import com.lemi.controller.lemigameassistance.download.DownloadUtils;
import com.lemi.controller.lemigameassistance.utils.PathUtils;
import com.lemi.controller.lemigameassistance.utils.ThreadPool;
import com.lemi.mario.appmanager.AppManager;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.SizeConvertUtil;
import com.lemi.mario.base.utils.StringUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * use this class to clean useless data which create by game-master
 *
 * @author zhengjinguang@letv.com (shining)
 * @author liuxv5@letv.com (Liu Xu)
 */
public class CleanManager {

  private static CleanManager instance;

  private static final String[] PATH_TO_SEARCH = {"/Android/data",
      "/Android/obb"};
  private static final String[] EXCEPT_PATHS = {"/SF_IV_DATA",
      "/gameloft/games/GloftD4HC"};
  private static final String[] EXCEPT_PACKAGE_NAME = {
      "jp.co.capcom.android.sf4_no_compress_googleplay",
      "com.gameloft.android.ANMP.GloftD4HC"};
  private static final String LETV_LOG_PATH = "/Log";
  private static final String HIDEN_FILE_BEGIN = ".";
  private long releaseSizeInByte = 0l;
  private CleanState cleanState = CleanState.IDLE;
  private final Set<WeakReference<OnFinishCleanListener>> listeners =
      new HashSet<WeakReference<OnFinishCleanListener>>();

  private OnFinishCleanListener defaultToastListener = new OnFinishCleanListener() {
    @Override
    public void onFinishClean(long releaseSizeInByte) {
      if (releaseSizeInByte <= 0l) {
        MainThreadPostUtils.toastLong(R.string.no_garbage);
      } else {
        MainThreadPostUtils.toastLong(StringUtil.getString(R.string.clear_space,
            SizeConvertUtil.transByte2MBAndFormat(releaseSizeInByte)));
      }
    }
  };

  private WeakReference<OnFinishCleanListener> defaultToastListenerWeakReference =
      new WeakReference<OnFinishCleanListener>(defaultToastListener);

  private static enum CleanState {
    IDLE, CLEANING, DONE
  }

  private CleanManager() {

  }

  public static synchronized CleanManager getInstance() {
    if (instance == null) {
      instance = new CleanManager();
    }
    return instance;
  }

  public void setCleanListener(OnFinishCleanListener onFinishCleanListener) {
    if (onFinishCleanListener != null) {
      synchronized (listeners) {
        listeners.add(new WeakReference<>(onFinishCleanListener));
      }
    }
  }

  /**
   *
   * @return garbage is being cleaned.
   *
   */
  public boolean isCleaning() {
    return cleanState == CleanState.CLEANING;
  }

  /**
   * async clean garbage in a ThreadPool.
   */
  public void asyncClean() {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        syncClean();
      }
    });
  }

  /**
   * use this function to receive default toast, default toast only use once
   */
  public void asyncCleanWithDefaultToast() {
    synchronized (listeners) {
      listeners.add(defaultToastListenerWeakReference);
    }
    asyncClean();
  }

  /**
   * clean garbage, can not be called in UI Thread.
   * 
   */
  public void syncClean() {
    checkNonUIThread();
    if (cleanState == CleanState.CLEANING) {
      return;
    }
    cleanState = CleanState.CLEANING;
    releaseSizeInByte = 0;

    cleanAllDataFile();
    cleanAllDownloadFile();
    cleanAllLogFile();

    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        notifyFinish();
        cleanState = CleanState.DONE;
      }
    });
  }

  public void cleanAllLogFile() {
    String path = Constants.SDCARD_PATH + LETV_LOG_PATH;
    countReleaseSize(path);
    FileUtil.deletePath(path);
  }

  /**
   * clean all data folder , without processing or installed
   */
  public void cleanAllDataFile() {
    Set<String> processingSet = getOnProcessSet();
    cleanAllDataFileInRegularPath(processingSet);
    cleanAllDataFileInParticularPath(processingSet);
  }

  /**
   * clean all download file , without process file
   * 
   */
  public void cleanAllDownloadFile() {
    Set<String> processingSet = getOnProcessSet();
    cleanAllDownloadApkFile(processingSet);
    cleanAllDownloadZipFile(processingSet);
  }

  /**
   * clean unzip file by packageName
   * notice: this function will not check process status and install status , caller must make sure
   * the package which will be cleaned is not in-processing or in-install
   * 
   */
  public void cleanDataFileByPackageName(String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return;
    }
    for (String folderPath : PATH_TO_SEARCH) {
      cleanDataFile(Constants.SDCARD_PATH + folderPath, packageName);
    }

  }

  /**
   * clean download file by packageName
   * notice: this function will not check process status , caller must make sure the package which
   * will be cleaned is not in-processing
   * 
   * @param packageName the data by which packageName
   */
  public void cleanDownloadFileByPackageName(String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return;
    }
    checkNonUIThread();
    DownloadInfo downloadInfo = DownloadManager.getInstance().getDownloadInfo(packageName);
    if (downloadInfo != null) {
      cleanDownloadFileByDownloadInfo(downloadInfo);
    } else {
      /**
       * clean download files when download DB is not exist
       */
      cleanDownloadFileAtProbabilityPosition(packageName);
    }

  }

  /**
   * clean download file by download info
   * notice: this function will not check process status , caller must make sure the package which
   * will be cleaned is not in-processing
   *
   * @param downloadInfo the download-info when downloaded
   */
  public void cleanDownloadFileByDownloadInfo(DownloadInfo downloadInfo) {
    if (downloadInfo == null) {
      return;
    }
    /**
     * delete download file , maybe on SD-card or U-disk, the file maybe xxx.zip or xxx.apk.
     */
    FileUtil.deleteFile(downloadInfo.getFilePath());
    /**
     * delete the unzip apk on SD-card.
     */
    FileUtil.deleteFile(PathUtils.getApkFullPath(downloadInfo.getIdentity()));
  }


  private void cleanDownloadFileAtProbabilityPosition(String packageName) {
    /**
     * delete install apk file or download apk file on SD-card
     */
    FileUtil.deleteFile(PathUtils.getApkFullPath(packageName));
    /**
     * delete download zip file on SD-card
     */
    FileUtil.deleteFile(PathUtils.getDownloadFolderPath(DownloadInfo.ContentType.ZIP)
        + File.separator + packageName + Constants.ZIP_SUFFIX);
    /**
     * delete download apk file on U-disk
     */
    FileUtil.deleteFile(PathUtils.getExternalStorageDownloadPath() + File.separator
        + DownloadInfo.ContentType.APP.toString()
        + File.separator + packageName + Constants.APK_SUFFIX);
    /**
     * delete download zip file on U-disk
     */
    FileUtil.deleteFile(PathUtils.getExternalStorageDownloadPath() + File.separator
        + DownloadInfo.ContentType.ZIP.toString()
        + File.separator + packageName + Constants.ZIP_SUFFIX);
  }

  private void cleanAllDownloadZipFile(Set<String> processingSet) {
    /**
     * delete download .zip in SD-card
     */
    cleanAllFileWithoutProcessing(PathUtils.getDownloadFolderPath(DownloadInfo.ContentType.ZIP),
        processingSet);
    /**
     * delete download .zip in U-disk
     */
    cleanAllFileWithoutProcessing(PathUtils.getExternalStorageDownloadPath() + File.separator
        + DownloadInfo.ContentType.ZIP.toString(), processingSet);
  }

  private void cleanAllDownloadApkFile(Set<String> processingSet) {
    /**
     * delete download .apk in SD-card
     */
    cleanAllFileWithoutProcessing(PathUtils.getDownloadFolderPath(DownloadInfo.ContentType.APP),
        processingSet);
    /**
     * delete download .apk in U-disk
     */
    cleanAllFileWithoutProcessing(PathUtils.getExternalStorageDownloadPath() + File.separator
        + DownloadInfo.ContentType.APP.toString(), processingSet);
  }

  private void cleanAllFileWithoutProcessing(String path, Set<String> processingSet) {
    File file = new File(path);
    if (!file.exists()) {
      return;
    }
    String[] fileList = file.list();
    for (String fileName : fileList) {
      if (isOnPrecessing(getPackageNameFromFile(fileName), processingSet)) {
        continue;
      }
      String tmpPath = null;
      if (path.endsWith(File.separator)) {
        tmpPath = path + fileName;
      } else {
        tmpPath = path + File.separator + fileName;
      }
      File tmpFile = new File(tmpPath);
      if (tmpFile.isFile()) {
        countReleaseSize(tmpPath);
        tmpFile.delete();
      }
      if (tmpFile.isDirectory()) {
        cleanAllFileWithoutProcessing(tmpPath, processingSet);
      }
    }
  }

  private void cleanAllDataFileInRegularPath(Set<String> processingSet) {
    for (String folderPath : PATH_TO_SEARCH) {
      cleanAllDataFileWithoutProcessingAndInstalled(Constants.SDCARD_PATH + folderPath,
          processingSet);
    }
  }

  private void cleanAllDataFileInParticularPath(Set<String> processingSet) {
    for (int i = 0; i < EXCEPT_PATHS.length; i++) {
      cleanParticularDataFileWithoutProcessingAndInstalled(Constants.SDCARD_PATH + EXCEPT_PATHS[i],
          EXCEPT_PACKAGE_NAME[i], processingSet);
    }

  }

  private void cleanAllDataFileWithoutProcessingAndInstalled(String path, Set<String> processingSet) {
    File file = new File(path);
    if (!file.exists()) {
      return;
    }
    String[] folderList = file.list();
    for (String fileName : folderList) {
      if (isHidenFile(fileName)) {
        continue;
      }
      if (isOnPrecessing(fileName, processingSet)) {
        continue;
      }
      if (AppManager.getInstance().syncIsAppInstalled(fileName)) {
        continue;
      }
      String tmpPath = null;
      if (path.endsWith(File.separator)) {
        tmpPath = path + fileName;
      } else {
        tmpPath = path + File.separator + fileName;
      }
      File tmpFile = new File(tmpPath);
      if (tmpFile.isDirectory()) {
        countReleaseSize(tmpPath);
        FileUtil.deletePath(tmpPath);
      }
    }
  }

  private void cleanParticularDataFileWithoutProcessingAndInstalled(String path,
      String packageName, Set<String> processingSet) {
    File file = new File(path);
    if (!file.exists()) {
      return;
    }
    if (isOnPrecessing(packageName, processingSet)) {
      return;
    }
    if (AppManager.getInstance().syncIsAppInstalled(packageName)) {
      return;
    }
    if (file.isDirectory()) {
      countReleaseSize(path);
      FileUtil.deletePath(path);
    }
  }


  private void cleanDataFile(String path, String packageName) {
    if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(path)) {
      return;
    }
    FileUtil.deletePath(path + File.separator + packageName);
  }


  /**
   * getPackageName from file name , in our name protocol.
   * See {@link com.lemi.controller.lemigameassistance.download.DownloadRequestBuilder}
   */
  private String getPackageNameFromFile(String fileName) {
    if (TextUtils.isEmpty(fileName)) {
      return null;
    }
    if (fileName.contains(Constants.APK_SUFFIX)) {
      return fileName.substring(0, fileName.indexOf(Constants.APK_SUFFIX));
    }
    if (fileName.contains(Constants.ZIP_SUFFIX)) {
      return fileName.substring(0, fileName.indexOf(Constants.ZIP_SUFFIX));
    }
    return null;
  }


  private boolean isOnPrecessing(String packageName, Set<String> processingSet) {
    if (TextUtils.isEmpty(packageName) || CollectionUtils.isEmpty(processingSet)) {
      return false;
    }
    return processingSet.contains(packageName);
  }

  private boolean isHidenFile(String fileName) {
    if (TextUtils.isEmpty(fileName)) {
      return false;
    }
    return fileName.indexOf(HIDEN_FILE_BEGIN) == 0;
  }


  private Set<String> getOnProcessSet() {
    Set<String> onProcessSet = new HashSet<>();
    Set<String> downloadingSet = getDownloadProcessSet();
    Set<String> unzipingSet = getUnzipProcessSet();
    Set<String> installingSet = getInstallProcessSet();
    if (!CollectionUtils.isEmpty(downloadingSet)) {
      onProcessSet.addAll(downloadingSet);
    }
    if (!CollectionUtils.isEmpty(unzipingSet)) {
      onProcessSet.addAll(unzipingSet);
    }
    if (!CollectionUtils.isEmpty(installingSet)) {
      onProcessSet.addAll(installingSet);
    }
    return onProcessSet;
  }

  private Set<String> getDownloadProcessSet() {
    List<DownloadInfo> downloadInfoList =
        DownloadManager.getInstance().getDownloadInfoList(
            DownloadUtils.createOnGoingDownloadFilterBuilder().build());
    if (CollectionUtils.isEmpty(downloadInfoList)) {
      return null;
    }
    Set<String> onDownloadProcessSet = new HashSet<>();
    for (DownloadInfo downloadInfo : downloadInfoList) {
      if (downloadInfo == null || TextUtils.isEmpty(downloadInfo.getIdentity())) {
        continue;
      }
      onDownloadProcessSet.add(downloadInfo.getIdentity());
    }
    return onDownloadProcessSet;
  }

  private Set<String> getUnzipProcessSet() {
    return ZipManager.getInstance().getUnFinishedToken();
  }

  private Set<String> getInstallProcessSet() {
    return InstallManager.getInstance().getUnFinishedToken();
  }

  /**
   * count release size before delete
   */
  private void countReleaseSize(String filePath) {
    releaseSizeInByte += FileUtil.getFileSize(filePath);
  }

  private void notifyFinish() {
    invokeAndNotify(new ListenerRunnable() {
      @Override
      public void run(OnFinishCleanListener listener) {
        listener.onFinishClean(releaseSizeInByte);
      }
    });
  }

  private void invokeAndNotify(final ListenerRunnable runnable) {
    synchronized (listeners) {
      Iterator<WeakReference<OnFinishCleanListener>> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        WeakReference<OnFinishCleanListener> reference = iterator.next();
        final OnFinishCleanListener listener = reference.get();
        if (listener == null) {
          iterator.remove();
        } else if (listener == defaultToastListener) {
          /**
           * toast default string and remove
           */
          iterator.remove();
          MainThreadPostUtils.post(new Runnable() {
            @Override
            public void run() {
              runnable.run(listener);
            }
          });
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

  private static void checkNonUIThread() {
    if (Looper.getMainLooper() == Looper.myLooper()) {
      throw new IllegalStateException("Cannot call in UI thread.");
    }
  }

  private interface ListenerRunnable {
    void run(OnFinishCleanListener listener);
  }

  public interface OnFinishCleanListener {
    /**
     * interface to call when finished cleaning garbage
     * 
     * @param releaseSizeInByte total released space of system in bytes
     */
    void onFinishClean(long releaseSizeInByte);
  }


}
