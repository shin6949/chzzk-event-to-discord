package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkCategoryRepository;
import me.cocoblue.chzzkeventtodiscord.util.TimeZoneConverter;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkCategoryResponseVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
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

    public ChzzkCategoryResponseVO getCategoryInfo(final String gameId) {
        final Optional<ChzzkCategoryEntity> categoryEntity = chzzkCategoryRepository.findByCategoryId(gameId);
        final LocalDateTime categoryUpdatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        final ZonedDateTime threeDaysAgo = ZonedDateTime.now();

        if(categoryEntity.isEmpty() || categoryEntity.get().getUpdatedAt().isBefore(threeDaysAgo)) {
            return getCategoryInfoFromAPI(gameId);
        }



        return getCategoryInfoFromAPI(gameId);
    }

    private ChzzkCategoryResponseVO getCategoryInfoFromAPI(final String gameId) {
        final String url = "/service/v1/categories/GAME/%s/info";

        return WEB_CLIENT
                .post()
                .uri(String.format(url, gameId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ChzzkCategoryResponseVO.class)
                .block();
    }
}
