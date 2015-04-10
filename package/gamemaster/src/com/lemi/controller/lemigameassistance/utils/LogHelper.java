package com.lemi.controller.lemigameassistance.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.lemi.mario.base.utils.NetworkUtil;
import com.lemi.mario.base.utils.SystemUtil;
import com.lemi.mario.log.LogReporter;
import com.lemi.mario.log.LogReporterFactory;
import com.lemi.mario.log.config.LogConfiguration;
import com.lemi.mario.log.sender.LogSender;
import com.lemi.mario.udid.UDIDUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class LogHelper {
  public static final String LOG_APP_KEY = "EBB252A4968E27CFA096F0FA09FA57B4";
  public static final String EVENT_REPORT = "count";
  // recommend name
  public static final String RECOMMEND_LARGE0 = "LARGE0";
  public static final String RECOMMEND_LARGE4 = "LARGE4";
  public static final String RECOMMEND_LITTLE1 = "LITTLE1";
  public static final String RECOMMEND_LITTLE2 = "LITTLE2";
  public static final String RECOMMEND_LITTLE3 = "LITTLE3";
  public static final String RECOMMEND_LITTLE5 = "LITTLE5";
  public static final String RECOMMEND_LITTLE6 = "LITTLE6";
  public static final String RECOMMEND_LITTLE7 = "LITTLE7";
  public static final String MANAGEMENT_ACCOUNT = "ACCOUNT";
  public static final String MANAGEMENT_SETTING = "SETTING";
  public static final String MANAGEMENT_DOWNLOAD = "DOWNLOAD";
  public static final String MANAGEMENT_UNINSTALL = "UNINSTALL";
  public static final String MANAGEMENT_CHECK_VERSION = "CHECK_VERSION";
  public static final String MANAGEMENT_ABOUT = "ABOUT";
  public static final String SETTING_CLEAR = "CLEAR";
  public static final String SETTING_SDCARD = "SDCARD";
  public static final String ACCOUNT_CHANGE_INFO = "CHANGE_INFO";
  public static final String ACCOUNT_LOTTERY = "LOTTERY";
  public static final String LOTTERY_GET_AWARDS = "GET_AWARDS";
  public static final String LOTTERY_DRAW = "DRAW";
  private static final long SEND_DURATION = 60 * 60 * 1000L;
  private static final LogConfiguration LOG_CONFIGURATION = new LogConfiguration() {

    @Override
    public String getProfileName() {
      return LOG_APP_KEY;
    }

    /**
     * log header
     */
    @Override
    public Map<String, String> buildHeaderParams(Context context) {
      Map<String, String> headerParams = new HashMap<String, String>();
      headerParams.put("udid", UDIDUtil.getUDID(context));
      headerParams.put("ui_vn", "TEST_UI_VERSION_FOR_SAMPLE");
      headerParams.put("release_vn", "TEST_RELEASE_VERSION_FOR_SAMPLE");
      headerParams.put("mac", SystemUtil.getMacAddress(context));
      headerParams.put("imei", SystemUtil.getImei(context));
      headerParams.put("ip", NetworkUtil.getIPAddress(true));
      headerParams.put("model", Build.MODEL);
      headerParams.put("vendor", Build.PRODUCT);
      headerParams.put("android_vn", String.valueOf(Build.VERSION.SDK_INT));
      headerParams.put("vc", String.valueOf(SystemUtil.getVersionCode(context)));
      headerParams.put("vn", SystemUtil.getVersionName(context));
      headerParams.put("channel", SystemUtil.getMetaChannel(context));

      return headerParams;
    }

    /**
     * stable common info
     */
    @Override
    public Map<String, String> buildStableCommonParams(Context context) {
      return Collections.emptyMap();
    }

    /**
     * volatile common info
     */
    @Override
    public Map<String, String> buildVolatileCommonParams(Context context) {
      return Collections.emptyMap();
    }

    @Override
    public LogSender.SenderPolicyModel getWifiSendPolicy() {
      return new LogSender.SenderPolicyModel(LogSender.TimePolicy.SCHEDULE, SEND_DURATION);
    }

    @Override
    public LogSender.SenderPolicyModel getMobileSendPolicy() {
      return new LogSender.SenderPolicyModel(LogSender.TimePolicy.SCHEDULE, SEND_DURATION);
    }
  };
  // EventId
  private static final String EVENT_LAUNCH = "launch";
  private static final String EVENT_CONTROLLER = "controller";
  private static final String EVENT_DOWNLOAD = "download";
  private static final String GAME_LAUNCH = "game_launch";
  private static final String RECOMMEND_CLICK = "recommend_click";
  private static final String GAME_DETAIL_CLICK = "game_detail_click";
  private static final String SUBJECT_DETAIL_CLICK = "subject_detail_click";
  private static final String MANAGEMENT_CLICK = "management_click";
  private static final String SETTING_CLICK = "setting_click";
  private static final String ACCOUNT_DETAIL_CLICK = "account_detail_click";
  private static final String ACCOUNT_CLICK = "account_click";
  private static final String LOTTERY_DETAIL_CLICK = "lottery_detail_click";
  private static final String LOTTERY_CLICK = "lottery_click";
  // info key
  private static final String KEY_PRIMARY = "primary";
  private static final String KEY_CONTROLLER_MODEL = "model";
  private static final String KEY_NAME = "name";
  private static final String KEY_PACKAGE_NAME = "package_name";
  private static final String KEY_DOWNLOAD_STATUS = "download_success";
  private static final String KEY_DOWNLOAD_FAILED_REASON = "reason";
  private static LogReporter logReporter;
  private static boolean inited = false;

  public static synchronized void initLogReporter(Context context) {
    if (inited) {
      return;
    }

    logReporter = LogReporterFactory.newLogReporter(context, LOG_CONFIGURATION);
    inited = true;
  }

  public static LogReporter getLogReporter() {
    return logReporter;
  }

  public static void gameLaunch(String packageName, String gameName, long startTime) {
    Map<String, String> infos = new HashMap<String, String>();
    if (!TextUtils.isEmpty(packageName)) {
      infos.put(KEY_PACKAGE_NAME, packageName);
    }
    if (!TextUtils.isEmpty(gameName)) {
      infos.put(KEY_NAME, gameName);
    }
    reportTime(GAME_LAUNCH, infos, String.valueOf(startTime), null);
  }

  public static void gameBackToBackground(String packageName, String gameName, long endTime) {
    Map<String, String> infos = new HashMap<String, String>();
    if (!TextUtils.isEmpty(packageName)) {
      infos.put(KEY_PACKAGE_NAME, packageName);
    }
    if (!TextUtils.isEmpty(gameName)) {
      infos.put(KEY_NAME, gameName);
    }
    reportTime(GAME_LAUNCH, infos, null, String.valueOf(endTime));
  }

  public static void launch(long startTime) {

    reportTime(EVENT_LAUNCH, null, String.valueOf(startTime), null);
  }

  public static void backToBackground(long endTime) {

    reportTime(EVENT_LAUNCH, null, null, String.valueOf(endTime));
  }

  public static void connectController(String model) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_CONTROLLER_MODEL, model);

    reportEvent(EVENT_CONTROLLER, infos);
  }

  public static void subjectDetailClick(String subjectName) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_NAME, subjectName);
    reportEvent(SUBJECT_DETAIL_CLICK, infos);
  }

  public static void recommendClick(String name) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_NAME, name);
    reportEvent(RECOMMEND_CLICK, infos);
  }

  public static void gameDetailCLick(String gameName) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_NAME, gameName);
    reportEvent(GAME_DETAIL_CLICK, infos);
  }

  public static void managementCLick(String itemName) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_NAME, itemName);
    reportEvent(MANAGEMENT_CLICK, infos);
  }

  public static void accountDetailClick() {
    reportEvent(ACCOUNT_DETAIL_CLICK, null);
  }

  public static void accountClick(String itemName) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_NAME, itemName);
    reportEvent(ACCOUNT_CLICK, infos);
  }

  public static void lotteryDetailClick(String lotteryName) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_NAME, lotteryName);
    reportEvent(LOTTERY_DETAIL_CLICK, infos);
  }

  public static void lotteryClick(String lotteryName) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_NAME, lotteryName);
    reportEvent(LOTTERY_CLICK, infos);
  }

  public static void settingClick(String itemName) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_NAME, itemName);
    reportEvent(SETTING_CLICK, infos);
  }

  public static void downloadComplete(String downloadPackageName, StatusType statusType,
      String reason) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_PACKAGE_NAME, downloadPackageName);
    if (statusType != null) {
      infos.put(KEY_DOWNLOAD_STATUS, statusType.toString());
    }
    if (!TextUtils.isEmpty(reason)) {
      infos.put(KEY_DOWNLOAD_FAILED_REASON, reason);
    }

    reportEvent(EVENT_DOWNLOAD, infos);
  }

  private static void reportEvent(String eventId, Map<String, String> infos) {
    if (infos == null) {
      infos = new HashMap<String, String>();
    }
    infos.put(KEY_PRIMARY, EVENT_REPORT);
    report(eventId, infos);
  }

  private static void reportTime(String eventId, Map<String, String> infos, String startTime,
      String endTime) {
    if (TextUtils.isEmpty(startTime) && TextUtils.isEmpty(endTime)) {
      return;
    }
    if (infos == null) {
      infos = new HashMap<String, String>();
    }
    if (!TextUtils.isEmpty(startTime)) {
      infos.put(KEY_PRIMARY, startTime);
    } else {
      infos.put(KEY_PRIMARY, "-" + endTime);
    }
    report(eventId, infos);
  }

  private static void reportCount(String eventId, Map<String, String> infos, String countValue) {
    if (TextUtils.isEmpty(countValue)) {
      return;
    }
    if (infos == null) {
      infos = new HashMap<String, String>();
    }
    infos.put(KEY_PRIMARY, countValue);
    report(eventId, infos);
  }

  private static void report(String eventId, Map<String, String> infos) {
    if (logReporter == null) {
      return;
    }
    if (infos == null || infos.get(KEY_PRIMARY) == null) {
      return;
    }
    logReporter.onEvent(eventId, infos);

  }


  public static enum StatusType {
    SUCCESS, FAILED
  }
}
