package com.key2publish.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentResponse {

  List<Document> result;
  boolean hasMore;
  String id;
  Map<String, Object> extra;
  boolean cached;
  String nextBatchId;
  boolean error;
  int code;

  public DocumentResponse() {

  }

  public List<Document> getResult() {
    return result;
  }

  public void setResult(List<Document> result) {
    this.result = result;
  }

  public boolean isHasMore() {
    return hasMore;
  }

  public void setHasMore(boolean hasMore) {
    this.hasMore = hasMore;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, Object> getExtra() {
    return extra;
  }

  public void setExtra(Map<String, Object> extra) {
    this.extra = extra;
  }

  public boolean isCached() {
    return cached;
  }

  public void setCached(boolean cached) {
    this.cached = cached;
  }

  public String getNextBatchId() {
    return nextBatchId;
  }

  public void setNextBatchId(String nextBatchId) {
    this.nextBatchId = nextBatchId;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }
}
