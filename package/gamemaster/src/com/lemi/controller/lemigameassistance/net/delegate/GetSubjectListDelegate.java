package com.lemi.controller.lemigameassistance.net.delegate;


import com.google.gson.reflect.TypeToken;
import com.lemi.controller.lemigameassistance.model.SubjectListModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetSubjectListRequestBuilder;
import com.lemi.mario.rpc.http.delegate.CacheableGZipHttpDelegate;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetSubjectListDelegate
    extends CacheableGZipHttpDelegate<GetSubjectListRequestBuilder, SubjectListModel> {

  public GetSubjectListDelegate() {
    super(new GetSubjectListRequestBuilder(), new GetSubjectListProcessor());
  }

  @Override
  public TypeToken<SubjectListModel> getTypeToken() {
    return new TypeToken<SubjectListModel>() {};
  }


  private static final class GetSubjectListProcessor
      extends LemiJsonProcessor<SubjectListModel> {}
}
