package com.key2publish.model;

import java.util.Map;

public class
QueryParams {

  private String query;
  private Map<String, Object> options;
  int batchSize;

  public QueryParams() {

  }

  public QueryParams(String query, Map<String, Object> options, int batchSize) {
    this.query = query;
    this.options = options;
    this.batchSize = batchSize;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public Map<String, Object> getOptions() {
    return options;
  }

  public void setOptions(Map<String, Object> options) {
    this.options = options;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }
}
