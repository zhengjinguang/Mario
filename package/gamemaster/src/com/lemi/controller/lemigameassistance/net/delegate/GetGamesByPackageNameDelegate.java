package com.lemi.controller.lemigameassistance.net.delegate;


import com.google.gson.reflect.TypeToken;
import com.lemi.controller.lemigameassistance.model.GameListModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetGamesByPackageNameRequestBuilder;
import com.lemi.mario.rpc.http.delegate.CacheableGZipHttpDelegate;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetGamesByPackageNameDelegate
    extends CacheableGZipHttpDelegate<GetGamesByPackageNameRequestBuilder, GameListModel> {

  public GetGamesByPackageNameDelegate() {
    super(new GetGamesByPackageNameRequestBuilder(), new GetGamesByPackageNameProcessor());
  }

  @Override
  public TypeToken<GameListModel> getTypeToken() {
    return new TypeToken<GameListModel>() {};
  }


  private static final class GetGamesByPackageNameProcessor
      extends LemiJsonProcessor<GameListModel> {}
}
