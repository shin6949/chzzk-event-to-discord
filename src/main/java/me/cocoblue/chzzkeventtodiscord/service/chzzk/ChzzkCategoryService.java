package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import jakarta.annotation.PostConstruct;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChzzkCategoryService {
    private final ChzzkCategoryRepository chzzkCategoryRepository;
    private WebClient WEB_CLIENT;

    @PostConstruct
    public void postConstructJob() {
        WEB_CLIENT = WebClient.builder()
                .baseUrl(ChzzkEventToDiscordApplication.CHZZK_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ChzzkCategoryDto getCategoryInfo(final String categoryType, final String categoryId) {
        final Optional<ChzzkCategoryEntity> categoryEntity = chzzkCategoryRepository.findByIdCategoryId(categoryId);
        // PostgreSQL에서는 UTC로 저장되기 때문에, UTC로 변환해서 비교해야 함
        final ZonedDateTime threeDaysAgo = ZonedDateTime.now(ZoneId.of("UTC"));

        // 3일 이상 지난 데이터는 API를 통해 갱신
        if (categoryEntity.isEmpty() || categoryEntity.get().getUpdatedAt().isBefore(threeDaysAgo)) {
            final ChzzkCategoryApiResponseVo apiResult = getCategoryInfoFromAPI(categoryType, categoryId);
            if (apiResult == null) {
                log.info("Failed to get category info from Chzzk API. Check the API status or categoryType and categoryId is valid.");
                return null;
            }

            return apiResult.toDto();
        }

        return new ChzzkCategoryDto(categoryEntity.get());
    }

    protected ChzzkCategoryApiResponseVo getCategoryInfoFromAPI(final String categoryType, final String categoryId) {
        final String url = "/service/v1/categories/%s/%s/info";

        final ChzzkCategoryApiResponseVo result = WEB_CLIENT
                .get()
                .uri(String.format(url, categoryType, categoryId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ChzzkCategoryApiResponseVo.class)
                .block();

        if (result == null || result.getCode() != 200) {
            log.error("Failed to get category info from Chzzk API. Check the API status or categoryId is valid.");
            return null;
        }

        log.info("Successfully get category info from Chzzk API. categoryId: {}", categoryId);
        log.debug("Category Entity info: {}", result.toDto().toEntity());
        chzzkCategoryRepository.save(result.toDto().toEntity());
        return result;
    }
}
