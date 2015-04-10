package com.lemi.controller.lemigameassistance.net.delegate;


import com.lemi.controller.lemigameassistance.model.StatisticsCountModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.SetStatisticsRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SetStatisticsDelegate
    extends GZipHttpDelegate<SetStatisticsRequestBuilder, StatisticsCountModel> {

  public SetStatisticsDelegate() {
    super(new SetStatisticsRequestBuilder(), new SetStatisticsProcessor());
  }

  private static final class SetStatisticsProcessor extends LemiJsonProcessor<StatisticsCountModel> {}
}
