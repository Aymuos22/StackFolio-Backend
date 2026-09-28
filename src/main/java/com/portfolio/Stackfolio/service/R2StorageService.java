package com.portfolio.Stackfolio.service;

import com.portfolio.Stackfolio.config.R2Properties;
import com.portfolio.Stackfolio.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class R2StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    private final R2Properties properties;
    private volatile S3Client s3Client;

    public R2StorageService(R2Properties properties) {
        this.properties = properties;
    }

    public String uploadProfileImage(Long userId, MultipartFile file) {
        S3Client client = requireClient();
        validateImage(file);

        String contentType = normalizeContentType(file.getContentType());
        String extension = EXTENSION_BY_CONTENT_TYPE.get(contentType);
        String objectKey = "profiles/" + userId + "/" + UUID.randomUUID() + "." + extension;

        try {
            client.putObject(
                    PutObjectRequest.builder()
                            .bucket(properties.getBucket())
                            .key(objectKey)
                            .contentType(contentType)
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException exception) {
            throw new BadRequestException("Failed to read uploaded image");
        } catch (BadRequestException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to upload image to R2", exception);
        }

        return publicUrlFor(objectKey);
    }

    public void deleteByPublicUrl(String publicUrl) {
        if (!StringUtils.hasText(publicUrl) || !properties.isConfigured()) {
            return;
        }

        String objectKey = objectKeyFromPublicUrl(publicUrl);
        if (objectKey == null) {
            return;
        }

        try {
            requireClient().deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build());
        } catch (Exception ignored) {
            // Best-effort cleanup; DB URL update should still succeed.
        }
    }

    private S3Client requireClient() {
        if (!properties.isConfigured()) {
            throw new IllegalStateException(
                    "Cloudflare R2 is not configured. Set R2_ACCOUNT_ID, R2_ACCESS_KEY_ID, "
                            + "R2_SECRET_ACCESS_KEY, R2_BUCKET, and R2_PUBLIC_BASE_URL."
            );
        }

        S3Client existing = s3Client;
        if (existing != null) {
            return existing;
        }

        synchronized (this) {
            if (s3Client == null) {
                String endpoint = "https://" + properties.getAccountId() + ".r2.cloudflarestorage.com";
                s3Client = S3Client.builder()
                        .endpointOverride(URI.create(endpoint))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        properties.getAccessKeyId(),
                                        properties.getSecretAccessKey()
                                )
                        ))
                        .region(Region.of("auto"))
                        .serviceConfiguration(S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build())
                        .build();
            }
            return s3Client;
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new BadRequestException(
                    "Image must be at most " + (properties.getMaxFileSizeBytes() / (1024 * 1024)) + "MB"
            );
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Only JPEG, PNG, WebP, and GIF images are allowed");
        }
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    private String publicUrlFor(String objectKey) {
        String base = properties.getPublicBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + objectKey;
    }

    private String objectKeyFromPublicUrl(String publicUrl) {
        String base = properties.getPublicBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!publicUrl.startsWith(base + "/")) {
            return null;
        }
        return publicUrl.substring(base.length() + 1);
    }
}
