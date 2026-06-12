package me.cocoblue.chzzkeventtodiscord.service;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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

/**
 * {@code BotProfileImageStorageService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class BotProfileImageStorageService {
  private final S3Client s3Client;
  private final S3StorageProperties s3StorageProperties;
  private final StorageUploadProperties storageUploadProperties;
  private final StaticContentUrlResolver staticContentUrlResolver;
  private final S3StorageAvailabilityService s3StorageAvailabilityService;

  /**
   * {@code upload}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public String upload(String ownerChannelId, MultipartFile file) {
    s3StorageAvailabilityService.requireAvailable();
    validate(file);

    final String contentType = normalizeContentType(file.getContentType());
    final String objectKey = buildObjectKey(ownerChannelId, contentType);
    final byte[] avatarBytes;
    try {
      avatarBytes = file.getBytes();
    } catch (IOException exception) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "unable to read avatar file", exception);
    }
    validateBytes(avatarBytes, contentType);

    final PutObjectRequest request =
        PutObjectRequest.builder()
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
      log.warn(
          "Failed to upload bot profile image to object storage. key={}", objectKey, exception);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "image storage connection failed", exception);
    }
  }

  /**
   * {@code deleteBestEffort}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public void deleteBestEffort(String objectKeyOrUrl) {
    if (!staticContentUrlResolver.isManagedObjectKey(objectKeyOrUrl)) {
      return;
    }

    try {
      s3Client.deleteObject(
          DeleteObjectRequest.builder()
              .bucket(s3StorageProperties.getBucket())
              .key(objectKeyOrUrl)
              .build());
    } catch (RuntimeException exception) {
      log.warn(
          "Failed to delete bot profile image from object storage. key={}",
          objectKeyOrUrl,
          exception);
    }
  }

  /**
   * {@code validate}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void validate(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar is required");
    }
    if (file.getSize() > storageUploadProperties.getMaxBytes()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar file is too large");
    }

    final String contentType = normalizeContentType(file.getContentType());
    final Set<String> allowedTypes =
        storageUploadProperties.getAllowedTypes().stream()
            .map(this::normalizeContentType)
            .collect(Collectors.toSet());
    if (!allowedTypes.contains(contentType)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "avatar content type is not allowed");
    }
  }

  /**
   * {@code validateBytes}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void validateBytes(byte[] avatarBytes, String contentType) {
    if (avatarBytes.length == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar is required");
    }
    if (avatarBytes.length > storageUploadProperties.getMaxBytes()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar file is too large");
    }

    final Dimensions dimensions =
        switch (contentType) {
          case "image/png" -> parsePngDimensions(avatarBytes);
          case "image/jpeg" -> parseJpegDimensions(avatarBytes);
          case "image/webp" -> parseWebpDimensions(avatarBytes);
          default ->
              throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST, "avatar content type is not allowed");
        };
    if (dimensions == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "avatar file signature does not match content type");
    }
    if (dimensions.width() <= 0 || dimensions.height() <= 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "avatar image dimensions are invalid");
    }
    if (dimensions.width() > storageUploadProperties.getMaxWidth()
        || dimensions.height() > storageUploadProperties.getMaxHeight()
        || (long) dimensions.width() * dimensions.height()
            > storageUploadProperties.getMaxPixels()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "avatar image dimensions are too large");
    }
  }

  /**
   * {@code buildObjectKey}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String buildObjectKey(String ownerChannelId, String contentType) {
    final String normalizedPrefix = trimSlashes(s3StorageProperties.getPrefix());
    final String ownerPath = ownerChannelId.replaceAll("[^a-zA-Z0-9._-]", "_");
    final String fileName = UUID.randomUUID() + "." + extensionFor(contentType);
    if (!StringUtils.hasText(normalizedPrefix)) {
      return ownerPath + "/" + fileName;
    }
    return normalizedPrefix + "/" + ownerPath + "/" + fileName;
  }

  /**
   * {@code extensionFor}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String extensionFor(String contentType) {
    return switch (contentType) {
      case "image/png" -> "png";
      case "image/webp" -> "webp";
      case "image/jpeg" -> "jpg";
      default ->
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "avatar content type is not allowed");
    };
  }

  /**
   * {@code normalizeContentType}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String normalizeContentType(String contentType) {
    return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
  }

  /**
   * {@code trimSlashes}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
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

  /**
   * {@code parsePngDimensions}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private Dimensions parsePngDimensions(byte[] bytes) {
    if (bytes.length < 24
        || bytes[0] != (byte) 0x89
        || bytes[1] != 0x50
        || bytes[2] != 0x4E
        || bytes[3] != 0x47
        || bytes[4] != 0x0D
        || bytes[5] != 0x0A
        || bytes[6] != 0x1A
        || bytes[7] != 0x0A
        || bytes[12] != 0x49
        || bytes[13] != 0x48
        || bytes[14] != 0x44
        || bytes[15] != 0x52) {
      return null;
    }
    return new Dimensions(readIntBigEndian(bytes, 16), readIntBigEndian(bytes, 20));
  }

  /**
   * {@code parseJpegDimensions}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private Dimensions parseJpegDimensions(byte[] bytes) {
    if (bytes.length < 4 || bytes[0] != (byte) 0xFF || bytes[1] != (byte) 0xD8) {
      return null;
    }

    int offset = 2;
    while (offset + 9 < bytes.length) {
      if (bytes[offset] != (byte) 0xFF) {
        offset++;
        continue;
      }
      final int marker = bytes[offset + 1] & 0xFF;
      offset += 2;
      if (marker == 0xD9 || marker == 0xDA) {
        return null;
      }
      if (offset + 2 > bytes.length) {
        return null;
      }
      final int segmentLength = readUnsignedShortBigEndian(bytes, offset);
      if (segmentLength < 2 || offset + segmentLength > bytes.length) {
        return null;
      }
      if (isStartOfFrame(marker)) {
        final int height = readUnsignedShortBigEndian(bytes, offset + 3);
        final int width = readUnsignedShortBigEndian(bytes, offset + 5);
        return new Dimensions(width, height);
      }
      offset += segmentLength;
    }
    return null;
  }

  /**
   * {@code parseWebpDimensions}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private Dimensions parseWebpDimensions(byte[] bytes) {
    if (bytes.length < 30
        || bytes[0] != 0x52
        || bytes[1] != 0x49
        || bytes[2] != 0x46
        || bytes[3] != 0x46
        || bytes[8] != 0x57
        || bytes[9] != 0x45
        || bytes[10] != 0x42
        || bytes[11] != 0x50) {
      return null;
    }

    final String chunkType = new String(bytes, 12, 4, java.nio.charset.StandardCharsets.US_ASCII);
    return switch (chunkType) {
      case "VP8X" ->
          new Dimensions(
              readUInt24LittleEndian(bytes, 24) + 1, readUInt24LittleEndian(bytes, 27) + 1);
      case "VP8 " ->
          new Dimensions(
              readUnsignedShortLittleEndian(bytes, 26) & 0x3FFF,
              readUnsignedShortLittleEndian(bytes, 28) & 0x3FFF);
      case "VP8L" -> parseLosslessWebpDimensions(bytes);
      default -> null;
    };
  }

  /**
   * {@code parseLosslessWebpDimensions}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private Dimensions parseLosslessWebpDimensions(byte[] bytes) {
    if (bytes.length < 25 || bytes[20] != 0x2F) {
      return null;
    }
    final int b1 = bytes[21] & 0xFF;
    final int b2 = bytes[22] & 0xFF;
    final int b3 = bytes[23] & 0xFF;
    final int b4 = bytes[24] & 0xFF;
    final int width = 1 + (((b2 & 0x3F) << 8) | b1);
    final int height = 1 + (((b4 & 0x0F) << 10) | (b3 << 2) | ((b2 & 0xC0) >> 6));
    return new Dimensions(width, height);
  }

  /**
   * {@code isStartOfFrame}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private boolean isStartOfFrame(int marker) {
    return marker >= 0xC0 && marker <= 0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC;
  }

  /**
   * {@code readIntBigEndian}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private int readIntBigEndian(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xFF) << 24)
        | ((bytes[offset + 1] & 0xFF) << 16)
        | ((bytes[offset + 2] & 0xFF) << 8)
        | (bytes[offset + 3] & 0xFF);
  }

  /**
   * {@code readUnsignedShortBigEndian}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private int readUnsignedShortBigEndian(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF);
  }

  /**
   * {@code readUnsignedShortLittleEndian}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private int readUnsignedShortLittleEndian(byte[] bytes, int offset) {
    return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
  }

  /**
   * {@code readUInt24LittleEndian}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private int readUInt24LittleEndian(byte[] bytes, int offset) {
    return (bytes[offset] & 0xFF)
        | ((bytes[offset + 1] & 0xFF) << 8)
        | ((bytes[offset + 2] & 0xFF) << 16);
  }

  /**
   * {@code Dimensions}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private record Dimensions(int width, int height) {}
}
