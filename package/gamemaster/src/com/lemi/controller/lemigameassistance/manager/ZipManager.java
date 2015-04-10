package com.lemi.controller.lemigameassistance.manager;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.utils.SuffixUtils;
import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.base.utils.MainThreadPostUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Zip Manager used to observe zip and unzip both status and progress.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ZipManager {

  private final Set<WeakReference<ZipListener>> listeners =
      new HashSet<WeakReference<ZipListener>>();
  private final Map<String, ZipStatus> statusMap = new HashMap<String, ZipStatus>();
  private final Map<String, Integer> progressMap = new HashMap<String, Integer>();
  private final Set<String> cancelSet = new HashSet<>();

  private static final int BUFFER_SIZE = 1024;
  private static final int MIN_NOTIFY_STEP = 1024 * 15;
  private static final int GAME_UNZIP_THREAD_NUM = 1;
  private static final long GAME_UNZIP_THREAD_CACHE_TIME_MS = 60 * 1000L;

  private long notifiedByteSize;
  private CachedThreadPoolExecutorWithCapacity gameUnzipThreadPool;


  // used to ensure won't start unzip progress when rolling back.
  private byte[] rollBackLock = new byte[0];

  public enum ZipStatus {
    // unzip task is created.
    CREATE,
    // unzip task has began and haven't finished.
    UNZIPPING,
    // unzip task execute failed.
    FAILED,
    // cancel by cancel() method
    CANCEL_BY_USER,
    // unzip task succeeded.
    SUCCESS
  }

  private static ZipManager instance;

  /**
   * Get Singleton instance of zipmanager
   * 
   * @return instance.
   */
  public static synchronized ZipManager getInstance() {
    if (instance == null) {
      instance = new ZipManager();
    }
    return instance;
  }

  private ZipManager() {
    gameUnzipThreadPool =
        new CachedThreadPoolExecutorWithCapacity(GAME_UNZIP_THREAD_NUM,
            GAME_UNZIP_THREAD_CACHE_TIME_MS);
  }

  /**
   * Listen used to observe zip status and progress change.
   */
  public interface ZipListener {
    /**
     * notify that zip task with given token that status has changed.
     * 
     * @param token used to differentiate zip task.
     * @param status status of zip task.
     */
    void onZipStatusChange(String token, ZipStatus status);

    /**
     * notify that zip task with given token that progress has changed.
     * 
     * @param token used to differentiate zip task.
     * @param percent percent of current progress.
     */
    void onZipProgressChange(String token, int percent);

  }

  /**
   * Add listener to observe status and progress change.
   * 
   * @param listener ziplistener
   */
  public void addListener(ZipListener listener) {
    synchronized (listeners) {
      listeners.add(new WeakReference<ZipListener>(listener));
    }
  }

  /**
   * Add unzip task to unzip given sourceFile to assigned output path.
   * 
   * <p>
   * it will start a new thread to unzip file, you can observe progress and status change by
   * register callback. {@link ZipManager.ZipListener}
   * </p>
   * 
   * @param token token to mark unzip task.
   * @param sourceFile which file to unzip.
   * @param outPutPath where to store unzipped files.
   */
  public void startUnZipTaskAsync(String token, String sourceFile,
      final String outPutPath, final UnzipCallback callback) {
    final UnZipRunnable runnable;
    synchronized (rollBackLock) {
      runnable = new UnZipRunnable(token, sourceFile, outPutPath);
      updateAndNotifyStatus(token, ZipStatus.CREATE);
      new Thread() {
        @Override
        public void run() {
          runnable.run();
          if (callback != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
              @Override
              public void run() {
                callback.onUnzipCompleted(outPutPath);
              }
            });
          }
        }
      }.start();
    }
  }



  /**
   * Add unzip task to unzip given sourceFile to assigned output path.
   *
   * <p>
   * it will start a new thread to unzip file, you can observe progress and status change by
   * register callback. {@link ZipManager.ZipListener}
   * </p>
   *
   * @param token token to mark unzip task.
   * @param sourceFile which file to unzip.
   * @param gameApkOutPutPath where to store apk unzipped files.
   * @param gameDataOutPutPath where to store data unzipped files.
   * @param installApkFullPath the full path which apk place
   */
  public void startUnZipGameDataTaskAsync(String token, String sourceFile,
      final String gameApkOutPutPath, final String gameDataOutPutPath, String installApkFullPath) {
    UnZipGameDataRunnable runnable;
    synchronized (rollBackLock) {
      runnable =
          new UnZipGameDataRunnable(token, sourceFile, gameApkOutPutPath, gameDataOutPutPath,
              installApkFullPath);
      updateAndNotifyStatus(token, ZipStatus.CREATE);
      gameUnzipThreadPool.execute(runnable);
    }
  }


  /**
   * get status of task with given token.
   * 
   * <p>
   * If unzip task is stopped by unpredicted reason, e.x. cellphone shutting download. you can not
   * get status of this stopped one, because we don't persistence executed tasks.
   * </p>
   * 
   * @param token token to mark unzip task.
   * @return status if task is in list, else return none.
   */
  public ZipStatus getStatus(String token) {
    synchronized (statusMap) {
      return statusMap.get(token);
    }
  }

  public void cancel(String token) {
    if (TextUtils.isEmpty(token)) {
      return;
    }
    synchronized (cancelSet) {
      cancelSet.add(token);
    }
  }

  /**
   * get Token Set which is Un-Done
   *
   * @return UnFinish Token Set
   */
  public Set<String> getUnFinishedToken() {
    Set<String> unFinishedList = new HashSet<>();
    synchronized (statusMap) {
      Iterator<String> iterator = statusMap.keySet().iterator();
      while (iterator.hasNext()) {
        String token = iterator.next();
        ZipStatus zipStatus = statusMap.get(token);
        if (zipStatus == ZipStatus.CREATE || zipStatus == ZipStatus.UNZIPPING) {
          unFinishedList.add(token);
        }
      }
    }
    return unFinishedList;
  }

  /**
   * get UnZip progress
   * 
   * @param token token to mark unzip task.
   * @return progress.
   */
  public int getProgress(String token) {
    synchronized (progressMap) {
      return progressMap.get(token) == null ? 0 : progressMap.get(token);
    }
  }

  private void invokeAndNotify(final ListenerRunnable runnable) {
    synchronized (listeners) {
      Iterator<WeakReference<ZipListener>> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        WeakReference<ZipListener> reference = iterator.next();
        final ZipListener listener = reference.get();
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
    void run(ZipListener listener);
  }

  private void updateAndNotifyStatus(final String token, final ZipStatus status) {
    synchronized (statusMap) {
      statusMap.put(token, status);
    }
    invokeAndNotify(new ListenerRunnable() {
      @Override
      public void run(ZipListener listener) {
        listener.onZipStatusChange(token, status);
      }
    });
  }

  private void updateAndNotifyProgress(final String token, final long current, final long total) {
    final int progress = (int) (current * 100 / total) > 100 ? 100 : (int) (current * 100 / total);
    synchronized (progressMap) {
      progressMap.put(token, progress);
    }
    if (current - notifiedByteSize > MIN_NOTIFY_STEP) {
      notifiedByteSize = current;
      invokeAndNotify(new ListenerRunnable() {
        @Override
        public void run(ZipListener listener) {
          listener.onZipProgressChange(token, progress);
        }
      });
    }
  }

  /**
   * If error happens when zipping or unzipping files, we will need to delete files we have
   * produced.
   * 
   * @param paths root paths of files to delete.
   */
  private void rollback(List<String> paths) {
    // TODO(tangqisen) if in roolback step, it didn't not allow to unzip.
    synchronized (rollBackLock) {
      for (String path : paths) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
          FileUtil.deletePath(path);
        }
      }
    }
  }

  /**
   * Callback of unzip process.
   */
  public interface UnzipCallback {

    /**
     * Get called when unzip completed
     * 
     * @param output
     */
    void onUnzipCompleted(String output);
  }

  /**
   * UnZip thread with name "UnZip Task token".
   * 
   * <p>
   * when progress or status change, it will call notify function to send messages about progress
   * and status change.
   * </p>
   */
  private class UnZipRunnable implements Runnable {

    private final String token;
    private final String sourceFile;
    private String outPutPath;

    /**
     * runnable to unzip given source file to assigned outPutPath.
     * 
     * @param token token to mark unzip task.
     * @param source file path of unzip file.
     * @param outPutPath unzip destination.
     */
    public UnZipRunnable(String token, String source, String outPutPath) {
      this.token = token;
      this.sourceFile = source;
      this.outPutPath = outPutPath;
    }

    @Override
    public void run() {
      ZipInputStream inZip = null;
      FileOutputStream outputStream = null;
      ZipEntry zipEntry;
      String entryName;
      long totalSize;
      long currentSize = 0;
      ZipStatus finalStatus = ZipStatus.FAILED;
      List<String> createdDirs = new ArrayList<String>();

      try {
        inZip = new ZipInputStream(new FileInputStream(
            sourceFile));
        byte[] buffer = new byte[BUFFER_SIZE];
        if (!outPutPath.endsWith(File.separator)) {
          outPutPath += File.separator;
        }
        if (!TextUtils.isEmpty(outPutPath) && !outPutPath.endsWith(File.separator)) {
          outPutPath += File.separator;
        }

        // get size of compressed file.
        totalSize = new File(sourceFile).length();

        // notify unzip currentSize has began.
        updateAndNotifyStatus(token, ZipStatus.UNZIPPING);


        while ((zipEntry = inZip.getNextEntry()) != null) {
          checkIfCancel();
          entryName = zipEntry.getName();

          if (zipEntry.isDirectory()) {
            entryName = entryName.substring(0, entryName.length() - 1);
            // make directory.
            new File(outPutPath + entryName).mkdirs();
            createdDirs.add(outPutPath + entryName);
          } else {
            File file = new File(outPutPath + entryName);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
              parentFile.mkdirs();
            }
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            int len;
            while ((len = inZip.read(buffer)) != -1) {
              checkIfCancel();
              outputStream.write(buffer, 0, len);
              outputStream.flush();
              currentSize += len;
              updateAndNotifyProgress(token, currentSize, totalSize);
            }
          }
        }
        finalStatus = ZipStatus.SUCCESS;
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        // exception happens, notify with failed status.
        finalStatus = ZipStatus.FAILED;
      } catch (IOException e) {
        e.printStackTrace();
        // exception happens, notify with failed status.
        finalStatus = ZipStatus.FAILED;
        rollback(createdDirs);
      } catch (InterruptedException e) {
        e.printStackTrace();
        finalStatus = ZipStatus.CANCEL_BY_USER;
        rollback(createdDirs);
      } catch (Exception e) {
        e.printStackTrace();
        finalStatus = ZipStatus.FAILED;
        rollback(createdDirs);
      } finally {
        updateAndNotifyStatus(token, finalStatus);
        close(outputStream);
        close(inZip);
      }
    }

    protected void checkIfCancel() throws InterruptedException {
      synchronized (cancelSet) {
        if (cancelSet.contains(token)) {
          throw new InterruptedException("task is cancel");
        }
      }
    }

    protected void setIsCanceled() {
      synchronized (cancelSet) {
        cancelSet.remove(token);
      }
    }

  }



  /**
   * UnZip thread with name "UnZip Task token".
   *
   * <p>
   * when progress or status change, it will call notify function to send messages about progress
   * and status change.
   * </p>
   */
  private class UnZipGameDataRunnable implements Runnable {

    private final String token;
    private final String sourceFile;
    private String outPutPath;


    // for game data use only
    private String gameDataOutPutPath;
    private String installApkFullPath;

    /**
     * runnable to unzip given source file to assigned outPutPath.
     *
     * @param token token to mark unzip task.
     * @param source file path of unzip file.
     * @param gameApkOutPutPath game apk unzip destination.
     * @param gameDataOutPutPath game data unzip destination.
     */
    public UnZipGameDataRunnable(String token, String source, String gameApkOutPutPath,
        String gameDataOutPutPath, String installApkFullPath) {
      this.token = token;
      this.sourceFile = source;
      this.outPutPath = gameApkOutPutPath;
      this.gameDataOutPutPath = gameDataOutPutPath;
      this.installApkFullPath = installApkFullPath;
    }

    @Override
    public void run() {
      ZipInputStream inZip = null;
      FileOutputStream outputStream = null;
      ZipEntry zipEntry;
      String entryName;
      long totalSize;
      long currentSize = 0;
      ZipStatus finalStatus = ZipStatus.FAILED;
      List<String> createdDirs = new ArrayList<String>();

      try {
        inZip = new ZipInputStream(new FileInputStream(
            sourceFile));
        byte[] buffer = new byte[BUFFER_SIZE];
        if (!TextUtils.isEmpty(outPutPath) && !outPutPath.endsWith(File.separator)) {
          outPutPath += File.separator;
        }
        if (!TextUtils.isEmpty(gameDataOutPutPath) && !gameDataOutPutPath.endsWith(File.separator)) {
          gameDataOutPutPath += File.separator;
        }

        // get size of compressed file.
        totalSize = new File(sourceFile).length();

        // notify unzip currentSize has began.
        updateAndNotifyStatus(token, ZipStatus.UNZIPPING);


        while ((zipEntry = inZip.getNextEntry()) != null) {
          checkIfCancel();
          entryName = dropFirstFolder(zipEntry.getName());

          if (TextUtils.isEmpty(entryName)) {
            continue;
          }

          String exactOutPutPath = getExactOutPutPath(entryName);

          if (zipEntry.isDirectory()) {
            entryName = entryName.substring(0, entryName.length() - 1);
            // make directory.
            new File(exactOutPutPath + entryName).mkdirs();
            createdDirs.add(exactOutPutPath + entryName);
          } else {
            File file;
            if (SuffixUtils.getContentTypeBySuffix(entryName) == DownloadInfo.ContentType.APP) {
              file = new File(installApkFullPath);
            } else {
              file = new File(exactOutPutPath + entryName);
            }
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
              parentFile.mkdirs();
            }
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            int len;
            while ((len = inZip.read(buffer)) != -1) {
              checkIfCancel();
              outputStream.write(buffer, 0, len);
              outputStream.flush();
              currentSize += len;
              updateAndNotifyProgress(token, currentSize, totalSize);
            }
          }
        }
        finalStatus = ZipStatus.SUCCESS;
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        // exception happens, notify with failed status.
        finalStatus = ZipStatus.FAILED;
      } catch (IOException e) {
        e.printStackTrace();
        // exception happens, notify with failed status.
        finalStatus = ZipStatus.FAILED;
        rollback(createdDirs);
      } catch (InterruptedException e) {
        e.printStackTrace();
        finalStatus = ZipStatus.CANCEL_BY_USER;
        rollback(createdDirs);
      } catch (Exception e) {
        e.printStackTrace();
        finalStatus = ZipStatus.FAILED;
        rollback(createdDirs);
      } finally {
        updateAndNotifyStatus(token, finalStatus);
        close(outputStream);
        close(inZip);
        setIsCanceled();
      }
    }


    private String getExactOutPutPath(String entryName) {
      if (TextUtils.isEmpty(gameDataOutPutPath)) {
        return outPutPath;
      }
      if (SuffixUtils.getContentTypeBySuffix(entryName) == DownloadInfo.ContentType.APP) {
        return outPutPath;
      } else {
        return gameDataOutPutPath;
      }
    }

    private String dropFirstFolder(String entryName) {
      if (TextUtils.isEmpty(entryName)) {
        return null;
      }
      if (!entryName.contains(File.separator) || entryName.length() < 2) {
        return entryName;
      }

      return entryName.substring(entryName.indexOf(File.separator) + 1);
    }

    protected void checkIfCancel() throws InterruptedException {
      synchronized (cancelSet) {
        if (cancelSet.contains(token)) {
          throw new InterruptedException("task is cancel");
        }
      }
    }

    protected void setIsCanceled() {
      synchronized (cancelSet) {
        cancelSet.remove(token);
      }
    }

  }


  private static void close(Closeable dest) {
    if (dest != null) {
      try {
        dest.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
