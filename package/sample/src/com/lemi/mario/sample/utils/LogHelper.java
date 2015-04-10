package com.lemi.mario.sample.utils;

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
  private static final long SEND_DURATION = 60 * 60 * 1000L;

  private static final String TAG = LogHelper.class.getSimpleName();

  public static final String LOG_APP_KEY = "EBB252A4968E27CFA096F0FA09FA57B4";

  public static final String EVENT_REPORT = "count";

  // EventId
  private static final String EVENT_LAUNCH = "launch";
  private static final String EVENT_CONTROLLER = "controller";
  private static final String EVENT_CLICK = "click";
  private static final String EVENT_VIEW = "view";
  private static final String EVENT_DOWNLOAD = "download";
  private static final String EVENT_GAME = "game";

  // info key
  private static final String KEY_PRIMARY = "primary";
  private static final String KEY_CONTROLLER_MODEL = "model";
  private static final String KEY_CLICK_NAME = "click_name";
  private static final String KEY_VIEW_NAME = "view_name";
  private static final String KEY_PACKAGE_NAME = "package_name";
  private static final String KEY_DOWNLOAD_STATUS = "download_success";
  private static final String KEY_DOWNLOAD_FAILED_REASON = "reason";


  public static enum StatusType {
    SUCCESS, FAILED
  }


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
      headerParams.put("channel", "TEST_CHANNEL_FOR_SAMPLE");

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


  public static void launch(long startTime) {

    reportTime(EVENT_LAUNCH, null, String.valueOf(startTime), null);
  }

  public static void backToBackgroud(long endTime) {

    reportTime(EVENT_LAUNCH, null, null, String.valueOf(endTime));
  }

  public static void connectController(String model) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_CONTROLLER_MODEL, model);

    reportEvent(EVENT_CONTROLLER, infos);
  }

  public static void clickEvent(String clickName, String packageName) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_CLICK_NAME, clickName);
    if (!TextUtils.isEmpty(packageName)) {
      infos.put(KEY_PACKAGE_NAME, packageName);
    }

    reportEvent(EVENT_CLICK, infos);
  }

  public static void enterPage(String pageName, String packageName, long startTime) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_VIEW_NAME, pageName);
    if (!TextUtils.isEmpty(packageName)) {
      infos.put(KEY_PACKAGE_NAME, packageName);
    }

    reportTime(EVENT_VIEW, infos, String.valueOf(startTime), null);
  }

  public static void exitPage(String pageName, String packageName, long endTime) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_VIEW_NAME, pageName);
    if (!TextUtils.isEmpty(packageName)) {
      infos.put(KEY_PACKAGE_NAME, packageName);
    }

    reportTime(EVENT_VIEW, infos, null, String.valueOf(endTime));
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

  public static void startGame(String gamePackageName, long startTime) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_PACKAGE_NAME, gamePackageName);

    reportTime(EVENT_GAME, infos, String.valueOf(startTime), null);
  }

  public static void quitGame(String gamePackageName, long endTime) {
    Map<String, String> infos = new HashMap<String, String>();
    infos.put(KEY_PACKAGE_NAME, gamePackageName);

    reportTime(EVENT_GAME, infos, null, String.valueOf(endTime));
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

    if (LogUtils.isLogEnabled()) {
      LogUtils.d(TAG, "eventId - " + eventId);
      if (infos != null) {
        for (Map.Entry<String, String> entry : infos.entrySet()) {
          LogUtils.d(TAG, "info - " + entry.getKey() + " : " + entry.getValue());
        }
      }
    }
  }

}
