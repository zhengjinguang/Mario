package com.lemi.mario.rpc.http.processor;

import android.text.TextUtils;

import com.lemi.mario.base.utils.IOUtils;
import com.lemi.mario.rpc.http.exception.HttpException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Http processor to parse http response to string, and handle gzip compressing case.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GZipHttpResponseProcessor implements Processor<HttpResponse, String, HttpException> {
  private static final String CONTENT_ENCODING = "Content-Encoding";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String GZIP = "gzip";
  private static final String GB2312 = "text/html; charset=GB2312";

  @Override
  public String process(HttpResponse httpResponse) throws HttpException {
    InputStream inputstream;
    try {
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      switch (statusCode) {
        case 200:
          try {
            inputstream = httpResponse.getEntity().getContent();
            Header header = httpResponse.getFirstHeader(CONTENT_ENCODING);
            if (header != null && GZIP.equals(header.getValue())) {
              inputstream = new GZIPInputStream(inputstream);
            }
            header = httpResponse.getFirstHeader(CONTENT_TYPE);
            if (header != null && GB2312.equals(header.getValue())) {
              return IOUtils.readString(inputstream, "gb2312");
            }
            return IOUtils.readString(inputstream, HTTP.UTF_8);
          } catch (IOException e) {
            throw new HttpException(statusCode, e.getMessage());
          }
        default:
          String message = null;
          try {
            inputstream = httpResponse.getEntity().getContent();
            message = IOUtils.readString(inputstream, HTTP.UTF_8);
          } catch (IOException e) {
            // ignore
          }
          if (TextUtils.isEmpty(message)) {
            message = httpResponse.getStatusLine().toString();
          }
          throw new HttpException(statusCode, message);
      }
    } finally {
      try {
        httpResponse.getEntity().consumeContent();
      } catch (IOException e) {}
    }
  }

}
