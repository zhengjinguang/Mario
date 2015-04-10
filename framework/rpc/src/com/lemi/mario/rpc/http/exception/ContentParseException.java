package com.lemi.mario.rpc.http.exception;

/**
 * Exception class to show a content parse error.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ContentParseException extends Exception {

  private static final long serialVersionUID = 1397363257477243232L;
  private final String contentString;

  public ContentParseException(String errorMessage, String contentString) {
    super(errorMessage);
    this.contentString = contentString;
  }

  public String getContentString() {
    return contentString;
  }
}
