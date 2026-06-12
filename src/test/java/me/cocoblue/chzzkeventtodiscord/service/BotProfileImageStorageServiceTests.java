package me.cocoblue.chzzkeventtodiscord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import me.cocoblue.chzzkeventtodiscord.config.AppStaticContentProperties;
import me.cocoblue.chzzkeventtodiscord.config.S3StorageProperties;
import me.cocoblue.chzzkeventtodiscord.config.StorageUploadProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * {@code BotProfileImageStorageServiceTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
class BotProfileImageStorageServiceTests {

  /**
   * {@code uploadStopsBeforeReadingAvatarWhenObjectStorageIsUnavailable}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void uploadStopsBeforeReadingAvatarWhenObjectStorageIsUnavailable() {
    final S3Client s3Client = mock(S3Client.class);
    final MultipartFile avatar = mock(MultipartFile.class);
    final S3StorageProperties s3StorageProperties = new S3StorageProperties();
    final StorageUploadProperties storageUploadProperties = new StorageUploadProperties();
    final S3StorageAvailabilityService s3StorageAvailabilityService =
        mock(S3StorageAvailabilityService.class);
    final StaticContentUrlResolver staticContentUrlResolver =
        new StaticContentUrlResolver(new AppStaticContentProperties(), s3StorageProperties);
    final BotProfileImageStorageService service =
        new BotProfileImageStorageService(
            s3Client,
            s3StorageProperties,
            storageUploadProperties,
            staticContentUrlResolver,
            s3StorageAvailabilityService);
    doThrow(
            new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "image storage is unavailable"))
        .when(s3StorageAvailabilityService)
        .requireAvailable();

    assertThatThrownBy(() -> service.upload("owner-channel", avatar))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception -> {
              final ResponseStatusException responseException = (ResponseStatusException) exception;
              assertThat(responseException.getStatusCode())
                  .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
              assertThat(responseException.getReason()).isEqualTo("image storage is unavailable");
            });
  }

  /**
   * {@code uploadUsesMaterializedAvatarBytesForContentLength}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void uploadUsesMaterializedAvatarBytesForContentLength() throws Exception {
    final S3Client s3Client = mock(S3Client.class);
    final MultipartFile avatar = mock(MultipartFile.class);
    final S3StorageProperties s3StorageProperties = new S3StorageProperties();
    final StorageUploadProperties storageUploadProperties = new StorageUploadProperties();
    final S3StorageAvailabilityService s3StorageAvailabilityService =
        mock(S3StorageAvailabilityService.class);
    final StaticContentUrlResolver staticContentUrlResolver =
        new StaticContentUrlResolver(new AppStaticContentProperties(), s3StorageProperties);
    final BotProfileImageStorageService service =
        new BotProfileImageStorageService(
            s3Client,
            s3StorageProperties,
            storageUploadProperties,
            staticContentUrlResolver,
            s3StorageAvailabilityService);

    when(avatar.isEmpty()).thenReturn(false);
    when(avatar.getSize()).thenReturn(999L);
    when(avatar.getContentType()).thenReturn("image/png");
    final byte[] avatarBytes = pngBytes(32, 32);
    when(avatar.getBytes()).thenReturn(avatarBytes);

    service.upload("owner-channel", avatar);

    final ArgumentCaptor<PutObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
    assertThat(requestCaptor.getValue().contentLength()).isEqualTo((long) avatarBytes.length);
  }

  /**
   * {@code uploadReturnsServiceUnavailableWhenObjectStorageFails}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void uploadReturnsServiceUnavailableWhenObjectStorageFails() throws Exception {
    final S3Client s3Client = mock(S3Client.class);
    final MultipartFile avatar = mock(MultipartFile.class);
    final S3StorageProperties s3StorageProperties = new S3StorageProperties();
    final StorageUploadProperties storageUploadProperties = new StorageUploadProperties();
    final S3StorageAvailabilityService s3StorageAvailabilityService =
        mock(S3StorageAvailabilityService.class);
    final StaticContentUrlResolver staticContentUrlResolver =
        new StaticContentUrlResolver(new AppStaticContentProperties(), s3StorageProperties);
    final BotProfileImageStorageService service =
        new BotProfileImageStorageService(
            s3Client,
            s3StorageProperties,
            storageUploadProperties,
            staticContentUrlResolver,
            s3StorageAvailabilityService);

    when(avatar.isEmpty()).thenReturn(false);
    when(avatar.getSize()).thenReturn(4L);
    when(avatar.getContentType()).thenReturn("image/png");
    when(avatar.getBytes()).thenReturn(pngBytes(32, 32));
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(SdkClientException.builder().message("timeout").build());

    assertThatThrownBy(() -> service.upload("owner-channel", avatar))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception -> {
              final ResponseStatusException responseException = (ResponseStatusException) exception;
              assertThat(responseException.getStatusCode())
                  .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
              assertThat(responseException.getReason())
                  .isEqualTo("image storage connection failed");
            });
  }

  /**
   * {@code uploadRejectsAvatarWhenSignatureDoesNotMatchContentType}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void uploadRejectsAvatarWhenSignatureDoesNotMatchContentType() throws Exception {
    final S3Client s3Client = mock(S3Client.class);
    final MultipartFile avatar = mock(MultipartFile.class);
    final S3StorageProperties s3StorageProperties = new S3StorageProperties();
    final StorageUploadProperties storageUploadProperties = new StorageUploadProperties();
    final S3StorageAvailabilityService s3StorageAvailabilityService =
        mock(S3StorageAvailabilityService.class);
    final StaticContentUrlResolver staticContentUrlResolver =
        new StaticContentUrlResolver(new AppStaticContentProperties(), s3StorageProperties);
    final BotProfileImageStorageService service =
        new BotProfileImageStorageService(
            s3Client,
            s3StorageProperties,
            storageUploadProperties,
            staticContentUrlResolver,
            s3StorageAvailabilityService);

    when(avatar.isEmpty()).thenReturn(false);
    when(avatar.getSize()).thenReturn(4L);
    when(avatar.getContentType()).thenReturn("image/png");
    when(avatar.getBytes()).thenReturn("data".getBytes());

    assertThatThrownBy(() -> service.upload("owner-channel", avatar))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception -> {
              final ResponseStatusException responseException = (ResponseStatusException) exception;
              assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(responseException.getReason())
                  .isEqualTo("avatar file signature does not match content type");
            });
  }

  /**
   * {@code uploadRejectsEmptyAvatarBeforeObjectStorageWrite}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void uploadRejectsEmptyAvatarBeforeObjectStorageWrite() {
    final S3Client s3Client = mock(S3Client.class);
    final MultipartFile avatar = mock(MultipartFile.class);
    final BotProfileImageStorageService service =
        newService(s3Client, new StorageUploadProperties());

    when(avatar.isEmpty()).thenReturn(true);

    assertThatThrownBy(() -> service.upload("owner-channel", avatar))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception -> {
              final ResponseStatusException responseException = (ResponseStatusException) exception;
              assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(responseException.getReason()).isEqualTo("avatar is required");
            });
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  /**
   * {@code uploadRejectsAvatarWhenDeclaredSizeIsTooLargeBeforeReadingBytes}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void uploadRejectsAvatarWhenDeclaredSizeIsTooLargeBeforeReadingBytes() throws Exception {
    final S3Client s3Client = mock(S3Client.class);
    final MultipartFile avatar = mock(MultipartFile.class);
    final StorageUploadProperties storageUploadProperties = new StorageUploadProperties();
    storageUploadProperties.setMaxBytes(3);
    final BotProfileImageStorageService service = newService(s3Client, storageUploadProperties);

    when(avatar.isEmpty()).thenReturn(false);
    when(avatar.getSize()).thenReturn(4L);

    assertThatThrownBy(() -> service.upload("owner-channel", avatar))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception -> {
              final ResponseStatusException responseException = (ResponseStatusException) exception;
              assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(responseException.getReason()).isEqualTo("avatar file is too large");
            });
    verify(avatar, never()).getBytes();
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  /**
   * {@code uploadRejectsAvatarWhenDecodedDimensionsAreTooLarge}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void uploadRejectsAvatarWhenDecodedDimensionsAreTooLarge() throws Exception {
    final S3Client s3Client = mock(S3Client.class);
    final MultipartFile avatar = mock(MultipartFile.class);
    final StorageUploadProperties storageUploadProperties = new StorageUploadProperties();
    storageUploadProperties.setMaxWidth(64);
    final BotProfileImageStorageService service = newService(s3Client, storageUploadProperties);

    when(avatar.isEmpty()).thenReturn(false);
    when(avatar.getSize()).thenReturn(24L);
    when(avatar.getContentType()).thenReturn("image/png");
    when(avatar.getBytes()).thenReturn(pngBytes(65, 32));

    assertThatThrownBy(() -> service.upload("owner-channel", avatar))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception -> {
              final ResponseStatusException responseException = (ResponseStatusException) exception;
              assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(responseException.getReason())
                  .isEqualTo("avatar image dimensions are too large");
            });
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  /**
   * {@code newService}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private static BotProfileImageStorageService newService(
      S3Client s3Client, StorageUploadProperties storageUploadProperties) {
    final S3StorageProperties s3StorageProperties = new S3StorageProperties();
    final S3StorageAvailabilityService s3StorageAvailabilityService =
        mock(S3StorageAvailabilityService.class);
    final StaticContentUrlResolver staticContentUrlResolver =
        new StaticContentUrlResolver(new AppStaticContentProperties(), s3StorageProperties);
    return new BotProfileImageStorageService(
        s3Client,
        s3StorageProperties,
        storageUploadProperties,
        staticContentUrlResolver,
        s3StorageAvailabilityService);
  }

  /**
   * {@code pngBytes}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private static byte[] pngBytes(int width, int height) {
    final byte[] bytes = new byte[24];
    bytes[0] = (byte) 0x89;
    bytes[1] = 0x50;
    bytes[2] = 0x4E;
    bytes[3] = 0x47;
    bytes[4] = 0x0D;
    bytes[5] = 0x0A;
    bytes[6] = 0x1A;
    bytes[7] = 0x0A;
    bytes[12] = 0x49;
    bytes[13] = 0x48;
    bytes[14] = 0x44;
    bytes[15] = 0x52;
    writeIntBigEndian(bytes, 16, width);
    writeIntBigEndian(bytes, 20, height);
    return bytes;
  }

  /**
   * {@code writeIntBigEndian}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private static void writeIntBigEndian(byte[] bytes, int offset, int value) {
    bytes[offset] = (byte) ((value >>> 24) & 0xFF);
    bytes[offset + 1] = (byte) ((value >>> 16) & 0xFF);
    bytes[offset + 2] = (byte) ((value >>> 8) & 0xFF);
    bytes[offset + 3] = (byte) (value & 0xFF);
  }
}
