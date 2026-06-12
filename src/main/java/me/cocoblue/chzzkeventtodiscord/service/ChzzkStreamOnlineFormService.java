package me.cocoblue.chzzkeventtodiscord.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkStreamOnlineFormEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkStreamOnlineFormRepository;
import org.springframework.stereotype.Service;

/**
 * {@code ChzzkStreamOnlineFormService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2024-03-26 17:00:14 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 커밋 ebaedcd.
 *
 * @since Ver.0.1.3
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ChzzkStreamOnlineFormService {
  private final ChzzkStreamOnlineFormRepository chzzkStreamOnlineFormRepository;

  /**
   * {@code findAllByChannelEntityAndSubscriptionTypeAndEnabled}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 17:00:14 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 HEAD blame 커밋
   * ebaedcd.
   *
   * @since Ver.0.1.3
   */
  public List<ChzzkStreamOnlineFormEntity> findAllByChannelEntityAndSubscriptionTypeAndEnabled(
      final String channelId, final ChzzkSubscriptionType type, final boolean enabled) {
    log.debug(
        "Get subscription form by channelId: {} / type: {} / enabled: {}",
        channelId,
        type,
        enabled);
    final ChzzkChannelEntity chzzkChannelEntity =
        ChzzkChannelEntity.builder().channelId(channelId).build();

    return chzzkStreamOnlineFormRepository
        .findAllByChzzkChannelEntityAndChzzkSubscriptionTypeAndEnabled(
            chzzkChannelEntity, type, enabled);
  }

  /**
   * {@code findAllByChannelEntityAndEnabled}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 17:00:14 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 HEAD blame 커밋
   * ebaedcd.
   *
   * @since Ver.0.1.3
   */
  public List<ChzzkStreamOnlineFormEntity> findAllByChannelEntityAndEnabled(
      final String channelId, final boolean enabled) {
    log.debug("Get subscription form by channelId: {} / enabled: {}", channelId, enabled);
    final ChzzkChannelEntity chzzkChannelEntity =
        ChzzkChannelEntity.builder().channelId(channelId).build();

    return chzzkStreamOnlineFormRepository.findAllByChzzkChannelEntityAndEnabled(
        chzzkChannelEntity, enabled);
  }

  /**
   * {@code findAllByEnabled}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 17:00:14 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 HEAD blame 커밋
   * ebaedcd.
   *
   * @since Ver.0.1.3
   */
  public List<ChzzkStreamOnlineFormEntity> findAllByEnabled(final boolean enabled) {
    log.debug("Get subscription form by enabled: {}", enabled);
    return chzzkStreamOnlineFormRepository.findAllByEnabled(enabled);
  }

  /**
   * {@code save}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 17:00:14 +0900, 작성자 shin6949, 작성 버전 Ver.0.1.3, 근거 HEAD blame 커밋
   * ebaedcd.
   *
   * @since Ver.0.1.3
   */
  public void save(final ChzzkStreamOnlineFormEntity chzzkStreamOnlineFormEntity) {
    log.debug("Save subscription form: {}", chzzkStreamOnlineFormEntity);
    chzzkStreamOnlineFormRepository.saveAndFlush(chzzkStreamOnlineFormEntity);
  }
}
