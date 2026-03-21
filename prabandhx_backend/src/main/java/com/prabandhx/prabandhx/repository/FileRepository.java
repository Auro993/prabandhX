package com.prabandhx.prabandhx.repository;

import com.prabandhx.prabandhx.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<ProjectFile, Long> {
    
    // ===== USE @Query FOR ALL CUSTOM METHODS =====
    // This avoids Spring Data JPA's automatic query creation
    
    @Query("SELECT f FROM ProjectFile f WHERE f.project.id = :projectId AND f.isDeleted = false")
    List<ProjectFile> findByProjectIdAndIsDeletedFalse(@Param("projectId") Long projectId);
    
    @Query("SELECT f FROM ProjectFile f WHERE f.task.id = :taskId AND f.isDeleted = false")
    List<ProjectFile> findByTaskIdAndIsDeletedFalse(@Param("taskId") Long taskId);
    
    @Query("SELECT f FROM ProjectFile f WHERE f.uploadedBy.id = :userId AND f.isDeleted = false")
    List<ProjectFile> findByUploadedByIdAndIsDeletedFalse(@Param("userId") Long userId);
    
    @Query("SELECT f FROM ProjectFile f WHERE f.isDeleted = false")
    List<ProjectFile> findByIsDeletedFalse();
    
    @Query("SELECT f FROM ProjectFile f WHERE f.shareableLink = :link AND f.isDeleted = false")
    Optional<ProjectFile> findByShareableLinkAndIsDeletedFalse(@Param("link") String link);
    
    @Query("SELECT f FROM ProjectFile f WHERE f.linkExpiry < :now AND f.isDeleted = false")
    List<ProjectFile> findExpiredLinks(@Param("now") LocalDateTime now);
    
    @Query("SELECT f FROM ProjectFile f WHERE (f.parentFile.id = :fileId OR f.id = :fileId) AND f.isDeleted = false ORDER BY f.version DESC")
    List<ProjectFile> findFileVersions(@Param("fileId") Long fileId);
    
    @Query("SELECT f FROM ProjectFile f WHERE (f.parentFile.id = :fileId OR f.id = :fileId) AND f.isLatestVersion = true AND f.isDeleted = false")
    Optional<ProjectFile> findLatestVersion(@Param("fileId") Long fileId);
    
    @Query("SELECT COUNT(f) FROM ProjectFile f WHERE f.project.id = :projectId AND f.isDeleted = false")
    Long countByProjectIdAndIsDeletedFalse(@Param("projectId") Long projectId);
    
    @Query("SELECT COUNT(f) FROM ProjectFile f WHERE f.uploadedBy.id = :userId AND f.isDeleted = false")
    Long countByUploadedByIdAndIsDeletedFalse(@Param("userId") Long userId);
    
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM ProjectFile f WHERE f.uploadedBy.id = :userId AND f.isDeleted = false")
    Long getTotalStorageUsedByUser(@Param("userId") Long userId);
    
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM ProjectFile f WHERE f.project.id = :projectId AND f.isDeleted = false")
    Long getTotalStorageUsedByProject(@Param("projectId") Long projectId);
    
    @Modifying
    @Transactional
    @Query("UPDATE ProjectFile f SET f.isDeleted = true, f.deletedAt = :now WHERE f.id = :fileId")
    void softDelete(@Param("fileId") Long fileId, @Param("now") LocalDateTime now);
    
    @Query("SELECT f FROM ProjectFile f WHERE f.mimeType LIKE %:type% AND f.isDeleted = false")
    List<ProjectFile> findByMimeTypeContaining(@Param("type") String type);
    
    @Query("SELECT f FROM ProjectFile f WHERE LOWER(f.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND f.isDeleted = false")
    List<ProjectFile> searchFiles(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT f FROM ProjectFile f WHERE f.isDeleted = false ORDER BY f.uploadedAt DESC LIMIT 10")
    List<ProjectFile> findTop10ByIsDeletedFalseOrderByUploadedAtDesc();
}