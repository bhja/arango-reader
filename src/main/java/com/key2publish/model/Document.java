package com.key2publish.model;

public class Document {

  private String key;
  private String code;


  public Document(String key, String code) {
    this.key = key;
    this.code = code;
  }

  public Document() {

  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

}
