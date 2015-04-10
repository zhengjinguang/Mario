package com.lemi.controller.lemigameassistance.net.filter;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public enum GameOptionFields {

  ALL(
      "gid,name,apkUrl,onlineTime,apkSize,realSize,downloadCount,category,playMode,operationMode,personMode,author,versionName,star,language,packageName,notice,description,images,posters,iconUrl"),

  CATEGORY_LITE("name,downloadCount,operationMode,version,packageName,iconUrl"),

  RECOMMEND_LITE("recommend,packageName,posters"),

  PACKAGE_LITE("packageName");

  private final String optionFields;

  private GameOptionFields(String optionFields) {
    this.optionFields = optionFields;
  }

  public String getOptionFields() {
    return optionFields;
  }

}
