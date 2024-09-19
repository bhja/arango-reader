package com.key2publish.service;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.key2publish.model.Document;
import com.key2publish.model.DocumentResponse;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataService {
  Logger logger = LoggerFactory.getLogger(DataService.class);

  private ObjectMapper objectMapper ;

  public DataService(){
    objectMapper = new ObjectMapper();
  }

  public void httpCall(ProgramParams params,String query){

    try {
      writeToFile(execute(params, query), params.file());
    }catch(IOException e){
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

   public void importCsv(ProgramParams params) {
     try{
         HTTPClient client = new HTTPClient(params.userName(), params.password());
         HttpRequest request = client.request(
           URI.create(String.format("%s/collection/%s",params.url(),params.collection())), "GET",BodyPublishers.noBody());
         HttpResponse<String> response = client.execute(request, BodyHandlers.ofString());
        if(response.statusCode() == 404){
          logger.info("Craating the collection as it does not exist");
          //Create the collection and write the data.
           request = client.request(
              URI.create(String.format("%s/collection",params.url(),params.collection())), "POST",BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of("name",params.collection()))));
           response = client.execute(request, BodyHandlers.ofString());
           if(response.statusCode() != 200){
              logger.error("Could not create the collection " + params.collection());
              throw new RuntimeException("Error creating collection " + response.body());
           }
        }
       String csvAsString = new BufferedReader(new FileReader(params.file(), Charset.forName(params.encoding()))).lines().collect(Collectors.joining(System.lineSeparator()));
       CsvSchema csv = CsvSchema.emptySchema().withHeader().withColumnSeparator(params.columnSeparator()).withLineSeparator(System.lineSeparator());
       CsvMapper csvMapper = new CsvMapper();
       MappingIterator<Map<String, String>> mappingIterator =  csvMapper.reader().forType(Map.class).with(csv).readValues(csvAsString.getBytes());
         List<Map<String, String>> list = mappingIterator.readAll();
         list.stream().map(r->r.get(params.uniqueKey())).collect(Collectors.toList());
         if(list.size()==0){
           logger.error("Check if the file type and the encoding are set according to the input provided");
           System.exit(1);
         }
        if(!list.get(0).containsKey(params.uniqueKey())){
          logger.error("Input file " + params.file()+" does not contain the key specified " + params.uniqueKey());
            System.exit(1);
       }
         //Get the key for the code.
         String query = "FOR r IN\n" + params.collection()
             + "  RETURN { 'key' : r['_key'],'code':r['code']}";
         //logger.info(query);
         List<Document> documentList = execute(params, query);
         JSONArray patch = new JSONArray();
         JSONArray create = new JSONArray();
         for (int i = 0; i < list.size(); i++) {
           JSONObject object = new JSONObject();
           Map<String, String> p = list.get(i);
           Document d = documentList.stream().filter(r0 -> p.get(params.uniqueKey()).equals(r0.code()))
                 .findFirst().orElse(null);
           object.put("basic",p);
           object.put("code",p.get(params.uniqueKey()));
             if (d != null) {
               object.put("_key",d.key());
               patch.put(object);
             } else {
               create.put(object);
             }
         }
         if (create.length() > 0) {
           logger.info("New record count " + create.length());
           writeToDB(create, params, client, "POST");
         }
         if (patch.length() > 0) {
           logger.info("Update record count " + patch.length());
           writeToDB(patch, params, client, "PATCH");
         }
       logger.info("Completed the merge and insert for the collection " + params.collection() );
     }catch (Exception e){
       e.printStackTrace();
       logger.error("Error reading the import" + e.getMessage());
       System.exit(1);
     }
   }

     private void writeToDB(JSONArray array, ProgramParams params, HTTPClient client,String method){
     List<Object> list = array.toList();
     int batch = list.size() > params.batchSize() ? params.batchSize()
         : list.size(), start = 0, size = list.size();
     try {
       do {
         HttpRequest request = client.request(
             URI.create(
                 String.format("%s/document/%s", params.url(), params.collection())),
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

     }catch (Exception e){
       throw new RuntimeException(e);
     }
   }

   private List<Document> execute(ProgramParams params,String query){
     HTTPClient client = new HTTPClient(params.userName(), params.password());
     QueryParams params1 = new QueryParams(query,
         Map.of("options",Map.of("stream",true)), params.batchSize());
     try {
       boolean hasMore;
       List<Document> documentList = new ArrayList<>();
       String url ;
       String method ;
       DocumentResponse rResponse = null;
       do {
         if(rResponse!=null){
           url = String.format("%s/cursor/%s",params.url(),rResponse.id());
           method = "PUT";
         }else{
           url = String.format("%s/cursor",params.url());
           method = "POST";
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
           logger.error("Error fetching the data "  + response.statusCode() + " error " + response.body());
           break;
         }
       }while(hasMore);
       return documentList;
     }
       catch (JsonProcessingException e){
         throw new RuntimeException(e);
       } catch (IOException e) {
         throw new RuntimeException(e);
       } catch (InterruptedException e) {
         throw new RuntimeException(e);
       }
   }


}
