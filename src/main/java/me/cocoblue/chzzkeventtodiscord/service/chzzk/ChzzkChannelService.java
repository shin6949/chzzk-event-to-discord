package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDTO;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkChannelVO;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkChannelInfoAPIResponseVO;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkSearchAPIResponseVO;
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
public class ChzzkChannelService {
    private final ChzzkChannelRepository chzzkChannelRepository;
    private WebClient WEB_CLIENT;

    @PostConstruct
    public void postConstructJob() {
        WEB_CLIENT = WebClient.builder()
                .baseUrl(ChzzkEventToDiscordApplication.CHZZK_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Transactional
    public ChzzkChannelEntity getChannelEntityByChannelIdFromDatabase(final String channelId) {
        return chzzkChannelRepository.findChzzkChannelEntityByChannelId(channelId).orElse(null);
    }

    @Transactional
    public ChzzkChannelDTO getChannelByChannelId(final String channelId) {
        if (channelId == null) {
            log.error("Channel id is null. channelId: {}", (Object) null);
            return null;
        }

        log.info("Get channel by channel id. channelId: {}", channelId);
        final Optional<ChzzkChannelEntity> resultFromDB = chzzkChannelRepository.findChzzkChannelEntityByChannelId(channelId);
        log.info("Result from DB: {}", resultFromDB);
        // PostgreSQL에서는 UTC로 저장되기 때문에, UTC로 변환해서 비교해야 함
        final ZonedDateTime threeDaysAgo = ZonedDateTime.now(ZoneId.of("UTC")).minusDays(3);

        // 3일 이상 지난 데이터는 API를 통해 갱신
        if (resultFromDB.isEmpty() || resultFromDB.get().getLastCheckTime().isBefore(threeDaysAgo)) {
            final ChzzkChannelDTO apiResult = getChannelByChannelIdAtAPI(channelId);
            if (apiResult == null) {
                log.info("Failed to get channel info from Chzzk API.");
                return null;
            }

            return apiResult;
        }

        return new ChzzkChannelDTO(resultFromDB.get());
    }

    @Transactional
    public ChzzkChannelDTO getChannelByChannelName(final String channelName) {
        log.info("Get channel by channel name. channelName: {}", channelName);
        final Optional<ChzzkChannelEntity> resultFromDB = chzzkChannelRepository.findChzzkChannelEntityByChannelName(channelName);
        log.info("Result from DB: {}", resultFromDB);
        // PostgreSQL에서는 UTC로 저장되기 때문에, UTC로 변환해서 비교해야 함
        final ZonedDateTime threeDaysAgo = ZonedDateTime.now(ZoneId.of("UTC")).minusDays(3);

        // 3일 이상 지난 데이터는 API를 통해 갱신
        if (resultFromDB.isEmpty() || resultFromDB.get().getLastCheckTime().isBefore(threeDaysAgo)) {
            final ChzzkChannelDTO apiResult = getChannelByChannelNameAtAPI(channelName);
            if (apiResult == null) {
                log.info("Failed to get channel info from Chzzk API.");
                return null;
            }

            return apiResult;
        }

        return new ChzzkChannelDTO(resultFromDB.get());
    }

    @Transactional
    public ChzzkChannelDTO getChannelByChannelIdAtAPI(final String channelId) {
        final String url = "/service/v1/channels/%s";

        final ChzzkChannelInfoAPIResponseVO result = WEB_CLIENT
                .get()
                .uri(String.format(url, channelId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ChzzkChannelInfoAPIResponseVO.class)
                .block();

        if (result == null || result.getCode() != 200) {
            log.error("Failed to get channel info by channel id from Chzzk API. Check argument is valid or update the API status. channelId: {}", channelId);
            return null;
        }

        final ChzzkChannelEntity entity = result.getContent().toDTO().toEntity();
        entity.setLastCheckTime(ZonedDateTime.now(ZoneId.of("UTC")));

        chzzkChannelRepository.save(entity);
        return result.getContent().toDTO();
    }

    @Transactional
    public ChzzkChannelDTO getChannelByChannelNameAtAPI(final String channelName) {
        final String url = "/service/v1/search/channels?keyword=%s&offset=0&size=1&withFirstChannelContent=false";

        final ChzzkSearchAPIResponseVO result = WEB_CLIENT
                .get()
                .uri(String.format(url, channelName))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ChzzkSearchAPIResponseVO.class)
                .block();

        if (result == null || result.getContentSize() == 0) {
            log.error("Failed to get channel info by channel name from Chzzk API. Check argument is valid or update the API status. channelName: {}", channelName);
            return null;
        }

        if (result.getContentSize() > 1) {
            log.warn("There are more than one channel with the same name. The first channel will be used. channelName: {}", channelName);
        }

        final ChzzkChannelVO resultChannelVO = result.getChannel(0);
        final ChzzkChannelEntity entity = resultChannelVO.toDTO().toEntity();
        entity.setLastCheckTime(ZonedDateTime.now(ZoneId.of("UTC")));

        log.info("Channel info updated. entity: {}", entity);
        chzzkChannelRepository.save(entity);
        return resultChannelVO.toDTO();
    }
}
