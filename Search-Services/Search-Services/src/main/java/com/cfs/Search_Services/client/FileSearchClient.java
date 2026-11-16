package com.cfs.Search_Services.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "file-service",
        url = "http://localhost:8081",
fallbackFactory = FileSearchClientFallbackFactory.class
) // we are enabling circuit breaker through feign client  ,so we enable this mechanism by feign client only  and feign client ka matlab hot hai ki mae communication kar raha hu and communication fail hui toh circuit breaker ki open state start ho jayegi  means fallback mechnaism start ho jayegi
public interface FileSearchClient {
    @GetMapping("/api/files")
    List<Map<String,Object>> getAllFiles();


    @GetMapping("/api/files/folder/{folderId}")
    List<Map<String,Object>> getAllFilesByFolderId(@PathVariable Long folderId);

}// fiegn client yehi do method use karraha hai communication ke liye getAllFilesByFolderId  &getAllFiles
