package com.lemi.controller.lemigameassistance.net.delegate;


import com.lemi.controller.lemigameassistance.model.StartupModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetStartupRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetStartupDelegate
    extends GZipHttpDelegate<GetStartupRequestBuilder, StartupModel> {

  public GetStartupDelegate() {
    super(new GetStartupRequestBuilder(), new GetStartupProcessor());
  }

  private static final class GetStartupProcessor
      extends LemiJsonProcessor<StartupModel> {}
}
