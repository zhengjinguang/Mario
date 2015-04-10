package com.lemi.controller.lemigameassistance.net.delegate;


import com.google.gson.reflect.TypeToken;
import com.lemi.controller.lemigameassistance.model.StatisticsCountModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetStatisticsRequestBuilder;
import com.lemi.mario.rpc.http.delegate.CacheableGZipHttpDelegate;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetStatisticsDelegate
    extends CacheableGZipHttpDelegate<GetStatisticsRequestBuilder, StatisticsCountModel> {

  public GetStatisticsDelegate() {
    super(new GetStatisticsRequestBuilder(), new GetStatisticsProcessor());
  }

  @Override
  public TypeToken<StatisticsCountModel> getTypeToken() {
    return new TypeToken<StatisticsCountModel>() {};
  }

  private static final class GetStatisticsProcessor extends LemiJsonProcessor<StatisticsCountModel> {}
}
