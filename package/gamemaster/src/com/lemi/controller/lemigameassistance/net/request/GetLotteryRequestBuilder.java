package com.lemi.controller.lemigameassistance.net.request;

import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.net.base.request.GameMasterHttpRequestBuilder;

import java.util.Map;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class GetLotteryRequestBuilder extends GameMasterHttpRequestBuilder {

  private static final String URL = Constants.API_V3
      + "/get_lottery?api_version=" + Constants.LOTTERY_API_VERSION + "&is_test_version="
      + Constants.IS_IN_TEST;

  private static final String LOTTERY_ID = "lotteryId";

  private String lotteryId;


  public GetLotteryRequestBuilder() {
    super();
    setMethod(Method.POST);
  }

  public GetLotteryRequestBuilder setLotteryId(String lotteryId) {
    this.lotteryId = lotteryId;
    return this;
  }


  @Override
  protected String getUrl() {
    return URL;
  }

  @Override
  protected void setContentParams(Map<String, Object> params) {
    super.setContentParams(params);
    params.put(LOTTERY_ID, lotteryId);
  }

}
