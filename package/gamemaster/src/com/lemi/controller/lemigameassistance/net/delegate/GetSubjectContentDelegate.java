package com.lemi.controller.lemigameassistance.net.delegate;


import com.lemi.controller.lemigameassistance.model.SubjectContentModel;
import com.lemi.controller.lemigameassistance.net.base.processor.LemiJsonProcessor;
import com.lemi.controller.lemigameassistance.net.request.GetSubjectContentRequestBuilder;
import com.lemi.mario.rpc.http.delegate.GZipHttpDelegate;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetSubjectContentDelegate
    extends GZipHttpDelegate<GetSubjectContentRequestBuilder, SubjectContentModel> {

  public GetSubjectContentDelegate() {
    super(new GetSubjectContentRequestBuilder(), new GetSubjectContentProcessor());
  }


  private static final class GetSubjectContentProcessor
      extends LemiJsonProcessor<SubjectContentModel> {}
}
