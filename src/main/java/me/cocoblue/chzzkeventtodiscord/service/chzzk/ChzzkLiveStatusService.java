package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveDTO;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkLiveStatusDTO;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkLiveVO;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkLiveStatusAPIResponseVO;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkSearchAPIResponseVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@Service
public class ChzzkLiveStatusService {
    private WebClient WEB_CLIENT;

    @PostConstruct
    public void postConstructJob() {
        WEB_CLIENT = WebClient.builder()
                .baseUrl(ChzzkEventToDiscordApplication.CHZZK_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ChzzkLiveStatusDTO getLiveStatusFromAPI(final String channelId) {
        final String url = "/polling/v2/channels/%s/live-status";

        final ChzzkLiveStatusAPIResponseVO result = WEB_CLIENT
                .get()
                .uri(String.format(url, channelId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ChzzkLiveStatusAPIResponseVO.class)
                .block();

        if (result == null || result.getCode() != 200) {
            log.error("Failed to get channel info by channel id from Chzzk API. Check argument is valid or update the API status. channelId: {}", channelId);
            return null;
        }

        return result.toDTO();
    }

    public ChzzkLiveDTO getLiveStatusFromSearchAPI(final String channelName) {
        final String url = "/service/v1/search/channels?keyword=%s&offset=0&size=18&withFirstChannelContent=false";

        final ChzzkSearchAPIResponseVO result = WEB_CLIENT
                .get()
                .uri(String.format(url, channelName))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ChzzkSearchAPIResponseVO.class)
                .block();

        if (result == null || result.getContentSize() == 0) {
            log.error("Failed to get Live info by channel name from Chzzk API. Check argument is valid or update the API status. channelName: {}", channelName);
            return null;
        }

        if (result.getContentSize() > 1) {
            log.warn("There are more than one channel with the same name. The first channel will be used. channelName: {}", channelName);
        }

        final ChzzkLiveVO resultLiveVO = result.getLive(0);
        if(resultLiveVO == null) {
            log.error("Failed to get Live info by channel name from Chzzk API. channelName: {}", channelName);
            return null;
        }
        return resultLiveVO.toDTO();
    }
}
