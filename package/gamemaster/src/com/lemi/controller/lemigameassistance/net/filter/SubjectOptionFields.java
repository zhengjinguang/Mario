package com.lemi.controller.lemigameassistance.net.filter;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public enum SubjectOptionFields {

  ALL("sid,name,iconUrl,description,posters"),


  SUBJECT_LITE("sid,name,iconUrl");



  private final String optionFields;

  private SubjectOptionFields(String optionFields) {
    this.optionFields = optionFields;
  }

  public String getOptionFields() {
    return optionFields;
  }


}
