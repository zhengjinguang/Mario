package com.lemi.mario.rpc.http.processor;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.lemi.mario.rpc.http.exception.ContentParseException;

import java.lang.reflect.ParameterizedType;

/**
 * Json parser to parse string to java object.
 * 
 * <p>
 * Usage: define a processor which extends this base class, and the generic type T can be inferred
 * automatically.
 * 
 * <pre>
 * public class AppSearchProcessor extends JsonProcessor&lt;AppSearchResult&gt; {}
 * </pre>
 * 
 * </p>
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 * @param <T> java object type
 */
public class JsonProcessor<T> implements Processor<String, T, ContentParseException> {
  protected final Gson gson;

  public JsonProcessor(Gson gson) {
    this.gson = gson;
  }

  @Override
  public T process(String input) throws ContentParseException {
    @SuppressWarnings("unchecked")
    Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
    try {
      return gson.fromJson(input, entityClass);
    } catch (JsonSyntaxException e) {
      throw new ContentParseException(e.getMessage(), input);
    }
  }

}
