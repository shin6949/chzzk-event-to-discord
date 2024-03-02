package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.ChzzkEventToDiscordApplication;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

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
