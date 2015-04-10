package com.lemi.controller.lemigameassistance.net.delegate;

import com.lemi.controller.lemigameassistance.model.OtherAwardListModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetOtherAwardsRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class GetOtherAwardsDelegate
    extends GZipHttpDelegate<GetOtherAwardsRequestBuilder, OtherAwardListModel> {

  public GetOtherAwardsDelegate() {
    super(new GetOtherAwardsRequestBuilder(), new GetOtherAwardsProcessor());
  }


  private static final class GetOtherAwardsProcessor extends LemiJsonProcessor<OtherAwardListModel> {}
}
