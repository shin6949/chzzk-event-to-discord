package me.cocoblue.chzzkeventtodiscord.service.soop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.config.soop.SoopProperties;
import me.cocoblue.chzzkeventtodiscord.dto.soop.SoopLiveStatusDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Log4j2
@Service
@RequiredArgsConstructor
public class SoopLiveStatusService {
    private static final int DEFAULT_PAGE_BLOCK = 60;

    private final SoopProperties soopProperties;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    public SoopLiveStatusDto getLiveStatus(String soopUserId) {
        final String normalizedUserId = normalizeUserId(soopUserId);
        if (!soopProperties.hasClientId()) {
            log.warn("SOOP client id is not configured. Skip SOOP live status lookup. soopUserId: {}", normalizedUserId);
            return SoopLiveStatusDto.offline(normalizedUserId);
        }

        final WebClient webClient = webClientBuilder.baseUrl(soopProperties.apiBaseUrl()).build();
        for (int pageNo = 1; pageNo <= soopProperties.maxPages(); pageNo++) {
            final JsonNode response = requestBroadcastList(webClient, pageNo);
            final JsonNode broadcastList = response.path("broad");
            if (broadcastList.isArray()) {
                for (JsonNode broadcast : broadcastList) {
                    if (normalizedUserId.equalsIgnoreCase(broadcast.path("user_id").asText())) {
                        return toLiveStatus(broadcast, normalizedUserId);
                    }
                }
            }

            final int totalCount = response.path("total_cnt").asInt(0);
            final int pageBlock = response.path("page_block").asInt(DEFAULT_PAGE_BLOCK);
            if (totalCount <= pageNo * Math.max(1, pageBlock)) {
                break;
            }
        }

        return SoopLiveStatusDto.offline(normalizedUserId);
    }

    private JsonNode requestBroadcastList(WebClient webClient, int pageNo) {
        final String response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/broad/list")
                .queryParam("client_id", soopProperties.clientId())
                .queryParam("order_type", "broad_start")
                .queryParam("page_no", pageNo)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        try {
            return objectMapper.readTree(stripJsonp(response));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse SOOP broadcast list response", ex);
        }
    }

    private String stripJsonp(String response) {
        if (response == null) {
            return "{}";
        }
        final String trimmed = response.trim();
        final int objectStart = trimmed.indexOf('{');
        final int objectEnd = trimmed.lastIndexOf('}');
        if (objectStart >= 0 && objectEnd >= objectStart) {
            return trimmed.substring(objectStart, objectEnd + 1);
        }
        return trimmed;
    }

    private SoopLiveStatusDto toLiveStatus(JsonNode broadcast, String fallbackUserId) {
        final String userId = textOrDefault(broadcast.path("user_id"), fallbackUserId);
        final String broadNo = textOrNull(broadcast.path("broad_no"));
        return new SoopLiveStatusDto(
            userId,
            textOrDefault(broadcast.path("user_nick"), userId),
            normalizeProtocolRelativeUrl(textOrNull(broadcast.path("profile_img"))),
            true,
            broadNo,
            textOrNull(broadcast.path("broad_title")),
            normalizeProtocolRelativeUrl(textOrNull(broadcast.path("broad_thumb"))),
            textOrNull(broadcast.path("broad_start")),
            textOrNull(broadcast.path("total_view_cnt")),
            buildLiveUrl(userId, broadNo)
        );
    }

    private String buildLiveUrl(String userId, String broadNo) {
        if (StringUtils.hasText(broadNo)) {
            return UriComponentsBuilder.fromUriString("https://play.sooplive.co.kr/{userId}/{broadNo}")
                .buildAndExpand(userId, broadNo)
                .toUriString();
        }
        return "https://ch.sooplive.co.kr/" + userId;
    }

    private String normalizeUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("soopUserId is required");
        }
        return userId.trim();
    }

    private String normalizeProtocolRelativeUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        final String trimmed = value.trim();
        if (trimmed.startsWith("//")) {
            return "https:" + trimmed;
        }
        return trimmed;
    }

    private String textOrDefault(JsonNode node, String fallback) {
        final String value = textOrNull(node);
        return value == null ? fallback : value;
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        final String value = node.asText();
        return StringUtils.hasText(value) ? value : null;
    }
}
