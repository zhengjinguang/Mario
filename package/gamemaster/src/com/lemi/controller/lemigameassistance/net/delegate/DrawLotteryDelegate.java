package com.lemi.controller.lemigameassistance.net.delegate;

import com.lemi.controller.lemigameassistance.model.DrawLotteryModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.DrawLotteryRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class DrawLotteryDelegate
    extends GZipHttpDelegate<DrawLotteryRequestBuilder, DrawLotteryModel> {

  public DrawLotteryDelegate() {
    super(new DrawLotteryRequestBuilder(), new DrawLotteryProcessor());
  }


  private static final class DrawLotteryProcessor extends LemiJsonProcessor<DrawLotteryModel> {}
}
