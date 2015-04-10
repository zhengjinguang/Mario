package com.lemi.mario.log.sender;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lemi.mario.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.IOUtils;
import com.lemi.mario.base.utils.NetworkUtil;
import com.lemi.mario.base.utils.SharePrefSubmitor;
import com.lemi.mario.log.config.LogConfiguration;
import com.lemi.mario.log.model.LogReturnModel;
import com.lemi.mario.log.storage.LogStorage;
import com.lemi.mario.rpc.http.client.DefaultHttpClient;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class LogSender {
  public enum TimePolicy {
    NONE, ON_LAUNCH, REAL_TIME, SCHEDULE
  }

  // TODO: FIX URL when upload api ready
  private static final String POST_URL_RAW =
      "http://ledata.lemiplay.com/index.php/mc/onevent?profile=%1$s";
  private static final String POST_URL_RAW_DEBUG =
      "http://ledata.lemiplay.com/index.php/mc/onevent?profile=%1$s";

  private static final int BUFFER_SIZE = 1024;
  private static final int LOG_SENDER_THREAD_NUM = 1;
  private static final long LOG_SENDER_THREAD_CACHE_TIME = 10 * 60 * 1000L;
  private static final String LOG_SENDER_THREAD_NAME = "log-sender-thread";

  private static final String PREF_NAME = "log_module";
  private static final String PREF_KEY_LAST_SEND_SUCCESS_TIME = "log_sender_last_success_time";

  private static final String CONTENT_ENCODING = "Content-Encoding";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String GZIP = "gzip";
  private static final String GB2312 = "text/html; charset=GB2312";
  private static final String SEND_RESPONSE_OK = "OK";

  private final Context context;
  private final Executor senderExecutor;
  private final LogStorage logStorage;
  private final LogConfiguration logConfiguration;
  private HttpClient httpClient;
  private Gson gson;

  private final SenderPolicyModel wifiSenderPolicy;
  private final SenderPolicyModel mobileSenderPolicy;

  private boolean isJustLaunch = true;

  private String headerLine;
  private String postUrl;

  public LogSender(Context context, LogStorage logStorage, LogConfiguration configuration) {
    this.senderExecutor =
        new CachedThreadPoolExecutorWithCapacity(LOG_SENDER_THREAD_NUM,
            LOG_SENDER_THREAD_CACHE_TIME,
            LOG_SENDER_THREAD_NAME);
    this.logStorage = logStorage;
    this.logConfiguration = configuration;

    this.wifiSenderPolicy = logConfiguration.getWifiSendPolicy();
    this.mobileSenderPolicy = logConfiguration.getMobileSendPolicy();
    this.context = context.getApplicationContext();
    gson = new Gson();

    asyncInit();
  }

  private void asyncInit() {
    senderExecutor.execute(new Runnable() {
      @Override
      public void run() {
        postUrl = buildPostUrl(logConfiguration.getProfileName());
        headerLine = buildHeaderLine();

      }
    });
  }

  public void triggerSend(boolean isForceRealTime) {
    if (isForceRealTime || checkPolicy()) {
      senderExecutor.execute(new SendLogTask());
    }
  }

  private boolean checkPolicy() {
    if (GlobalConfig.isDebug()) {
      return true;
    }
    if (context == null) {
      return false;
    }
    int networkType = NetworkUtil.getNetworkType();
    SenderPolicyModel policyModel;
    switch (networkType) {
      case NetworkUtil.NETWORK_TYPE_MOBILE:
        policyModel = mobileSenderPolicy;
        break;
      case NetworkUtil.NETWORK_TYPE_WIFI:
        policyModel = wifiSenderPolicy;
        break;
      case NetworkUtil.NETWORK_TYPE_NONE:
        return false;
      default:
        return false;
    }
    switch (policyModel.timePolicy) {
      case NONE:
        return false;
      case REAL_TIME:
        return true;
      case ON_LAUNCH:
        if (isJustLaunch) {
          isJustLaunch = false;
          return true;
        } else {
          return false;
        }
      case SCHEDULE:
        long duration = policyModel.duration;
        long lastSendSuccessTime = context.getSharedPreferences(PREF_NAME,
            Context.MODE_PRIVATE).getLong(PREF_KEY_LAST_SEND_SUCCESS_TIME, 0);
        return (lastSendSuccessTime + duration <= System.currentTimeMillis());
      default:
        return false;
    }
  }

  private String buildPostUrl(String profileName) {
    if (GlobalConfig.isDebug()) {
      return String.format(POST_URL_RAW_DEBUG, profileName);
    } else {
      return String.format(POST_URL_RAW, profileName);
    }
  }

  // TODO: fix impl when upload api ready
  private String buildHeaderLine() {
    Map<String, String> headerParams = logConfiguration.buildHeaderParams(context);
    List<String> headerParamList = new ArrayList<String>();
    for (Map.Entry<String, String> e : headerParams.entrySet()) {
      headerParamList.add(e.getKey() + "=" + e.getValue());
    }
    return TextUtils.join(",", headerParamList) + "\n";
  }


  private class SendLogTask implements Runnable {

    @Override
    public void run() {
      GZIPOutputStream gzipOutputStream = null;
      ByteArrayOutputStream outputStream = null;
      long outputId = -1;
      boolean success = false;
      HttpEntity httpEntity = null;
      InputStream inputstream;
      LogReturnModel logReturnModel = null;
      String responseValue;
      try {
        outputStream = new ByteArrayOutputStream();
        gzipOutputStream = new GZIPOutputStream(outputStream, BUFFER_SIZE);
        gzipOutputStream.write(headerLine.getBytes());
        outputId = logStorage.output(gzipOutputStream);
        gzipOutputStream.finish();
        // TODO: cipher data

        HttpPost post = new HttpPost(postUrl);
        ByteArrayEntity entity = new ByteArrayEntity(outputStream.toByteArray());
        post.setEntity(entity);
        if (httpClient == null) {
          httpClient = new DefaultHttpClient();
        }
        HttpResponse response = httpClient.execute(post);
        httpEntity = response.getEntity();
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {

          if (httpEntity != null) {
            inputstream = httpEntity.getContent();
            Header header = response.getFirstHeader(CONTENT_ENCODING);
            if (header != null && GZIP.equals(header.getValue())) {
              inputstream = new GZIPInputStream(inputstream);
            }
            header = response.getFirstHeader(CONTENT_TYPE);
            if (header != null && GB2312.equals(header.getValue())) {
              responseValue = IOUtils.readString(inputstream, "gb2312");
            } else {
              responseValue = IOUtils.readString(inputstream, HTTP.UTF_8);
            }
            if (!TextUtils.isEmpty(responseValue)) {
              Log.d("LogSender", responseValue);
              logReturnModel =
                  gson.fromJson(responseValue, new TypeToken<LogReturnModel>() {}.getType());
            }
          }

          if (logReturnModel != null && SEND_RESPONSE_OK.equals(logReturnModel.getRet())) {
            success = true;
            SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE).edit();
            editor.putLong(PREF_KEY_LAST_SEND_SUCCESS_TIME, System.currentTimeMillis());
            SharePrefSubmitor.submit(editor);
          }

        }
      } catch (OutOfMemoryError e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (Exception e) {
        // Sometimes when execute this request, server may cause a redirect will null url,
        // so it will throw NullPointerException. Catch Exception here to avoid crash and try
        // to resend later.
        e.printStackTrace();
      } finally {
        IOUtils.close(gzipOutputStream);
        IOUtils.close(outputStream);
        if (outputId > 0) {
          if (success) {
            logStorage.deleteOutput(outputId);
          } else {
            logStorage.restoreOutput(outputId);
          }
        }
        if (httpEntity != null) {
          try {
            httpEntity.consumeContent();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  public static class SenderPolicyModel implements Serializable {
    private TimePolicy timePolicy;
    private long duration;

    public SenderPolicyModel() {}

    /**
     * @param timePolicy the send policy
     * @param duration the duration for send policy, which will only work when the timePolicy is
     *          TimePolicy.SCHEDULE
     */
    public SenderPolicyModel(TimePolicy timePolicy, long duration) {
      this.timePolicy = timePolicy;
      this.duration = duration;
    }
  }

}
