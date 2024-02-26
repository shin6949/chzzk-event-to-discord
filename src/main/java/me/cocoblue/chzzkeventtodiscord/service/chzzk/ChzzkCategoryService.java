package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import me.cocoblue.chzzkeventtodiscord.vo.ChzzkCategoryResponseVO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
public class ChzzkCategoryService {
    public ChzzkCategoryResponseVO getCategoryInfo(final String gameId) {
        final String url = "https://api.chzzk.naver.com/service/v1/categories/GAME/%s/info";

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        final HttpEntity<DiscordEmbed.Webhook> entity = new HttpEntity<>(, headers);

        final RestTemplate rt = new RestTemplate();
        rt.exchange(String.format(url, gameId), HttpMethod.POST, entity, String.class);

        String formattedUrl = String.format(url, gameId);

        return ChzzkCategoryResponseVO.builder()

                .gameName("gameName")
                .gameDescription("gameDescription")
                .gameThumbnail("gameThumbnail")
                .build();
    }
}
