package com.maalfa.communication_platform.service.impl;

import com.maalfa.communication_platform.exception.FileStorageException;
import com.maalfa.communication_platform.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    // Extensions always rejected regardless of category — executables and
    // scripts have no legitimate reason to be an "attachment" or "avatar".
    private static final Set<String> DENYLIST = Set.of(
            "exe", "sh", "bat", "cmd", "msi", "jar", "com", "scr", "vbs", "ps1");

    private static final Set<String> AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private static final Set<String> ATTACHMENT_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp",           // images
            "pdf", "doc", "docx", "txt", "csv", "xlsx",     // documents
            "mp3", "mp4", "mov", "zip");                    // media / archives

    private static final long AVATAR_MAX_BYTES = 5L * 1024 * 1024;       // 5 MB
    private static final long ATTACHMENT_MAX_BYTES = 25L * 1024 * 1024;  // 25 MB

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    // The path prefix the client will GET from — must match FileController's
    // @RequestMapping so returned URLs are actually fetchable.
    @Value("${app.file.base-url:/api/files}")
    private String baseUrl;

    @Override
    public String storeFile(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot upload an empty file");
        }

        CategoryRules rules = resolveRules(category);

        String extension = extractExtension(file.getOriginalFilename());
        if (extension.isEmpty() || DENYLIST.contains(extension)) {
            throw new FileStorageException("File type not allowed: " + extension);
        }
        if (!rules.allowedExtensions.contains(extension)) {
            throw new FileStorageException(
                    "File type ." + extension + " not allowed for category '" + category + "'");
        }
        if (file.getSize() > rules.maxBytes) {
            throw new FileStorageException(
                    "File exceeds the " + (rules.maxBytes / (1024 * 1024)) + "MB limit for '" + category + "'");
        }

        // Generated filename — never trust the client's original filename
        // for the actual path (prevents path traversal like "../../etc/passwd").
        String storedFilename = UUID.randomUUID() + "." + extension;

        try {
            Path categoryDir = Paths.get(uploadDir, category).normalize();
            Files.createDirectories(categoryDir);

            Path targetPath = categoryDir.resolve(storedFilename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file", ex);
        }

        return baseUrl + "/" + category + "/" + storedFilename;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            return; // not a file this service manages — nothing to do
        }

        String relativePath = fileUrl.substring(baseUrl.length());
        try {
            Path target = Paths.get(uploadDir, relativePath).normalize();
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            // Best-effort — a failed cleanup shouldn't fail the caller's
            // actual operation (e.g. updating a user's avatar URL).
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
    }

    private CategoryRules resolveRules(String category) {
        return switch (category) {
            case "avatars" -> new CategoryRules(AVATAR_EXTENSIONS, AVATAR_MAX_BYTES);
            case "attachments" -> new CategoryRules(ATTACHMENT_EXTENSIONS, ATTACHMENT_MAX_BYTES);
            default -> throw new FileStorageException("Unknown upload category: " + category);
        };
    }

    private record CategoryRules(Set<String> allowedExtensions, long maxBytes) {
    }
}
