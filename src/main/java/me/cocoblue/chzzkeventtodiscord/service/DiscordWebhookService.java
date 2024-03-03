package me.cocoblue.chzzkeventtodiscord.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@Log4j2
@Service
@RequiredArgsConstructor
public class DiscordWebhookService {
    private WebClient webClient;

    @PostConstruct
    public void postConstructJob() {
        webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void sendDiscordWebhook(final DiscordEmbed.Webhook discordWebhookMessage, final String webhookUrl) {
        // ObjectMapper 인스턴스 생성
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // 객체를 JSON 문자열로 변환
            String json = objectMapper.writeValueAsString(discordWebhookMessage);
            log.info(json);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert DiscordWebhookMessage to JSON", e);
        }

        webClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(discordWebhookMessage)
                .retrieve()
                .toBodilessEntity()
//                .map(ResponseEntity::getStatusCode)
                .block();
    }
}
