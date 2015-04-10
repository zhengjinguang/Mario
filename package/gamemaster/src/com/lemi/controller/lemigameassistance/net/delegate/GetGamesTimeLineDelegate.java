package com.lemi.controller.lemigameassistance.net.delegate;


import com.google.gson.reflect.TypeToken;
import com.lemi.controller.lemigameassistance.model.GamesTimeLineModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetGamesTimeLineRequestBuilder;
import com.lemi.mario.rpc.http.delegate.CacheableGZipHttpDelegate;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetGamesTimeLineDelegate
    extends CacheableGZipHttpDelegate<GetGamesTimeLineRequestBuilder, GamesTimeLineModel> {

  public GetGamesTimeLineDelegate() {
    super(new GetGamesTimeLineRequestBuilder(), new GetGamesTimeLineProcessor());
  }

  @Override
  public TypeToken<GamesTimeLineModel> getTypeToken() {
    return new TypeToken<GamesTimeLineModel>() {};
  }


  private static final class GetGamesTimeLineProcessor
      extends LemiJsonProcessor<GamesTimeLineModel> {}
}
