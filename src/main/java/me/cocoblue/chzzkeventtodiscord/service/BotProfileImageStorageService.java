package me.cocoblue.chzzkeventtodiscord.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.config.S3StorageProperties;
import me.cocoblue.chzzkeventtodiscord.config.StorageUploadProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class BotProfileImageStorageService {
    private final S3Client s3Client;
    private final S3StorageProperties s3StorageProperties;
    private final StorageUploadProperties storageUploadProperties;
    private final StaticContentUrlResolver staticContentUrlResolver;
    private final S3StorageAvailabilityService s3StorageAvailabilityService;

    public String upload(String ownerChannelId, MultipartFile file) {
        s3StorageAvailabilityService.requireAvailable();
        validate(file);

        final String contentType = normalizeContentType(file.getContentType());
        final String objectKey = buildObjectKey(ownerChannelId, contentType);
        final byte[] avatarBytes;
        try {
            avatarBytes = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unable to read avatar file", exception);
        }
        validateBytes(avatarBytes);

        final PutObjectRequest request = PutObjectRequest.builder()
            .bucket(s3StorageProperties.getBucket())
            .key(objectKey)
            .contentType(contentType)
            .contentLength((long) avatarBytes.length)
            .cacheControl("public, max-age=31536000, immutable")
            .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(avatarBytes));
            return objectKey;
        } catch (SdkException exception) {
            log.warn("Failed to upload bot profile image to object storage. key={}", objectKey, exception);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "image storage connection failed", exception);
        }
    }

    public void deleteBestEffort(String objectKeyOrUrl) {
        if (!staticContentUrlResolver.isManagedObjectKey(objectKeyOrUrl)) {
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(s3StorageProperties.getBucket())
                .key(objectKeyOrUrl)
                .build());
        } catch (RuntimeException exception) {
            log.warn("Failed to delete bot profile image from object storage. key={}", objectKeyOrUrl, exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar is required");
        }
        if (file.getSize() > storageUploadProperties.getMaxBytes()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar file is too large");
        }

        final String contentType = normalizeContentType(file.getContentType());
        final Set<String> allowedTypes = storageUploadProperties.getAllowedTypes().stream()
            .map(this::normalizeContentType)
            .collect(Collectors.toSet());
        if (!allowedTypes.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar content type is not allowed");
        }
    }

    private void validateBytes(byte[] avatarBytes) {
        if (avatarBytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar is required");
        }
        if (avatarBytes.length > storageUploadProperties.getMaxBytes()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar file is too large");
        }
    }

    private String buildObjectKey(String ownerChannelId, String contentType) {
        final String normalizedPrefix = trimSlashes(s3StorageProperties.getPrefix());
        final String ownerPath = ownerChannelId.replaceAll("[^a-zA-Z0-9._-]", "_");
        final String fileName = UUID.randomUUID() + "." + extensionFor(contentType);
        if (!StringUtils.hasText(normalizedPrefix)) {
            return ownerPath + "/" + fileName;
        }
        return normalizedPrefix + "/" + ownerPath + "/" + fileName;
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/jpeg" -> "jpg";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar content type is not allowed");
        };
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    }

    private String trimSlashes(String value) {
        if (value == null) {
            return "";
        }
        String next = value.trim();
        while (next.startsWith("/")) {
            next = next.substring(1);
        }
        while (next.endsWith("/")) {
            next = next.substring(0, next.length() - 1);
        }
        return next;
    }
}
