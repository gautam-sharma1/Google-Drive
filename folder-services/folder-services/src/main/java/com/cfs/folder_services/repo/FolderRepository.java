package com.cfs.folder_services.repo;

import com.cfs.folder_services.model.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<FolderEntity,Long>
{
    List<FolderEntity> findByParentId(Long ParentId);
}
