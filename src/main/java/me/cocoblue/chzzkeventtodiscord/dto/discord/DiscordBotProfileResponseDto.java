package me.cocoblue.chzzkeventtodiscord.dto.discord;

import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.service.StaticContentUrlResolver;

/**
 * {@code DiscordBotProfileResponseDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
public record DiscordBotProfileResponseDto(
    Long id, String alias, String username, String avatarUrl, String ownerChannelId) {
  /**
   * {@code fromEntity}은 Discord 봇 프로필 엔티티를 응답 DTO로 변환합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public static DiscordBotProfileResponseDto fromEntity(
      DiscordBotProfileDataEntity entity, StaticContentUrlResolver staticContentUrlResolver) {
    return new DiscordBotProfileResponseDto(
        entity.getId(),
        entity.getAlias(),
        entity.getUsername(),
        staticContentUrlResolver.resolve(entity.getAvatarUrl()),
        entity.getOwnerId().getChannelId());
  }
}
