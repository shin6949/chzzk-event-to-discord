package me.cocoblue.chzzkeventtodiscord.dto.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouTubeApiModels() {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchResponse(List<SearchItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChannelsResponse(List<ChannelItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchItem(Id id, Snippet snippet) {
        public String videoId() {
            return id == null ? null : id.videoId();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChannelItem(String id, Snippet snippet) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Id(@JsonProperty("videoId") String videoId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snippet(String title, String description, OffsetDateTime publishedAt, Map<String, Thumbnail> thumbnails) {
        public String defaultThumbnailUrl() {
            if (thumbnails == null || thumbnails.isEmpty()) {
                return null;
            }
            Thumbnail selected = thumbnails.get("high");
            if (selected == null) {
                selected = thumbnails.get("medium");
            }
            if (selected == null) {
                selected = thumbnails.get("default");
            }
            return selected == null ? null : selected.url();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnail(String url) {
    }
}
