package me.cocoblue.chzzkeventtodiscord.controller;

import me.cocoblue.chzzkeventtodiscord.service.S3StorageAvailabilityService;
import me.cocoblue.chzzkeventtodiscord.service.S3StorageAvailabilitySnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SystemCapabilitiesControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3StorageAvailabilityService s3StorageAvailabilityService;

    @Test
    void returnsUploadCapabilitiesFromObjectStorageAvailability() throws Exception {
        when(s3StorageAvailabilityService.current()).thenReturn(new S3StorageAvailabilitySnapshot(
            false,
            Instant.parse("2026-05-21T02:20:00Z"),
            "ApiCallTimeoutException: timeout",
            "http://minio:9000",
            "chzzk-event-assets"
        ));

        mockMvc.perform(get("/api/v1/system/capabilities")
                .with(SecurityMockMvcRequestPostProcessors.user("owner-channel").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uploads.botProfileAvatar.enabled").value(false))
            .andExpect(jsonPath("$.uploads.botProfileAvatar.reason").value("ApiCallTimeoutException: timeout"))
            .andExpect(jsonPath("$.uploads.botProfileAvatar.checkedAt").value("2026-05-21T02:20:00Z"));
    }
}
