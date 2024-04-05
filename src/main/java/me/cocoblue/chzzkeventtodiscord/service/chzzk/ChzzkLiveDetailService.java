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

@Log4j2
@Service
public class ChzzkLiveDetailService {
    private WebClient webClient;

    @PostConstruct
    public void postConstructJob() {
        webClient = WebClient.builder()
                .baseUrl(ChzzkEventToDiscordApplication.CHZZK_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ChzzkLiveDetailDto getLiveDetailFromApi(final String channelId) {
        final String url = "/service/v2/channels/%s/live-detail";

        final ChzzkLiveDetailVo result = webClient
                .get()
                .uri(String.format(url, channelId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ChzzkLiveDetailVo.class)
                .block();

        if (result == null) {
            log.error("Failed to get channel info by channel id from Chzzk API. Check argument is valid or update the API status. channelId: {}", channelId);
            return null;
        }

        return result.toDto();
    }
}
