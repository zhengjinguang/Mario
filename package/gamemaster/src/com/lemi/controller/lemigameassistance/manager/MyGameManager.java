package com.lemi.controller.lemigameassistance.manager;

import android.content.Context;
import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.model.GameListModel;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.net.filter.GameOptionFields;
import com.lemi.controller.lemigameassistance.recycleview.model.MyGameModel;
import com.lemi.controller.lemigameassistance.utils.ThreadPool;
import com.lemi.mario.appmanager.AppManager;
import com.lemi.mario.appmanager.LocalAppChangedListener;
import com.lemi.mario.appmanager.error.Error;
import com.lemi.mario.appmanager.filter.AppInfoFilter;
import com.lemi.mario.appmanager.model.AppInfo;
import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.NetworkUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhoulei@letv.com (Zhou Lei)
 */
public class MyGameManager {

  private final static int MAX_THREAD_NUMBER = 5;
  private final static long MAX_THREAD_LIVE_TIME = 3000l;

  private final byte[] gamesLock = new byte[0];

  private static MyGameManager instance;
  private Context appContext;

  private Map<String, GameModel> myGames;

  private CachedThreadPoolExecutorWithCapacity threadPool;

  private final Set<WeakReference<OnMyGameChangeListener>> onMyGameChangeListeners =
      new HashSet<>();

