package com.lemi.mario.download.rpc;

import android.util.Log;

import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.download.listener.InnerDownloadListener;
import com.lemi.mario.download.rpc.DownloadConstants.Status;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is used to hold download listeners and notify observers.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadMessageCenter {

  // the download progress or status change.
  private InnerDownloadListener downloadListener;

  private static DownloadMessageCenter instance = new DownloadMessageCenter();

  private DownloadMessageCenter() {}

  // Download id to executor, in order to serially execute callback belonging to the same download
  private final Map<Long, NotifyExecutor> messageNotifier =
      new HashMap<Long, NotifyExecutor>();

  static DownloadMessageCenter getInstance() {
    return instance;
  }

  void setDownloadListener(InnerDownloadListener listener) {
    downloadListener = listener;
  }

  void notifyStatusChange(InnerDownloadInfo downloadInfo) {
    if (downloadListener != null) {
      final InnerDownloadInfo snapShot = downloadInfo.clone();
      notify(downloadInfo.mId, new Runnable() {

        @Override
        public void run() {
          Log.i("May", "[notify] " + snapShot.getStatus() + " " + snapShot.mTitle
              + " taskId-> " + snapShot.mId + " ");
          downloadListener.onStatusChanged(snapShot);
        }
      });
    }
  }

  void notifyMimetypeAchieved(final long id, final String mimeType) {
    if (downloadListener != null) {
      notify(id, new Runnable() {

        @Override
        public void run() {
          downloadListener.onMimeTypeAchieved(id, mimeType);
        }
      });
    }
  }

  void notifyProgressChanged(InnerDownloadInfo downloadInfo) {
    if (downloadListener != null && downloadInfo.getStatus() == Status.STATUS_RUNNING) {
      final InnerDownloadInfo snapShot = downloadInfo.clone();
      notify(downloadInfo.mId, new Runnable() {

        @Override
        public void run() {
          // Log.d("May", "downloadInfo Progress Change " + downloadInfo.mStatus + " "
          // + downloadInfo.mTitle);
          downloadListener.onProgressChanged(snapShot);
        }
      });
    }
  }

  private void notify(final long id, final Runnable runnable) {
    synchronized (messageNotifier) {
      NotifyExecutor notifyExecutor = messageNotifier.get(id);
      if (notifyExecutor != null) {
        notifyExecutor.execute(runnable);
      } else {
        NotifyExecutor recycledNotifyExecutor = null;
        Iterator<Map.Entry<Long, NotifyExecutor>> iter = messageNotifier.entrySet().iterator();
        while (iter.hasNext()) {
          Map.Entry<Long, NotifyExecutor> entry = iter.next();
          if (entry.getValue().isTimeout()) {
            iter.remove();
            // Re-used old executors
            if (recycledNotifyExecutor == null) {
              recycledNotifyExecutor = entry.getValue();
            }
          }
        }
        if (recycledNotifyExecutor == null) {
          recycledNotifyExecutor = new NotifyExecutor();
        }
        messageNotifier.put(id, recycledNotifyExecutor);
        recycledNotifyExecutor.execute(runnable);
      }
    }
  }

  private static final class NotifyExecutor {
    private static final long TIMEOUT_INTERVAL_MS = 10 * 1000L;
    private CachedThreadPoolExecutorWithCapacity executor;
    private AtomicInteger pendingNum = new AtomicInteger();
    private long lastExecuteTime;

    NotifyExecutor() {
      this.executor = new CachedThreadPoolExecutorWithCapacity(1, TIMEOUT_INTERVAL_MS);
    }

    void execute(final Runnable runnable) {
      pendingNum.incrementAndGet();
      executor.execute(new Runnable() {

        @Override
        public void run() {
          runnable.run();
          pendingNum.decrementAndGet();
          lastExecuteTime = System.currentTimeMillis();
        }
      });
    }

    boolean isTimeout() {
      return pendingNum.get() <= 0
          && System.currentTimeMillis() - lastExecuteTime > TIMEOUT_INTERVAL_MS;
    }
  }

}
