package com.lemi.controller.lemigameassistance.net.request;


import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.net.base.request.GameMasterHttpRequestBuilder;

import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetGamesTimeLineRequestBuilder extends GameMasterHttpRequestBuilder {

  private static final String URL = Constants.API_V3
      + "/timeline?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String START_INDEX = "startIndex";
  private static final String SIZE = "size";
  private static final String CATEGORY_ID = "cid";
  private static final String GAME_FILTER = "gameFilter";

  private int startIndex;
  private int size;
  private String categoryId;
  private String gameFilter;


  public GetGamesTimeLineRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public GetGamesTimeLineRequestBuilder setStartIndex(int startIndex) {
    this.startIndex = startIndex;
    return this;
  }

  public GetGamesTimeLineRequestBuilder setSize(int size) {
    this.size = size;
    return this;
  }

  public GetGamesTimeLineRequestBuilder setCategoryId(String category) {
    this.categoryId = category;
    return this;
  }

  public GetGamesTimeLineRequestBuilder setGameFilter(String gameFilter) {
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
    params.put(START_INDEX, startIndex);
    params.put(SIZE, size);
    params.put(CATEGORY_ID, categoryId);
    params.put(GAME_FILTER, gameFilter);
  }

}
