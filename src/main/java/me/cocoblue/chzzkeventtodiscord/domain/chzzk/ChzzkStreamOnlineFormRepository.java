package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import java.util.List;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@code ChzzkStreamOnlineFormRepository}는 엔티티의 데이터 접근과 조회 메서드를 제공합니다.
 *
 * <p>Git 이력: 생성 2024-03-26 17:00:14 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 커밋 ebaedcd.
 *
 * @since Ver.0.1.3
 */
@Repository
public interface ChzzkStreamOnlineFormRepository
    extends JpaRepository<ChzzkStreamOnlineFormEntity, ChzzkSubscriptionFormEntity> {
  /**
   * {@code findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled}은 채널, 구독 유형, 활성 상태로 스트림 알림
   * 폼 목록을 조회합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 17:00:14 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 커밋 ebaedcd.
   *
   * @since Ver.0.1.3
   */
  List<ChzzkStreamOnlineFormEntity> findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(
      ChzzkChannelEntity chzzkChannelEntity,
      ChzzkSubscriptionType chzzkSubscriptionType,
      boolean enabled);

  /**
   * {@code findAllByChzzkChannelEntityAndEnabled}은 채널과 활성 상태로 스트림 알림 폼 목록을 조회합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 17:00:14 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 커밋 ebaedcd.
   *
   * @since Ver.0.1.3
   */
  List<ChzzkStreamOnlineFormEntity> findAllByChzzkChannelEntityAndEnabled(
      ChzzkChannelEntity chzzkChannelEntity, boolean enabled);

  /**
   * {@code findAllByEnabled}은 활성 상태로 스트림 알림 폼 목록을 조회합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 17:00:14 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 커밋 ebaedcd.
   *
   * @since Ver.0.1.3
   */
  List<ChzzkStreamOnlineFormEntity> findAllByEnabled(boolean enabled);
}
