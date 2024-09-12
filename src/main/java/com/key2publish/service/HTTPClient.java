package com.key2publish.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

public class HTTPClient {
  public static final int DEFAULT_CONNECT_TIMEOUT = 5;
  public static final int DEFAULT_READ_TIMEOUT = 5;
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String ACCEPT = "Accept";
  private static final String APPLICATION_JSON = "application/json";
  private static final String AUTHORIZATION = "Authorization";
  private static final String BASIC = "Basic ";
  private String userName;
  private String password;
  HttpClient client;
  public HTTPClient(String userName,String password) {
    client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
       .build();
    this.userName = userName;
    this.password = password;

  }

  public HttpRequest request(URI uri, String method, HttpRequest.BodyPublisher bodyPublisher) {
    return request(uri, defaultHeaders(), method, bodyPublisher);
  }

  public HttpRequest request(URI uri, Map<String, String> headers, String method,
      HttpRequest.BodyPublisher bodyPublisher) {
    HttpRequest.Builder request = HttpRequest.newBuilder().uri(URI.create(uri.toString()));
    headers.forEach(request::header);
    request.method(method, bodyPublisher);
    return request.build();
  }

  public HttpResponse<String> execute(HttpRequest request,
      HttpResponse.BodyHandler<String> bodyHandler)
      throws InterruptedException, IOException {
    return client.send(request, bodyHandler);
  }

  private Map<String, String> defaultHeaders() {
    String encoding = Base64.getEncoder().encodeToString((this.userName + ":" +this.password).getBytes());
    return Map.of(AUTHORIZATION, BASIC + encoding, CONTENT_TYPE, APPLICATION_JSON, ACCEPT,
        APPLICATION_JSON);

  }

}
