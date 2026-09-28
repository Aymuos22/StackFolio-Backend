package com.portfolio.Stackfolio.controller;

import com.portfolio.Stackfolio.service.R2StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/api/media")
@Tag(name = "Media", description = "Public media served from Cloudflare R2")
public class MediaController {

    private final R2StorageService r2StorageService;

    public MediaController(R2StorageService r2StorageService) {
        this.r2StorageService = r2StorageService;
    }

    @GetMapping("/{*objectKey}")
    @Operation(summary = "Fetch a stored media object by key")
    public ResponseEntity<InputStreamResource> getMedia(
            @PathVariable("objectKey") String objectKey
    ) {
        String key = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        ResponseInputStream<GetObjectResponse> object = r2StorageService.getObject(key);
        GetObjectResponse metadata = object.response();

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (metadata.contentType() != null && !metadata.contentType().isBlank()) {
            mediaType = MediaType.parseMediaType(metadata.contentType());
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable");

        if (metadata.contentLength() != null && metadata.contentLength() >= 0) {
            builder.contentLength(metadata.contentLength());
        }

        return builder.body(new InputStreamResource(object));
    }
}
