package com.maalfa.communication_platform.controller;

import com.maalfa.communication_platform.exception.FileStorageException;
import com.maalfa.communication_platform.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // category is "avatars" or "attachments" — see FileStorageServiceImpl
    // for the allowed extensions / size limit each one enforces.
    @PostMapping("/upload/{category}")
    public ResponseEntity<Map<String, String>> upload(@PathVariable String category,
                                                      @RequestParam("file") MultipartFile file) {
        String url = fileStorageService.storeFile(file, category);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/{category}/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String category, @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, category, filename).normalize();
            Path uploadRoot = Paths.get(uploadDir).normalize().toAbsolutePath();

            // Defense in depth: even though filenames are server-generated
            // (never taken raw from client input), double-check the
            // resolved path never escapes the upload directory.
            if (!filePath.toAbsolutePath().startsWith(uploadRoot)) {
                throw new FileStorageException("Invalid file path");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException("File not found: " + filename);
            }

            String contentType;
            try {
                contentType = Files.probeContentType(filePath);
            } catch (IOException ex) {
                contentType = null;
            }
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Invalid file path", ex);
        }
    }
}
