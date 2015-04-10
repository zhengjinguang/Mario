package com.lemi.mario.accountmanager.net;

import android.util.Log;

import com.lemi.mario.accountmanager.config.Constants;
import com.lemi.mario.accountmanager.model.ChangeUserInfoModel;
import com.lemi.mario.accountmanager.model.CheckAuthModel;
import com.lemi.mario.accountmanager.model.LoginModel;
import com.lemi.mario.accountmanager.model.RegisterModel;
import com.lemi.mario.accountmanager.model.User;
import com.lemi.mario.accountmanager.model.base.BaseErrorModel;
import com.lemi.mario.accountmanager.net.delegate.ChangeUserInfoDelegate;
import com.lemi.mario.accountmanager.net.delegate.CheckAuthDelegate;
import com.lemi.mario.accountmanager.net.delegate.LoginDelegate;
import com.lemi.mario.accountmanager.net.delegate.RegisterDelegate;
import com.lemi.mario.accountmanager.net.filter.UserFilter;
import com.lemi.mario.accountmanager.net.request.AccountHttpRequestBuilder;
import com.lemi.mario.rpc.http.client.DataApi;
import com.lemi.mario.rpc.http.client.DataClient;
import com.lemi.mario.rpc.http.client.DataClientCache;
import com.lemi.mario.rpc.http.delegate.ApiDelegate;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class AccountHttpHelper {
  public static final int VALID_RETURN = 0;
  private static final String DATA_CACHE_FOLDER = "DataCache";
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
      ApiDelegate<T, E> delegate, AccountHttpRequestBuilder requestBuilder)
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

  public static RegisterModel register(String udid) {
    RegisterDelegate delegate = new RegisterDelegate();
    delegate.getRequestBuilder().setUdid(udid);
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static LoginModel login(String udid) {
    LoginDelegate delegate = new LoginDelegate();
    delegate.getRequestBuilder().setUdid(udid).setUserFilter(UserFilter.ALL.getFilter());
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static CheckAuthModel checkAuth() {
    CheckAuthDelegate delegate = new CheckAuthDelegate();
    delegate.getRequestBuilder()
        .setUserFilter(UserFilter.ALL.getFilter()).addAuth();
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static ChangeUserInfoModel changeUserInfo(User user) {
    ChangeUserInfoDelegate delegate = new ChangeUserInfoDelegate();
    delegate.getRequestBuilder().setUser(user).addAuth();
    try {
      return doExecute(delegate, delegate.getRequestBuilder());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static void logI(String message) {
    Log.i("AccountHttpHelper", message);
  }

}
