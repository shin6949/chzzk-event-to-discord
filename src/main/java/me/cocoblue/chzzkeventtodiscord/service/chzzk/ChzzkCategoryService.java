package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.ChzzkCategoryType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryRepository;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkCategoryDTO;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkCategoryAPIResponseVO;
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
    private WebClient WEB_CLIENT;
    private final ChzzkCategoryRepository chzzkCategoryRepository;

    @PostConstruct
    public void postConstructJob() {
        WEB_CLIENT = WebClient.builder()
                .baseUrl("https://api.chzzk.naver.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ChzzkCategoryDTO getCategoryInfo(final ChzzkCategoryType categoryType, final String categoryId) {
        final Optional<ChzzkCategoryEntity> categoryEntity = chzzkCategoryRepository.findByCategoryId(categoryId);
        // PostgreSQL에서는 UTC로 저장되기 때문에, UTC로 변환해서 비교해야 함
        final ZonedDateTime threeDaysAgo = ZonedDateTime.now(ZoneId.of("UTC"));

        if(categoryEntity.isEmpty() || categoryEntity.get().getUpdatedAt().isBefore(threeDaysAgo)) {
            final ChzzkCategoryAPIResponseVO apiResult = getCategoryInfoFromAPI(categoryType, categoryId);
            if(apiResult == null) {
                log.info("Failed to get category info from Chzzk API. Check the API status or categoryType and categoryId is valid.");
                return null;
            }

            return apiResult.toDTO();
        }

        return new ChzzkCategoryDTO(categoryEntity.get());
    }

    private ChzzkCategoryAPIResponseVO getCategoryInfoFromAPI(final ChzzkCategoryType categoryType, final String categoryId) {
        final String url = "/service/v1/categories/%s/%s/info";

        final ChzzkCategoryAPIResponseVO result = WEB_CLIENT
                .post()
                .uri(String.format(url, categoryType.getValue(), categoryId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ChzzkCategoryAPIResponseVO.class)
                .block();

        if(result == null) {
            log.error("Failed to get category info from Chzzk API. Check the API status or categoryId is valid.");
            return null;
        }

        chzzkCategoryRepository.save(result.toEntity());
        return result;
    }
}
