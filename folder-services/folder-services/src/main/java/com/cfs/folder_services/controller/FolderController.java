package com.cfs.folder_services.controller;

import com.cfs.folder_services.model.FolderEntity;
import com.cfs.folder_services.repo.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

public class FolderController {
    @Autowired
 
    private FolderRepository folderRepository;


    @GetMapping
    public List<FolderEntity> getAllFolders()
    {
        return folderRepository.findAll();
    }

    @GetMapping("/{id}")
    public FolderEntity getFolder(@PathVariable Long id)
    {
        return folderRepository.findById(id).orElse(null);
    }

    @PostMapping
    public FolderEntity createFolder(@RequestBody FolderEntity folder)
    {
        return folderRepository.save(folder);

    }


    @DeleteMapping("/{id}")
    public void deleteFolder(@PathVariable Long id)
    {
        folderRepository.deleteById(id);
    }

    @GetMapping("/parent/{parentId}")
    public List<FolderEntity> getFoldersByParent(@PathVariable Long parentId)
    {
        return folderRepository.findByParentId(parentId);
    }

    @PostMapping("/create")
    public Map<String,Object> createNewFolder(@RequestBody Map<String,Object> request)
    {//Suppose frontend sends:

//        {// this is requestbody
//            "name":"Java",
//                "parentId":1
//        }
        try {
            String name= (String) request.get("name");
            //So internally:
            //request.get("name")
            //returns:
            //Java
            //gets value from JSON.
            //Example:
            //"name":"Java"
            Long parentId=request.get("parentId")!=null ?
                    Long.valueOf(request.get("parentId").toString() ): null; // because root folder not have parent id child folder have parent id
            //request.get("parentId") Long.valueOf will convert the string parent id into Long
            //returns:
            //1

            FolderEntity newFolder=new FolderEntity();
            newFolder.setId(System.currentTimeMillis());
            newFolder.setName(name);
            newFolder.setParentId(parentId);
//json mawe evry key will be in string and value is in corresponding value
            //| JSON Value | Type    |
            //| ---------- | ------- |
            //| `"Java"`   | String  |
            //| `1`        | Number  |
            //| `true`     | Boolean |
            //| `null`     | Null    |
            //| `[]`       | Array   |
            //| `{}`       | Object  |
            FolderEntity saved=folderRepository.save(newFolder);
            return Map.of("Success",true,"folder",saved);
        }
        catch (Exception e)
        {
            return Map.of("Success",false,"error",e.getMessage());
        }
    }

}
