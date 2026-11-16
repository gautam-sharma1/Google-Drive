package com.cfs.Search_Services.controller;

import com.cfs.Search_Services.client.FileSearchClient;
import com.cfs.Search_Services.client.FolderSearchClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final FileSearchClient fileSearchClient;
    private final FolderSearchClient folderSearchClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    public SearchController(FileSearchClient fileSearchClient,
                            FolderSearchClient folderSearchClient, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.fileSearchClient = fileSearchClient;
        this.folderSearchClient = folderSearchClient;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }
    //enter file name
    @GetMapping
    public Map<String,Object> search(@RequestParam String query)
    {
        System.out.println("[SEARCH] Request for query "+query);
        CircuitBreaker fileServiceCB = circuitBreakerRegistry.circuitBreaker("file-service");// name of my circuit breaker
        CircuitBreaker folderServiceCB = circuitBreakerRegistry.circuitBreaker("folder-service");
        List<Map<String,Object>> allFiles = new ArrayList<>();// this is the data of file ,not a  complete file
        List<Map<String,Object>> allFolders = new ArrayList<>();
//List<Map<String,Object>> allFiles=fileSearchClient.getAllFiles();
  //      List<Map<String,Object>> allFolders=folderSearchClient.getAllFolders();
        try {
            Supplier<List<Map<String, Object>>> fileCallable = () -> fileSearchClient.getAllFiles();
            allFiles = fileServiceCB.executeSupplier(fileCallable);
        } catch (CallNotPermittedException e) {
            System.err.println("[SEARCH] ⚠ File-service circuit breaker OPEN - returning empty file results");
        } catch (Exception e) {
            System.err.println("[SEARCH] ❌ File-service error: " + e.getMessage());
        }

        // folder search was previously commented out here, leaving "/api/search" without folder results at all
        try {
            Supplier<List<Map<String, Object>>> folderCallable = () -> folderSearchClient.getAllFolders();
            allFolders = folderServiceCB.executeSupplier(folderCallable);
        } catch (CallNotPermittedException e) {
            System.err.println("[SEARCH] ⚠ Folder-service circuit breaker OPEN - returning empty folder results");
        } catch (Exception e) {
            System.err.println("[SEARCH] ❌ Folder-service error: " + e.getMessage());
        }

        List<Map<String, Object>> fileResults = (allFiles != null ? allFiles : new ArrayList<Map<String, Object>>())
                .stream()
                .filter(f -> f != null && f.get("name") != null &&
                        f.get("name").toString().toLowerCase().contains(query.toLowerCase()))
                .toList();

        List<Map<String, Object>> folderResults = (allFolders != null ? allFolders : new ArrayList<Map<String, Object>>())
                .stream()
                .filter(f -> f != null && f.get("name") != null &&
                        f.get("name").toString().toLowerCase().contains(query.toLowerCase()))
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("files", fileResults);
        result.put("folders", folderResults);
        return result;
    }

    @GetMapping("/files")
    public List<Map<String,Object>> searchFiles(@RequestParam String query)
    {
        List<Map<String,Object>> allFiles=fileSearchClient.getAllFiles();

        return allFiles.stream()
                .filter(f->f.get("name").toString().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    @GetMapping("/folders")
    public List<Map<String,Object>> searchFolders(@RequestParam String query)
    {
        List<Map<String,Object>> allFolders=folderSearchClient.getAllFolders();
        return allFolders.stream()
                .filter(f->f.get("name").toString().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }
}
