package me.cocoblue.chzzkeventtodiscord.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import me.cocoblue.chzzkeventtodiscord.service.S3StorageAvailabilityService;
import me.cocoblue.chzzkeventtodiscord.service.S3StorageAvailabilitySnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code SystemCapabilitiesControllerTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SystemCapabilitiesControllerTests {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private S3StorageAvailabilityService s3StorageAvailabilityService;

  /**
   * {@code returnsUploadCapabilitiesFromObjectStorageAvailability}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void returnsUploadCapabilitiesFromObjectStorageAvailability() throws Exception {
    when(s3StorageAvailabilityService.current())
        .thenReturn(
            new S3StorageAvailabilitySnapshot(
                false,
                Instant.parse("2026-05-21T02:20:00Z"),
                "ApiCallTimeoutException: timeout",
                "http://minio:9000",
                "streaming-alert-service-assets"));

    mockMvc
        .perform(
            get("/api/v1/system/capabilities")
                .with(SecurityMockMvcRequestPostProcessors.user("owner-channel").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uploads.botProfileAvatar.enabled").value(false))
        .andExpect(
            jsonPath("$.uploads.botProfileAvatar.reason").value("ApiCallTimeoutException: timeout"))
        .andExpect(jsonPath("$.uploads.botProfileAvatar.checkedAt").value("2026-05-21T02:20:00Z"));
  }
}
