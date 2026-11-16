package com.cfs.folder_services.controller;

import com.cfs.folder_services.model.FolderEntity;
import com.cfs.folder_services.repo.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
public class FolderController {
    @Autowired
 
    private FolderRepository folderRepository;


    @GetMapping
    public List<FolderEntity> getAllFolders()
    {
        return folderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FolderEntity> getFolder(@PathVariable Long id)
    {
        // return folderRepository.findById(id).orElse(null); // old: returned null body with 200 OK when not found
        return folderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public FolderEntity createFolder(@RequestBody FolderEntity folder)
    {
        folder.setId(null); // ignore any client-supplied id — prevents overwriting an existing row by id (IDOR)
        return folderRepository.save(folder);

    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long id)
    {
        if (!folderRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        folderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parent/{parentId}")
    public List<FolderEntity> getFoldersByParent(@PathVariable Long parentId)
    {
        return folderRepository.findByParentId(parentId);
    }

    // old: duplicate create endpoint with a different, inconsistent contract (raw Map body + manual
    // timestamp id) alongside createFolder() above — consolidated into the single POST / endpoint.
    // @PostMapping("/create")
    // public Map<String,Object> createNewFolder(@RequestBody Map<String,Object> request)
    // {
    //     try {
    //         String name= (String) request.get("name");
    //         Long parentId=request.get("parentId")!=null ?
    //                 Long.valueOf(request.get("parentId").toString() ): null;
    //
    //         FolderEntity newFolder=new FolderEntity();
    //         newFolder.setId(System.currentTimeMillis());
    //         newFolder.setName(name);
    //         newFolder.setParentId(parentId);
    //         FolderEntity saved=folderRepository.save(newFolder);
    //         return Map.of("Success",true,"folder",saved);
    //     }
    //     catch (Exception e)
    //     {
    //         return Map.of("Success",false,"error",e.getMessage());
    //     }
    // }

}
