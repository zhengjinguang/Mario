package com.lemi.controller.lemigameassistance.net.filter;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public enum StartupOptionFields {

  ALL("sid,name,startTime,stopTime,description,posters"),

  RECOMMEND_LITE("sid,name,startTime,stopTime,posters");



  private final String optionFields;

  private StartupOptionFields(String optionFields) {
    this.optionFields = optionFields;
  }

  public String getOptionFields() {
    return optionFields;
  }


}
