package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.dto.FileDTO;
import com.prabandhx.prabandhx.entity.ProjectFile;
import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.UserRepository;
import com.prabandhx.prabandhx.service.FileService;
import com.prabandhx.prabandhx.service.FileService.FileStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileService fileService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Get current authenticated user ID from token
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        Object principal = auth.getPrincipal();
        
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String email = userDetails.getUsername();
            
            // Fetch user from database using email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            
            System.out.println("✅ Found user: " + user.getEmail() + " with ID: " + user.getId());
            return user.getId();
        }
        
        throw new RuntimeException("Unable to extract user ID from authentication");
    }

    /**
     * Upload a file
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "taskId", required = false) Long taskId) {
        
        try {
            Long userId = getCurrentUserId();
            System.out.println("📤 Uploading file for user ID: " + userId);
            
            FileDTO uploadedFile = fileService.uploadFile(file, projectId, taskId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("file", uploadedFile);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.err.println("❌ Upload error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("❌ User error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Authentication error: " + e.getMessage());
        }
    }

    /**
     * Download a file - UPDATED VERSION
     */
    @GetMapping("/download/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> downloadFile(@PathVariable Long fileId) {
        try {
            Path filePath = fileService.getFilePath(fileId);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String filename = resource.getFilename();
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                        .header(HttpHeaders.PRAGMA, "no-cache")
                        .header(HttpHeaders.EXPIRES, "0")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error downloading file: " + e.getMessage());
        }
    }

    /**
     * Preview a file (open in browser) - UPDATED VERSION
     */
    @GetMapping("/preview/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> previewFile(@PathVariable Long fileId) {
        try {
            Path filePath = fileService.getFilePath(fileId);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Get filename and determine content type
                String filename = resource.getFilename();
                String contentType = determineContentType(filename);
                
                System.out.println("📄 Previewing file: " + filename + " (" + contentType + ")");
                System.out.println("📁 File path: " + filePath.toString());
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                        .header(HttpHeaders.PRAGMA, "no-cache")
                        .header(HttpHeaders.EXPIRES, "0")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                System.err.println("❌ File not found or not readable: " + filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error previewing file: " + e.getMessage());
        }
    }

    /**
     * Determine content type from filename
     */
    private String determineContentType(String filename) {
        if (filename == null) return "application/octet-stream";
        
        filename = filename.toLowerCase();
        
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else if (filename.endsWith(".bmp")) {
            return "image/bmp";
        } else if (filename.endsWith(".webp")) {
            return "image/webp";
        } else if (filename.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (filename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (filename.endsWith(".mp4")) {
            return "video/mp4";
        } else if (filename.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (filename.endsWith(".txt")) {
            return "text/plain";
        } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
            return "text/html";
        } else if (filename.endsWith(".json")) {
            return "application/json";
        } else if (filename.endsWith(".xml")) {
            return "application/xml";
        } else if (filename.endsWith(".zip")) {
            return "application/zip";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * Get files by project
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileDTO>> getFilesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(fileService.getFilesByProject(projectId));
    }

    /**
     * Get files by task
     */
    @GetMapping("/task/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileDTO>> getFilesByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(fileService.getFilesByTask(taskId));
    }

    /**
     * Get my uploaded files
     */
    @GetMapping("/my-uploads")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileDTO>> getMyFiles() {
        Long userId = getCurrentUserId();
        System.out.println("📂 Fetching files for user ID: " + userId);
        return ResponseEntity.ok(fileService.getMyFiles(userId));
    }

    /**
     * Get all files (Admin only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FileDTO>> getAllFiles() {
        return ResponseEntity.ok(fileService.getAllFiles());
    }

    /**
     * Generate shareable link
     */
    @PostMapping("/share/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileDTO> generateShareLink(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "7") int expiryDays,
            @RequestParam(defaultValue = "VIEW_ONLY") String permissions) {
        
        try {
            FileDTO sharedFile = fileService.generateShareableLink(fileId, expiryDays, permissions);
            return ResponseEntity.ok(sharedFile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Access shared file via link
     */
    @GetMapping("/shared/{link}")
    public ResponseEntity<?> accessSharedFile(@PathVariable String link) {
        try {
            ProjectFile file = fileService.getFileByShareableLink(link);
            Path filePath = Path.of(file.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String filename = resource.getFilename();
                String contentType = determineContentType(filename);
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Invalid or expired link: " + e.getMessage());
        }
    }

    /**
     * Get file versions
     */
    @GetMapping("/versions/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileDTO>> getFileVersions(@PathVariable Long fileId) {
        try {
            return ResponseEntity.ok(fileService.getFileVersions(fileId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Upload new version
     */
    @PostMapping("/version/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadNewVersion(
            @PathVariable Long fileId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            Long userId = getCurrentUserId();
            FileDTO newVersion = fileService.uploadNewVersion(fileId, file, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "New version uploaded successfully");
            response.put("file", newVersion);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error uploading new version: " + e.getMessage());
        }
    }

    /**
     * Delete file (soft delete)
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        try {
            fileService.deleteFile(fileId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error deleting file: " + e.getMessage());
        }
    }

    /**
     * Get file statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileStats> getFileStats() {
        try {
            Long userId = getCurrentUserId();
            return ResponseEntity.ok(fileService.getFileStats(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
}