package com.lemi.controller.lemigameassistance.net.delegate;


import com.google.gson.reflect.TypeToken;
import com.lemi.controller.lemigameassistance.model.CategoryListModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetCategoryListRequestBuilder;
import com.lemi.mario.rpc.http.delegate.CacheableGZipHttpDelegate;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetCategoryDelegate
    extends CacheableGZipHttpDelegate<GetCategoryListRequestBuilder, CategoryListModel> {

  public GetCategoryDelegate() {
    super(new GetCategoryListRequestBuilder(), new GetCategoryProcessor());
  }

  @Override
  public TypeToken<CategoryListModel> getTypeToken() {
    return new TypeToken<CategoryListModel>() {};
  }


  private static final class GetCategoryProcessor extends LemiJsonProcessor<CategoryListModel> {}
}
