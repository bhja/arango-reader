package com.key2publish.service;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import com.arangodb.model.AqlQueryOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.key2publish.model.Document;
import com.key2publish.model.DocumentResponse;
import com.key2publish.model.ProgramParams;
import com.key2publish.model.QueryParams;
import io.vertx.core.http.HttpMethod;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataService {
  Logger logger = LoggerFactory.getLogger(DataService.class);
  private ArangoDB arangoDB;
  private String database;
  private ObjectMapper objectMapper ;

  public DataService(ProgramParams params){
    this.database = params.database();
    objectMapper = new ObjectMapper();
  }

  public void retrieveData(ProgramParams params){
    try{
      arangoDB = new ArangoDB.Builder().protocol(params.protocol()!=null ? params.protocol():Protocol.HTTP_JSON).user(params.userName()).host(params.host(),params.port()).password(
          params.password()).build();
      List<Document> documentList = new ArrayList<>();
      String query = "FOR c IN k2p_product RETURN {'key': c._key , 'code':c.code}";
      AqlQueryOptions options = new AqlQueryOptions().batchSize(10000).stream(true).cache(false);
      ArangoCursor<Document> cursor = arangoDB.db(database!=null?database:"_system").query(query,
          Document.class,options);
      while (cursor.hasNext()){
        documentList.add(cursor.next());
      }
      writeToFile(documentList, params.file());

    }catch (Exception e){
      System.err.println("Error writing to the file due to" + e.getMessage());
    }finally {
      arangoDB.shutdown();
    }
  }

  public void shutDown(){
    arangoDB.shutdown();
  }


  public void httpCall(ProgramParams params){
    System.out.println(params);
    HTTPClient client = new HTTPClient(params.userName(), params.password());
    QueryParams params1 = new QueryParams("FOR c IN k2p_product RETURN {key:c._key , code:c.code}",
        Map.of("options",Map.of("stream",true)),10000);
    try {
      boolean hasMore;
      List<Document> documentList = new ArrayList<>();
      String url ;
      String method ;
      DocumentResponse rResponse = null;
      do {
        if(rResponse!=null){
          url = String.format("%s/%s",params.url(),rResponse.id());
          method = HttpMethod.PUT.name();
        }else{
          url = params.url();
          method = HttpMethod.POST.name();
        }

        HttpRequest request = client.request(
            URI.create(url), method,
            BodyPublishers.ofString(objectMapper.writeValueAsString(params1)));
        HttpResponse<String> response = client.execute(request, BodyHandlers.ofString());
        if(response.statusCode()==201 || response.statusCode() == 200) {
          rResponse = objectMapper.readValue(response.body(),
              DocumentResponse.class);
          hasMore = rResponse.hasMore();
          documentList.addAll(rResponse.result());
        }else{
        System.err.println("Error fetching the data "  + response.statusCode() + " error " + response.body());
        break;
        }
      }while(hasMore);
      writeToFile(documentList, params.file());
    }catch (JsonProcessingException e){
      System.err.println("Error " + e.getMessage());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


   private void writeToFile(List<Document> results,String file) throws IOException{
     ObjectMapper mapper = new ObjectMapper();
     logger.info("Data dump to file started");
     FileWriter writer = new FileWriter(file,false);
     writer.write(mapper.writeValueAsString(results));
     writer.flush();
     writer.close();
     logger.info("Data dump to file completed");
   }
}
