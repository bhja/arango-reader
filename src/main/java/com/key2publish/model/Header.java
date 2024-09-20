package com.key2publish.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Header {

  @JsonProperty("name_csv")
  private String nameCsv;
  @JsonProperty("column_csv")
  private String columnCsv;
  @JsonProperty("name_k2e")
  private String nameK2e;

  public Header() {

  }

  public Header(String nameCsv, String columnCsv, String nameK2e) {
    this.nameCsv = nameCsv;
    this.columnCsv = columnCsv;
    this.nameK2e = nameK2e;
  }

  public String getNameCsv() {
    return nameCsv;
  }

  public void setNameCsv(String nameCsv) {
    this.nameCsv = nameCsv;
  }

  public String getColumnCsv() {
    return columnCsv;
  }

  public void setColumnCsv(String columnCsv) {
    this.columnCsv = columnCsv;
  }

  public String getNameK2e() {
    return nameK2e;
  }

  public void setNameK2e(String nameK2e) {
    this.nameK2e = nameK2e;
  }
}
