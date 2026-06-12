package me.cocoblue.chzzkeventtodiscord.domain.eventlog;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * {@code NotificationLogRepository}는 엔티티의 데이터 접근과 조회 메서드를 제공합니다.
 *
 * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
 *
 * @since Ver.0.1
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {
  /**
   * {@code getCountBySubscriptionFormAndCreatedAtBetween}은 구독 폼과 기간에 해당하는 알림 로그를 조회합니다.
   *
   * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
   *
   * @since Ver.0.1
   */
  List<NotificationLogEntity> getCountBySubscriptionFormAndCreatedAtBetween(
      ChzzkSubscriptionFormEntity subscriptionForm, ZonedDateTime start, ZonedDateTime end);

  @Query(
      """
      select notificationLog.subscriptionForm.id as subscriptionId,
             max(notificationLog.createdAt) as lastNotificationSentAt
      from notification_log notificationLog
      where notificationLog.subscriptionForm.id in :subscriptionIds
      group by notificationLog.subscriptionForm.id
      """)
  List<LastNotificationSentAtProjection> findLastNotificationSentAtBySubscriptionIds(
      @Param("subscriptionIds") Collection<Long> subscriptionIds);

  /**
   * {@code LastNotificationSentAtProjection}는 구현체 또는 Spring Data가 따라야 할 계약을 정의합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  interface LastNotificationSentAtProjection {
    /**
     * {@code getSubscriptionId}은 필요한 데이터를 조회하거나 해석합니다.
     *
     * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
     * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
     *
     * @since unreleased after Ver.0.1.4
     */
    Long getSubscriptionId();

    /**
     * {@code getLastNotificationSentAt}은 필요한 데이터를 조회하거나 해석합니다.
     *
     * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
     * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
     *
     * @since unreleased after Ver.0.1.4
     */
    ZonedDateTime getLastNotificationSentAt();
  }
}
