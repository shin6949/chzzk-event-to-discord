package me.cocoblue.chzzkeventtodiscord.service;

import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DiscordWebhookService {
    public HttpStatusCode sendDiscordWebhook(final DiscordEmbed.Webhook discordWebhookMessage, final String webhookUrl) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        final HttpEntity<DiscordEmbed.Webhook> entity = new HttpEntity<>(discordWebhookMessage, headers);

        final RestTemplate rt = new RestTemplate();
        return rt.exchange(webhookUrl, HttpMethod.POST, entity, String.class).getStatusCode();
    }
}
