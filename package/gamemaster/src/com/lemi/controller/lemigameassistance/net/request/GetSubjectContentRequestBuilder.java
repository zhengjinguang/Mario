package com.lemi.controller.lemigameassistance.net.request;


import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.net.base.request.GameMasterHttpRequestBuilder;

import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetSubjectContentRequestBuilder extends GameMasterHttpRequestBuilder {

  private static final String URL = Constants.API_V3
      + "/get_subject?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String SID = "sid";
  private static final String GAME_FILTER = "gameFilter";
  private static final String SUBJECT_FILTER = "subjectFilter";

  private long sid;
  private String gameFilter;
  private String subjectFilter;


  public GetSubjectContentRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public GetSubjectContentRequestBuilder setSubjectId(long sid) {
    this.sid = sid;
    return this;
  }

  public GetSubjectContentRequestBuilder setSubjectFilter(String subjectFilter) {
    this.subjectFilter = subjectFilter;
    return this;
  }

  public GetSubjectContentRequestBuilder setGameFilter(String gameFilter) {
    this.gameFilter = gameFilter;
    return this;
  }

  @Override
  protected String getUrl() {
    return URL;
  }

  @Override
  protected void setContentParams(Map<String, Object> params) {
    super.setContentParams(params);
    params.put(SID, sid);
    params.put(SUBJECT_FILTER, subjectFilter);
    params.put(GAME_FILTER, gameFilter);
  }

}
