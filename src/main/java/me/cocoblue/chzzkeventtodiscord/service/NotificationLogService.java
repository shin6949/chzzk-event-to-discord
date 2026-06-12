package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.eventlog.NotificationLogEntity;
import me.cocoblue.chzzkeventtodiscord.domain.eventlog.NotificationLogRepository;
import org.springframework.stereotype.Service;

/**
 * {@code NotificationLogService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
 *
 * @since Ver.0.1
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class NotificationLogService {
  private final NotificationLogRepository notificationLogRepository;

  /**
   * {@code insertNotificationLog}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
   *
   * @since Ver.0.1
   */
  @Transactional
  public void insertNotificationLog(final Long subscriptionId) {
    log.info("Insert notification log. subscriptionId: {}", subscriptionId);
    final ChzzkSubscriptionFormEntity subscriptionForm =
        ChzzkSubscriptionFormEntity.builder().id(subscriptionId).build();

    final NotificationLogEntity notificationLogEntity =
        NotificationLogEntity.builder().subscriptionForm(subscriptionForm).build();

    log.info("Insert notificationLogEntity. notificationLogEntity: {}", notificationLogEntity);
    notificationLogRepository.save(notificationLogEntity);
  }

  /**
   * {@code findLastNotificationSentAtBySubscriptionIds}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public Map<Long, ZonedDateTime> findLastNotificationSentAtBySubscriptionIds(
      final Collection<Long> subscriptionIds) {
    if (subscriptionIds.isEmpty()) {
      return Map.of();
    }

    return notificationLogRepository
        .findLastNotificationSentAtBySubscriptionIds(subscriptionIds)
        .stream()
        .collect(
            Collectors.toMap(
                NotificationLogRepository.LastNotificationSentAtProjection::getSubscriptionId,
                NotificationLogRepository.LastNotificationSentAtProjection
                    ::getLastNotificationSentAt));
  }
}
