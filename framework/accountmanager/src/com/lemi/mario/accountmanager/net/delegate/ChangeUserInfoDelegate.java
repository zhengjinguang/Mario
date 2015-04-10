package com.lemi.mario.accountmanager.net.delegate;

import com.lemi.mario.accountmanager.model.ChangeUserInfoModel;
import com.lemi.mario.accountmanager.net.processor.AccountJsonProcessor;
import com.lemi.mario.accountmanager.net.request.ChangeUserInfoRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class ChangeUserInfoDelegate
    extends GZipHttpDelegate<ChangeUserInfoRequestBuilder, ChangeUserInfoModel> {
  public ChangeUserInfoDelegate() {
    super(new ChangeUserInfoRequestBuilder(), new ChangeUserInfoJsonProcessor());
  }

  private static final class ChangeUserInfoJsonProcessor
      extends AccountJsonProcessor<ChangeUserInfoModel> {}
}
