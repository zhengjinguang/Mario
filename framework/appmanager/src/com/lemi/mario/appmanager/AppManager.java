package com.lemi.mario.appmanager;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;

import com.lemi.mario.appmanager.error.Error;
import com.lemi.mario.appmanager.filter.AppInfoFilter;
import com.lemi.mario.appmanager.intent.IntentAction;
import com.lemi.mario.appmanager.intent.IntentExtras;
import com.lemi.mario.appmanager.model.AppInfo;
import com.lemi.mario.appmanager.utils.AppQueryUtils;
import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.MainThreadPostUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by zhou on 14-12-1.
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class AppManager {
  public final static String PACKAGENAME_UNKNOWN = "package_name_unknown";

  private Context appContext;

  private AppOperateReceiver receiver;

  private AppInstaller appInstaller;

  private final Set<WeakReference<LocalAppChangedListener>> listeners =
      new HashSet<WeakReference<LocalAppChangedListener>>();

  private final long THREAD_ALIVE_TIME = 30000l;

  private final int MAX_THREAD_NUM = 5;

  private static AppManager instance;

  private Future<Map<String, AppInfo>> cacheFuture;

  private final byte[] appsLock = new byte[0];

  private Map<String, AppInfo> allApp = new HashMap<String, AppInfo>();

  private CachedThreadPoolExecutorWithCapacity threadPool;

  private static final int PACKAGE_NAME_INDEX = 8;

  private IntentService intentService = new IntentService("checkAndFillCacheService") {
    @Override
    protected void onHandleIntent(Intent intent) {
      checkAndFillCache();
    }
  };

  public enum OperateType {
    APP_INSTALL,
    APP_SILENCE_INSTALL,
    APP_ROOT_INSTALL,
    APP_UNINSTALL,
    APP_ROOT_UNINSTALL,
    APP_UPDATE
  }

  public enum AppOperateStatus {
    CREATE,
    PROCESSING,
    FAILED,
    SUCCESS
  }

  public interface ResultCallback<T> {
    void onResultCallback(T result);
  }

  private AppManager() {
    appContext = GlobalConfig.getAppContext();
    threadPool = new CachedThreadPoolExecutorWithCapacity(MAX_THREAD_NUM, THREAD_ALIVE_TIME);
    appInstaller = AppInstaller.getInstance(appContext);

    initReceiver();
    initAllAppData();
  }

  private AppInstaller.InstallerCallback installerCallback = new AppInstaller.InstallerCallback() {
    @Override
    public void onInstallFailed(String packageName, Error error) {
      invokeNotifyStatus(new NotifyStatusRunnable(packageName, OperateType.APP_INSTALL,
          AppOperateStatus.FAILED, error));
    }

    @Override
    public void onUninstallFailed(String packageName, Error error) {
      invokeNotifyStatus(new NotifyStatusRunnable(packageName, OperateType.APP_UNINSTALL,
          AppOperateStatus.FAILED, error));
    }

    @Override
    public void onStartNonRootInstall(String packageName) {
      invokeNotifyStatus(new NotifyStatusRunnable(packageName, OperateType.APP_INSTALL,
          AppOperateStatus.PROCESSING));

    }

    @Override
    public void onStartSilenceInstall(String packageName) {
      invokeNotifyStatus(new NotifyStatusRunnable(packageName, OperateType.APP_SILENCE_INSTALL,
          AppOperateStatus.PROCESSING));

    }

    @Override
    public void onStartUninstall(String packageName) {
      invokeNotifyStatus(new NotifyStatusRunnable(packageName, OperateType.APP_UNINSTALL,
          AppOperateStatus.PROCESSING));
    }
  };

  /**
   * register listener to listen local app change
   *
   * @param listener to register
   */
  public void registerLocalAppChangedListener(LocalAppChangedListener listener) {
    synchronized (listeners) {
      listeners.add(new WeakReference<LocalAppChangedListener>(listener));
    }
  }

  /**
   * get AppManger singleton Instance
   *
   * @return AppManager instance
   */
  public static synchronized AppManager getInstance() {
    if (instance == null) {
      instance = new AppManager();
    }
    return instance;
  }

  /**
   * query all installed app
   *
   * notice:
   * it's time-consuming if appManager be initialized just now and cache had not yet be build
   *
   * @return List<AppInfo>
   */
  public List<AppInfo> syncGetAllInstalledApp(final Set<AppInfoFilter.Filter> filters) {
    checkAndFillCache();
    return getCacheAppInfoList(filters);
  }

  /**
   * query all installed app
   *
   * @param callback: callback to get result
   */
  public void getAllInstalledApp(final ResultCallback<List<AppInfo>> callback,
      final Set<AppInfoFilter.Filter> filters) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        invokeResultCallback(new ResultCallbackRunnable<List<AppInfo>>(callback,
            syncGetAllInstalledApp(filters)));
      }
    });
  }

  /**
   * query all installed app,return Future<Map<String,AppInfo>>
   *
   * @return Future<Map<String,AppInfo>>
   */
  public Future<List<AppInfo>> getAllInstalledApp(final Set<AppInfoFilter.Filter> filters) {
    return threadPool.submit(new Callable<List<AppInfo>>() {
      @Override
      public List<AppInfo> call() throws Exception {
        return syncGetAllInstalledApp(filters);
      }
    });
  }

  /**
   * query all running app,return List<AppInfo>
   *
   * notice:
   * it's maybe time-consuming because it's get from system
   *
   * @return List<AppInfo>
   */
  public List<AppInfo> syncGetAllRunningApp(final Set<AppInfoFilter.Filter> filters) {
    Map<String, AppInfo> runningAppMap = new HashMap<>();
    runningAppMap = AppQueryUtils.getAllRunningApp(appContext, filters);
    return getAppInfoList(runningAppMap, filters);
  }

  /**
   * query all running app, return List<AppInfo> in callback
   *
   * @param callback: callback to get result
   */
  public void getAllRunningApp(final ResultCallback<List<AppInfo>> callback,
      final Set<AppInfoFilter.Filter> filters) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        invokeResultCallback(new ResultCallbackRunnable<List<AppInfo>>(callback,
            syncGetAllRunningApp(filters)));
      }
    });
  }

  /**
   * query all running app,return Future<Map<String,AppInfo>>
   *
   * @return Future<Map<String,AppInfo>>
   */
  public Future<List<AppInfo>> getAllRunningApp(final Set<AppInfoFilter.Filter> filters) {
    return threadPool.submit(new Callable<List<AppInfo>>() {
      @Override
      public List<AppInfo> call() throws Exception {
        return syncGetAllRunningApp(filters);
      }
    });
  }

  /**
   * query whether the app be installed
   *
   * notice:
   * it's time-consuming if appManager be initialized just now and cache had not yet be build
   * if packageName is null, result is false
   *
   * @param packageName: the packageName of app
   * @return boolean: true is installed, else not installed
   */
  public boolean syncIsAppInstalled(final String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return false;
    }
    checkAndFillCache();
    boolean isAppInstalled;
    synchronized (appsLock) {
      if (allApp == null || allApp.size() <= 0) {
        isAppInstalled = false;
      } else {
        isAppInstalled = allApp.containsKey(packageName);
      }
    }
    return isAppInstalled;
  }

  /**
   * query whether the app be installed
   *
   * notice:
   * if packageName is null, result is false
   *
   * @param packageName: the packageName of app
   * @param callback: callback to get result
   */
  public void isAppInstalled(final String packageName, final ResultCallback<Boolean> callback) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        invokeResultCallback(new ResultCallbackRunnable<Boolean>(callback, syncIsAppInstalled(packageName)));
      }
    });
  }

  /**
   * query whether the app be installed
   *
   * notice:
   * if packageName is null, result is false
   *
   * @return Future<Boolean>: true is installed, else no installed
   * @param packageName: the packageName of app
   */
  public Future<Boolean> isAppInstalled(final String packageName) {
    return threadPool.submit(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return syncIsAppInstalled(packageName);
      }
    });
  }

  /**
   * check whether the app is running
   *
   * notice:
   * it's maybe time-consuming because it's get from system
   * if packageName is null, result is false
   *
   * @param packageName: the packageName of app
   * @return boolean: true is running, else not running
   */
  public boolean syncIsAppRunning(final String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return false;
    }
    return AppQueryUtils.isAppRunning(packageName, appContext);
  }

  /**
   * check whether the app is running
   *
   * notice:
   * if packageName is null, result is false
   *
   * @param callback: callback to get result
   * @param packageName: the packageName of app
   */
  public void isAppRunning(final String packageName, final ResultCallback<Boolean> callback) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        invokeResultCallback(new ResultCallbackRunnable<Boolean>(callback, syncIsAppRunning(packageName)));
      }
    });
  }

  /**
   * check whether the app is running
   *
   * notice:
   * if packageName is null, result is false
   *
   * @return Future<Boolean>: true is running, else no running
   * @param packageName: the packageName of app
   */
  public Future<Boolean> isAppRunning(final String packageName) {
    return threadPool.submit(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return syncIsAppRunning(packageName);
      }
    });
  }

  /**
   * parse app info from apk
   *
   * notice:
   * result maybe null if apk path or apk file is incorrect
   * it's maybe time-consuming because it's get from system
   *
   * @param apkPath : the file path of apk
   * @return AppInfo
   */
  public AppInfo syncGetAppByApkFile(final String apkPath, final Set<AppInfoFilter.Filter> filters) {
    if (TextUtils.isEmpty(apkPath)) {
      return null;
    }
    return AppQueryUtils.getAppByApkFile(apkPath, appContext, filters);
  }

  /**
   * parse app info from apk
   *
   * notice:
   * result maybe null if apk path or apk file is incorrect
   *
   * @param callback: callback to get result
   * @param apkPath : the file path of apk
   */
  public void getAppByApkFile(final String apkPath, final ResultCallback<AppInfo> callback,
      final Set<AppInfoFilter.Filter> filters) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        invokeResultCallback(new ResultCallbackRunnable<AppInfo>(callback, syncGetAppByApkFile(apkPath, filters)));
      }
    });
  }

  /**
   * parse app info from apk
   *
   * notice:
   * result maybe null if apk path or apk file is incorrect
   *
   * @return Future<AppInfo>: the Future<AppInfo> of app, can be null if parse failed
   * @param apkPath : the file path of apk
   */
  public Future<AppInfo> getAppByApkFile(final String apkPath,
      final Set<AppInfoFilter.Filter> filters) {
    return threadPool.submit(new Callable<AppInfo>() {
      @Override
      public AppInfo call() throws Exception {
        return syncGetAppByApkFile(apkPath, filters);
      }
    });
  }

  /**
   * parse packageName from apk
   *
   * notice:
   * result maybe null if apk path or apk file is incorrect
   * it's maybe time-consuming because it's get from system
   *
   * @param apkPath: the file path of apk
   * @return String: packageName
   */
  public String syncGetPackageNameByApkFile(final String apkPath) {
    if (TextUtils.isEmpty(apkPath)) {
      return null;
    }
    return AppQueryUtils.getPackageNameByApkFile(apkPath, appContext);
  }

  /**
   * parse packageName from apk
   *
   * notice:
   * result maybe null if apk path or apk file is incorrect
   *
   * @param callback: callback to get result
   * @param apkPath : the file path of apk
   */
  public void getPackageNameByApkFile(final String apkPath, final ResultCallback<String> callback) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        invokeResultCallback(new ResultCallbackRunnable<String>(callback, syncGetPackageNameByApkFile(apkPath)));
      }
    });
  }

  /**
   * parse packageName from apk
   *
   * @return Future<AppInfo>: the AppInfo of app, can be null if parse failed
   * @param apkPath: the file path of apk
   */
  public Future<String> getPackageNameByApkFile(final String apkPath) {
    return threadPool.submit(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return syncGetPackageNameByApkFile(apkPath);
      }
    });
  }

  /**
   * parse app info from packageName
   *
   * notice:
   * it's time-consuming if appManager be initialized just now and cache had not yet be build
   * if app not installed or packageName is null, result is null
   *
   * @param packageName : the packageName of app
   * @return AppInfo
   */
  public AppInfo syncGetAppByPackageName(final String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return null;
    }
    checkAndFillCache();
    AppInfo appInfo;
    synchronized (appsLock) {
      if (allApp.size() <= 0 || !allApp.containsKey(packageName)) {
        appInfo = null;
      } else {
        appInfo = allApp.get(packageName);
      }
    }
    return appInfo;
  }

  /**
   * parse app info from packageName
   *
   * notice:
   * if app not installed or packageName is null, result is null
   *
   * @param callback: callback to get result
   * @param packageName : the packageName of app
   */
  public void getAppByPackageName(final String packageName, final ResultCallback<AppInfo> callback) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        invokeResultCallback(new ResultCallbackRunnable<AppInfo>(callback, syncGetAppByPackageName(packageName)));
      }
    });
  }

  /**
   * parse app info from packageName
   *
   * notice:
   * if app not installed or packageName is null, result is null
   *
   * @return Future<AppInfo>: the AppInfo of app, can be null if app is not installed
   * @param packageName: : the packageName of app
   */
  public Future<AppInfo> getAppByPackageName(final String packageName) {
    return threadPool.submit(new Callable<AppInfo>() {
      @Override
      public AppInfo call() throws Exception {
        return syncGetAppByPackageName(packageName);
      }
    });
  }

  /**
   * get appInfo of foreground activity
   *
   * it's maybe time-consuming because it's get from system
   *
   * @return AppInfo
   */
  public AppInfo syncGetForegroundApp(
      final Set<AppInfoFilter.Filter> filters) {
    return AppQueryUtils.getForegroundAppInfo(appContext, filters);
  }

  /**
   * get appInfo of foreground activity
   *
   * @param callback: callback to get result
   */
  public void getForegroundApp(final ResultCallback<AppInfo> callback,
      final Set<AppInfoFilter.Filter> filters) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        invokeResultCallback(new ResultCallbackRunnable<AppInfo>(callback, syncGetForegroundApp(filters)));
      }
    });
  }

  /**
   * get appInfo of foreground activity
   *
   * @return Future<AppInfo>: the AppInfo of foreground app, can be null if error
   */
  public Future<AppInfo> getForegroundApp(final Set<AppInfoFilter.Filter> filters) {
    return threadPool.submit(new Callable<AppInfo>() {
      @Override
      public AppInfo call() throws Exception {
        return syncGetForegroundApp(filters);
      }
    });
  }

  /**
   * install app by apk, use android standard interface
   *
   * can listen install status by registerLocalAppChangedListener()
   *
   * @param apkPath: the file path of apk
   */
  public void installApk(String apkPath) {
    appInstaller.installApk(apkPath, threadPool, installerCallback);
  }

  /**
   * silence install app by apk, only valid in LETV system
   *
   * can listen install status by registerLocalAppChangedListener()
   *
   * @param apkPath: the file path of apk
   */
  public void silenceInstallApk(String apkPath) {
    appInstaller.silenceInstallApk(apkPath, threadPool, installerCallback);
  }


  /**
   * uninstall app by apk, use android standard interface
   *
   * can listen uninstall status by registerLocalAppChangedListener()
   *
   * @param packageName: the packageName of app
   */
  public void uninstallApp(String packageName) {
    appInstaller.uninstallApp(packageName, threadPool, installerCallback);
  }

  private void initReceiver() {
    receiver = new AppOperateReceiver();

    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
    filter.addAction(Intent.ACTION_PACKAGE_ADDED);
    filter.addDataScheme("package");
    appContext.registerReceiver(receiver, filter);

    IntentFilter installFilter = new IntentFilter();
    installFilter.addAction(IntentAction.ACTION_SILENCE_INSTALL_FAIL);
    appContext.registerReceiver(receiver, installFilter);
  }

  private void initAllAppData() {
    cacheFuture = threadPool.submit(new InitCacheCallable());
  }

  private void invokeResultCallback(ResultCallbackRunnable runnable) {
    MainThreadPostUtils.post(runnable);
  }

  private void invokeNotifyStatus(NotifyStatusRunnable runnable) {
    MainThreadPostUtils.post(runnable);
  }

  private void notifyStatus(String packageName, OperateType type, AppOperateStatus status,
      Error error) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new IllegalStateException("can't call listener out of UI thread");
    }
    if (TextUtils.isEmpty(packageName)) {
      return;
    }
    synchronized (listeners) {
      if (!CollectionUtils.isEmpty(listeners)) {
        for (WeakReference<LocalAppChangedListener> listener : listeners) {
          LocalAppChangedListener localAppChangedListener = listener.get();
          if (localAppChangedListener == null) {
            continue;
          }
          switch (type) {
            case APP_INSTALL:
              switch (status) {
                case CREATE:
                  // localAppChangedListener.onStartNonRootInstall(packageName);
                  // break;
                case PROCESSING:
                  localAppChangedListener.onStartNonRootInstall(packageName);
                  break;
                case SUCCESS:
                  checkAndFillCache();
                  AppInfo appInfo;
                  synchronized (appsLock) {
                    appInfo = allApp.get(packageName);
                  }
                  localAppChangedListener.onInstalled(packageName, appInfo);
                  break;
                case FAILED:
                  localAppChangedListener.onInstallFailed(packageName, error);
                  break;
              }
              break;
            case APP_SILENCE_INSTALL:
              switch (status) {
                case CREATE:
                  // localAppChangedListener.onStartSilenceInstall(packageName);
                  // break;
                case PROCESSING:
                  localAppChangedListener.onStartSilenceInstall(packageName);
                  break;
                case SUCCESS:
                  checkAndFillCache();
                  AppInfo appInfo;
                  synchronized (appsLock) {
                    appInfo = allApp.get(packageName);
                  }
                  localAppChangedListener.onInstalled(packageName, appInfo);
                  break;
                case FAILED:
                  localAppChangedListener.onInstallFailed(packageName, error);
                  break;
              }
              break;
            case APP_UNINSTALL:
              switch (status) {
                case CREATE:
                  // localAppChangedListener.onWaitingUninstall(packageName);
                  // break;
                case PROCESSING:
                  localAppChangedListener.onStartUninstall(packageName);
                  break;
                case SUCCESS:
                  localAppChangedListener.onUninstalled(packageName);
                  break;
                case FAILED:
                  localAppChangedListener.onUninstallFailed(packageName, error);
                  break;
              }
              break;
          }
        }
      }
    }
  }

  private void addApp(String packageName) {
    AppInfo appInfo =
        AppQueryUtils.getAppByPackageName(packageName, appContext, AppInfoFilter.RECOMMEND);
    if (appInfo != null) {
      synchronized (appsLock) {
        if (allApp != null) {
          allApp.put(packageName, appInfo);
        }
      }
    }
  }

  private void deleteApp(String packageName) {
    synchronized (appsLock) {
      if (allApp != null && allApp.containsKey(packageName)) {
        allApp.remove(packageName);
      }
    }
  }

  private void checkAndFillCache() {
    synchronized (appsLock) {
      if (allApp != null && allApp.size() > 0) {
        return;
      }
      if (cacheFuture == null) {
        cacheFuture = threadPool.submit(new InitCacheCallable());
      }
      try {
        allApp = cacheFuture.get();
      } catch (Exception e) {
        allApp = AppQueryUtils.getAllInstalledApp(appContext, AppInfoFilter.RECOMMEND);
      }
    }
  }

  private void checkAndFillCacheInReceiver() {
    synchronized (appsLock) {
      if (allApp != null && allApp.size() > 0) {
        return;
      }
    }
    Intent intent = new Intent(appContext, intentService.getClass());
    appContext.startService(intent);
  }

  private List<AppInfo> getAppInfoList(Map<String, AppInfo> appMap,
      Set<AppInfoFilter.Filter> filters) {
    if (appMap == null || appMap.size() <= 0) {
      return null;
    }
    Set<String> packageNameSet = appMap.keySet();
    List<AppInfo> allAppInfo = new ArrayList<>();
    for (String packageName : packageNameSet) {
      AppInfo appInfo = appMap.get(packageName);
      if (appInfo == null) {
        continue;
      }
      allAppInfo.add(getAppInfo(appInfo, filters));
    }
    return allAppInfo;
  }

  private List<AppInfo> getCacheAppInfoList(Set<AppInfoFilter.Filter> filters) {
    List<AppInfo> appInfos;
    synchronized (appsLock) {
      appInfos = getAppInfoList(allApp, filters);
    }
    return appInfos;
  }

  private AppInfo getAppInfo(AppInfo data, Set<AppInfoFilter.Filter> filters) {
    if (data == null) {
      return null;
    }

    AppInfo appInfo = new AppInfo();

    if (filters.contains(AppInfoFilter.Filter.ICON)) {
      if (data.getIcon() == null && !TextUtils.isEmpty(data.getPackageName())) {
        data.setIcon(AppQueryUtils.getAppIcon(data.getPackageName(), appContext));
      }
      appInfo.setIcon(data.getIcon());
    }
    if (filters.contains(AppInfoFilter.Filter.APP_NAME)) {
      appInfo.setAppName(data.getAppName());
    }
    if (filters.contains(AppInfoFilter.Filter.PACKAGE_NAME)) {
      appInfo.setPackageName(data.getPackageName());
    }
    if (filters.contains(AppInfoFilter.Filter.VERSION_NAME)) {
      appInfo.setVersionName(data.getVersionName());
    }
    if (filters.contains(AppInfoFilter.Filter.VERSION_CODE)) {
      appInfo.setVersionCode(data.getVersionCode());
    }
    if (filters.contains(AppInfoFilter.Filter.PERMISSIONS)) {
      appInfo.setPermissions(data.getPermissions());
    }
    if (filters.contains(AppInfoFilter.Filter.SIGNATURES)) {
      appInfo.setSignatures(data.getSignatures());
    }
    if (filters.contains(AppInfoFilter.Filter.SOURCE_DIR)) {
      appInfo.setSourceDir(data.getSourceDir());
    }
    if (filters.contains(AppInfoFilter.Filter.DATA_DIR)) {
      appInfo.setDataDir(data.getDataDir());
    }
    if (filters.contains(AppInfoFilter.Filter.TARGET_SDK_VERSION)) {
      appInfo.setTargetSdkVersion(data.getTargetSdkVersion());
    }
    if (filters.contains(AppInfoFilter.Filter.APK_SIZE)) {
      appInfo.setApkSize(data.getApkSize());
    }
    if (filters.contains(AppInfoFilter.Filter.IS_INSTALLED)) {
      appInfo.setIsInstalled(data.isInstalled());
    }
    if (filters.contains(AppInfoFilter.Filter.IS_SYSTEM_APP)) {
      appInfo.setIsSystemApp(data.isSystemApp());
    }

    return appInfo;
  }

  private Error getSilenceInstallError(String errorString) {
    if (TextUtils.isEmpty(errorString)) {
      return Error.UNKNOWN;
    }

    if (errorString.equals(IntentExtras.INSTALL_FAILED_ALREADY_EXIST)
        || errorString.equals(IntentExtras.INSTALL_FAILED_DUPLICATE_PACKAGE)) {
      return Error.PACKAGE_ALREADY_EXIST;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_CONFLICTING_PROVIDER)) {
      return Error.INCONSISTENT_CERTIFICATES;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_DEXOPT)) {
      return Error.INSTALL_FAILED_DEXOPT;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_INSUFFICIENT_STORAGE)) {
      return Error.INSUFFICIENT_STORAGE;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_INTERNAL_ERROR)) {
      return Error.INSTALL_FAILED_INTERNAL_ERROR;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_INVALID_INSTALL_LOCATION)) {
      return Error.INSTALL_FAILED_INVALID_INSTALL_LOCATION;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_INVALID_URI)) {
      return Error.FILE_NO_EXIST_ERROR;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_MEDIA_UNAVAILABLE)) {
      return Error.INSTALL_FAILED_MEDIA_UNAVAILABLE;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_MISSING_FEATURE)) {
      return Error.INSTALL_FAILED_MISSING_FEATURE;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_MISSING_SHARED_LIBRARY)) {
      return Error.INSTALL_FAILED_MISSING_SHARED_LIBRARY;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_NO_SHARED_USER)) {
      return Error.INSTALL_FAILED_NO_SHARED_USER;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_NEWER_SDK)) {
      return Error.INSTALL_FAILED_NEWER_SDK;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_REPLACE_COULDNT_DELETE)) {
      return Error.INSTALL_FAILED_REPLACE_COULDNT_DELETE;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_SHARED_USER_INCOMPATIBLE)) {
      return Error.INSTALL_FAILED_SHARED_USER_INCOMPATIBLE;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_TEST_ONLY)) {
      return Error.INSTALL_FAILED_TEST_ONLY;
    }
    if (errorString.equals(IntentExtras.INSTALL_FAILED_UPDATE_INCOMPATIBLE)) {
      return Error.INSTALL_FAILED_UPDATE_INCOMPATIBLE;
    }

    return Error.UNKNOWN;
  }

  private class AppOperateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent == null || intent.getAction() == null) {
        return;
      }

      checkAndFillCacheInReceiver();

      if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {

        /**
         * intent.getDataString() is "package:com.xxx.xxx"
         */
        String packageName = intent.getDataString().substring(PACKAGE_NAME_INDEX);

        if (TextUtils.isEmpty(packageName)) {
          return;
        }

        invokeNotifyStatus(new NotifyStatusRunnable(packageName, OperateType.APP_INSTALL,
            AppOperateStatus.SUCCESS));

        addApp(packageName);

      } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {

        /**
         * intent.getDataString() is "package:com.xxx.xxx"
         */
        String packageName = intent.getDataString().substring(PACKAGE_NAME_INDEX);

        if (TextUtils.isEmpty(packageName)) {
          return;
        }

        invokeNotifyStatus(new NotifyStatusRunnable(packageName, OperateType.APP_UNINSTALL,
            AppOperateStatus.SUCCESS));

        deleteApp(packageName);

      } else if (intent.getAction().equals(IntentAction.ACTION_SILENCE_INSTALL_FAIL)) {

        String failInfo = intent.getExtras().getString(
            IntentExtras.SILENCE_INSTALL_FAIL_REASON_KEY);
        String packageName =
            intent.getExtras().getString(IntentExtras.SILENCE_INSTALL_FAIL_PACKAGENAME_KEY);
        if (TextUtils.isEmpty(packageName)) {
          return;
        }
        Error error = getSilenceInstallError(failInfo);
        invokeNotifyStatus(new NotifyStatusRunnable(packageName, OperateType.APP_SILENCE_INSTALL,
            AppOperateStatus.FAILED, error));
      }
    }
  }

  private class InitCacheCallable implements Callable<Map<String, AppInfo>> {
    @Override
    public Map<String, AppInfo> call() throws Exception {
      return AppQueryUtils.getAllInstalledApp(appContext, AppInfoFilter.RECOMMEND);
    }
  }

  private class ResultCallbackRunnable<T> implements Runnable {
    private ResultCallback<T> resultCallback;
    private T result;

    public ResultCallbackRunnable(ResultCallback<T> resultCallback, T result) {
      this.resultCallback = resultCallback;
      this.result = result;
    }

    @Override
    public void run() {
      if (resultCallback != null) {
        resultCallback.onResultCallback(result);
      }
    }
  }

  private class NotifyStatusRunnable implements Runnable {
    private String packageName;
    private OperateType operateType;
    private AppOperateStatus operateStatus;
    private Error error;

    public NotifyStatusRunnable(String packageName, OperateType operateType,
        AppOperateStatus operateStatus, Error error) {
      this.packageName = packageName;
      this.operateType = operateType;
      this.operateStatus = operateStatus;
      this.error = error;
    }

    public NotifyStatusRunnable(String packageName, OperateType operateType,
        AppOperateStatus operateStatus) {
      this.packageName = packageName;
      this.operateType = operateType;
      this.operateStatus = operateStatus;
    }

    @Override
    public void run() {
      if (TextUtils.isEmpty(packageName) || operateType == null
          || operateStatus == null) {
        return;
      }
      notifyStatus(packageName, operateType, operateStatus, error);
    }
  }
}
