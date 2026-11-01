package me.cocoblue.chzzkeventtodiscord.service.youtube;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YouTubeApiServiceTests {
    private MockWebServer server;
    private YouTubeApiService service;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        YouTubeApiProperties properties = new YouTubeApiProperties();
        properties.setApiBaseUrl(server.url("/youtube/v3").toString());
        properties.setApiKey("test-key");
        service = new YouTubeApiService(properties);
        service.postConstruct();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void getsChannelByOfficialChannelsEndpoint() throws Exception {
        server.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {"items":[{"id":"UC123","snippet":{"title":"Official Channel","thumbnails":{"high":{"url":"https://example.test/high.jpg"}}}}]}
                """));

        YouTubeChannelSnapshot channel = service.getChannel("UC123");

        assertThat(channel.channelId()).isEqualTo("UC123");
        assertThat(channel.title()).isEqualTo("Official Channel");
        assertThat(channel.thumbnailUrl()).isEqualTo("https://example.test/high.jpg");
        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).contains("/youtube/v3/channels");
        assertThat(request.getPath()).contains("part=snippet");
        assertThat(request.getPath()).contains("id=UC123");
        assertThat(request.getPath()).contains("key=test-key");
    }

    @Test
    void getsLiveVideoByOfficialSearchEndpointWithEventTypeLive() throws Exception {
        server.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {"items":[{"id":{"videoId":"live123"},"snippet":{"title":"Live now","description":"desc","publishedAt":"2026-06-12T00:00:00Z","thumbnails":{"default":{"url":"https://example.test/live.jpg"}}}}]}
                """));

        YouTubeVideoSnapshot live = service.getLiveVideo("UC123");

        assertThat(live.videoId()).isEqualTo("live123");
        assertThat(live.title()).isEqualTo("Live now");
        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).contains("/youtube/v3/search");
        assertThat(request.getPath()).contains("channelId=UC123");
        assertThat(request.getPath()).contains("type=video");
        assertThat(request.getPath()).contains("eventType=live");
    }
}
