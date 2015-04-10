package com.lemi.controller.lemigameassistance.net.delegate;

import com.lemi.controller.lemigameassistance.model.GetLotteryModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetLotteryRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class GetLotteryDelegate
    extends GZipHttpDelegate<GetLotteryRequestBuilder, GetLotteryModel> {

  public GetLotteryDelegate() {
    super(new GetLotteryRequestBuilder(), new GetLotteryProcessor());
  }

  private static final class GetLotteryProcessor extends LemiJsonProcessor<GetLotteryModel> {}
}
