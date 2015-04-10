package com.lemi.controller.lemigameassistance.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.GameMasterApplication;
import com.lemi.controller.lemigameassistance.model.GameListModel;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.net.filter.GameOptionFields;
import com.lemi.controller.lemigameassistance.utils.LogHelper;
import com.lemi.mario.appmanager.AppManager;
import com.lemi.mario.appmanager.model.AppInfo;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.NetworkUtil;
import com.lemi.mario.base.utils.TimeUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author zhengjinguang@letv.com (shining)
 */
public class GameMonitorService extends Service {

  private static final String MONITOR_TIMER = "game_monitor_timer";
  private static final long MONITOR_INTERVAL = 5000l;
  private static String gameMasterPackageName;
  private final Set<String> hitSet = new HashSet<>();
  private final Set<String> unHitSet = new HashSet<>();
  private Timer monitorTimer;
  private TimerTask monitorTask;

  public static void launch(Context context) {
    Intent intent = new Intent(context, GameMonitorService.class);
    context.startService(intent);
    gameMasterPackageName = GameMasterApplication.getAppContext().getPackageName();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    startMonitor();
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public synchronized void startMonitor() {
    if (monitorTimer == null) {
      monitorTimer = new Timer(MONITOR_TIMER, true);
      if (monitorTask == null) {
        monitorTask = new MonitorTask();
      }
      monitorTimer.schedule(monitorTask, MONITOR_INTERVAL, MONITOR_INTERVAL);
    }
  }

  private boolean isLemiGame(String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return false;
    }
    if (unHitSet.contains(packageName)) {
      return false;
    }
    if (hitSet.contains(packageName)) {
      return true;
    }
    GameModel gameModel = getFromNetWork(packageName);
    if (gameModel == null) {
      return false;
    }
    if (packageName.equals(gameModel.getPackageName())) {
      hitSet.add(packageName);
      return true;
    } else {
      unHitSet.add(packageName);
      return false;
    }

  }

  private GameModel getFromNetWork(String packageName) {
    if (!NetworkUtil.isNetworkConnected(GameMasterApplication.getAppContext())) {
      return null;
    }
    GameListModel gameList =
        GameMasterHttpHelper.getGamesByPackageName(packageName,
            GameOptionFields.PACKAGE_LITE.getOptionFields());
    if (gameList == null || gameList.getRet() != GameMasterHttpHelper.VALID_RETURN) {
      return null;
    }
    if (CollectionUtils.isEmpty(gameList.getGames())) {
      return new GameModel();
    }

    return gameList.getGames().get(0);
  }

  private class MonitorTask extends TimerTask {
    private String lastRunningPackageName = null;

    @Override
    public void run() {
      ActivityManager manager =
          (ActivityManager) GameMasterApplication.getAppContext().getSystemService(
              Context.ACTIVITY_SERVICE);
      if (manager == null) {
        return;
      }
      List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
      if (runningTasks == null || runningTasks.size() < 1) {
        return;
      }
      ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
      if (runningTaskInfo == null) {
        return;
      }

      ComponentName topActivity = runningTaskInfo.topActivity;
      if (topActivity == null) {
        return;
      }

      String runningPackageName = topActivity.getPackageName();
      checkLaunchState(runningPackageName);

    }

    private void checkLaunchState(String runningPackageName) {
      long nowTime = TimeUtils.getCurrentTime();
      if (lastRunningPackageName != null && lastRunningPackageName.equals(runningPackageName)) {
        return;
      }

      if (lastRunningPackageName != null) {
        if (lastRunningPackageName.equals(gameMasterPackageName)) {
          LogHelper.backToBackground(nowTime);
        } else {
          AppInfo info = AppManager.getInstance().syncGetAppByPackageName(lastRunningPackageName);
          LogHelper.gameBackToBackground(lastRunningPackageName,
              info == null ? null : info.getAppName(), nowTime);
        }
      }

      lastRunningPackageName = runningPackageName;

      if (isLemiGame(runningPackageName)) {
        AppInfo info = AppManager.getInstance().syncGetAppByPackageName(runningPackageName);
        LogHelper.gameLaunch(runningPackageName, info == null ? null : info.getAppName(), nowTime);
      } else if (runningPackageName.equals(gameMasterPackageName)) {
        LogHelper.launch(nowTime);
      } else {
        lastRunningPackageName = null;
      }
    }

  }
}
