package com.lemi.mario.externalmanager.manager;

import android.content.Context;
import android.content.Intent;

import com.lemi.mario.base.utils.MainThreadPostUtils;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class MountMessageCenter {

  private static MountMessageCenter instance;

  private final Set<WeakReference<MountMessageListener>> listeners = new HashSet<>();

  private MountMessageCenter() {}

  public static synchronized MountMessageCenter getInstance() {
    if (instance == null) {
      instance = new MountMessageCenter();
    }
    return instance;
  }

  public void setMountMessageListener(MountMessageListener mountMessageListener) {
    if (mountMessageListener != null) {
      synchronized (listeners) {
        listeners.add(new WeakReference<>(mountMessageListener));
      }
    }
  }

  public void handleMountMessage(final Context context, final Intent intent) {
    invokeAndNotifyCopy(new ListenerRunnable() {
      @Override
      public void run(MountMessageListener listener) {
        listener.onMessage(context, intent);
      }
    });
    ExternalStorageManager.getInstance().receiveMountMessage(context, intent);
  }

  private void invokeAndNotifyCopy(final ListenerRunnable runnable) {
    synchronized (listeners) {
      Iterator<WeakReference<MountMessageListener>> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        WeakReference<MountMessageListener> reference = iterator.next();
        final MountMessageListener listener = reference.get();
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

  public interface MountMessageListener {
    void onMessage(Context context, Intent intent);
  }

  private interface ListenerRunnable {
    void run(MountMessageListener listener);
  }
}
