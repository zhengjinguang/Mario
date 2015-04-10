package com.lemi.controller.lemigameassistance.net.delegate;


import com.google.gson.reflect.TypeToken;
import com.lemi.controller.lemigameassistance.model.RecommendsModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetRecommendRequestBuilder;
import com.lemi.mario.rpc.http.delegate.CacheableGZipHttpDelegate;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetRecommendDelegate
    extends CacheableGZipHttpDelegate<GetRecommendRequestBuilder, RecommendsModel> {

  public GetRecommendDelegate() {
    super(new GetRecommendRequestBuilder(), new GetRecommendProcessor());
  }

  @Override
  public TypeToken<RecommendsModel> getTypeToken() {
    return new TypeToken<RecommendsModel>() {};
  }


  private static final class GetRecommendProcessor
      extends LemiJsonProcessor<RecommendsModel> {}
}
