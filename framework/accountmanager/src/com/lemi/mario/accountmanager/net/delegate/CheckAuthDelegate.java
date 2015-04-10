package com.lemi.mario.accountmanager.net.delegate;

import com.lemi.mario.accountmanager.model.CheckAuthModel;
import com.lemi.mario.accountmanager.net.processor.AccountJsonProcessor;
import com.lemi.mario.accountmanager.net.request.CheckAuthRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class CheckAuthDelegate extends GZipHttpDelegate<CheckAuthRequestBuilder, CheckAuthModel> {
  public CheckAuthDelegate() {
    super(new CheckAuthRequestBuilder(), new CheckAuthcodeJsonProcessor());
  }

  private static final class CheckAuthcodeJsonProcessor
      extends AccountJsonProcessor<CheckAuthModel> {}
}
