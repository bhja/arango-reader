package com.key2publish.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgramParams {

  String host;
  String database;
  String userName;
  String password;
  int port;
  String file;
  String url;
  String action;
  String collection;
  String uniqueKey;
  int batchSize;
  char
      columnSeparator;
  String encoding;
  boolean useHeader;

  public ProgramParams() {

  }


  public ProgramParams(String host, String database, String userName, String password, int port,
      String file, String url, String action, String collection, String uniqueKey, int batchSize,
      char columnSeparator, String encoding, boolean useHeader) {
    this.host = host;
    this.database = database;
    this.userName = userName;
    this.password = password;
    this.port = port;
    this.file = file;
    this.url = url;
    this.action = action;
    this.collection = collection;
    this.uniqueKey = uniqueKey;
    this.batchSize = batchSize;
    this.columnSeparator = columnSeparator;
    this.encoding = encoding;
    this.useHeader = useHeader;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public String getUniqueKey() {
    return uniqueKey;
  }

  public void setUniqueKey(String uniqueKey) {
    this.uniqueKey = uniqueKey;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public char getColumnSeparator() {
    return columnSeparator;
  }

  public void setColumnSeparator(char columnSeparator) {
    this.columnSeparator = columnSeparator;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public boolean isUseHeader() {
    return useHeader;
  }

  public void setUseHeader(boolean useHeader) {
    this.useHeader = useHeader;
  }
}
