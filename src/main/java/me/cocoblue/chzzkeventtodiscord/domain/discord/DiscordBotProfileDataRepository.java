package me.cocoblue.chzzkeventtodiscord.domain.discord;

import java.util.Optional;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@code DiscordBotProfileDataRepository}는 엔티티의 데이터 접근과 조회 메서드를 제공합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Repository
public interface DiscordBotProfileDataRepository
    extends JpaRepository<DiscordBotProfileDataEntity, Long> {
  Optional<DiscordBotProfileDataEntity>
      findDiscordBotProfileDataEntityByAvatarUrlAndOwnerIdAndUsername(
          String avatarUrl, ChzzkChannelEntity ownerId, String username);

  /**
   * {@code findAllByOwnerId_ChannelId}은 소유자 채널 ID로 Discord 봇 프로필 목록을 조회합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  Page<DiscordBotProfileDataEntity> findAllByOwnerId_ChannelId(String channelId, Pageable pageable);

  /**
   * {@code findByIdAndOwnerId_ChannelId}은 ID와 소유자 채널 ID로 Discord 봇 프로필을 조회합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  Optional<DiscordBotProfileDataEntity> findByIdAndOwnerId_ChannelId(Long id, String channelId);

  /**
   * {@code existsByAliasAndOwnerId_ChannelId}은 별칭과 소유자 채널 ID가 일치하는 봇 프로필 존재 여부를 확인합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  boolean existsByAliasAndOwnerId_ChannelId(String alias, String channelId);
}
