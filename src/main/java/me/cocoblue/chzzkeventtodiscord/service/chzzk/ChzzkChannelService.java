package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import me.cocoblue.chzzkeventtodiscord.config.ChzzkOAuthProperties;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDto;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkChannelVo;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkChannelInfoApiResponseVo;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkSearchApiResponseVo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChzzkChannelService {
    private static final String USER_AGENT = "chzzk-event-to-discord/0.1.4";
    private static final int CACHE_TTL_DAYS = 3;

    private final ChzzkChannelRepository chzzkChannelRepository;
    private final ChzzkOAuthProperties chzzkOAuthProperties;
    private WebClient openApiWebClient;
    private WebClient legacyWebClient;

    @PostConstruct
    public void postConstructJob() {
        openApiWebClient = WebClient.builder()
                .baseUrl(chzzkOAuthProperties.getApiBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
        legacyWebClient = WebClient.builder()
                .baseUrl(ChzzkEventToDiscordApplication.CHZZK_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    @Transactional
    public ChzzkChannelEntity getChannelEntityByChannelIdFromDatabase(final String channelId) {
        return chzzkChannelRepository.findChzzkChannelEntityByChannelId(channelId).orElse(null);
    }

    @Transactional
    public ChzzkChannelDto getChannelByChannelId(final String channelId) {
        if (channelId == null) {
            log.error("Channel id is null. channelId: {}", (Object) null);
            return null;
        }

        log.info("Get channel by channel id. channelId: {}", channelId);
        final Optional<ChzzkChannelEntity> resultFromDB = chzzkChannelRepository.findChzzkChannelEntityByChannelId(channelId);
        log.info("Result from DB: {}", resultFromDB);
        // PostgreSQL에서는 UTC로 저장되기 때문에, UTC로 변환해서 비교해야 함
        final ZonedDateTime cacheExpiredAt = ZonedDateTime.now(ZoneId.of("UTC")).minusDays(CACHE_TTL_DAYS);

        // 3일 이상 지난 데이터나 프로필 이미지가 비어 있는 데이터는 API를 통해 갱신
        if (resultFromDB.isEmpty() || shouldRefreshCachedChannel(resultFromDB.get(), cacheExpiredAt)) {
            final ChzzkChannelDto apiResult = getChannelByChannelIdAtAPI(channelId);
            if (apiResult == null) {
                log.info("Failed to get channel info from Chzzk API.");
                return resultFromDB.map(ChzzkChannelDto::new).orElse(null);
            }

            return apiResult;
        }

        return new ChzzkChannelDto(resultFromDB.get());
    }

    @Transactional
    public ChzzkChannelDto getChannelByChannelName(final String channelName) {
        log.info("Get channel by channel name. channelName: {}", channelName);
        final Optional<ChzzkChannelEntity> resultFromDB = chzzkChannelRepository.findChzzkChannelEntityByChannelName(channelName);
        log.info("Result from DB: {}", resultFromDB);
        // PostgreSQL에서는 UTC로 저장되기 때문에, UTC로 변환해서 비교해야 함
        final ZonedDateTime threeDaysAgo = ZonedDateTime.now(ZoneId.of("UTC")).minusDays(CACHE_TTL_DAYS);

        // 3일 이상 지난 데이터는 API를 통해 갱신
        if (resultFromDB.isEmpty() || resultFromDB.get().getLastCheckTime().isBefore(threeDaysAgo)) {
            final ChzzkChannelDto apiResult = getChannelByChannelNameAtAPI(channelName);
            if (apiResult == null) {
                log.info("Failed to get channel info from Chzzk API.");
                return null;
            }

            return apiResult;
        }

        return new ChzzkChannelDto(resultFromDB.get());
    }

    @Transactional
    public ChzzkChannelDto getChannelByChannelIdAtAPI(final String channelId) {
        final ChzzkChannelInfoApiResponseVo result;
        try {
            result = openApiWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/open/v1/channels")
                            .queryParam("channelIds", channelId)
                            .build())
                    .header("Client-Id", chzzkOAuthProperties.getClientId())
                    .header("Client-Secret", chzzkOAuthProperties.getClientSecret())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(ChzzkChannelInfoApiResponseVo.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to get channel info by channel id from Chzzk Open API. channelId: {}", channelId, e);
            return null;
        }

        final ChzzkChannelVo resultChannel = result == null ? null : result.getFirstChannel();
        if (result == null || result.getCode() != 200 || resultChannel == null) {
            log.error("Failed to get channel info by channel id from Chzzk Open API. Check argument is valid or update the API status. channelId: {}", channelId);
            return null;
        }

        final ChzzkChannelDto channelDto = resultChannel.toDto();
        final String resolvedChannelId = StringUtils.hasText(channelDto.getChannelId()) ? channelDto.getChannelId() : channelId;
        final ChzzkChannelEntity entity = chzzkChannelRepository.findById(resolvedChannelId)
                .orElseGet(() -> ChzzkChannelEntity.builder()
                        .channelId(resolvedChannelId)
                        .channelName(resolvedChannelId)
                        .isVerifiedMark(false)
                        .profileUrl(null)
                        .channelDescription(null)
                        .followerCount(0)
                        .subscriptionAvailability(false)
                        .isLive(false)
                        .build());
        entity.setChannelName(StringUtils.hasText(channelDto.getChannelName()) ? channelDto.getChannelName() : resolvedChannelId);
        entity.setProfileUrl(channelDto.getChannelImageUrl());
        entity.setVerifiedMark(Boolean.TRUE.equals(channelDto.getVerifiedMark()));
        entity.setFollowerCount(channelDto.getFollowerCount());
        entity.setLastCheckTime(ZonedDateTime.now(ZoneId.of("UTC")));

        chzzkChannelRepository.save(entity);
        return new ChzzkChannelDto(entity);
    }

    @Transactional
    public ChzzkChannelDto getChannelByChannelNameAtAPI(final String channelName) {
        final String url = "/service/v1/search/channels?keyword=%s&offset=0&size=1&withFirstChannelContent=false";

        final ChzzkSearchApiResponseVo result = legacyWebClient
                .get()
                .uri(String.format(url, channelName))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ChzzkSearchApiResponseVo.class)
                .block();

        if (result == null || result.getContentSize() == 0) {
            log.error("Failed to get channel info by channel name from Chzzk API. Check argument is valid or update the API status. channelName: {}", channelName);
            return null;
        }

        if (result.getContentSize() > 1) {
            log.warn("There are more than one channel with the same name. The first channel will be used. channelName: {}", channelName);
        }

        final ChzzkChannelVo resultChannelVO = result.getChannel(0);
        final ChzzkChannelEntity entity = resultChannelVO.toDto().toEntity();
        entity.setLastCheckTime(ZonedDateTime.now(ZoneId.of("UTC")));

        log.info("Channel info updated. entity: {}", entity);
        chzzkChannelRepository.save(entity);
        return resultChannelVO.toDto();
    }

    private boolean shouldRefreshCachedChannel(ChzzkChannelEntity channelEntity, ZonedDateTime cacheExpiredAt) {
        return channelEntity.getLastCheckTime() == null
                || channelEntity.getLastCheckTime().isBefore(cacheExpiredAt)
                || channelEntity.getProfileUrl() == null
                || channelEntity.getProfileUrl().isBlank();
    }
}
