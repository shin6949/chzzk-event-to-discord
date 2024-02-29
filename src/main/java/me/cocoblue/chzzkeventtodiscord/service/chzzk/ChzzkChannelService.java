package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDTO;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkChannelVO;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkCategoryAPIResponseVO;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkChannelInfoAPIResponseVO;
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
    private WebClient WEB_CLIENT;
    private final ChzzkChannelRepository chzzkChannelRepository;

    @PostConstruct
    public void postConstructJob() {
        WEB_CLIENT = WebClient.builder()
                .baseUrl(ChzzkEventToDiscordApplication.CHZZK_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


    public ChzzkChannelDTO getChannelByChannelId(final String channelId) {
        final Optional<ChzzkChannelEntity> resultFromDB = chzzkChannelRepository.findByChannelId(channelId);
        if (resultFromDB.isEmpty()) {
            log.error("Channel is not cached in DB. channelId: {}", channelId);
            return null;
        }
    }

    public ChzzkChannelDTO getChannelByChannelIdAtAPI(final String channelId) {
        final String url = "/service/v1/channels/%s";

        final ChzzkChannelInfoAPIResponseVO result = WEB_CLIENT
                .post()
                .uri(String.format(url, channelId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ChzzkChannelInfoAPIResponseVO.class)
                .block();

        if(result == null || result.getCode() != 200) {
            log.error("Failed to get channel info from Chzzk API. Check argument is valid or update the API status. channelId: {}", channelId);
            return null;
        }

        final ChzzkChannelEntity entity = result.getContent().toEntity();
        entity.setLastCheckTime(ZonedDateTime.now(ZoneId.of("UTC")));

        chzzkChannelRepository.save(entity);
        return result.getContent().toDTO();
    }

}
