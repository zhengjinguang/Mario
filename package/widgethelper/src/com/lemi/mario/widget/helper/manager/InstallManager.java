package com.lemi.mario.widget.helper.manager;

import android.text.TextUtils;

import com.lemi.mario.appmanager.AppManager;
import com.lemi.mario.appmanager.LocalAppChangedListener;
import com.lemi.mario.appmanager.error.Error;
import com.lemi.mario.appmanager.model.AppInfo;
import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.externalmanager.config.TVConfig;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class InstallManager {

  private final String TAG = this.getClass().getName();


  private final Set<WeakReference<InstallListener>> listeners =
      new HashSet<WeakReference<InstallListener>>();
  private final Map<String, InstallStatus> statusMap = new HashMap<String, InstallStatus>();

  private static final int INSTALL_THREAD_NUM = 1;
  private static final long INSTALL_THREAD_CACHE_TIME_MS = 30 * 1000L;

  private CachedThreadPoolExecutorWithCapacity installThreadPool;
  private AppStatusChangeListener appStatusChangeListener;

  private static InstallManager instance;

  public static synchronized InstallManager getInstance() {
    if (instance == null) {
      instance = new InstallManager();
    }
    return instance;
  }

  private InstallManager() {

    installThreadPool =
        new CachedThreadPoolExecutorWithCapacity(INSTALL_THREAD_NUM, INSTALL_THREAD_CACHE_TIME_MS);
    appStatusChangeListener = new AppStatusChangeListener();
    AppManager.getInstance().registerLocalAppChangedListener(
        appStatusChangeListener);

  }


  /**
   * Listen used to observe install status change.
   */
  public interface InstallListener {
    /**
     * notify that install task with given package name that status has changed.
     *
     * @param packageName install package name ,used to differentiate install task.
     * @param status status of install task.
     */
    void onInstallStatusChange(String packageName, InstallStatus status);


  }


  public enum InstallStatus {
    CREATE,
    SILENCE_INSTALLING,
    NORMAL_INSTALLING,
    FAILED,
    SUCCESS
  }


  private interface ListenerRunnable {
    void run(InstallListener listener);
  }

  public void asyncInstallPackage(final String packageName, final String apkPath) {
    if (TextUtils.isEmpty(apkPath)) {
      return;
    }
    updateAndNotifyStatus(packageName, InstallStatus.CREATE);
    installThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        install(packageName, apkPath);
      }
    });

  }

  /**
   * Add listener to observe status change.
   *
   * @param listener ziplistener
   */
  public void addListener(InstallListener listener) {
    synchronized (listeners) {
      listeners.add(new WeakReference<>(listener));
    }
  }


  /**
   * get install status right now
   * 
   * @param packageName
   * @return
   */
  public InstallStatus getStatus(String packageName) {
    synchronized (statusMap) {
      return statusMap.get(packageName);
    }
  }

  public boolean isInstalled(String packageName) {
    return AppManager.getInstance().syncIsAppInstalled(packageName);
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
        String packageName = iterator.next();
        InstallStatus installStatus = statusMap.get(packageName);
        if (installStatus == InstallStatus.CREATE
            || installStatus == InstallStatus.NORMAL_INSTALLING
            || installStatus == InstallStatus.SILENCE_INSTALLING) {
          unFinishedList.add(packageName);
        }
      }
    }
    return unFinishedList;
  }

  private void invokeAndNotify(final ListenerRunnable runnable) {
    synchronized (listeners) {
      Iterator<WeakReference<InstallListener>> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        WeakReference<InstallListener> reference = iterator.next();
        final InstallListener listener = reference.get();
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

  private void updateAndNotifyStatus(final String packageName, final InstallStatus status) {
    synchronized (statusMap) {
      statusMap.put(packageName, status);
    }
    invokeAndNotify(new ListenerRunnable() {
      @Override
      public void run(InstallListener listener) {
        listener.onInstallStatusChange(packageName, status);
      }
    });
  }

  private void install(String packageName, String apkPath) {
    if (TVConfig.isIsLetv()) {
      silenceInstall(apkPath);
    } else {
      normalInstall(apkPath);
    }
  }

  private void silenceInstall(String apkPath) {
    AppManager.getInstance().silenceInstallApk(apkPath);
  }


  private void normalInstall(String apkPath) {
    AppManager.getInstance().installApk(apkPath);
  }

  private class AppStatusChangeListener implements LocalAppChangedListener {

    @Override
    public void onInstalled(String packageName, AppInfo appInfo) {
      updateAndNotifyStatus(packageName, InstallStatus.SUCCESS);
    }

    @Override
    public void onUninstalled(String packageName) {
      synchronized (statusMap) {
        statusMap.remove(packageName);
      }
    }

    @Override
    public void onInstallFailed(String packageName, Error error) {
      updateAndNotifyStatus(packageName, InstallStatus.FAILED);
    }

    @Override
    public void onUninstallFailed(String packageName, Error error) {}

    @Override
    public void onStartNonRootInstall(String packageName) {
      updateAndNotifyStatus(packageName, InstallStatus.NORMAL_INSTALLING);
    }

    @Override
    public void onStartSilenceInstall(String packageName) {
      updateAndNotifyStatus(packageName, InstallStatus.SILENCE_INSTALLING);
    }

    @Override
    public void onStartUninstall(String packageName) {}
  }



}
