package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDto;
import org.springframework.stereotype.Service;

/**
 * {@code ChzzkEventClassifier}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
 *
 * @since Ver.0.1
 */
@Log4j2
@Service
public class ChzzkEventClassifier {
  /**
   * {@code isOnNewLive}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
   *
   * @since Ver.0.1
   */
  public boolean isOnNewLive(
      final ChzzkChannelDto channelDataFromDatabase, final ChzzkChannelDto channelDataFromApi) {
    if (!channelDataFromDatabase.isOpenLive() && channelDataFromApi.isOpenLive()) {
      log.info("New live streaming started. channelId: {}", channelDataFromDatabase.getChannelId());
      return true;
    }

    log.debug("Live status is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
    return false;
  }

  /**
   * {@code isOnNewOffline}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
   *
   * @since Ver.0.1
   */
  public boolean isOnNewOffline(
      final ChzzkChannelDto channelDataFromDatabase, final ChzzkChannelDto channelDataFromApi) {
    if (channelDataFromDatabase.isOpenLive() && !channelDataFromApi.isOpenLive()) {
      log.info("Streaming is now offline. channelId: {}", channelDataFromDatabase.getChannelId());
      return true;
    }

    log.debug("Live status is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
    return false;
  }

  /**
   * {@code isChannelInformationChanged}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
   *
   * @since Ver.0.1
   */
  public boolean isChannelInformationChanged(
      final ChzzkChannelDto channelDataFromDatabase, final ChzzkChannelDto channelDataFromApi) {
    if (!channelDataFromDatabase.equals(channelDataFromApi)) {
      log.info(
          "Channel information is changed. channelId: {}", channelDataFromDatabase.getChannelId());
      return true;
    }

    log.debug(
        "Channel information is not changed. channelId: {}",
        channelDataFromDatabase.getChannelId());
    return false;
  }

  /**
   * {@code isFollowerCountChanged}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
   *
   * @since Ver.0.1
   */
  public boolean isFollowerCountChanged(
      final ChzzkChannelDto channelDataFromDatabase, final ChzzkChannelDto channelDataFromApi) {
    if (channelDataFromDatabase.getFollowerCount() != channelDataFromApi.getFollowerCount()) {
      log.info("Follower count is changed. channelId: {}", channelDataFromDatabase.getChannelId());
      return true;
    }

    log.debug(
        "Follower count is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
    return false;
  }
}
