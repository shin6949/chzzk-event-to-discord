package me.cocoblue.chzzkeventtodiscord.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.config.S3StorageProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * {@code S3StorageAvailabilityService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class S3StorageAvailabilityService {
  private static final byte[] CANARY_BODY = "ok".getBytes(StandardCharsets.UTF_8);

  private final S3Client s3Client;
  private final S3StorageProperties s3StorageProperties;
  private final AtomicReference<S3StorageAvailabilitySnapshot> snapshot = new AtomicReference<>();

  /**
   * {@code current}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public S3StorageAvailabilitySnapshot current() {
    final S3StorageAvailabilitySnapshot currentSnapshot = snapshot.get();
    if (currentSnapshot != null) {
      return currentSnapshot;
    }
    if (!s3StorageProperties.isAvailabilityCheckEnabled()) {
      return available("object storage availability check is disabled");
    }
    return unavailable("object storage has not been checked yet");
  }

  /**
   * {@code requireAvailable}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public void requireAvailable() {
    if (!current().available()) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "image storage is unavailable");
    }
  }

  /**
   * {@code checkOnStartup}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @EventListener(ApplicationReadyEvent.class)
  public void checkOnStartup() {
    if (!s3StorageProperties.isAvailabilityCheckEnabled()) {
      snapshot.set(available("object storage availability check is disabled"));
      return;
    }

    final S3StorageAvailabilitySnapshot startupSnapshot = checkNow();
    if (startupSnapshot.available()) {
      log.info(
          "S3-compatible object storage is available. endpoint={}, bucket={}",
          startupSnapshot.endpoint(),
          startupSnapshot.bucket());
    }
    if (s3StorageProperties.isRequiredOnStartup() && !startupSnapshot.available()) {
      throw new IllegalStateException(
          "Object storage is required on startup but unavailable: " + startupSnapshot.reason());
    }
  }

  /**
   * {@code refreshAvailability}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Scheduled(
      fixedDelayString = "${storage.s3.availability-check-interval-millis:60000}",
      initialDelayString = "${storage.s3.availability-check-interval-millis:60000}")
  public void refreshAvailability() {
    if (!s3StorageProperties.isAvailabilityCheckEnabled()) {
      snapshot.set(available("object storage availability check is disabled"));
      return;
    }
    checkNow();
  }

  /**
   * {@code checkNow}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public synchronized S3StorageAvailabilitySnapshot checkNow() {
    if (!s3StorageProperties.isAvailabilityCheckEnabled()) {
      final S3StorageAvailabilitySnapshot disabled =
          available("object storage availability check is disabled");
      snapshot.set(disabled);
      return disabled;
    }

    final String canaryKey = buildCanaryKey();
    final Duration timeout =
        Duration.ofMillis(s3StorageProperties.getAvailabilityCheckTimeoutMillis());

    try {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(s3StorageProperties.getBucket())
              .key(canaryKey)
              .contentType("text/plain")
              .contentLength((long) CANARY_BODY.length)
              .overrideConfiguration(configuration -> configuration.apiCallTimeout(timeout))
              .build(),
          RequestBody.fromBytes(CANARY_BODY));
      s3Client.deleteObject(
          DeleteObjectRequest.builder()
              .bucket(s3StorageProperties.getBucket())
              .key(canaryKey)
              .overrideConfiguration(configuration -> configuration.apiCallTimeout(timeout))
              .build());

      final S3StorageAvailabilitySnapshot available = available(null);
      snapshot.set(available);
      return available;
    } catch (RuntimeException exception) {
      final String reason = describe(exception);
      log.warn(
          "S3-compatible object storage availability check failed. endpoint={}, bucket={}, key={},"
              + " reason={}",
          s3StorageProperties.getEndpoint(),
          s3StorageProperties.getBucket(),
          canaryKey,
          reason);
      log.debug("S3-compatible object storage availability check failure detail.", exception);
      final S3StorageAvailabilitySnapshot unavailable = unavailable(reason);
      snapshot.set(unavailable);
      return unavailable;
    }
  }

  /**
   * {@code available}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private S3StorageAvailabilitySnapshot available(String reason) {
    return new S3StorageAvailabilitySnapshot(
        true,
        Instant.now(),
        reason,
        s3StorageProperties.getEndpoint(),
        s3StorageProperties.getBucket());
  }

  /**
   * {@code unavailable}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private S3StorageAvailabilitySnapshot unavailable(String reason) {
    return new S3StorageAvailabilitySnapshot(
        false,
        Instant.now(),
        reason,
        s3StorageProperties.getEndpoint(),
        s3StorageProperties.getBucket());
  }

  /**
   * {@code buildCanaryKey}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String buildCanaryKey() {
    final String normalizedPrefix = trimSlashes(s3StorageProperties.getPrefix());
    final String fileName = ".health/s3-availability-" + UUID.randomUUID() + ".txt";
    if (!StringUtils.hasText(normalizedPrefix)) {
      return fileName;
    }
    return normalizedPrefix + "/" + fileName;
  }

  /**
   * {@code describe}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String describe(RuntimeException exception) {
    Throwable cursor = exception;
    while (cursor.getCause() != null && cursor.getCause() != cursor) {
      cursor = cursor.getCause();
    }

    final String message = cursor.getMessage();
    if (StringUtils.hasText(message)) {
      return cursor.getClass().getSimpleName() + ": " + message;
    }
    return cursor.getClass().getSimpleName();
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
}
