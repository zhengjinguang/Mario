package com.lemi.mario.download.rpc;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.SparseIntArray;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.download.listener.DownloadVerifier;
import com.lemi.mario.download.listener.InnerBlockDownloadListener;
import com.lemi.mario.download.listener.MD5Confirmer;
import com.lemi.mario.download.listener.NetworkStatusStub;
import com.lemi.mario.download.listener.SizeWatcher;
import com.lemi.mario.download.log.DownloadLog;
import com.lemi.mario.download.model.DownloadUrlInfo;
import com.lemi.mario.download.model.segments.DownloadBlockInfo;
import com.lemi.mario.download.model.segments.DownloadConfigInfo;
import com.lemi.mario.download.rpc.BlockDownloadTask.BlockStatus;
import com.lemi.mario.download.rpc.DownloadConstants.PauseReason;
import com.lemi.mario.download.rpc.speed.GlobalSpeedManager;
import com.lemi.mario.download.rpc.speed.SpeedHelper;
import com.lemi.mario.download.utils.DownloadUtil;
import com.lemi.mario.download.utils.MD5Util;
import com.lemi.mario.download.utils.Pf5Util;
import com.lemi.mario.download.utils.StatusUtil;
import com.twmacinta.util.MD5;
import com.twmacinta.util.MD5State;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;


