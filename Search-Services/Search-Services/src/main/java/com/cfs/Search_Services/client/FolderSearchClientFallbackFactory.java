package com.cfs.Search_Services.client;

import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FolderSearchClientFallbackFactory implements FallbackFactory<FolderSearchClient> {
    @Override
    public FolderSearchClient create(Throwable cause) {
        return new FolderSearchClient() {
            @Override
            public List<Map<String, Object>> getAllFolders() {
                return new ArrayList<>();
            }

            @Override
            public List<Map<String, Object>> getAllFolderByParent(Long parentId) {
                return new ArrayList<>();
            }
        };
    }
}
