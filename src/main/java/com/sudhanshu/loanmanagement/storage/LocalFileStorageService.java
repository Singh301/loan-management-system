package com.sudhanshu.loanmanagement.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "file.storage", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadPath;

    public LocalFileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) throws IOException {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    @Override
    public String store(MultipartFile file, String preferredExtension) throws IOException {
        String ext = preferredExtension != null ? preferredExtension : "";
        String safeName = UUID.randomUUID() + ext;
        Path target = uploadPath.resolve(safeName).normalize();
        if (!target.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid file path.");
        }
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    @Override
    public byte[] load(String storageKey) throws IOException {
        Path path = Paths.get(storageKey).normalize();
        if (!path.startsWith(uploadPath) && !path.isAbsolute()) {
            path = uploadPath.resolve(storageKey).normalize();
        }
        return Files.readAllBytes(path);
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path path = Paths.get(storageKey).normalize();
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }
}
