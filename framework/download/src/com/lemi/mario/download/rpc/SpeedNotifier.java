package com.lemi.mario.download.rpc;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.lemi.mario.download.rpc.speed.SpeedHelper;

/**
 * Speed notifier used to notify speed to listeners.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SpeedNotifier {

  private static final int MSG_NOTIFY_SPEED = 1000;

  private static final long NOTIFY_SPEED_DELAY = 150L;
  private static final long NOTIFY_SPEED_FIRST_DELAY = 800L;
  private static final long MIN_UPDATE_SPEED_DURATION = 1500L;

  private static final String THREAD_NAME = "SPEED_NOTIFIER";

  private Handler handler;
  private long lastSpeed;
  private long lastUpdateSpeed;
  private final InnerDownloadInfo target;
  private final SpeedHelper speedHelper;

  public SpeedNotifier(InnerDownloadInfo target, SpeedHelper speedHelper) {
    this.target = target;
    this.speedHelper = speedHelper;
  }

  public synchronized void start() {
    if (handler == null) {
      HandlerThread thread = new HandlerThread(THREAD_NAME);
      thread.start();
      this.handler = new NotifyHandler(thread.getLooper());
    }
    handler.sendEmptyMessageDelayed(MSG_NOTIFY_SPEED, NOTIFY_SPEED_FIRST_DELAY);
  }

  public synchronized void stop() {
    if (handler == null) {
      return;
    }
    handler.getLooper().quit();
    handler = null;
  }

  private void notifySpeed() {
    if (target.getStatus() != DownloadConstants.Status.STATUS_RUNNING) {
      return;
    }
    long now = System.currentTimeMillis();
    if (now - lastUpdateSpeed > MIN_UPDATE_SPEED_DURATION) {
      lastSpeed = speedHelper.getSpeed();
      lastUpdateSpeed = now;
    }
    target.setSpeed(lastSpeed);
    DownloadMessageCenter.getInstance().notifyProgressChanged(target);
  }

  private class NotifyHandler extends Handler {

    private NotifyHandler(Looper looper) {
      super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
      if (msg.what != MSG_NOTIFY_SPEED) {
        return;
      }
      SpeedNotifier.this.notifySpeed();
      sendEmptyMessageDelayed(MSG_NOTIFY_SPEED, NOTIFY_SPEED_DELAY);
    }
  }
}
