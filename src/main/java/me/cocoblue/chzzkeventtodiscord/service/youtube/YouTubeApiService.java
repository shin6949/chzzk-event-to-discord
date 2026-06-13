package me.cocoblue.chzzkeventtodiscord.service.youtube;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.youtube.YouTubeApiModels;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YouTubeApiService {
    private final YouTubeApiProperties properties;
    private WebClient webClient;

    @PostConstruct
    public void postConstruct() {
        webClient = WebClient.builder().baseUrl(properties.getApiBaseUrl()).build();
    }

    public YouTubeChannelSnapshot getChannel(String channelId) {
        requireConfiguredApiKey();
        YouTubeApiModels.ChannelsResponse response = webClient.get()
            .uri(uriBuilder -> uriBuilder.path("/channels")
                .queryParam("part", "snippet")
                .queryParam("id", channelId)
                .queryParam("key", properties.getApiKey())
                .build())
            .retrieve()
            .bodyToMono(YouTubeApiModels.ChannelsResponse.class)
            .block();

        List<YouTubeApiModels.ChannelItem> items = response == null ? List.of() : response.items();
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "YouTube channel not found");
        }
        YouTubeApiModels.ChannelItem item = items.get(0);
        return new YouTubeChannelSnapshot(item.id(), item.snippet().title(), item.snippet().defaultThumbnailUrl());
    }

    public YouTubeChannelState getChannelState(String channelId) {
        return new YouTubeChannelState(getChannel(channelId), getLiveVideo(channelId), getLatestVideo(channelId));
    }

    public YouTubeVideoSnapshot getLiveVideo(String channelId) {
        return searchFirstVideo(channelId, "date", "live");
    }

    public YouTubeVideoSnapshot getLatestVideo(String channelId) {
        return searchFirstVideo(channelId, "date", null);
    }

    private YouTubeVideoSnapshot searchFirstVideo(String channelId, String order, String eventType) {
        requireConfiguredApiKey();
        YouTubeApiModels.SearchResponse response = webClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path("/search")
                    .queryParam("part", "snippet")
                    .queryParam("channelId", channelId)
                    .queryParam("type", "video")
                    .queryParam("order", order)
                    .queryParam("maxResults", 1)
                    .queryParam("key", properties.getApiKey());
                if (StringUtils.hasText(eventType)) {
                    builder.queryParam("eventType", eventType);
                }
                return builder.build();
            })
            .retrieve()
            .bodyToMono(YouTubeApiModels.SearchResponse.class)
            .block();

        List<YouTubeApiModels.SearchItem> items = response == null ? List.of() : response.items();
        if (items == null || items.isEmpty()) {
            return null;
        }
        YouTubeApiModels.SearchItem item = items.get(0);
        return new YouTubeVideoSnapshot(
            item.videoId(),
            item.snippet().title(),
            item.snippet().description(),
            item.snippet().defaultThumbnailUrl(),
            item.snippet().publishedAt()
        );
    }

    private void requireConfiguredApiKey() {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "YouTube Data API key is not configured");
        }
    }
}
