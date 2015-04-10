package com.lemi.controller.lemigameassistance.net;

import android.text.TextUtils;
import android.util.Log;

import com.lemi.controller.lemigameassistance.account.GameMasterAccountManager;
import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.model.AwardListModel;
import com.lemi.controller.lemigameassistance.model.CategoryListModel;
import com.lemi.controller.lemigameassistance.model.CheckVersionModel;
import com.lemi.controller.lemigameassistance.model.DrawLotteryModel;
import com.lemi.controller.lemigameassistance.model.GameListModel;
import com.lemi.controller.lemigameassistance.model.GamesTimeLineModel;
import com.lemi.controller.lemigameassistance.model.GetLotteryModel;
import com.lemi.controller.lemigameassistance.model.OtherAwardListModel;
import com.lemi.controller.lemigameassistance.model.RecommendsModel;
import com.lemi.controller.lemigameassistance.model.StartupModel;
import com.lemi.controller.lemigameassistance.model.StatisticsCountModel;
import com.lemi.controller.lemigameassistance.model.SubjectContentModel;
import com.lemi.controller.lemigameassistance.model.SubjectListModel;
import com.lemi.controller.lemigameassistance.model.base.BaseErrorModel;
import com.lemi.controller.lemigameassistance.net.base.request.GameMasterHttpRequestBuilder;
import com.lemi.controller.lemigameassistance.net.delegate.CheckVersionDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.DrawLotteryDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetAwardsDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetCategoryDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetGamesByPackageNameDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetGamesTimeLineDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetLotteryDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetOtherAwardsDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetRecommendDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetStartupDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetStatisticsDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetSubjectContentDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.GetSubjectListDelegate;
import com.lemi.controller.lemigameassistance.net.delegate.SetStatisticsDelegate;
import com.lemi.mario.accountmanager.MarioAccountManager;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.rpc.http.client.DataApi;
import com.lemi.mario.rpc.http.client.DataClient;
import com.lemi.mario.rpc.http.client.DataClientCache;
import com.lemi.mario.rpc.http.delegate.ApiDelegate;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameMasterHttpHelper {


  public static final int VALID_RETURN = 0;
  private static final String DATA_CACHE_FOLDER = "DataCache";
  private static final String GAME_MASTER_TAG = "GAME_MASTER_TAG";
  private static DataClient dataClient;

  private static synchronized DataApi getDataApi() {
    if (dataClient == null) {
      String cacheDir;
      cacheDir = Constants.ROOT_PATH + File.separator + DATA_CACHE_FOLDER;
      dataClient = new DataClientCache(cacheDir);
    }

    return dataClient;
  }

  private static <T extends BaseErrorModel, E extends Exception> T doExecute(
      ApiDelegate<T, E> delegate, GameMasterHttpRequestBuilder requestBuilder)
      throws ExecutionException {
    T result = null;
    try {
      result = getDataApi().execute(delegate);
    } finally {

      JSONObject paramsJson = new JSONObject(requestBuilder.getRequestParams());

      if (result != null && result.getRet() == VALID_RETURN) {
        logI("HTTP_SUCCESS: Url = " + requestBuilder.getRequestUrl() + " , Params = "
            + paramsJson.toString());
      } else if (result != null) {
        logI("HTTP_FAIL: Url = " + requestBuilder.getRequestUrl() + " , Params = "
            + paramsJson.toString() + " , Error Return = " + result.getRet()
            + " , Error Reason = " + result.getReason());
      } else {
        logI("HTTP_FAIL_THROWS_EXCEPTION: Url = " + requestBuilder.getRequestUrl() + " , Params = "
            + paramsJson.toString());
      }
    }
    return result;
  }

  private static void logI(String message) {
    if (GlobalConfig.isDebug()) {
      Log.i(GAME_MASTER_TAG, message);
    }
  }


  public static StatisticsCountModel getStatisticsCount(String packageName,
      StatisticsCountModel.StatisticsType type) {
    GetStatisticsDelegate delegate = new GetStatisticsDelegate();
    delegate.getRequestBuilder().setPackageName(packageName).setType(type);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static StatisticsCountModel setStatisticsCount(String packageName,
      StatisticsCountModel.StatisticsType type) {
    SetStatisticsDelegate delegate = new SetStatisticsDelegate();
    delegate.getRequestBuilder().setPackageName(packageName).setType(type);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static GamesTimeLineModel getGamesTimeLine(int startIndex, int size, String category,
      String gameFilter) {
    GetGamesTimeLineDelegate delegate = new GetGamesTimeLineDelegate();
    delegate.getRequestBuilder().setStartIndex(startIndex).setSize(size).setCategoryId(category)
        .setGameFilter(gameFilter);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static RecommendsModel getRecommend(String gameFilter, String subjectFilter) {
    GetRecommendDelegate delegate = new GetRecommendDelegate();
    delegate.getRequestBuilder().setGameFilter(gameFilter).setSubjectFilter(subjectFilter);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static GameListModel getGamesByPackageName(String packageName, String gameFilter) {
    if (TextUtils.isEmpty(packageName)) {
      return null;
    }
    GetGamesByPackageNameDelegate delegate = new GetGamesByPackageNameDelegate();
    delegate.getRequestBuilder().setPackageName(packageName).setGameFilter(gameFilter);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static GameListModel getGamesByPackageName(List<String> packageNames, String gameFilter) {
    if (packageNames == null || packageNames.size() < 1) {
      return null;
    }
    GetGamesByPackageNameDelegate delegate = new GetGamesByPackageNameDelegate();
    delegate.getRequestBuilder().setPackageNames(packageNames).setGameFilter(gameFilter);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static CheckVersionModel checkVersion(int versionCode) {
    CheckVersionDelegate delegate = new CheckVersionDelegate();
    delegate.getRequestBuilder().setVersionCode(versionCode);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static CategoryListModel getCategroys() {
    GetCategoryDelegate delegate = new GetCategoryDelegate();
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static SubjectContentModel getSubjectContent(long subjectId, String gameFilter,
      String subjectFilter) {
    GetSubjectContentDelegate delegate = new GetSubjectContentDelegate();
    delegate.getRequestBuilder().setSubjectId(subjectId).setGameFilter(gameFilter)
        .setSubjectFilter(subjectFilter);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static SubjectListModel getSubjects(int startIndex, int size, String subjectFilter) {
    GetSubjectListDelegate delegate = new GetSubjectListDelegate();
    delegate.getRequestBuilder().setStartIndex(startIndex).setSize(size)
        .setSubjectFilter(subjectFilter);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static StartupModel getStartup(String startupFilter) {
    GetStartupDelegate delegate = new GetStartupDelegate();
    delegate.getRequestBuilder().setStartupFilter(startupFilter);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static GetLotteryModel getLottery() {
    GetLotteryDelegate delegate = new GetLotteryDelegate();
    delegate.getRequestBuilder().setLotteryId(Constants.LOTTERY_ID);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static DrawLotteryModel drawLottery() {
    DrawLotteryDelegate delegate = new DrawLotteryDelegate();
    delegate.getRequestBuilder().setLotteryId(Constants.LOTTERY_ID)
        .setAuth(GameMasterAccountManager.getInstance().getAuth());
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static AwardListModel getAwards() {
    GetAwardsDelegate delegate = new GetAwardsDelegate();
    delegate.getRequestBuilder().setAuth(MarioAccountManager.getInstance().getAuth());
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static OtherAwardListModel getOtherAwards() {
    GetOtherAwardsDelegate delegate = new GetOtherAwardsDelegate();
    delegate.getRequestBuilder().setLotteryId(Constants.LOTTERY_ID);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    return null;
  }
}
