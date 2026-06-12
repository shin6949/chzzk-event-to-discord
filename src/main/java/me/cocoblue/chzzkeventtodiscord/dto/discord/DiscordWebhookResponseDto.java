package me.cocoblue.chzzkeventtodiscord.dto.discord;

import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.security.SecretEncryptionService;

/**
 * {@code DiscordWebhookResponseDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
public record DiscordWebhookResponseDto(
    Long id, String alias, String maskedUrl, boolean hasUrl, String ownerChannelId) {
  /**
   * {@code fromEntity}은 Discord webhook 엔티티를 응답 DTO로 변환합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public static DiscordWebhookResponseDto fromEntity(
      DiscordWebhookDataEntity entity, SecretEncryptionService secretEncryptionService) {
    final String url = secretEncryptionService.decryptIfNeeded(entity.getWebhookUrl());
    return new DiscordWebhookResponseDto(
        entity.getId(),
        entity.getName(),
        mask(url),
        url != null && !url.isBlank(),
        entity.getOwnerId().getChannelId());
  }

  /**
   * {@code mask}는 민감한 webhook URL을 표시 가능한 형태로 마스킹합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private static String mask(String url) {
    if (url == null || url.isBlank()) {
      return "";
    }
    final int lastSlash = url.lastIndexOf('/');
    if (lastSlash < 0 || lastSlash == url.length() - 1) {
      return "****";
    }
    return url.substring(0, lastSlash + 1) + "****";
  }
}
