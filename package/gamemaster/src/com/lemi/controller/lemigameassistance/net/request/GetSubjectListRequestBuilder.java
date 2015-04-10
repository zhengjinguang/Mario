package com.lemi.controller.lemigameassistance.net.request;


import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.net.base.request.GameMasterHttpRequestBuilder;

import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetSubjectListRequestBuilder extends GameMasterHttpRequestBuilder {

  private static final String URL = Constants.API_V3
      + "/subject?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String START_INDEX = "startIndex";
  private static final String SIZE = "size";
  private static final String SUBJECT_FILTER = "subjectFilter";

  private int startIndex;
  private int size;
  private String subjectFilter;


  public GetSubjectListRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public GetSubjectListRequestBuilder setStartIndex(int startIndex) {
    this.startIndex = startIndex;
    return this;
  }

  public GetSubjectListRequestBuilder setSize(int size) {
    this.size = size;
    return this;
  }


  public GetSubjectListRequestBuilder setSubjectFilter(String subjectFilter) {
    this.subjectFilter = subjectFilter;
    return this;
  }

  @Override
  protected String getUrl() {
    return URL;
  }

  @Override
  protected void setContentParams(Map<String, Object> params) {
    super.setContentParams(params);
    params.put(START_INDEX, startIndex);
    params.put(SIZE, size);
    params.put(SUBJECT_FILTER, subjectFilter);
  }

}
