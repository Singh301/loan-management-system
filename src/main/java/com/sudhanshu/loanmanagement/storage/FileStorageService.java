package com.sudhanshu.loanmanagement.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Abstraction over local disk vs S3 document storage.
 */
public interface FileStorageService {

    /**
     * Store file and return a storage key / path reference.
     */
    String store(MultipartFile file, String preferredExtension) throws IOException;

    /**
     * Load file bytes by storage key.
     */
    byte[] load(String storageKey) throws IOException;

    /**
     * Delete file by storage key (best-effort).
     */
    void delete(String storageKey);
}
