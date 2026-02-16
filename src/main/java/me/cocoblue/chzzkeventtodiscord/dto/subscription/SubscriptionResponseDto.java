package me.cocoblue.chzzkeventtodiscord.dto.subscription;

import com.fasterxml.jackson.annotation.JsonInclude;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkStreamOnlineFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionResponseDto(
    Long id,
    String channelId,
    String formOwnerChannelId,
    String subscriptionType,
    Long webhookId,
    Long botProfileId,
    String language,
    Integer intervalMinute,
    Boolean enabled,
    String content,
    String colorHex,
    Boolean showDetail,
    Boolean showThumbnail,
    Boolean showViewerCount,
    Boolean showTag,
    ZonedDateTime createdAt
) {
    public static SubscriptionResponseDto fromEntity(ChzzkSubscriptionFormEntity entity) {
        final ChzzkStreamOnlineFormEntity streamOnlineForm =
            entity instanceof ChzzkStreamOnlineFormEntity so ? so : null;

        return new SubscriptionResponseDto(
            entity.getId(),
            entity.getChzzkChannelEntity().getChannelId(),
            entity.getFormOwner().getChannelId(),
            entity.getChzzkSubscriptionType().name(),
            entity.getWebhookId().getId(),
            entity.getBotProfileId().getId(),
            entity.getLanguageIsoData().name(),
            entity.getIntervalMinute(),
            entity.isEnabled(),
            entity.getContent(),
            entity.getColorHex(),
            streamOnlineForm == null ? null : streamOnlineForm.isShowDetail(),
            streamOnlineForm == null ? null : streamOnlineForm.isShowThumbnail(),
            streamOnlineForm == null ? null : streamOnlineForm.isShowViewerCount(),
            streamOnlineForm == null ? null : streamOnlineForm.isShowTag(),
            entity.getCreatedAt()
        );
    }
}
