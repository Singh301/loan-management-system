package com.sudhanshu.loanmanagement.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "file.storage", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final String bucket;

    public S3FileStorageService(S3Client s3Client,
                                @Value("${file.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public String store(MultipartFile file, String preferredExtension) throws IOException {
        String ext = preferredExtension != null ? preferredExtension : "";
        String key = "documents/" + UUID.randomUUID() + ext;

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(put, RequestBody.fromBytes(file.getBytes()));
        return key;
    }

    @Override
    public byte[] load(String storageKey) throws IOException {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build();
        try (var is = s3Client.getObject(get)) {
            return is.readAllBytes();
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .build());
        } catch (Exception ignored) {
        }
    }
}
