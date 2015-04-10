package com.lemi.mario.accountmanager.net.processor;

import com.lemi.mario.rpc.http.processor.JsonProcessor;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class AccountJsonProcessor<T> extends JsonProcessor<T> {
  public AccountJsonProcessor() {
    super(AccountGsonFactory.getGson());
  }
}
