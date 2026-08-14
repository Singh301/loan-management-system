package com.sudhanshu.loanmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reports health of the document upload directory (exists + writable).
 */
@Component
public class UploadHealthIndicator implements HealthIndicator {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public Health health() {
        try {
            Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            boolean writable = Files.isWritable(path);
            if (writable) {
                return Health.up()
                        .withDetail("uploadDir", path.toString())
                        .withDetail("writable", true)
                        .build();
            }
            return Health.down()
                    .withDetail("uploadDir", path.toString())
                    .withDetail("writable", false)
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("uploadDir", uploadDir)
                    .build();
        }
    }
}
