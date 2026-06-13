package me.cocoblue.chzzkeventtodiscord.dto.soop;

import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionEntity;

import java.time.ZonedDateTime;

public record SoopSubscriptionResponseDto(
    Long id,
    String soopUserId,
    String soopChannelName,
    String profileImageUrl,
    boolean live,
    String liveTitle,
    String liveUrl,
    Long webhookId,
    Long botProfileId,
    String formOwnerChannelId,
    boolean enabled,
    boolean notifyOnline,
    boolean notifyOffline,
    String content,
    String colorHex,
    ZonedDateTime createdAt
) {
    public static SoopSubscriptionResponseDto fromEntity(SoopSubscriptionEntity entity) {
        return new SoopSubscriptionResponseDto(
            entity.getId(),
            entity.getSoopUserId(),
            entity.getSoopChannelName(),
            entity.getProfileImageUrl(),
            entity.isLive(),
            entity.getLiveTitle(),
            entity.getLiveUrl(),
            entity.getWebhook().getId(),
            entity.getBotProfile().getId(),
            entity.getFormOwner().getChannelId(),
            entity.isEnabled(),
            entity.isNotifyOnline(),
            entity.isNotifyOffline(),
            entity.getContent(),
            entity.getColorHex(),
            entity.getCreatedAt()
        );
    }
}