  private LocalAppChangedListener appChangedListener = new LocalAppChangedListener() {
    @Override
    public void onInstalled(final String packageName, AppInfo appInfo) {
      ThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          addGame(packageName);
        }
      });

    }

    @Override
    public void onUninstalled(final String packageName) {
      ThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          removeGame(packageName);
        }
      });
    }

    @Override
    public void onInstallFailed(String packageName, Error error) {}

    @Override
    public void onUninstallFailed(String packageName, Error error) {}

    @Override
    public void onStartNonRootInstall(String packageName) {}

    @Override
    public void onStartSilenceInstall(String packageName) {}

    @Override
    public void onStartUninstall(String packageName) {}
  };

  public interface OnMyGameChangeListener {
    void onMyGameAdd(String packageName, GameModel gameModel);

    void onMyGameRemove(String packageName);
  }


  private MyGameManager() {
    appContext = GlobalConfig.getAppContext();
    threadPool = new CachedThreadPoolExecutorWithCapacity(MAX_THREAD_NUMBER, MAX_THREAD_LIVE_TIME);
    AppManager.getInstance().registerLocalAppChangedListener(appChangedListener);
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        init();
      }
    });
  }

  /**
   * register listener to listen myGames change
   *
   * @param listener: listener to listen myGames change
   */
  public void registerMyGameChangeListener(OnMyGameChangeListener listener) {
    synchronized (onMyGameChangeListeners) {
      onMyGameChangeListeners.add(new WeakReference<OnMyGameChangeListener>(listener));
    }
  }

  /**
   * get MyGameManager singleton Instance
   *
   * @return AppManager instance
   */
  public static synchronized MyGameManager getInstance() {
    if (instance == null) {
      instance = new MyGameManager();
    }
    return instance;
  }

  /**
   * query all my installed games
   *
   * notice:
   * it's time-consuming if MyGameManager be initialized just now and cache had not yet be build
   * need network, if network state is incorrect, result is null
   *
   * @return List<GameModel> if null network is no-valid , list.size() == 0 is no-data
   */
  public List<GameModel> getMyInstalledGames() {
    checkAndInitCache();
    return getCachedGameList();
  }


  /**
   * get the game model if the app of packageName is my game and be installed
   *
   * notice:
   * it's time-consuming if MyGameManager be initialized just now and cache had not yet be build
   * if the app of packageName is not installed and no my game, result is null
   *
   * @return GameModel
   */
  public GameModel getMyGameByPackageNameFormCache(String packageName) {
    return getFromCache(packageName);
  }


  /**
   * get the game of packageName from cache or server
   * if is my game, return GameModel, else return mull
   *
   * notice:
   * it's time consuming if cache don't contain the game
   * need network, if network state incorrect and cache don't contain the game, return null
   *
   * @return GameModel
   */
  public GameModel getMyGameByPackageName(String packageName) {

    GameModel model = getFromCache(packageName);
    if (model != null) {
      return model;
    }

    model = getFromNetWork(packageName);
    if (model != null) {
      return model;
    }

    return null;
  }



  private void init() {
    Map<String, GameModel> gameModelMap = getFromNetWork(getInstalledPackageName());
    writeCache(gameModelMap);
  }

  private boolean checkCacheValid() {
    synchronized (gamesLock) {
      return myGames != null;
    }

  }

  private Map<String, GameModel> getFromNetWork(List<String> packageNameList) {
    if (!NetworkUtil.isNetworkConnected(appContext)) {
      return null;
    }
    if (CollectionUtils.isEmpty(packageNameList)) {
      return null;
    }
    GameListModel gameListModel;
    gameListModel = GameMasterHttpHelper.getGamesByPackageName(packageNameList,
        GameOptionFields.ALL.getOptionFields());
    if (gameListModel == null || gameListModel.getRet() != GameMasterHttpHelper.VALID_RETURN) {
      return null;
    }

    /**
     * if return valid , but no package init cache.
     */
    Map<String, GameModel> myGameMap = new HashMap<String, GameModel>();
    if (CollectionUtils.isEmpty(gameListModel.getGames())) {
      return myGameMap;
    }

    for (GameModel gameModel : gameListModel.getGames()) {
      if (gameModel == null || gameModel.getPackageName() == null) {
        continue;
      }
      myGameMap.put(gameModel.getPackageName(), convert(gameModel));
    }

    return myGameMap;
  }

  private GameModel getFromNetWork(String packageName) {
    if (!NetworkUtil.isNetworkConnected(appContext)) {
      return null;
    }
    if (TextUtils.isEmpty(packageName)) {
      return null;
    }
    GameListModel gameList =
        GameMasterHttpHelper.getGamesByPackageName(packageName,
            GameOptionFields.ALL.getOptionFields());
    if (gameList == null || gameList.getGames() == null || gameList.getGames().size() < 1) {
      return null;
    }
    return convert(gameList.getGames().get(0));
  }

  private GameModel getFromCache(String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return null;
    }
    if (checkCacheValid()) {
      synchronized (gamesLock) {
        return myGames.get(packageName);
      }
    }
    return null;
  }

  private void writeCache(Map<String, GameModel> gameModelMap) {
    if (gameModelMap == null) {
      return;
    }

    synchronized (gamesLock) {
      if (myGames == null) {
        myGames = new HashMap<>();
      }
      myGames.putAll(gameModelMap);
    }

  }

  private void writeCache(GameModel gameModel) {
    if (gameModel == null || TextUtils.isEmpty(gameModel.getPackageName())) {
      return;
    }

    if (!checkCacheValid()) {
      return;
    }
    synchronized (gamesLock) {
      myGames.put(gameModel.getPackageName(), gameModel);
    }
  }

  private void removeCache(String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return;
    }

    if (!checkCacheValid()) {
      return;
    }
    synchronized (gamesLock) {
      myGames.remove(packageName);
    }
  }

  private void checkAndInitCache() {
    if (!checkCacheValid()) {
      init();
    }
  }

  private List<String> getInstalledPackageName() {
    List<AppInfo> installedAppInfos =
        AppManager.getInstance().syncGetAllInstalledApp(
            AppInfoFilter.SIMPLE);

    List<String> installedPackageNameList = new ArrayList<String>();
    if (!CollectionUtils.isEmpty(installedAppInfos)) {
      for (AppInfo appInfo : installedAppInfos) {
        installedPackageNameList.add(appInfo.getPackageName());
      }
    }

    return installedPackageNameList;
  }

  private List<GameModel> getCachedGameList() {
    List<GameModel> gameModels = new ArrayList<>();
    synchronized (gamesLock) {
      if (myGames == null) {
        return null;
      }
      if (myGames.size() < 1) {
        return gameModels;
      }
      Set<String> keySet = myGames.keySet();
      for (String packageName : keySet) {
        GameModel gameModel = myGames.get(packageName);
        if (gameModel == null) {
          continue;
        }
        gameModels.add(gameModel);
      }
    }
    return gameModels;
  }

  private void addGame(String packageName) {
    checkAndInitCache();
    GameModel gameModel = getFromNetWork(packageName);
    writeCache(gameModel);
    notifyAddGame(gameModel);
  }

  private void removeGame(String packageName) {
    checkAndInitCache();
    removeCache(packageName);
    notifyRemoveGame(packageName);
  }

  private void notifyAddGame(final GameModel model) {
    if (model == null || TextUtils.isEmpty(model.getPackageName())) {
      return;
    }
    synchronized (onMyGameChangeListeners) {
      if (!onMyGameChangeListeners.isEmpty()) {
        for (WeakReference<OnMyGameChangeListener> onMyGameChangeListener : onMyGameChangeListeners) {
          final OnMyGameChangeListener listener = onMyGameChangeListener.get();
          if (listener != null) {
            MainThreadPostUtils.post(new Runnable() {
              @Override
              public void run() {
                listener.onMyGameAdd(model.getPackageName(), model);
              }
            });

          }
        }
      }
    }
  }

  private void notifyRemoveGame(final String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return;
    }
    synchronized (onMyGameChangeListeners) {
      if (!onMyGameChangeListeners.isEmpty()) {
        for (WeakReference<OnMyGameChangeListener> onMyGameChangeListener : onMyGameChangeListeners) {
          final OnMyGameChangeListener listener = onMyGameChangeListener.get();
          if (listener != null) {
            MainThreadPostUtils.post(new Runnable() {
              @Override
              public void run() {
                listener.onMyGameRemove(packageName);
              }
            });

          }
        }
      }
    }
  }


  private MyGameModel convert(GameModel gameModel) {
    if (gameModel == null) {
      return null;
    }
    return new MyGameModel(gameModel.getGid(), gameModel.getName(), gameModel.getApkUrl(),
        gameModel.getOnlineTime(), gameModel.getApkSize(), gameModel.getRealSize(),
        gameModel.getDownloadCount(), gameModel.getCategory(), gameModel.getPlayMode(),
        gameModel.getOperationMode(), gameModel.getPersonMode(), gameModel.getAuthor(),
        gameModel.getVersionName(), gameModel.getStar(), gameModel.getLanguage(),
        gameModel.getPackageName(), gameModel.getNotice(), gameModel.getDescription(),
        gameModel.getIconUrl(), gameModel.getImages(), gameModel.getPosters());
  }

}
