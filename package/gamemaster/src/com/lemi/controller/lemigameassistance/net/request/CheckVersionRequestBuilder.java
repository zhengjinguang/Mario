package com.lemi.controller.lemigameassistance.net.request;


import android.text.TextUtils;
import android.util.Log;

import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.net.base.request.GameMasterHttpRequestBuilder;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.SystemUtil;

import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CheckVersionRequestBuilder extends GameMasterHttpRequestBuilder {

  private static final String URL = Constants.API_V3
      + "/check_version?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String VERSION_CODE = "versionCode";
  private static final String CHANNEL = "channel";

  private int versionCode;
  private String channel;


  public CheckVersionRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public CheckVersionRequestBuilder setVersionCode(int versionCode) {
    this.versionCode = versionCode;
    return this;
  }


  @Override
  protected String getUrl() {
    return URL;
  }

  @Override
  protected void setContentParams(Map<String, Object> params) {
    super.setContentParams(params);
    params.put(VERSION_CODE, versionCode);

    channel = SystemUtil.getMetaChannel(GlobalConfig.getAppContext());
    Log.i("CheckVersionRequestBuilder", "channel: " + channel + "  versionCode:" + versionCode);
    if (!TextUtils.isEmpty(channel)) {
      params.put(CHANNEL, channel);
    }
  }

}
