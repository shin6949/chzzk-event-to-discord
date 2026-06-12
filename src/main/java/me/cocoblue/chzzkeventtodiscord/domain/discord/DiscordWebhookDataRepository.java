package me.cocoblue.chzzkeventtodiscord.domain.discord;

import java.util.Optional;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@code DiscordWebhookDataRepository}는 엔티티의 데이터 접근과 조회 메서드를 제공합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Repository
public interface DiscordWebhookDataRepository
    extends JpaRepository<DiscordWebhookDataEntity, Long> {
  /**
   * {@code findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId}은 URL, 이름, 소유자로 Discord
   * webhook 데이터를 조회합니다.
   *
   * <p>Git 이력: 생성 2024-03-05 20:28:48 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.1, 근거 커밋 742a362.
   *
   * @since Ver.0.1.1
   */
  Optional<DiscordWebhookDataEntity> findDiscordWebhookDataEntityByWebhookUrlAndNameAndOwnerId(
      String webhookUrl, String name, ChzzkChannelEntity ownerId);

  /**
   * {@code findAllByOwnerId_ChannelId}은 소유자 채널 ID로 Discord webhook 목록을 조회합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  Page<DiscordWebhookDataEntity> findAllByOwnerId_ChannelId(String channelId, Pageable pageable);

  /**
   * {@code findByIdAndOwnerId_ChannelId}은 ID와 소유자 채널 ID로 Discord webhook을 조회합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  Optional<DiscordWebhookDataEntity> findByIdAndOwnerId_ChannelId(Long id, String channelId);

  /**
   * {@code existsByNameAndOwnerId_ChannelId}은 이름과 소유자 채널 ID가 일치하는 webhook 존재 여부를 확인합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  boolean existsByNameAndOwnerId_ChannelId(String name, String channelId);
}
