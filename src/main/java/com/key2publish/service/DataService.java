package com.key2publish.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.key2publish.model.Document;
import com.key2publish.model.DocumentResponse;
import com.key2publish.model.Header;
import com.key2publish.model.HeaderResponse;
import com.key2publish.model.ProgramParams;
import com.key2publish.model.QueryParams;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataService {

  Logger logger = LoggerFactory.getLogger(DataService.class);
  private final ProgramParams params;

  private final ObjectMapper objectMapper;


  public DataService(ProgramParams params) {
    objectMapper = new ObjectMapper();
    this.params = params;


  }

  public void httpCall(String query) {
    try {
      writeToFile(execute(params, query), params.getFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private void writeToFile(List<Document> results, String file) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    logger.info("Data dump to file started");
    FileWriter writer = new FileWriter(file, false);
    writer.write(mapper.writeValueAsString(results));
    writer.flush();
    writer.close();
    logger.info("Data dump to file completed");
  }

  public void importCsv() {
    try {
      HTTPClient client = new HTTPClient(params.getUserName(), params.getPassword());
      HttpRequest request = client.request(
          URI.create(String.format("%s/collection/%s", params.getUrl(), params.getCollection())), "GET",
          BodyPublishers.noBody());
      HttpResponse<String> response = client.execute(request, BodyHandlers.ofString());
      if (response.statusCode() == 404) {
        logger.info("Craating the collection as it does not exist");
        //Create the collection and write the data.
        request = client.request(
            URI.create(String.format("%s/collection", params.getUrl(), params.getCollection())), "POST",
            BodyPublishers.ofString(
                objectMapper.writeValueAsString(Map.of("name", params.getCollection()))));
        response = client.execute(request, BodyHandlers.ofString());
        if (response.statusCode() != 200) {
          logger.error("Could not create the collection " + params.getCollection());
          throw new RuntimeException("Error creating collection " + response.body());
        }
      }
      String csvAsString = new BufferedReader(
          new FileReader(params.getFile(), Charset.forName(params.getEncoding()))).lines()
          .collect(Collectors.joining(System.lineSeparator()));
      CsvSchema csv = CsvSchema.emptySchema().withHeader()
          .withColumnSeparator(params.getColumnSeparator()).withLineSeparator(System.lineSeparator());
      CsvMapper csvMapper = new CsvMapper();

      MappingIterator<Map<String, String>> mappingIterator = csvMapper.reader().forType(Map.class)
          .with(csv).readValues(csvAsString.getBytes());
      List<Map<String, String>> list = mappingIterator.readAll();
      list.stream().map(r -> r.get(params.getUniqueKey())).collect(Collectors.toList());

      if (list.size() == 0) {
        logger.error(
            "Check if the file type and the encoding are set according to the input provided");
        System.exit(1);
      }
      if (!list.get(0).containsKey(params.getUniqueKey())) {
        logger.error("Input file " + params.getFile() + " does not contain the key specified "
            + params.getUniqueKey());
        System.exit(1);
      }

      //Get the key for the code.
      String query = "FOR r IN\n" + params.getCollection()
          + "  RETURN { 'key' : r['_key'],'code':r['code']}";
      //logger.info(query);
      List<Document> documentList = execute(params, query);
      List<Map<String, Object>> create = new ArrayList<>();
      List<Map<String, Object>> patch = new ArrayList<>();
      List<Header> headers = new ArrayList<>();
      if (params.isUseHeader()) {
        headers.addAll(retrieveHeader("FOR c in k2p_header return c.basic"));
      }
      for (int i = 0; i < list.size(); i++) {
        Map<String, Object> object = new HashMap<>();
        Map<String, String> p = list.get(i);
        Document d = documentList.stream().filter(r0 -> p.get(params.getUniqueKey()).equals(r0.getCode()))
            .findFirst().orElse(null);
        Map<String, String> mappedData = headers.size() > 0 ? mapData(headers, p) : p;
        object.put("basic", mappedData);
        object.put("code",
            params.isUseHeader() ? mappedData.get("code") : mappedData.get(params.getUniqueKey()));
        if (d != null) {
          object.put("_key", d.getKey());
          patch.add(object);
        } else {
          create.add(object);
        }
      }
      if (create.size() > 0) {
        logger.info("New record count " + create.size());
        writeToDB(create, params, client, "POST");
      }
      if (patch.size() > 0) {
        logger.info("Update record count " + patch.size());
        writeToDB(patch, params, client, "PATCH");
      }
      logger.info("Completed the merge and insert for the collection " + params.getCollection());
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Error reading the import" + e.getMessage());
      System.exit(1);
    }
  }

  private void writeToDB(List<Map<String, Object>> list, ProgramParams params, HTTPClient client,
      String method) {
    int batch = list.size() > params.getBatchSize() ? params.getBatchSize()
        : list.size(), start = 0, size = list.size();
    try {
      do {
        HttpRequest request = client.request(
            URI.create(
                String.format("%s/document/%s?silent=true", params.getUrl(), params.getCollection())),
            method, BodyPublishers.ofString(
                objectMapper.writeValueAsString(list.subList(start, start + batch))));
        HttpResponse response = client.execute(request, BodyHandlers.ofString());
        if (response.statusCode() == 202) {
          logger.info("Batched document write successful");
        } else {
          logger.error("Issue creating the documents " + response.body());
          System.exit(1);
        }
        start = start + batch;
      } while (start < size);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<Document> execute(ProgramParams params, String query) {
    HTTPClient client = new HTTPClient(params.getUserName(), params.getPassword());
    QueryParams params1 = new QueryParams(query,
        Map.of("options", Map.of("stream", true)), params.getBatchSize());
    try {
      boolean hasMore;
      List<Document> out = new ArrayList<>();
      String url;
      String method;
      DocumentResponse rResponse = null;
      do {
        if (rResponse != null) {
          url = String.format("%s/cursor/%s", params.getUrl(), rResponse.getId());
          method = "PUT";
        } else {
          url = String.format("%s/cursor", params.getUrl());
          method = "POST";
        }

        HttpRequest request = client.request(
            URI.create(url), method,
            BodyPublishers.ofString(objectMapper.writeValueAsString(params1)));
        HttpResponse<String> response = client.execute(request, BodyHandlers.ofString());
        if (response.statusCode() == 201 || response.statusCode() == 200) {
          rResponse = objectMapper.readValue(response.body(),
              DocumentResponse.class);
          hasMore = rResponse.isHasMore();
          out.addAll(rResponse.getResult());

        } else {
          logger.error(
              "Error fetching the data " + response.statusCode() + " error " + response.body());
          break;
        }
      } while (hasMore);
      return out;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, String> mapData(List<Header> headers, Map<String, String> data) {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    HashSet<Header> missingHeader = new HashSet<>();
    for (Header header : headers) {
      String column = data.get(header.getNameCsv());
      if (column != null) {
        map.put(header.getNameK2e(), column);
      } else {
        missingHeader.add(header);
      }
    }
    return map;
  }

  private List<Header> retrieveHeader(String query) {
    HTTPClient client = new HTTPClient(params.getUserName(), params.getPassword());
    QueryParams params1 = new QueryParams(query,
        Map.of("options", Map.of("stream", true)), params.getBatchSize());
    try {
      boolean hasMore;
      List<Header> out = new ArrayList<>();
      String url;
      String method;
      HeaderResponse rResponse = null;
      do {
        if (rResponse != null) {
          url = String.format("%s/cursor/%s", params.getUrl(), rResponse.getId());
          method = "PUT";
        } else {
          url = String.format("%s/cursor", params.getUrl());
          method = "POST";
        }
        HttpRequest request = client.request(
            URI.create(url), method,
            BodyPublishers.ofString(objectMapper.writeValueAsString(params1)));
        HttpResponse<String> response = client.execute(request, BodyHandlers.ofString());
        if (response.statusCode() == 201 || response.statusCode() == 200) {
          rResponse = objectMapper.readValue(response.body(),
              HeaderResponse.class);
          hasMore = rResponse.isHasMore();
          out.addAll(rResponse.getResult());
        } else {
          logger.error(
              "Error fetching the data " + response.statusCode() + " error " + response.body());
          break;
        }
      } while (hasMore);
      Collections.sort(out, Comparator.comparing(Header::getNameK2e));
      return out;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


}
