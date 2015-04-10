package com.lemi.controller.lemigameassistance.net.request;


import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.model.StatisticsCountModel;
import com.lemi.controller.lemigameassistance.net.base.request.GameMasterHttpRequestBuilder;

import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SetStatisticsRequestBuilder extends GameMasterHttpRequestBuilder {

  private static final String URL = Constants.API_V3
      + "/inc_game_count?api_version=" + Constants.HTTP_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String PACKAGE_NAME = "packageName";
  private static final String TYPE = "type";

  private String packageName;
  private String type;

  public SetStatisticsRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public SetStatisticsRequestBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public SetStatisticsRequestBuilder setType(StatisticsCountModel.StatisticsType type) {
    if (type != null) {
      this.type = type.toString();
    }
    return this;
  }

  @Override
  protected String getUrl() {
    return URL;
  }

  @Override
  protected void setContentParams(Map<String, Object> params) {
    super.setContentParams(params);
    params.put(PACKAGE_NAME, packageName);
    params.put(TYPE, type);
  }

}
