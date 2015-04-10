package com.lemi.controller.lemigameassistance.net.request;


import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.net.base.request.GameMasterHttpRequestBuilder;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GetGamesByPackageNameRequestBuilder extends GameMasterHttpRequestBuilder {

  private static final String URL = Constants.API_V3
      + "/game_by_packagenames?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String PACKAGE_NAME_LIST = "packageNames";
  private static final String GAME_FILTER = "gameFilter";

  private List<String> packageNames;
  private String gameFilter;


  public GetGamesByPackageNameRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public GetGamesByPackageNameRequestBuilder setPackageName(String packageName) {
    List<String> packageList = new ArrayList<String>();
    packageList.add(packageName);
    this.packageNames = packageList;
    return this;
  }

  public GetGamesByPackageNameRequestBuilder setPackageNames(List<String> packageNames) {
    this.packageNames = packageNames;
    return this;
  }

  public GetGamesByPackageNameRequestBuilder setGameFilter(String gameFilter) {
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
    params.put(PACKAGE_NAME_LIST, new JSONArray(packageNames));
    params.put(GAME_FILTER, gameFilter);
  }

}
