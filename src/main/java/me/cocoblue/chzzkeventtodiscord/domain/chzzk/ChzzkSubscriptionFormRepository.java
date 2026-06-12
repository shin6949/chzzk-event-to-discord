package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import java.util.List;
import java.util.Optional;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@code ChzzkSubscriptionFormRepository}는 엔티티의 데이터 접근과 조회 메서드를 제공합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Repository
public interface ChzzkSubscriptionFormRepository
    extends JpaRepository<ChzzkSubscriptionFormEntity, Long> {
  /**
   * {@code findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled}은 채널, 구독 유형, 활성 상태로 구독 폼
   * 목록을 조회합니다.
   *
   * <p>Git 이력: 생성 2024-03-01 23:49:24 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 de44356.
   *
   * @since Ver.0.1
   */
  List<ChzzkSubscriptionFormEntity> findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(
      ChzzkChannelEntity chzzkChannelEntity,
      ChzzkSubscriptionType chzzkSubscriptionType,
      boolean enabled);

  /**
   * {@code findAllByChzzkChannelEntityAndEnabled}은 채널과 활성 상태로 구독 폼 목록을 조회합니다.
   *
   * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
   *
   * @since Ver.0.1
   */
  List<ChzzkSubscriptionFormEntity> findAllByChzzkChannelEntityAndEnabled(
      ChzzkChannelEntity chzzkChannelEntity, boolean enabled);

  /**
   * {@code findAllByEnabled}은 활성 상태로 구독 폼 목록을 조회합니다.
   *
   * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
   *
   * @since Ver.0.1
   */
  List<ChzzkSubscriptionFormEntity> findAllByEnabled(boolean enabled);

  /**
   * {@code findAllByFormOwner_ChannelId}은 소유자 채널 ID로 구독 폼 목록을 조회합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  Page<ChzzkSubscriptionFormEntity> findAllByFormOwner_ChannelId(
      String channelId, Pageable pageable);

  /**
   * {@code findByIdAndFormOwner_ChannelId}은 ID와 소유자 채널 ID로 구독 폼을 조회합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  Optional<ChzzkSubscriptionFormEntity> findByIdAndFormOwner_ChannelId(Long id, String channelId);

  /**
   * {@code existsByWebhookId_Id}은 webhook ID를 참조하는 구독 폼 존재 여부를 확인합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  boolean existsByWebhookId_Id(Long webhookId);

  /**
   * {@code existsByBotProfileId_Id}은 봇 프로필 ID를 참조하는 구독 폼 존재 여부를 확인합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  boolean existsByBotProfileId_Id(Long botProfileId);
}
