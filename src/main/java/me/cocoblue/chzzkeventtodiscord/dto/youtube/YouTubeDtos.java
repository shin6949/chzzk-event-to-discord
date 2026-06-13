package me.cocoblue.chzzkeventtodiscord.dto.youtube;

import lombok.Builder;
import lombok.Data;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.youtube.YouTubeSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeSubscriptionEntity;

import java.time.ZonedDateTime;

public final class YouTubeDtos {
    private YouTubeDtos() {
    }

    @Data
    public static class SubscriptionRequest {
        private String youtubeChannelId;
        private YouTubeSubscriptionType type;
        private Long webhookId;
        private String formOwnerChannelId;
        private LanguageIsoData language;
        private Integer intervalMinute;
        private Boolean enabled;
        private Long botProfileId;
        private String content;
        private String colorHex;
    }

    @Data
    @Builder
    public static class SubscriptionResponse {
        private Long id;
        private String youtubeChannelId;
        private String youtubeChannelTitle;
        private String youtubeChannelThumbnailUrl;
        private YouTubeSubscriptionType type;
        private boolean enabled;
        private Long webhookId;
        private Long botProfileId;
        private int intervalMinute;
        private String colorHex;
        private String content;
        private ZonedDateTime createdAt;

        public static SubscriptionResponse fromEntity(YouTubeSubscriptionEntity entity) {
            return SubscriptionResponse.builder()
                .id(entity.getId())
                .youtubeChannelId(entity.getYoutubeChannel().getChannelId())
                .youtubeChannelTitle(entity.getYoutubeChannel().getTitle())
                .youtubeChannelThumbnailUrl(entity.getYoutubeChannel().getThumbnailUrl())
                .type(entity.getType())
                .enabled(entity.isEnabled())
                .webhookId(entity.getWebhook().getId())
                .botProfileId(entity.getBotProfile().getId())
                .intervalMinute(entity.getIntervalMinute())
                .colorHex(entity.getColorHex())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
        }
    }

    @Data
    @Builder
    public static class ChannelSummary {
        private String channelId;
        private String title;
        private String thumbnailUrl;

        public static ChannelSummary fromEntity(YouTubeChannelEntity entity) {
            return ChannelSummary.builder()
                .channelId(entity.getChannelId())
                .title(entity.getTitle())
                .thumbnailUrl(entity.getThumbnailUrl())
                .build();
        }
    }
}
