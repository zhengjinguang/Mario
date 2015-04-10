package com.lemi.controller.lemigameassistance.net.delegate;


import com.lemi.controller.lemigameassistance.model.CheckVersionModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.CheckVersionRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CheckVersionDelegate
    extends GZipHttpDelegate<CheckVersionRequestBuilder, CheckVersionModel> {

  public CheckVersionDelegate() {
    super(new CheckVersionRequestBuilder(), new CheckVersionProcessor());
  }


  private static final class CheckVersionProcessor extends LemiJsonProcessor<CheckVersionModel> {}
}
