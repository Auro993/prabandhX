package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.dto.FileDTO;
import com.prabandhx.prabandhx.entity.ProjectFile;
import com.prabandhx.prabandhx.entity.Project;
import com.prabandhx.prabandhx.entity.Task;
import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.FileRepository;
import com.prabandhx.prabandhx.repository.ProjectRepository;
import com.prabandhx.prabandhx.repository.TaskRepository;
import com.prabandhx.prabandhx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ActivityLogService activityLogService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080}")
    private String baseUrl;

    // ===============================
    // CONVERTER METHODS
    // ===============================

    private FileDTO convertToDTO(ProjectFile file) {
        FileDTO dto = new FileDTO();
        dto.setId(file.getId());
        dto.setFileName(file.getFileName());
        dto.setFileType(file.getFileType());
        dto.setFileSize(file.getFileSize());
        dto.setFileSizeDisplay(file.getFileSizeDisplay());
        dto.setFilePath(file.getFilePath());
        dto.setMimeType(file.getMimeType());
        dto.setThumbnailPath(file.getThumbnailPath());
        
        dto.setProjectId(file.getProjectId());
        dto.setProjectName(file.getProjectName());
        dto.setTaskId(file.getTaskId());
        dto.setTaskTitle(file.getTaskTitle());
        
        dto.setUploadedById(file.getUploadedById());
        dto.setUploadedByName(file.getUploadedByName());
        
        dto.setUploadedAt(file.getUploadedAt());
        dto.setUpdatedAt(file.getUpdatedAt());
        
        dto.setVersion(file.getVersion());
        dto.setDownloadCount(file.getDownloadCount());
        
        dto.setShareableLink(file.getShareableLink());
        dto.setLinkExpiry(file.getLinkExpiry());
        dto.setIsExpired(file.isExpired());
        
        dto.setPermissions(file.getPermissions());
        
        if (file.getParentFile() != null) {
            dto.setParentFileId(file.getParentFile().getId());
        }
        dto.setIsLatestVersion(file.getIsLatestVersion());
        
        // Generate URLs
        dto.setDownloadUrl(baseUrl + "/api/files/download/" + file.getId());
        dto.setPreviewUrl(baseUrl + "/api/files/preview/" + file.getId());
        
        return dto;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private String generateShareableLink() {
        return UUID.randomUUID().toString();
    }

    // ===============================
    // UPLOAD FILE
    // ===============================

    @Transactional
    public FileDTO uploadFile(MultipartFile file, Long projectId, Long taskId, Long userId) throws IOException {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Get project if provided
        Project project = null;
        if (projectId != null) {
            project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
        }

        // Get task if provided
        Task task = null;
        if (taskId != null) {
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
        }

        // Create upload directory if not exists
        String uploadPath = uploadDir + "/project_" + (projectId != null ? projectId : "general");
        Path path = Paths.get(uploadPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            System.out.println("📁 Created upload directory: " + path.toString());
        }

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = path.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        System.out.println("📁 File saved at: " + filePath.toString());

        // Create file entity
        ProjectFile projectFile = new ProjectFile();
        projectFile.setFileName(originalFileName);
        projectFile.setFileType(fileExtension.replace(".", "").toUpperCase());
        projectFile.setFileSize(file.getSize());
        projectFile.setFileSizeDisplay(formatFileSize(file.getSize()));
        projectFile.setFilePath(filePath.toString()); // Store full path
        projectFile.setMimeType(file.getContentType());
        projectFile.setProject(project);
        projectFile.setTask(task);
        projectFile.setUploadedBy(user);
        projectFile.setVersion(1);
        projectFile.setDownloadCount(0);
        projectFile.setPermissions("VIEW_ONLY");
        projectFile.setIsDeleted(false);
        projectFile.setIsLatestVersion(true);

        // Generate thumbnail for images (optional)
        if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
            // You can add thumbnail generation logic here
            projectFile.setThumbnailPath(filePath.toString());
        }

        ProjectFile savedFile = fileRepository.save(projectFile);
        System.out.println("✅ File saved to database with ID: " + savedFile.getId());
        
        // ✅ LOG ACTIVITY: File Uploaded
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileSize", file.getSize());
        metadata.put("fileType", file.getContentType());
        metadata.put("originalName", originalFileName);
        
        activityLogService.logActivity(
            userId,
            "UPLOAD_FILE",
            "Uploaded file",
            "FILE",
            savedFile.getId(),
            savedFile.getFileName(),
            projectId,
            "Uploaded file '" + savedFile.getFileName() + "' (" + savedFile.getFileSizeDisplay() + ")" +
                (project != null ? " to project '" + project.getName() + "'" : "") +
                (task != null ? " to task '" + task.getTitle() + "'" : ""),
            metadata
        );
        
        return convertToDTO(savedFile);
    }

    // ===============================
    // DOWNLOAD FILE - FIXED VERSION
    // ===============================

    public Path getFilePath(Long fileId) {
        ProjectFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        if (file.getIsDeleted()) {
            throw new RuntimeException("File has been deleted");
        }

        // Increment download count
        file.setDownloadCount(file.getDownloadCount() + 1);
        fileRepository.save(file);

        // Get the file path from database
        String filePathStr = file.getFilePath();
        System.out.println("📁 File path from DB: " + filePathStr);
        
        Path path = Paths.get(filePathStr);
        
        // Check if file exists
        if (!Files.exists(path)) {
            System.err.println("❌ File does not exist at path: " + path.toString());
            
            // Try to find file in uploads directory
            String fileName = path.getFileName().toString();
            Path alternativePath = Paths.get(uploadDir).resolve(fileName);
            
            System.out.println("📁 Trying alternative path: " + alternativePath.toString());
            
            if (Files.exists(alternativePath)) {
                System.out.println("✅ Found file at alternative path");
                
                // Update database with correct path
                file.setFilePath(alternativePath.toString());
                fileRepository.save(file);
                
                // ✅ LOG ACTIVITY: File Downloaded (with path correction)
                activityLogService.logActivity(
                    file.getUploadedById(),
                    "DOWNLOAD_FILE",
                    "Downloaded file",
                    "FILE",
                    fileId,
                    file.getFileName(),
                    file.getProjectId(),
                    "Downloaded file '" + file.getFileName() + "' (download #" + file.getDownloadCount() + ")",
                    null
                );
                
                return alternativePath;
            } else {
                // Search recursively in uploads directory
                try {
                    Path foundPath = findFileRecursively(Paths.get(uploadDir), fileName);
                    if (foundPath != null) {
                        System.out.println("✅ Found file recursively at: " + foundPath.toString());
                        file.setFilePath(foundPath.toString());
                        fileRepository.save(file);
                        
                        // ✅ LOG ACTIVITY: File Downloaded (after recursive search)
                        activityLogService.logActivity(
                            file.getUploadedById(),
                            "DOWNLOAD_FILE",
                            "Downloaded file",
                            "FILE",
                            fileId,
                            file.getFileName(),
                            file.getProjectId(),
                            "Downloaded file '" + file.getFileName() + "' (download #" + file.getDownloadCount() + ")",
                            null
                        );
                        
                        return foundPath;
                    }
                } catch (IOException e) {
                    System.err.println("❌ Error searching for file: " + e.getMessage());
                }
                
                throw new RuntimeException("File not found on disk: " + fileName);
            }
        }
        
        // ✅ LOG ACTIVITY: File Downloaded
        activityLogService.logActivity(
            file.getUploadedById(),
            "DOWNLOAD_FILE",
            "Downloaded file",
            "FILE",
            fileId,
            file.getFileName(),
            file.getProjectId(),
            "Downloaded file '" + file.getFileName() + "' (download #" + file.getDownloadCount() + ")",
            null
        );

        return path;
    }
    
    /**
     * Recursively search for a file in directory
     */
    private Path findFileRecursively(Path directory, String fileName) throws IOException {
        return Files.walk(directory)
                .filter(path -> path.getFileName().toString().equals(fileName))
                .findFirst()
                .orElse(null);
    }

    // ===============================
    // GET FILES BY PROJECT
    // ===============================

    public List<FileDTO> getFilesByProject(Long projectId) {
        return fileRepository.findByProjectIdAndIsDeletedFalse(projectId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // GET FILES BY TASK
    // ===============================

    public List<FileDTO> getFilesByTask(Long taskId) {
        return fileRepository.findByTaskIdAndIsDeletedFalse(taskId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // GET MY FILES
    // ===============================

    public List<FileDTO> getMyFiles(Long userId) {
        return fileRepository.findByUploadedByIdAndIsDeletedFalse(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // GET ALL FILES (ADMIN ONLY)
    // ===============================

    public List<FileDTO> getAllFiles() {
        return fileRepository.findByIsDeletedFalse()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // GENERATE SHAREABLE LINK
    // ===============================

    @Transactional
    public FileDTO generateShareableLink(Long fileId, int expiryDays, String permissions) {
        ProjectFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        String shareLink = generateShareableLink();
        file.setShareableLink(shareLink);
        file.setLinkExpiry(LocalDateTime.now().plusDays(expiryDays));
        file.setPermissions(permissions);

        ProjectFile updatedFile = fileRepository.save(file);
        System.out.println("🔗 Generated share link for file ID: " + fileId + " - " + shareLink);
        
        // ✅ LOG ACTIVITY: Share Link Generated
        activityLogService.logActivity(
            file.getUploadedById(),
            "GENERATE_SHARE_LINK",
            "Generated share link",
            "FILE",
            fileId,
            file.getFileName(),
            file.getProjectId(),
            "Generated share link for file '" + file.getFileName() + "' with " + permissions + 
                " permissions (expires in " + expiryDays + " days)",
            null
        );
        
        return convertToDTO(updatedFile);
    }

    // ===============================
    // ACCESS SHARED FILE
    // ===============================

    public ProjectFile getFileByShareableLink(String link) {
        ProjectFile file = fileRepository.findByShareableLinkAndIsDeletedFalse(link)
                .orElseThrow(() -> new RuntimeException("Invalid or expired link"));

        if (file.isExpired()) {
            throw new RuntimeException("Shareable link has expired");
        }

        // ✅ LOG ACTIVITY: Shared File Accessed
        activityLogService.logActivity(
            null, // Unknown user accessing via link
            "ACCESS_SHARED_FILE",
            "Accessed shared file",
            "FILE",
            file.getId(),
            file.getFileName(),
            file.getProjectId(),
            "Shared file '" + file.getFileName() + "' accessed via share link",
            null
        );

        return file;
    }

    // ===============================
    // GET FILE VERSIONS
    // ===============================

    public List<FileDTO> getFileVersions(Long fileId) {
        return fileRepository.findFileVersions(fileId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // UPLOAD NEW VERSION
    // ===============================

    @Transactional
    public FileDTO uploadNewVersion(Long fileId, MultipartFile newFile, Long userId) throws IOException {
        // Get existing file
        ProjectFile existingFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        // Mark existing as not latest
        existingFile.setIsLatestVersion(false);
        fileRepository.save(existingFile);

        // Upload as new file with reference to parent
        FileDTO newVersion = uploadFile(newFile, 
                existingFile.getProjectId(), 
                existingFile.getTaskId(), 
                userId);
        
        // Get the newly created file
        ProjectFile latestVersion = fileRepository.findById(newVersion.getId())
                .orElseThrow(() -> new RuntimeException("Error creating new version"));

        // Set parent reference and version
        latestVersion.setParentFile(existingFile);
        latestVersion.setVersion(existingFile.getVersion() + 1);
        
        ProjectFile savedVersion = fileRepository.save(latestVersion);
        System.out.println("📄 New version created: v" + savedVersion.getVersion() + " for file ID: " + fileId);
        
        // ✅ LOG ACTIVITY: New Version Uploaded
        activityLogService.logActivity(
            userId,
            "UPLOAD_FILE_VERSION",
            "Uploaded new version",
            "FILE",
            savedVersion.getId(),
            savedVersion.getFileName(),
            savedVersion.getProjectId(),
            "Uploaded new version (v" + savedVersion.getVersion() + ") of file '" + 
                savedVersion.getFileName() + "'",
            null
        );
        
        return convertToDTO(savedVersion);
    }

    // ===============================
    // SOFT DELETE FILE
    // ===============================

    @Transactional
    public void deleteFile(Long fileId) {
        ProjectFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));
        
        fileRepository.softDelete(fileId, LocalDateTime.now());
        System.out.println("🗑️ File soft deleted: " + fileId);
        
        // ✅ LOG ACTIVITY: File Deleted
        activityLogService.logActivity(
            file.getUploadedById(),
            "DELETE_FILE",
            "Deleted file",
            "FILE",
            fileId,
            file.getFileName(),
            file.getProjectId(),
            "File '" + file.getFileName() + "' was deleted",
            null
        );
    }

    // ===============================
    // GET FILE STATISTICS
    // ===============================

    public FileStats getFileStats(Long userId) {
        FileStats stats = new FileStats();
        
        stats.setTotalFiles(fileRepository.countByUploadedByIdAndIsDeletedFalse(userId));
        stats.setTotalStorage(fileRepository.getTotalStorageUsedByUser(userId));
        
        List<ProjectFile> recentFiles = fileRepository.findTop10ByIsDeletedFalseOrderByUploadedAtDesc()
                .stream()
                .limit(5)
                .collect(Collectors.toList());
        
        stats.setRecentFiles(recentFiles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        
        return stats;
    }

    // Inner class for stats
    public static class FileStats {
        private Long totalFiles;
        private Long totalStorage;
        private List<FileDTO> recentFiles;

        public Long getTotalFiles() {
            return totalFiles;
        }

        public void setTotalFiles(Long totalFiles) {
            this.totalFiles = totalFiles;
        }

        public Long getTotalStorage() {
            return totalStorage;
        }

        public void setTotalStorage(Long totalStorage) {
            this.totalStorage = totalStorage;
        }

        public List<FileDTO> getRecentFiles() {
            return recentFiles;
        }

        public void setRecentFiles(List<FileDTO> recentFiles) {
            this.recentFiles = recentFiles;
        }
    }
}