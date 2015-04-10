package com.lemi.mario.accountmanager.net.delegate;

import com.google.gson.reflect.TypeToken;
import com.lemi.mario.accountmanager.model.LoginModel;
import com.lemi.mario.accountmanager.net.processor.AccountJsonProcessor;
import com.lemi.mario.accountmanager.net.request.LoginRequestBuilder;
import com.lemi.mario.rpc.http.delegate.CacheableGZipHttpDelegate;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class LoginDelegate extends CacheableGZipHttpDelegate<LoginRequestBuilder, LoginModel> {
  public LoginDelegate() {
    super(new LoginRequestBuilder(), new LoginJsonProcessor());
  }

  @Override
  public TypeToken<LoginModel> getTypeToken() {
    return new TypeToken<LoginModel>() {};
  }

  private static final class LoginJsonProcessor extends AccountJsonProcessor<LoginModel> {}
}
