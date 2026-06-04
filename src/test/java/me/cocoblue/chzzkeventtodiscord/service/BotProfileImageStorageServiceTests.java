package me.cocoblue.chzzkeventtodiscord.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

class BotProfileImageStorageServiceTests {

    @Test
    void uploadStopsBeforeReadingAvatarWhenObjectStorageIsUnavailable() {
        final S3Client s3Client = mock(S3Client.class);
        final MultipartFile avatar = mock(MultipartFile.class);
        final S3StorageProperties s3StorageProperties = new S3StorageProperties();
        final StorageUploadProperties storageUploadProperties = new StorageUploadProperties();
        final S3StorageAvailabilityService s3StorageAvailabilityService = mock(S3StorageAvailabilityService.class);
        final StaticContentUrlResolver staticContentUrlResolver = new StaticContentUrlResolver(
            new AppStaticContentProperties(),
            s3StorageProperties
        );
        final BotProfileImageStorageService service = new BotProfileImageStorageService(
            s3Client,
            s3StorageProperties,
            storageUploadProperties,
            staticContentUrlResolver,
            s3StorageAvailabilityService
        );
        doThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "image storage is unavailable"))
            .when(s3StorageAvailabilityService)
            .requireAvailable();

        assertThatThrownBy(() -> service.upload("owner-channel", avatar))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> {
                final ResponseStatusException responseException = (ResponseStatusException) exception;
                assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                assertThat(responseException.getReason()).isEqualTo("image storage is unavailable");
            });
    }

    @Test
    void uploadUsesMaterializedAvatarBytesForContentLength() throws Exception {
        final S3Client s3Client = mock(S3Client.class);
        final MultipartFile avatar = mock(MultipartFile.class);
        final S3StorageProperties s3StorageProperties = new S3StorageProperties();
        final StorageUploadProperties storageUploadProperties = new StorageUploadProperties();
        final S3StorageAvailabilityService s3StorageAvailabilityService = mock(S3StorageAvailabilityService.class);
        final StaticContentUrlResolver staticContentUrlResolver = new StaticContentUrlResolver(
            new AppStaticContentProperties(),
            s3StorageProperties
        );
        final BotProfileImageStorageService service = new BotProfileImageStorageService(
            s3Client,
            s3StorageProperties,
            storageUploadProperties,
            staticContentUrlResolver,
            s3StorageAvailabilityService
        );

        when(avatar.isEmpty()).thenReturn(false);
        when(avatar.getSize()).thenReturn(999L);
        when(avatar.getContentType()).thenReturn("image/png");
        when(avatar.getBytes()).thenReturn("data".getBytes());

        service.upload("owner-channel", avatar);

        final ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        assertThat(requestCaptor.getValue().contentLength()).isEqualTo(4L);
    }

    @Test
    void uploadReturnsServiceUnavailableWhenObjectStorageFails() throws Exception {
        final S3Client s3Client = mock(S3Client.class);
        final MultipartFile avatar = mock(MultipartFile.class);
        final S3StorageProperties s3StorageProperties = new S3StorageProperties();
        final StorageUploadProperties storageUploadProperties = new StorageUploadProperties();
        final S3StorageAvailabilityService s3StorageAvailabilityService = mock(S3StorageAvailabilityService.class);
        final StaticContentUrlResolver staticContentUrlResolver = new StaticContentUrlResolver(
            new AppStaticContentProperties(),
            s3StorageProperties
        );
        final BotProfileImageStorageService service = new BotProfileImageStorageService(
            s3Client,
            s3StorageProperties,
            storageUploadProperties,
            staticContentUrlResolver,
            s3StorageAvailabilityService
        );

        when(avatar.isEmpty()).thenReturn(false);
        when(avatar.getSize()).thenReturn(4L);
        when(avatar.getContentType()).thenReturn("image/png");
        when(avatar.getBytes()).thenReturn("data".getBytes());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(SdkClientException.builder().message("timeout").build());

        assertThatThrownBy(() -> service.upload("owner-channel", avatar))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> {
                final ResponseStatusException responseException = (ResponseStatusException) exception;
                assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                assertThat(responseException.getReason()).isEqualTo("image storage connection failed");
            });
    }
}
