package com.lemi.controller.lemigameassistance.net.base.processor;


import com.lemi.mario.rpc.http.processor.JsonProcessor;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class LemiJsonProcessor<T> extends JsonProcessor<T> {
  public LemiJsonProcessor() {
    super(LemiGsonFactory.getGson());
  }
}
