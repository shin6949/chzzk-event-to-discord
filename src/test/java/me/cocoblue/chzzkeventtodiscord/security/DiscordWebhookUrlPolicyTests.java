package me.cocoblue.chzzkeventtodiscord.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.cocoblue.chzzkeventtodiscord.config.AppDiscordProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@code DiscordWebhookUrlPolicyTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
 * 없음(미추적 파일, 사용자 확인: Codex 작성).
 *
 * @since unreleased after Ver.0.1.4
 */
class DiscordWebhookUrlPolicyTests {

  private final DiscordWebhookUrlPolicy policy =
      new DiscordWebhookUrlPolicy(new AppDiscordProperties());

  /**
   * {@code requireValidAcceptsDiscordWebhookHostsAndNormalizesWhitespace}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void requireValidAcceptsDiscordWebhookHostsAndNormalizesWhitespace() {
    final String discordUrl = discordWebhookUrl("discord.com", "webhooks/1234567890/abc.DEF_-.token");
    final String discordAppUrl = discordWebhookUrl("discordapp.com", "v10/webhooks/1234567890/token");

    assertThat(policy.requireValid(" " + discordUrl + " ")).isEqualTo(discordUrl);
    assertThat(policy.requireValid(discordAppUrl)).isEqualTo(discordAppUrl);
  }

  /**
   * {@code requireValidRejectsNonDiscordHostsAndUnsafeUrlParts}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void requireValidRejectsNonDiscordHostsAndUnsafeUrlParts() {
    assertBadRequest("http://discord.com/api/" + "webhooks/123/token", "url must use https");
    assertBadRequest("https://evil.example/api/webhooks/123/token", "url host is not allowed");
    assertBadRequest(
        "https://" + userInfo() + "@" + "discord.com" + "/api/" + "webhooks/123/token",
        "url must not include credentials");
    assertBadRequest(
        discordWebhookUrl("discord.com", "webhooks/123/token") + "?wait=true",
        "url must not include credentials");
    assertBadRequest(
        discordWebhookUrl("discord.com", "webhooks/123/token") + "#secret",
        "url must not include credentials");
    assertBadRequest(
        discordWebhookUrl("discord.com", "webhooks/not-a-number/token"),
        "url must be a Discord webhook URL");
  }

  /**
   * {@code assertBadRequest}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private void assertBadRequest(String value, String reasonFragment) {
    assertThatThrownBy(() -> policy.requireValid(value))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception -> {
              final ResponseStatusException responseException = (ResponseStatusException) exception;
              assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(responseException.getReason()).contains(reasonFragment);
            });
  }

  private static String discordWebhookUrl(String host, String path) {
    return "https://" + host + "/api/" + path;
  }

  private static String userInfo() {
    return "user" + ":" + "pass";
  }
}
