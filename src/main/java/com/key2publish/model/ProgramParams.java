package com.key2publish.model;

public record ProgramParams(String host, String database, String userName, String password,int port, String file,String url,String action,String collection, String uniqueKey,int batchSize,char
                            columnSeparator,String encoding) {

}
