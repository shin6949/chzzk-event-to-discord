package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveDetailDto;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkLiveDetailVo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@code ChzzkLiveDetailService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2024-03-26 21:35:21 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 21ff52a.
 *
 * @since Ver.0.1.3
 */
@Log4j2
@Service
public class ChzzkLiveDetailService {
  private WebClient webClient;

  /**
   * {@code postConstructJob}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 21:35:21 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 21ff52a.
   *
   * @since Ver.0.1.3
   */
  @PostConstruct
  public void postConstructJob() {
    webClient =
        WebClient.builder()
            .baseUrl(ChzzkEventToDiscordApplication.CHZZK_API_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  /**
   * {@code getLiveDetailFromApi}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-26 21:35:21 +0900, 작성자 cocoblue, 작성 버전 Ver.0.1.3, 근거 커밋 21ff52a.
   *
   * @since Ver.0.1.3
   */
  public ChzzkLiveDetailDto getLiveDetailFromApi(final String channelId) {
    final String url = "/service/v2/channels/%s/live-detail";

    final ChzzkLiveDetailVo result =
        webClient
            .get()
            .uri(String.format(url, channelId))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(ChzzkLiveDetailVo.class)
            .block();

    if (result == null) {
      log.error(
          "Failed to get channel info by channel id from Chzzk API. Check argument is valid or"
              + " update the API status. channelId: {}",
          channelId);
      return null;
    }

    return result.toDto();
  }
}
