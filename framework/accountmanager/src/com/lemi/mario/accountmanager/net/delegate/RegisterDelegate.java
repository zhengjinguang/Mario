package com.lemi.mario.accountmanager.net.delegate;

import com.lemi.mario.accountmanager.model.RegisterModel;
import com.lemi.mario.accountmanager.net.processor.AccountJsonProcessor;
import com.lemi.mario.accountmanager.net.request.RegisterRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class RegisterDelegate extends GZipHttpDelegate<RegisterRequestBuilder, RegisterModel> {
  public RegisterDelegate() {
    super(new RegisterRequestBuilder(), new RegisterProcessor());
  }

  private static final class RegisterProcessor extends AccountJsonProcessor<RegisterModel> {}
}
