package com.key2publish.model;

import com.arangodb.Protocol;


public record ProgramParams(String host, String database, String userName, String password, int port, String file,
                            Protocol protocol,String url) {



}
