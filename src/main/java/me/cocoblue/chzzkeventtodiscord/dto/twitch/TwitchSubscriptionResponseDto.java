package me.cocoblue.chzzkeventtodiscord.dto.twitch;

import me.cocoblue.chzzkeventtodiscord.domain.twitch.TwitchEventSubscriptionEntity;

import java.time.ZonedDateTime;

public record TwitchSubscriptionResponseDto(
    Long id,
    String ownerChannelId,
    String broadcasterUserId,
    String broadcasterLogin,
    String broadcasterDisplayName,
    Boolean enabled,
    String eventsubOnlineId,
    String eventsubOfflineId,
    ZonedDateTime createdAt
) {
    public static TwitchSubscriptionResponseDto fromEntity(TwitchEventSubscriptionEntity entity) {
        return new TwitchSubscriptionResponseDto(
            entity.getId(),
            entity.getOwnerChannelId(),
            entity.getBroadcasterUserId(),
            entity.getBroadcasterLogin(),
            entity.getBroadcasterDisplayName(),
            entity.isEnabled(),
            entity.getEventsubOnlineId(),
            entity.getEventsubOfflineId(),
            entity.getCreatedAt()
        );
    }
}
