package com.maalfa.communication_platform.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    // category is a subfolder + validation profile, e.g. "avatars" or
    // "attachments" — each has its own allowed extensions and size limit.
    // Returns a URL the client can GET immediately (e.g. "/api/files/avatars/abc123.png").
    String storeFile(MultipartFile file, String category);

    // Deletes the file a previously-returned URL points to. Best-effort —
    // silently no-ops if the file is already gone, since a missing file
    // on delete isn't something the caller needs to handle specially.
    void deleteFile(String fileUrl);
}
