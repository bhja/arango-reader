package com.key2publish.model;

import java.util.List;
import java.util.Map;

public record DocumentResponse(List<Document> result,boolean hasMore,String id, Map<String, Object> extra,boolean cached,String nextBatchId,boolean error,int code) {




}
