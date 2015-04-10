package com.lemi.controller.lemigameassistance.manager;

import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.GameMasterPreferences;
import com.lemi.controller.lemigameassistance.helper.PosterHelper;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.model.RecommendsModel;
import com.lemi.controller.lemigameassistance.model.StartupModel;
import com.lemi.controller.lemigameassistance.model.SubjectModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.net.filter.GameOptionFields;
import com.lemi.controller.lemigameassistance.net.filter.StartupOptionFields;
import com.lemi.controller.lemigameassistance.net.filter.SubjectOptionFields;
import com.lemi.controller.lemigameassistance.utils.ThreadPool;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhoulei@letv.com (Zhou Lei)
 * @author liuxu5@letv.com (Liu Xu)
 */
public class StartupManager {

  private StartupModel startupModel;
  private final List<String> postersFilePath = new ArrayList<String>();

  private static StartupManager instance;

  private static final long NONE_TIME = 0l;
  private static final long NONE_SID = 0L;

  public synchronized static StartupManager getInstance() {
    if (instance == null) {
      instance = new StartupManager();
    }
    return instance;
  }

  public void asyncLoadStartupInfo() {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        loadStartupInfo();
      }
    });
  }

  public void asyncLoadRecommendInfo() {
    ThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        loadRecommendInfo();
      }
    });
  }

  public void syncLoadRecommendInfo() {
    loadRecommendInfo();
  }

  // when startup showed, need to set state
  public void setStartupShown() {
    if (startupModel == null) {
      return;
    }
    GameMasterPreferences.setLastStartUpId(startupModel.getSid());
    GameMasterPreferences.setLastStartUpDay(TimeUtils.getTodayTime());
  }

  public boolean canShowStartup() {
    if (needShowStartup()) {
      synchronized (postersFilePath) {
        if (!CollectionUtils.isEmpty(postersFilePath)) {
          return true;
        }
      }
    }
    return false;
  }

  // size > 0 show startup, else should not show
  public List<String> getPostersFilePath() {
    List<String> posters = new ArrayList<>();
    synchronized (postersFilePath) {
      if (!CollectionUtils.isEmpty(postersFilePath)) {
        return posters;
      }
      for (String path : postersFilePath) {
        posters.add(path);
      }
    }
    return posters;
  }


  private boolean needShowStartup() {
    if (startupModel == null || startupModel.getStartTime() == 0
        || startupModel.getStopTime() == 0
        || startupModel.getSid() == 0) {
      return false;
    } else {
      long currentTime = TimeUtils.getCurrentTime() / 1000; // model time unit is second
      long startTime = startupModel.getStartTime();
      long stopTime = startupModel.getStopTime();
      if (currentTime >= startTime && currentTime <= stopTime) {
        long sid = startupModel.getSid();
        if (isNewStartup(sid) || isNewDayToShow()) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isNewStartup(long sid) {
    long lastSid = GameMasterPreferences.getLastStartUpId();
    if (lastSid == NONE_SID) {
      return true;
    } else if (sid != lastSid) {
      return true;
    } else {
      return false;
    }
  }

  private boolean isNewDayToShow() {
    long lastShowDayTime = GameMasterPreferences.getLastStartUpDay();
    long nowDayTime = TimeUtils.getTodayTime();
    if (lastShowDayTime == NONE_TIME) {
      return true;
    } else if (nowDayTime != lastShowDayTime) {
      return true;
    } else {
      return false;
    }
  }

  private void loadRecommendInfo() {
    RecommendsModel recommend =
        GameMasterHttpHelper.getRecommend(GameOptionFields.RECOMMEND_LITE.getOptionFields(),
            SubjectOptionFields.SUBJECT_LITE.getOptionFields());
    if (recommend != null && recommend.getRet() == GameMasterHttpHelper.VALID_RETURN) {
      PosterHelper.syncDownloadPosters(getRecommendPosterUrls(recommend));
    }
  }


  private void loadStartupInfo() {
    startupModel =
        GameMasterHttpHelper.getStartup(StartupOptionFields.RECOMMEND_LITE.getOptionFields());
    if (startupModel != null && needShowStartup()) {
      PosterHelper.syncDownloadPosters(getStartupPosterUrls());
      insertPostersFilePath(startupModel);
    }
  }

  private List<String> getRecommendPosterUrls(RecommendsModel recommend) {
    List<String> urls = new ArrayList<String>();
    if (recommend == null) {
      return urls;
    }
    if (!CollectionUtils.isEmpty(recommend.getGames())) {
      for (GameModel gameModel : recommend.getGames()) {
        if (!CollectionUtils.isEmpty(gameModel.getPosters())) {
          for (GameModel.ImageInnerItem posterInnerItem : gameModel.getPosters()) {
            urls.add(posterInnerItem.getUrl());
          }
        }
      }
    }
    if (!CollectionUtils.isEmpty(recommend.getSubjects())) {
      for (SubjectModel subjectModel : recommend.getSubjects()) {
        if (!TextUtils.isEmpty(subjectModel.getIconUrl())) {
          urls.add(subjectModel.getIconUrl());
        }
      }
    }
    return urls;
  }

  private List<String> getStartupPosterUrls() {
    List<String> urls = new ArrayList<String>();
    if (startupModel == null) {
      return urls;
    }
    for (GameModel.ImageInnerItem posterInnerItem : startupModel.getPosters()) {
      urls.add(posterInnerItem.getUrl());
    }
    return urls;
  }

  private void insertPostersFilePath(StartupModel model) {
    String filePathTmp;
    if (model == null || CollectionUtils.isEmpty(model.getPosters())) {
      return;
    }
    synchronized (postersFilePath) {
      postersFilePath.clear();
      for (GameModel.ImageInnerItem item : model.getPosters()) {
        filePathTmp = PosterHelper.getPosterFileNameFromUrl(item.getUrl());
        if (!TextUtils.isEmpty(filePathTmp)) {
          postersFilePath.add(filePathTmp);
        }
      }
    }
  }

}
