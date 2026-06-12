package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import me.cocoblue.chzzkeventtodiscord.security.DiscordWebhookUrlPolicy;
import me.cocoblue.chzzkeventtodiscord.security.SecretEncryptionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@code DiscordWebhookService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class DiscordWebhookService {
  private final DiscordWebhookUrlPolicy discordWebhookUrlPolicy;
  private final SecretEncryptionService secretEncryptionService;

  private WebClient webClient;

  /**
   * {@code postConstructJob}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
   *
   * @since Ver.0.1
   */
  @PostConstruct
  public void postConstructJob() {
    webClient =
        WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  /**
   * {@code sendDiscordWebhook}은 관련 처리 흐름을 실행합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  public void sendDiscordWebhook(
      final DiscordEmbed.Webhook discordWebhookMessage, final String storedWebhookUrl) {
    final String webhookUrl =
        discordWebhookUrlPolicy.requireValid(
            secretEncryptionService.decryptIfNeeded(storedWebhookUrl));
    webClient
        .post()
        .uri(webhookUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(discordWebhookMessage)
        .retrieve()
        .toBodilessEntity()
        //                .map(ResponseEntity::getStatusCode)
        .block();
  }
}
