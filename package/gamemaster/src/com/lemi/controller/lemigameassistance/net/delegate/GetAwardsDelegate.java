package com.lemi.controller.lemigameassistance.net.delegate;

import com.lemi.controller.lemigameassistance.model.AwardListModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetAwardsRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class GetAwardsDelegate
    extends GZipHttpDelegate<GetAwardsRequestBuilder, AwardListModel> {

  public GetAwardsDelegate() {
    super(new GetAwardsRequestBuilder(), new GetAwardsProcessor());
  }


  private static final class GetAwardsProcessor extends LemiJsonProcessor<AwardListModel> {}
}
