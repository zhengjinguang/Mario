package com.lemi.mario.accountmanager.net.request;


import com.google.gson.Gson;
import com.lemi.mario.accountmanager.MarioAccountManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public abstract class AccountHttpRequestBuilder extends BaseHttpRequestBuilder {


  private static final String CONTENT = "content";
  private static final String AUTHCODE = "authcode";
  private final Gson gson;
  private boolean addAuthCode = false;

  protected AccountHttpRequestBuilder() {
    gson = new Gson();
  }


  @Override
  final protected void setParams(Params params) {
    super.setParams(params);

    String content = getContentParam();
    if (addAuthCode) {
      params.put(AUTHCODE, MarioAccountManager.getInstance().getAuth());
    }
    params.put(CONTENT, content);
  }

  public void addAuth() {
    addAuthCode = true;
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
    if (addAuthCode) {
      params.put(AUTHCODE, MarioAccountManager.getInstance().getAuth());
    }
    return new HashMap<String, Object>(params);

  }

}
