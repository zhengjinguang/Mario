package com.lemi.controller.lemigameassistance.net.base.request;


import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class GameMasterHttpRequestBuilder extends BaseHttpRequestBuilder {


  private static final String CONTENT = "content";
  private static final String AUTHCODE = "authcode";
  private final Gson gson;
  private String auth = null;

  protected GameMasterHttpRequestBuilder() {
    gson = new Gson();
    setAttachDefaultCookie(false);
  }

  public void setAuth(String auth) {
    this.auth = auth;
  }

  @Override
  final protected void setParams(Params params) {
    super.setParams(params);

    String content = getContentParam();
    if (!TextUtils.isEmpty(auth)) {
      params.put(AUTHCODE, auth);
    }
    params.put(CONTENT, content);
  }


  private String getContentParam() {
    String content;
    if (getRequestModel() == null) {
      Map<String, Object> contentParams = new HashMap<String, Object>();
      setContentParams(contentParams);

      JSONObject json = new JSONObject(contentParams);
      content = json.toString();
    } else {
      content = gson.toJson(getRequestModel());
    }
    return content;
  }


  /**
   * Set content params, sub class should override this function
   * 
   * @param params
   */
  protected void setContentParams(Map<String, Object> params) {
    // TODO add some basic model ,such as version
  }



  public Map<String, Object> getRequestParams() {
    Map<String, Object> params = new HashMap<String, Object>();
    String content = getContentParam();

    params.put(CONTENT, content);

    return new HashMap<String, Object>(params);

  }

}
