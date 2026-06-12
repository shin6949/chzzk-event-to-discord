package me.cocoblue.chzzkeventtodiscord.dto.subscription;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.ZonedDateTime;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkStreamOnlineFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;

/**
 * {@code SubscriptionResponseDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 7fec3f1.
 *
 * @since unreleased after Ver.0.1.4
 */
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
    ZonedDateTime createdAt,
    ZonedDateTime lastNotificationSentAt) {
  /**
   * {@code fromEntity}은 구독 폼 엔티티를 응답 DTO로 변환합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  public static SubscriptionResponseDto fromEntity(ChzzkSubscriptionFormEntity entity) {
    return fromEntity(entity, null);
  }

  /**
   * {@code fromEntity}은 구독 폼 엔티티와 마지막 알림 시각을 응답 DTO로 변환합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  public static SubscriptionResponseDto fromEntity(
      ChzzkSubscriptionFormEntity entity, ZonedDateTime lastNotificationSentAt) {
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
        entity.getCreatedAt(),
        lastNotificationSentAt);
  }
}
