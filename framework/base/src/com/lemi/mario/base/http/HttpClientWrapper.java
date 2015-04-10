package com.lemi.mario.base.http;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class HttpClientWrapper implements HttpClient {

  private HttpClient httpClient;

  public HttpClientWrapper(HttpClient client) {
    setWrappedHttpClient(client);
  }

  public void setWrappedHttpClient(HttpClient client) {
    this.httpClient = client;
  }

  public static HttpClientWrapper newInstance(HttpClient client) {
    return new HttpClientWrapper(client);
  }

  @Override
  public HttpResponse execute(HttpUriRequest request) throws IOException {
    return execute(request, (HttpContext) null);
  }

  @Override
  public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
    if (request == null) {
      throw new IllegalArgumentException("Request must not be null.");
    }

    return execute(determineTarget(request), request, context);
  }

  private HttpHost determineTarget(HttpUriRequest request) {
    // A null target may be acceptable if there is a default target.
    // Otherwise, the null target is detected in the director.
    HttpHost target = null;

    URI requestURI = request.getURI();
    if (requestURI.isAbsolute()) {
      target = new HttpHost(
          requestURI.getHost(),
          requestURI.getPort(),
          requestURI.getScheme());
    }
    return target;
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
    return execute(target, request, (HttpContext) null);
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
      throws IOException {
    return httpClient.execute(target, request, context);
  }

  @Override
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> handler)
      throws IOException {
    return execute(request, handler, null);
  }

  @Override
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> handler,
      HttpContext context)
      throws IOException {
    HttpHost target = determineTarget(request);
    return execute(target, request, handler, context);
  }

  @Override
  public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> handler)
      throws IOException {
    return execute(target, request, handler, null);
  }

  @Override
  public <T> T execute(HttpHost target, HttpRequest request,
      final ResponseHandler<? extends T> handler,
      HttpContext context) throws IOException {
    return httpClient.execute(target, request, handler, context);
  }

  @Override
  public ClientConnectionManager getConnectionManager() {
    return httpClient.getConnectionManager();
  }

  @Override
  public HttpParams getParams() {
    return httpClient.getParams();
  }
}
