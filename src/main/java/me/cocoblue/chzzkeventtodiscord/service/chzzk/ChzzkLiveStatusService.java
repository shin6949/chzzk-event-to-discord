package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveDto;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveStatusDto;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkLiveVo;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkLiveStatusApiResponseVo;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkSearchApiResponseVo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@code ChzzkLiveStatusService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2024-02-28 21:28:31 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1, 근거 커밋 99215d6.
 *
 * @since Ver.0.1
 */
@Log4j2
@Service
public class ChzzkLiveStatusService {
  private WebClient WEB_CLIENT;

  /**
   * {@code postConstructJob}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-03-01 00:49:18 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 ad3cd2f.
   *
   * @since Ver.0.1
   */
  @PostConstruct
  public void postConstructJob() {
    WEB_CLIENT =
        WebClient.builder()
            .baseUrl(ChzzkEventToDiscordApplication.CHZZK_API_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  /**
   * {@code getLiveStatusFromApi}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
   *
   * @since Ver.0.1.3
   */
  public ChzzkLiveStatusDto getLiveStatusFromApi(final String channelId) {
    final String url = "/polling/v2/channels/%s/live-status";

    final ChzzkLiveStatusApiResponseVo result =
        WEB_CLIENT
            .get()
            .uri(String.format(url, channelId))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(ChzzkLiveStatusApiResponseVo.class)
            .block();

    if (result == null || result.getCode() != 200) {
      log.error(
          "Failed to get channel info by channel id from Chzzk API. Check argument is valid or"
              + " update the API status. channelId: {}",
          channelId);
      return null;
    }

    return result.toDto();
  }

  /**
   * {@code getLiveStatusFromSearchApi}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-25 21:46:46 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 6a6084e.
   *
   * @since Ver.0.1.3
   */
  public ChzzkLiveDto getLiveStatusFromSearchApi(final String channelName) {
    final String url =
        "/service/v1/search/channels?keyword=%s&offset=0&size=18&withFirstChannelContent=false";

    final ChzzkSearchApiResponseVo result =
        WEB_CLIENT
            .get()
            .uri(String.format(url, channelName))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(ChzzkSearchApiResponseVo.class)
            .block();

    if (result == null || result.getContentSize() == 0) {
      log.error(
          "Failed to get Live info by channel name from Chzzk API. Check argument is valid or"
              + " update the API status. channelName: {}",
          channelName);
      return null;
    }

    if (result.getContentSize() > 1) {
      log.warn(
          "There are more than one channel with the same name. The first channel will be used."
              + " channelName: {}",
          channelName);
    }

    final ChzzkLiveVo resultLiveVO = result.getLive(0);
    if (resultLiveVO == null) {
      log.error(
          "Failed to get Live info by channel name from Chzzk API. channelName: {}", channelName);
      return null;
    }
    return resultLiveVO.toDto();
  }
}
