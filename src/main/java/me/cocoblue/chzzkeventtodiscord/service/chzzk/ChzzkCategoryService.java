package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import jakarta.annotation.PostConstruct;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryRepository;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkCategoryDto;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkCategoryApiResponseVo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@code ChzzkCategoryService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class ChzzkCategoryService {
  private final ChzzkCategoryRepository chzzkCategoryRepository;
  private WebClient WEB_CLIENT;

  /**
   * {@code postConstructJob}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-02-28 00:24:24 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 41cafb0.
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
   * {@code getCategoryInfo}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  public ChzzkCategoryDto getCategoryInfo(final String categoryType, final String categoryId) {
    final Optional<ChzzkCategoryEntity> categoryEntity =
        chzzkCategoryRepository.findByIdCategoryId(categoryId);
    // PostgreSQL에서는 UTC로 저장되기 때문에, UTC로 변환해서 비교해야 함
    final ZonedDateTime threeDaysAgo = ZonedDateTime.now(ZoneId.of("UTC"));

    // 3일 이상 지난 데이터는 API를 통해 갱신
    if (categoryEntity.isEmpty() || categoryEntity.get().getUpdatedAt().isBefore(threeDaysAgo)) {
      final ChzzkCategoryApiResponseVo apiResult = getCategoryInfoFromAPI(categoryType, categoryId);
      if (apiResult == null) {
        log.info(
            "Failed to get category info from Chzzk API. Check the API status or categoryType and"
                + " categoryId is valid.");
        return null;
      }

      return apiResult.toDto();
    }

    return new ChzzkCategoryDto(categoryEntity.get());
  }

  /**
   * {@code getCategoryInfoFromAPI}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-02-28 00:24:24 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 41cafb0.
   *
   * @since Ver.0.1
   */
  protected ChzzkCategoryApiResponseVo getCategoryInfoFromAPI(
      final String categoryType, final String categoryId) {
    final String url = "/service/v1/categories/%s/%s/info";

    final ChzzkCategoryApiResponseVo result =
        WEB_CLIENT
            .get()
            .uri(String.format(url, categoryType, categoryId))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(ChzzkCategoryApiResponseVo.class)
            .block();

    if (result == null || result.getCode() != 200) {
      log.error(
          "Failed to get category info from Chzzk API. Check the API status or categoryId is"
              + " valid.");
      return null;
    }

    log.info("Successfully get category info from Chzzk API. categoryId: {}", categoryId);
    log.debug("Category Entity info: {}", result.toDto().toEntity());
    chzzkCategoryRepository.save(result.toDto().toEntity());
    return result;
  }
}
