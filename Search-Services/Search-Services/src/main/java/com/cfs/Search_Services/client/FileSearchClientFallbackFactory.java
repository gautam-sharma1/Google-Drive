package com.cfs.Search_Services.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// agar fiegn client ke voh do method fail hue toh fallback ke do methofd chal jayenge  yahan val for filesearch
@Component
public class FileSearchClientFallbackFactory implements FallbackFactory<FileSearchClient> {

    @Override
    public FileSearchClient create(Throwable cause) {
        return new FileSearchClient() {
            @Override
            public List<Map<String, Object>> getAllFiles() {
                System.out.println("[FALLBACK] file service unavailable -  returning emplty list ");
                return new ArrayList<>();
            }

            @Override
            public List<Map<String, Object>> getAllFilesByFolderId(Long folderId) {
                System.out.println("[FALLBACK] file service unavailable -  returning emplty list ");
                return new ArrayList<>();
            }


    };
}}