/**
 * This class is used to draw a abstract implementation of download task.
 * A download task may use one or more {@link BlockDownloadTask}, and it's transparent to upper
 * layer.
 * 
 * Every download thread means downloading a segment.
 * If download task only contains one thread, it means one segment which download data from start to
 * finish.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class DownloadTask {

  private static final String TAG = DownloadTask.class.getSimpleName();

  // separation time 200ms
  private static final long RETRY_INTERVAL_MS = 200L;

  private static final int MAX_RETRY_TIME = 5;

  // avoid notify progress change too frequently.
  private static final long NOTIFY_PROGRESS_INTERVAL_MS = 1500L;

  private static final long UPDATE_PROGRESS_INTERVAL_MS = 2000L;

  private static final long SPEED_NOTIFY_DELAY = 500L;
  private static final long MIN_SPEED = 5 * 1024L;

  /** used for dservice request, it must be single thread to keep request order. */
  private static final Executor DSERVICE_EXECUTOR = new CachedThreadPoolExecutorWithCapacity(1);

  // download info
  private final InnerDownloadInfo info;

  private final SpeedHelper speedHelper;

  /**
   * This SparseIntArray is used to recode retried times.
   */
  private final SparseIntArray retryTimes = new SparseIntArray(
      DownloadConstants.MAX_THREADS_USED_FOR_ONE_TASK);

  // fibonacci array used to recode next retry time.
  private final int[] fibonacciArray = new int[] {1, 1, 2, 3, 5};

  private final ExecutorService executor;

  private final HashMap<Integer, TimerTask> scheduledContinueDownloadTimerTask =
      new HashMap<Integer, TimerTask>();

  private final DownloadFinishedListener finishedListener;

  private final Context context;

  private final DownloadVerifier downloadVerifier;

  private final MD5Confirmer md5Confirmer;

  private final NetworkStatusStub networkStatusStub;

  private final SizeWatcher sizeWatcher;

  private final DatabaseDataController databaseDataController;

  private final Map<Integer, BlockDownloadTask> blockDownloadTasks =
      new HashMap<Integer, BlockDownloadTask>();

  private final SpeedNotifier speedNotifier;

  private final Gson gson = new Gson();

  // md5 calc
  private MD5 md5;

  private long lastUpdateProgressTime = 0L;


  private volatile int receivedBytes = 0;

  private final InnerBlockDownloadListener blockListener = new InnerBlockDownloadListener() {

    @Override
    public void onBlockProgressChange(int blockId, long currentBytes, byte[] data, int length) {
      synchronized (info) {
        // pre check condition.
        if (info.getStatus() != DownloadConstants.Status.STATUS_RUNNING) {
          DownloadLog.w("Receiver progress change, but task status is " + info.getStatus());
          return;
        }
        receivedBytes += length;
        if (info.mConfig == null) {
          info.mCurrentBytes = currentBytes;
          if (md5 != null) {
            MD5Util.update(md5, data, 0, length);
          }
        } else {
          // contains multi segments
          info.mConfig.updateBlockInfo(blockId, currentBytes);
          long currents = 0L;
          for (DownloadBlockInfo block : info.mConfig.getBlocks()) {
            currents += block.getCurrentSize();
          }
          info.mCurrentBytes = currents;
        }
        // record self task speed
        speedHelper.record(length);
        // record to global speed manager
        GlobalSpeedManager.getInstance().record(length);
        long now = System.currentTimeMillis();
        // Update progress change.
        if ((lastUpdateProgressTime > 0
            && now - lastUpdateProgressTime > UPDATE_PROGRESS_INTERVAL_MS)
            || info.mCurrentBytes == info.mTotalBytes) {
          ContentValues values = new ContentValues();
          if (info.mConfig == null) {
            if (md5 != null) {
              values.put(DownloadConstants.Database.COLUMNS.COLUMN_MD5_STATE,
                  gson.toJson(info.md5State));
            }
          } else {
            values.put(DownloadConstants.Database.COLUMNS.COLUMN_SEGMENT_CONFIG,
                info.mConfig.toJson());
          }
          values.put(DownloadConstants.Database.COLUMNS.COLUMN_CURRENT_BYTES,
              info.mCurrentBytes);
          // we don't calulate speed here, but we can get the speed from download info,
          // because {@link SpeedNotifier} will put the value to this info,
          // here we need to write the speed value to database.
          values.put(DownloadConstants.Database.COLUMNS.COLUMN_SPEED, info.getSpeed());
          databaseDataController.updateDownloadInfo(info.mId, values);
          lastUpdateProgressTime = now;
        } else if (lastUpdateProgressTime == 0) {
          lastUpdateProgressTime = now;
        }
      }
    }

    @Override
    public void onBlockStatusChange(int blockId, BlockStatus status) {
      synchronized (info) {
        if (info.getStatus() == DownloadConstants.Status.STATUS_CANCELED
            || info.getStatus() == DownloadConstants.Status.STATUS_DELETED) {
          return;
        }
        if (status.getParentStatus() == BlockDownloadTask.BlockParentStatus.FAILED
            && handleDownloadFailedCase(blockId, status)) {
          return;
        }
        BlockDownloadTask blockDownloadTask;
        synchronized (blockDownloadTasks) {
          blockDownloadTask = blockDownloadTasks.get(blockId);
        }
        // Maybe stopped by user click, and happen in another thread
        if (blockDownloadTask == null) {
          return;
        }
        Integer infoStatus = getDownloadInfoStatus(status, blockDownloadTask);
        // Don't update if download info status can not change
        if (infoStatus == null || infoStatus == info.getStatus()) {
          return;
        }
        updateDownloadInfoStatusAndNotify(blockId, infoStatus);
      }
    }

    @Override
    public String onFilePathDeterminated(int blockId, String filePath) {
      synchronized (info) {
        info.mFilePath = filePath;
        ContentValues values = new ContentValues();
        values.put(DownloadConstants.Database.COLUMNS.COLUMN_FILE_PATH, info.mFilePath);
        databaseDataController.updateDownloadInfo(info.mId, values);
        return info.getIntermediateFilePath();
      }
    }

    @Override
    public void onTotalSizeConfirmed(int blockId, long totalSize) {
      synchronized (info) {
        if (info.mTotalBytes == 0 && !isMultiSegDownload()) {
          info.mTotalBytes = totalSize;
          if (sizeWatcher != null) {
            sizeWatcher.onSizeConfirmed(info.clone());
          }
          ContentValues values = new ContentValues();
          values.put(DownloadConstants.Database.COLUMNS.COLUMN_TOTAL_BYTES, info.mTotalBytes);
          if (info.mConfig != null && info.mConfig.getBlocks().size() == 1
              && info.mConfig.getBlocks().get(0).getEndPos() <= 0) {
            // update one-block-download config
            info.mConfig.getBlocks().get(0).setEndPos(totalSize - 1);
            values.put(DownloadConstants.Database.COLUMNS.COLUMN_SEGMENT_CONFIG,
                info.mConfig.toJson());
          }
          databaseDataController.updateDownloadInfo(info.mId, values);
        }
      }
    }

    @Override
    public void onDurationIncreased(int blockId, long duration) {
      synchronized (info) {
        info.mDuration = info.mDuration + duration;
        ContentValues values = new ContentValues();
        values.put(DownloadConstants.Database.COLUMNS.COLUMN_DURATION, info.mDuration);
        databaseDataController.updateDownloadInfo(info.mId, values);
      }
    }

    @Override
    public void onETagChanged(int blockId, String eTag) {
      synchronized (info) {
        info.mETag = eTag;
        ContentValues values = new ContentValues();
        values.put(DownloadConstants.Database.COLUMNS.COLUMN_ETAG, info.mETag);
        databaseDataController.updateDownloadInfo(info.mId, values);
      }
    }
  };

  private final DownloadStrategy downloadStrategy;

  public DownloadTask(Context context, InnerDownloadInfo info, DownloadFinishedListener listener,
      ExecutorService executor, DownloadVerifier downloadVerifier,
      MD5Confirmer md5Confirmer, DatabaseDataController databaseDataController,
      NetworkStatusStub networkStatusStub, SizeWatcher sizeWatcher) {
    // TODO too many arguments in this constructor.
    this.context = context;
    this.info = info;
    this.finishedListener = listener;
    this.executor = executor;
    this.downloadVerifier = downloadVerifier;
    this.md5Confirmer = md5Confirmer;
    this.databaseDataController = databaseDataController;
    this.networkStatusStub = networkStatusStub;
    this.sizeWatcher = sizeWatcher;
    if (needToCalculateMD5()) {
      if (info.md5State == null) {
        info.md5State = new MD5State();
      }
      md5 = new MD5(context, info.md5State);
    } else {
      md5 = null;
    }
    // speed helper
    this.speedHelper = new SpeedHelper();
    // speed notifier
    this.speedNotifier = new SpeedNotifier(info, speedHelper);

    // init download strategy
    switch (info.mType) {
      case VIDEO:
      case MUSIC:
      case APP:
      case COMIC:
      case MISC:
      case PATCH:
      case UNKNOWN:
      case EBOOK:
      case IMAGE:
      case ZIP:
      default:
        downloadStrategy = new CommonDownloadStrategy();
        break;
    }
  }

  private void updateDownloadInfoStatusAndNotify(int blockId, int infoStatus) {
    ContentValues values = new ContentValues();
    switch (infoStatus) {
      case DownloadConstants.Status.STATUS_SUCCESS:
        speedNotifier.stop();
        FileUtil.renameFile(info.getIntermediateFilePath(), info.mFilePath);
        changeDestinationFilePermission(info.mFilePath);
        /* Part of calcing md5 */
        if (md5 != null) {
          info.mMd5 = MD5Util.getHexString(md5);
          values.put(DownloadConstants.Database.COLUMNS.COLUMN_MD5_CHECKSUM, info.mMd5);
        }

        /* Part of basic verify */
        info.setStatus(getVerifyResult(info));
        break;
      case DownloadConstants.Status.STATUS_RUNNING:
        cancelScheduledDownloadTask(blockId);
        speedNotifier.start();
        lastUpdateProgressTime = 0L;
        info.setStatus(infoStatus);
        break;
      case DownloadConstants.Status.STATUS_CREATED:
      case DownloadConstants.Status.STATUS_PENDING:
        info.setStatus(infoStatus);
        break;
      case DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB:
      case DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED:
      default:
        speedNotifier.stop();
        stopAllBlockDownloadTasks();
        info.setStatus(infoStatus);
        break;
    }
    // update data in database and notify upper layer.
    values.put(DownloadConstants.Database.COLUMNS.COLUMN_STATUS, info.getStatus());
    if (info.mConfig != null) {
      values.put(DownloadConstants.Database.COLUMNS.COLUMN_SEGMENT_CONFIG,
          info.mConfig.toJson());
    }
    databaseDataController.updateDownloadInfo(info.mId, values);
    if (info.getStatus() != DownloadConstants.Status.STATUS_CREATED
        && info.getStatus() != DownloadConstants.Status.STATUS_PENDING
        && info.getStatus() != DownloadConstants.Status.STATUS_RUNNING) {
      finishedListener.onDownloadFinished(info.mId);
    }
    DownloadMessageCenter.getInstance().notifyStatusChange(info);
  }

  private boolean continueByUpdateUrl() {
    stopAllBlockDownloadTasks();
    int current = info.mConfig.getBlocks().get(0).getUsedUrlIndex();
    List<DownloadUrlInfo> availableUrls = info.mConfig.getBackupUrls();
    if (current < availableUrls.size() - 1) {
      for (int i = 0; i < info.mConfig.getBlocks().size(); ++i) {
        info.mConfig.getBlocks().get(i).setUsedUrlIndex(current + 1);
      }
      executeBlockDownloads(info.mConfig);
      return true;
    } else {
      return false;
    }
  }


  /**
   * handle failed callback.
   * 
   * @param blockId block index.
   * @param status download status.
   * @return if able to deal with it, return true, else return false.
   */
  private boolean handleDownloadFailedCase(int blockId, BlockStatus status) {
    if (status == BlockStatus.STORAGE_NOT_READY
        || status == BlockStatus.INSUFFICIENT_STORAGE
        || status == BlockStatus.FILE_ERROR
        || status == BlockStatus.FILE_NOT_FOUND) {
      return false;
    }
    return downloadStrategy.handleDownloadFailedCase(blockId, status);
  }


  private static boolean isFatalError(BlockStatus status) {
    return status == BlockStatus.CRC_VERIFY_ERROR
        || status == BlockStatus.TOO_MANY_REDIRECTS
        || status == BlockStatus.EXCEED_MAX_RETRY_TIMES
        || status == BlockStatus.DOWNLOAD_SIZE_UNKNOWN
        || status == BlockStatus.CONNECTION_TIMEOUT
        || status == BlockStatus.HTTP_ERROR
        || status == BlockStatus.URL_NULL_ERROR
        || status == BlockStatus.UNKNOWN_ERROR;
  }

  private int getVerifyResult(InnerDownloadInfo info) {
    // check download valid
    if (!TextUtils.isEmpty(info.mFilePath) && TextUtils.isEmpty(info.mRetriedUrls)) {
      File file = new File(info.mFilePath);
      long length = file.length();
      if (length != info.mTotalBytes) {
        // download file length dis matched
        return DownloadConstants.Status.STATUS_FILE_LENGTH_VERIFY_ERROR;
      }
    }
    if (info.mVerifyType != null && !TextUtils.isEmpty(info.mVerifyValue)
        && TextUtils.isEmpty(info.mRetriedUrls)) {
      switch (info.mVerifyType) {
        case MD5:
          if (info.mMd5 != null && !info.mVerifyValue.equals(info.mMd5)) {
            return DownloadConstants.Status.STATUS_MD5_VERIFY_ERROR;
          }
          break;
        case PF5:
          String pf5value;
          try {
            pf5value = Pf5Util.getPf5String(context, info.mFilePath);
            if (!TextUtils.isEmpty(pf5value)
                && !TextUtils.isEmpty(info.mVerifyValue)
                && !info.mVerifyValue.equals(pf5value)) {
              return DownloadConstants.Status.STATUS_PF5_VERIFY_ERROR;
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        default:
          break;
      }
    }
    // use verifier to check
    if (downloadVerifier != null && !downloadVerifier.isDownloadFileValid(info)) {
      return DownloadConstants.Status.STATUS_OTHER_VERIFY_ERROR;
    }
    return DownloadConstants.Status.STATUS_SUCCESS;
  }

  private boolean hasReachedMaxRetryTimes(int blockId) {
    synchronized (retryTimes) {
      int times = retryTimes.get(blockId, 0);
      retryTimes.put(blockId, ++times);
      return times < MAX_RETRY_TIME;
    }
  }

  private void scheduleContinueDownloadWithSameUrl(int blockId) {
    cancelScheduledDownloadTask(blockId);
    int times = retryTimes.get(blockId, 0);
    Timer timer = new Timer();
    ContinueDownloadTask continueDownloadTask = new ContinueDownloadTask(blockId);
    timer.schedule(continueDownloadTask, fibonacciArray[times - 1] * RETRY_INTERVAL_MS);
    synchronized (scheduledContinueDownloadTimerTask) {
      scheduledContinueDownloadTimerTask.put(blockId, continueDownloadTask);
    }
  }

  private final class ContinueDownloadTask extends TimerTask {

    private final int blockId;

    ContinueDownloadTask(int blockId) {
      this.blockId = blockId;
    }

    @Override
    public void run() {
      synchronized (info) {
        int status = info.getStatus();
        if (status == DownloadConstants.Status.STATUS_CANCELED
            || status == DownloadConstants.Status.STATUS_DELETED
            || status == DownloadConstants.Status.STATUS_PAUSED_BY_APP
            || status == DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB
            || status == DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED) {
          // if status has been canceled or deleted, cannot change status again.
          return;
        }
      }
      continueDownload(blockId);
    }
  }


  private boolean needToCalculateMD5() {
    // Only to check whether need to calc md5 or need verify md5 checksum.
    // when start to download.
    return ((md5Confirmer != null && md5Confirmer.needToCalculateMd5(info))
    || info.mVerifyType == DownloadConstants.VerifyType.MD5);
  }

  /**
   * Start download.
   * 
   * If download task is able to download with multi threads,
   * 
   * <b>Can't call this function in UI thread.</b>
   */
  public void start() {
    if (info.getStatus() != DownloadConstants.Status.STATUS_CREATED) {
      return;
    }
    downloadStrategy.start();
  }

  public long getSpaceNeeded() {
    return info.mTotalBytes - info.mCurrentBytes;
  }

  public String getIdentity() {
    return info.mIdentity;
  }

  public boolean isVisible() {
    return info.mVisible;
  }

  /**
   * Executes download by single thread.
   */
  private boolean executeBlockDownload(InnerBlockDownloadRequest request) {
    synchronized (info) {
      if (info.getStatus() == DownloadConstants.Status.STATUS_CANCELED
          || info.getStatus() == DownloadConstants.Status.STATUS_DELETED) {
        return false;
      }
      BlockDownloadTask blockDownloadTask = new BlockDownloadTask(context, request, blockListener,
          networkStatusStub, executor);
      blockDownloadTasks.put(request.blockId, blockDownloadTask);
      blockDownloadTask.execute();
    }
    return true;
  }

  /**
   * Executes block download of specific block id.
   * 
   * @param configuration block download configuration
   * @param blockId block id
   * @return success or not
   */
  private boolean executeBlockDownload(DownloadConfigInfo configuration, int blockId) {
    if (configuration.getBackupUrls() == null || configuration.getBackupUrls().isEmpty()
        || configuration.getBlocks() == null || configuration.getBlocks().isEmpty()) {
      return false;
    }
    synchronized (info) {
      if (info.getStatus() == DownloadConstants.Status.STATUS_CANCELED
          || info.getStatus() == DownloadConstants.Status.STATUS_DELETED) {
        return false;
      }
      DownloadBlockInfo block = configuration.getBlocks().get(blockId);
      /* query block config info whether block has been success */
      if (!DownloadUtil.isBlockDownloadFinished(block)) {
        DownloadUrlInfo urlInfo = info.mConfig.getBackupUrls().get(block.getUsedUrlIndex());
        long speedLimit =
            info.speedLimit > 0 ? info.speedLimit / configuration.getSegSize() : -1L;
        InnerBlockDownloadRequest request = new InnerBlockDownloadRequest(
            info,
            urlInfo.getUrl(),
            blockId,
            block.getStartPos(),
            block.getEndPos(),
            block.getCurrentSize(),
            configuration.getSegInfos(),
            configuration.getSegSize(),
            speedLimit, configuration.getBlocks().size());
        executeBlockDownload(request);
      }
    }
    return true;
  }

  /**
   * Executes multiple download thread with download configuration.
   * 
   * @param configuration download config info
   */
  private void executeBlockDownloads(DownloadConfigInfo configuration) {
    List<DownloadUrlInfo> urls = configuration.getBackupUrls();
    if (urls == null || urls.isEmpty()) {
      return;
    }
    List<DownloadBlockInfo> blocks = configuration.getBlocks();
    synchronized (info) {
      if (info.getStatus() == DownloadConstants.Status.STATUS_CANCELED
          || info.getStatus() == DownloadConstants.Status.STATUS_DELETED) {
        return;
      }
      /*
       * task start maybe at different threads, it will cause one task's blocks not insert at same
       * time. for example, task A has two blocks and task B also two blocks, they call resume at
       * different thread, then the runnable in executor maybe like: [A.1, B.1, A.2, B.2].
       * but we want this: [A.1, A.2, B.1, B.2], so that we can control running tasks num.
       */
      synchronized (executor) {
        /* query block config info whether block has been success */
        for (DownloadBlockInfo block : blocks) {
          executeBlockDownload(configuration, block.getBlockId());
        }
      }
    }
  }

  /**
   * Resume download task with allowInMobile argument.
   * 
   * <p>
   * this part will update allowInMobile argument to download database.
   * </p>
   * 
   * @param allowInMobile able to download with 2G/3G.
   * @return whether resume success.
   */
  public boolean resume(boolean allowInMobile) {
    synchronized (info) {
      // precondition check.
      if (info.getStatus() != DownloadConstants.Status.STATUS_PAUSED_BY_APP
          && info.getStatus() != DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED
          && info.getStatus() != DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB
          && !(StatusUtil.isStatusRunning(info.getStatus())
          && blockDownloadTasks.isEmpty() /** Launch application and resume running task */
          )) {
        return false;
      }

      // update allowInMobile argument in database and start to resume.
      info.mAllowInMobile = allowInMobile;
      ContentValues contentValues = new ContentValues();
      contentValues.put(DownloadConstants.Database.COLUMNS.COLUMN_ALLOWED_DOWNLOAD_WITHOUT_WIFI,
          allowInMobile);
      boolean success = databaseDataController.updateDownloadInfo(info.mId, contentValues);

      if (!isFileExists(info.getIntermediateFilePath())) {
        resetDownloadTask();
        start();
        return false;
      }

      if (success) {
        return resume();
      }

    }
    return false;
  }

  /**
   * Resumes download task when paused.
   * 
   * @return whether resume success.
   */
  public boolean resume() {
    synchronized (info) {
      // precondition check.
      if (info.getStatus() != DownloadConstants.Status.STATUS_PAUSED_BY_APP
          && info.getStatus() != DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED
          && info.getStatus() != DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB
          && !(StatusUtil.isStatusRunning(info.getStatus())
          && blockDownloadTasks.isEmpty() /** Launch application and resume running task */
          )) {
        return false;
      }

      if (!isFileExists(info.getIntermediateFilePath())) {
        resetDownloadTask();
        start();
        return false;
      }

      if (info.mConfig == null) {
        executeBlockDownload(new InnerBlockDownloadRequest(info));
      } else {
        executeBlockDownloads(info.mConfig);
      }
    }
    return true;
  }

  /**
   * Cancels schedule download task which retries download from break point.
   * 
   * @param blockId
   */
  private void cancelScheduledDownloadTask(int blockId) {
    TimerTask timerTask;
    synchronized (scheduledContinueDownloadTimerTask) {
      timerTask = scheduledContinueDownloadTimerTask.remove(blockId);
    }
    if (timerTask != null) {
      timerTask.cancel();
    }
  }

  private void stopAllBlockDownloadTasks() {
    List<BlockDownloadTask> cloneList = new LinkedList<BlockDownloadTask>();
    synchronized (blockDownloadTasks) {
      cloneList.addAll(blockDownloadTasks.values());
      blockDownloadTasks.clear();
    }
    for (BlockDownloadTask blockDownloadTask : cloneList) {
      blockDownloadTask.stop();
      cancelScheduledDownloadTask(blockDownloadTask.getBlockId());
    }
  }

  /**
   * Cancels download task.
   * 
   * @return whether success
   */
  public boolean cancel() {
    synchronized (info) {
      if (info.getStatus() == DownloadConstants.Status.STATUS_CANCELED
          || info.getStatus() == DownloadConstants.Status.STATUS_DELETED) {
        // if status has been canceled or deleted, can't change status again.
        return false;
      }
      speedNotifier.stop();
      stopAllBlockDownloadTasks();
      // delete downloadInfo immediately.
      // don't wait listener's callback and then delete downloadInfo.
      boolean deletedSuccess = databaseDataController.removeDownloadTask(info.mId);
      if (!deletedSuccess) {
        return false;
      }

      // notify DownloadTaskController that download task has been deleted.
      finishedListener.onDownloadFinished(info.mId);
      if (!StatusUtil.isStatusCompleted(info.getStatus())) {
        // if download thread is not completed, notify upper layer with both canceled and
        // deleted status.
        info.setStatus(DownloadConstants.Status.STATUS_CANCELED);
        DownloadMessageCenter.getInstance().notifyStatusChange(info);
      }
      info.setStatus(DownloadConstants.Status.STATUS_DELETED);
      DownloadMessageCenter.getInstance().notifyStatusChange(info);
    }

    // As deleting file may be time-consuming, move it to another thread to avoid blocking other
    // operations.
    if (!TextUtils.isEmpty(info.getIntermediateFilePath())) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          /* Delete the local file */
          FileUtil.deleteFile(info.getIntermediateFilePath());
        }
      }).start();
    }

    return true;
  }

  /**
   * Stop download thread and set pauseReason.
   * 
   * @param pauseReason why to pause
   * @return whether success
   */
  public boolean pause(PauseReason pauseReason) {
    synchronized (info) {
      if (info.getStatus() != DownloadConstants.Status.STATUS_CREATED
          && info.getStatus() != DownloadConstants.Status.STATUS_PENDING
          && info.getStatus() != DownloadConstants.Status.STATUS_RUNNING) {
        return false;
      }
      speedNotifier.stop();
      stopAllBlockDownloadTasks();
      info.setStatus(pauseReason.getStatus());
      ContentValues contentValues = new ContentValues();
      if (info.mConfig != null) {
        contentValues.put(DownloadConstants.Database.COLUMNS.COLUMN_SEGMENT_CONFIG,
            info.mConfig.toJson());
      }
      contentValues.put(DownloadConstants.Database.COLUMNS.COLUMN_STATUS, pauseReason.getStatus());
      contentValues.put(
          DownloadConstants.Database.COLUMNS.COLUMN_CURRENT_BYTES, info.mCurrentBytes);
      contentValues.put(
          DownloadConstants.Database.COLUMNS.COLUMN_MD5_STATE, new Gson().toJson(info.md5State));
      boolean updateSuccess = databaseDataController.updateDownloadInfo(info.mId, contentValues);
      if (!updateSuccess) {
        return false;
      }
      finishedListener.onDownloadFinished(info.mId);
      DownloadMessageCenter.getInstance().notifyStatusChange(info);
    }
    return true;
  }

  // /**
  // * Continues download with new url from download config info.
  // *
  // * @param blockId
  // * @return success or not
  // */
  // private boolean continueDownloadWithAnotherUrl(int blockId) {
  // if (info.mConfig.useNextUrlInCurrentUrlGroup(blockId) == null) {
  // return false;
  // }
  // ContentValues contentValues = new ContentValues();
  // contentValues.put(DownloadConstants.Database.COLUMNS.COLUMN_SEGMENT_CONFIG,
  // info.mConfig.toJson());
  // databaseDataController.updateDownloadInfo(info.mId, contentValues);
  // return continueDownload(blockId);
  // }

  /**
   * Retry download with another url.
   * 
   * <p>
   * If download task has got data, resume it first.
   * </p>
   * 
   * @param newUrl
   * @return retry successfully or not
   */
  public boolean retry(String newUrl) {
    synchronized (info) {
      if (info.getStatus() == DownloadConstants.Status.STATUS_CANCELED
          || info.getStatus() == DownloadConstants.Status.STATUS_DELETED
          || info.getStatus() == DownloadConstants.Status.STATUS_RUNNING
          || info.getStatus() == DownloadConstants.Status.STATUS_CREATED
          || info.getStatus() == DownloadConstants.Status.STATUS_PENDING) {
        return false;
      }

      if (!TextUtils.isEmpty(newUrl) && !newUrl.equals(info.mUri)) {
        // retry apk-server.
        ContentValues contentValues = new ContentValues();
        // apk from apk-server may have a different size, but it's unknown.
        // so reset total bytes to 0.
        info.mTotalBytes = 0L;
        info.mUri = newUrl;
        if (!TextUtils.isEmpty(info.mRetriedUrls)) {
          if (info.mRetriedUrls.endsWith(info.mUri)) {
            ++info.mLastUrlRetriedTimes;
            return false;
          } else {
            info.mRetriedUrls = TextUtils.join(",", new String[] {info.mRetriedUrls, info.mUri});
            info.mLastUrlRetriedTimes = 1;
          }
        } else {
          info.mRetriedUrls = info.mUri;
          info.mLastUrlRetriedTimes = 1;
        }
        contentValues.put(DownloadConstants.Database.COLUMNS.COLUMN_TOTAL_BYTES, info.mTotalBytes);
        contentValues.put(DownloadConstants.Database.COLUMNS.COLUMN_URI, info.mUri);
        contentValues.put(DownloadConstants.Database.COLUMNS.COLUMN_RETRIED_URLS,
            info.mRetriedUrls);
        contentValues.put(DownloadConstants.Database.COLUMNS.COLUMN_LAST_URL_RETRIED_TIMES,
            info.mLastUrlRetriedTimes);
        databaseDataController.updateDownloadInfo(info.mId, contentValues);

        resetDownloadTask();
      } else {
        // not retry apk server
        if (StatusUtil.isStatusError(info.getStatus())
            && !isFileExists(info.getIntermediateFilePath())) {
          // if intermediate file does not exist, reset all download state.
          resetDownloadTask();
        } else if (StatusUtil.isStatusSuccess(info.getStatus()) && !isFileExists(info.mFilePath)) {
          resetDownloadTask();
        }
      }

      return downloadStrategy.retry(newUrl);
    }
  }

  private static boolean isFileExists(String filePath) {
    return !TextUtils.isEmpty(filePath) && (new File(filePath).exists());
  }

  public void putExtraArgument(long id, HashMap<String, String> values) {
    Gson gson = new Gson();
    HashMap<String, String> extras = gson.fromJson(
        info.mExtras, new TypeToken<HashMap<String, String>>() {}.getType());
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

  /**
   * Continues download of specific block id.
   * 
   * @param blockId
   */
  private boolean continueDownload(int blockId) {
    if (info.mConfig == null) {
      return executeBlockDownload(new InnerBlockDownloadRequest(info));
    } else {
      return executeBlockDownload(info.mConfig, blockId);
    }
  }

  /**
   * Get If this download task is using multi-thread downloading.
   * 
   * @return whether multi-thread.
   */
  private boolean isMultiSegDownload() {
    return info.mConfig != null && (info.mConfig.getBlocks().size() > 1);
  }

  /**
   * Gets corresponding download info status according to block download status, if downlaod info
   * can change to new state.
   * 
   * @param status block download status
   * @param blockDownloadTask block download task instance
   * @return {@link DownloadConstants.Status}, return null if cannot change to new download info
   */
  private Integer getDownloadInfoStatus(BlockStatus status, BlockDownloadTask blockDownloadTask) {
    // Gets the status with highest priority
    BlockStatus otherHighestStatus = BlockStatus.SUCCESS;
    synchronized (blockDownloadTasks) {
      for (Map.Entry<Integer, BlockDownloadTask> entry : blockDownloadTasks.entrySet()) {
        if (entry.getKey() != blockDownloadTask.getBlockId()) {
          if (otherHighestStatus.getPriority() < entry.getValue().getStatus().getPriority()) {
            otherHighestStatus = entry.getValue().getStatus();
          }
        }
      }
    }
    if (status.getPriority() < otherHighestStatus.getPriority()) {
      return null;
    }
    switch (status) {
      case SUCCESS:
        return DownloadConstants.Status.STATUS_SUCCESS;
      case PENDING:
        return DownloadConstants.Status.STATUS_PENDING;
      case RUNNING:
        return DownloadConstants.Status.STATUS_RUNNING;
      case QUEUED_FOR_WIFI_OR_USB:
        return DownloadConstants.Status.STATUS_QUEUED_FOR_WIFI_OR_USB;
      case CRC_VERIFY_ERROR:
        return DownloadConstants.Status.STATUS_CRC_VERIFY_ERROR;
      case TOO_MANY_REDIRECTS:
        return DownloadConstants.Status.STATUS_TOO_MANY_REDIRECTS;
      case RESOLVE_REDIRECT_URL_FAILED:
        return DownloadConstants.Status.STATUS_RESOLVE_REDIRECT_URL_FAILED;
      case EXCEED_MAX_RETRY_TIMES:
        return DownloadConstants.Status.STATUS_REACHED_MAX_RETRIED_TIMES;
      case DOWNLOAD_SIZE_UNKNOWN:
        return DownloadConstants.Status.STATUS_ACTUALLY_SIZE_UNKNOWN;
      case DOWNLOADED_BYTES_OVERFLOW:
        return DownloadConstants.Status.STATUS_DOWNLOADED_BYTES_OVERFLOW;
      case FILE_NOT_FOUND:
        return DownloadConstants.Status.STATUS_FILE_HAS_DELETED;
      case STORAGE_NOT_READY:
        return DownloadConstants.Status.STATUS_DEVICE_NOT_FOUND_ERROR;
      case INSUFFICIENT_STORAGE:
        return DownloadConstants.Status.STATUS_INSUFFICIENT_SPACE_ERROR;
      case NO_WRITE_PERMISSION:
        return DownloadConstants.Status.STATUS_NO_WRITE_PERMISSION;
      case FILE_ERROR:
        return DownloadConstants.Status.STATUS_FILE_ERROR;
      case URL_NULL_ERROR:
        // currently, the reason url is null that parse clip info error.
        return DownloadConstants.Status.STATUS_CLIP_INFO_ERROR;
      case QUEUED_FOR_MEDIA:
        return DownloadConstants.Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED;
      case CONNECTION_TIMEOUT:
        return DownloadConstants.Status.STATUS_CONNECTION_TIMEOUT;
      case HTTP_ERROR:
        if (blockDownloadTask.getHttpStatusCode() >= HttpStatus.SC_BAD_REQUEST
            && blockDownloadTask.getHttpStatusCode() <= HttpStatus.SC_FAILED_DEPENDENCY) {
          // Http status is the same as download status in this range.
          return blockDownloadTask.getHttpStatusCode();
        } else if (blockDownloadTask.getHttpStatusCode() >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
          return DownloadConstants.Status.STATUS_SERVER_ERROR;
        }
      case UNKNOWN_ERROR:
      default:
        return DownloadConstants.Status.STATUS_UNKNOWN_ERROR;
    }
  }

  private static void changeDestinationFilePermission(String fileName) {
    try {
      @SuppressWarnings("rawtypes")
      Class clazz = Class.forName("android.os.FileUtils");
      Method method = clazz.getMethod("setPermissions",
          new Class[] {String.class, int.class, int.class, int.class});
      method.invoke(clazz, new Object[] {fileName, 0644, -1, -1});
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * reset progress, md5state, filepath of downloadtask.
   */
  private boolean resetDownloadTask() {
    // already in synchronized block.
    ContentValues values = new ContentValues();
    info.mCurrentBytes = 0L;
    // folder path maybe null in some case like no sdcard.
    if (!TextUtils.isEmpty(info.mFolderPath)) {
      // temporary solution to previous video tasks.
      info.mFolderPath = info.mFolderPath.replace(".", "_");
    }
    if (!TextUtils.isEmpty(info.mFolderPath) && !info.mFolderPath.endsWith("/")) {
      info.mFolderPath += "/";
      File file = new File(info.mFolderPath);
      if (!file.exists()) {
        file.mkdirs();
      }
    }

    if (!TextUtils.isEmpty(info.mFilePath)) {
      File file = new File(info.mFilePath);
      if (file.exists()) {
        file.delete();
      }
    }

    info.setStatus(DownloadConstants.Status.STATUS_CREATED);
    info.md5State = new MD5State();
    md5 = new MD5(context, info.md5State);
    values.put(DownloadConstants.Database.COLUMNS.COLUMN_CURRENT_BYTES,
        info.mCurrentBytes);
    values.put(
        DownloadConstants.Database.COLUMNS.COLUMN_MD5_STATE, new Gson().toJson(info.md5State));
    values.put(DownloadConstants.Database.COLUMNS.COLUMN_STATUS, info.getStatus());
    values.put(DownloadConstants.Database.COLUMNS.COLUMN_FILE_PATH, info.mFilePath);
    values.put(DownloadConstants.Database.COLUMNS.COLUMN_FOLDER_PATH, info.mFolderPath);
    // reset download current bytes
    if (info.mConfig != null) {
      for (DownloadBlockInfo blockInfo : info.mConfig.getBlocks()) {
        info.mConfig.updateBlockInfo(blockInfo.getBlockId(), 0L);
      }
      values.put(DownloadConstants.Database.COLUMNS.COLUMN_SEGMENT_CONFIG,
          info.mConfig.toJson());
    }
    return databaseDataController.updateDownloadInfo(info.mId, values);
  }

  private interface DownloadStrategy {
    void start();

    boolean retry(String newUrl);

    boolean handleDownloadFailedCase(int blockId, BlockStatus status);
  }

  private class CommonDownloadStrategy implements DownloadStrategy {

    @Override
    public void start() {
      executeBlockDownload(new InnerBlockDownloadRequest(info));
    }

    @Override
    public boolean retry(String newUrl) {
      executeBlockDownload(new InnerBlockDownloadRequest(info));
      return true;
    }

    @Override
    public boolean handleDownloadFailedCase(int blockId, BlockStatus status) {
      if (!isFatalError(status) && !hasReachedMaxRetryTimes(blockId)) {
        scheduleContinueDownloadWithSameUrl(blockId);
        return true;
      }
      return false;
    }
  }

}
