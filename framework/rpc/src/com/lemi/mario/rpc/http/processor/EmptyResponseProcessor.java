package com.lemi.mario.rpc.http.processor;


import com.lemi.mario.rpc.http.exception.HttpException;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Processor who just returns success or fail info.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class EmptyResponseProcessor implements Processor<HttpResponse, Void, ExecutionException> {

  @Override
  public Void process(HttpResponse httpResponse) throws ExecutionException {
    try {
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      switch (statusCode) {
        case 200:
          return null;
        default:
          throw new ExecutionException(
              new HttpException(statusCode, httpResponse.getStatusLine().toString()));
      }
    } finally {
      try {
        httpResponse.getEntity().consumeContent();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
