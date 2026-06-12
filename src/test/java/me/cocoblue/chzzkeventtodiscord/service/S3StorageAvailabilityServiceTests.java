package me.cocoblue.chzzkeventtodiscord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import me.cocoblue.chzzkeventtodiscord.config.S3StorageProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * {@code S3StorageAvailabilityServiceTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
class S3StorageAvailabilityServiceTests {

  /**
   * {@code checkNowMarksStorageAvailableAfterCanaryPutAndDelete}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void checkNowMarksStorageAvailableAfterCanaryPutAndDelete() {
    final S3Client s3Client = mock(S3Client.class);
    final S3StorageProperties properties = new S3StorageProperties();
    final S3StorageAvailabilityService service =
        new S3StorageAvailabilityService(s3Client, properties);

    final S3StorageAvailabilitySnapshot snapshot = service.checkNow();

    final ArgumentCaptor<PutObjectRequest> putCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(putCaptor.capture(), any(RequestBody.class));
    verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    assertThat(snapshot.available()).isTrue();
    assertThat(snapshot.reason()).isNull();
    assertThat(putCaptor.getValue().key()).startsWith("bot-profiles/.health/s3-availability-");
  }

  /**
   * {@code checkNowMarksStorageUnavailableAndRequireAvailableFailsWhenCanaryPutFails}은 해당 클래스의 세부
   * 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void checkNowMarksStorageUnavailableAndRequireAvailableFailsWhenCanaryPutFails() {
    final S3Client s3Client = mock(S3Client.class);
    final S3StorageProperties properties = new S3StorageProperties();
    final S3StorageAvailabilityService service =
        new S3StorageAvailabilityService(s3Client, properties);
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(SdkClientException.builder().message("timeout").build());

    final S3StorageAvailabilitySnapshot snapshot = service.checkNow();

    assertThat(snapshot.available()).isFalse();
    assertThat(snapshot.reason()).contains("timeout");
    assertThatThrownBy(service::requireAvailable)
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception -> {
              final ResponseStatusException responseException = (ResponseStatusException) exception;
              assertThat(responseException.getStatusCode())
                  .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
              assertThat(responseException.getReason()).isEqualTo("image storage is unavailable");
            });
  }
}
