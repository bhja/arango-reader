package com.key2publish.model;

import java.util.Map;

public record QueryParams(String query, Map<String,Object> options,int batchSize) {

}
