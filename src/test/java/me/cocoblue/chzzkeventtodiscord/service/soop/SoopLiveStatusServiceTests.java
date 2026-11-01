package me.cocoblue.chzzkeventtodiscord.service.soop;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.cocoblue.chzzkeventtodiscord.config.soop.SoopProperties;
import me.cocoblue.chzzkeventtodiscord.dto.soop.SoopLiveStatusDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class SoopLiveStatusServiceTests {
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void getLiveStatusFindsTargetBroadcasterFromOfficialBroadList() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("callback({\"total_cnt\":1,\"page_no\":1,\"page_block\":60,\"broad\":[{\"user_id\":\"soop123\",\"user_nick\":\"꿀잼\",\"profile_img\":\"//profile.img.sooplive.com/logo.jpg\",\"broad_no\":\"2232\",\"broad_title\":\"테스트 방송\",\"broad_thumb\":\"//liveimg.sooplive.com/m/2232\",\"broad_start\":\"2026-06-12 10:00:00\",\"total_view_cnt\":\"42\"}],\"time\":1781234567});"));

        final SoopLiveStatusService service = new SoopLiveStatusService(
            new SoopProperties(mockWebServer.url("/").toString(), "client-id", 30, 2),
            new ObjectMapper(),
            WebClient.builder()
        );

        final SoopLiveStatusDto status = service.getLiveStatus("soop123");

        assertThat(status.live()).isTrue();
        assertThat(status.userNick()).isEqualTo("꿀잼");
        assertThat(status.liveUrl()).isEqualTo("https://play.sooplive.co.kr/soop123/2232");
        assertThat(mockWebServer.takeRequest().getPath()).contains("/broad/list").contains("client_id=client-id");
    }

    @Test
    void getLiveStatusReturnsOfflineWhenTargetIsMissing() {
        mockWebServer.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("{\"total_cnt\":0,\"page_no\":1,\"page_block\":60,\"broad\":[],\"time\":1781234567}"));

        final SoopLiveStatusService service = new SoopLiveStatusService(
            new SoopProperties(mockWebServer.url("/").toString(), "client-id", 30, 2),
            new ObjectMapper(),
            WebClient.builder()
        );

        final SoopLiveStatusDto status = service.getLiveStatus("offline_user");

        assertThat(status.live()).isFalse();
        assertThat(status.liveUrl()).isEqualTo("https://ch.sooplive.co.kr/offline_user");
    }
}
