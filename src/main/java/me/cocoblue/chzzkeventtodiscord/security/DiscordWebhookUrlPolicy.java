package me.cocoblue.chzzkeventtodiscord.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.AppDiscordProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@code DiscordWebhookUrlPolicy}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
 * 없음(미추적 파일, 사용자 확인: Codex 작성).
 *
 * @since unreleased after Ver.0.1.4
 */
@Component
@RequiredArgsConstructor
public class DiscordWebhookUrlPolicy {
  private static final Pattern WEBHOOK_PATH_PATTERN =
      Pattern.compile("^/api(?:/v\\d+)?/webhooks/\\d+/[A-Za-z0-9._-]+/?$");

  private final AppDiscordProperties properties;

  /**
   * {@code requireValid}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  public String requireValid(String value) {
    if (!StringUtils.hasText(value)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url is required");
    }

    final String url = value.trim();
    final URI uri;
    try {
      uri = new URI(url);
    } catch (URISyntaxException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url is invalid", exception);
    }

    if (!"https".equalsIgnoreCase(uri.getScheme())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url must use https");
    }
    if (StringUtils.hasText(uri.getUserInfo())
        || StringUtils.hasText(uri.getQuery())
        || StringUtils.hasText(uri.getFragment())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "url must not include credentials, query, or fragment");
    }

    final String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
    final Set<String> allowedHosts =
        properties.getWebhookAllowedHosts().stream()
            .filter(StringUtils::hasText)
            .map(hostValue -> hostValue.trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
    if (!allowedHosts.contains(host)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url host is not allowed");
    }
    if (!WEBHOOK_PATH_PATTERN.matcher(uri.getPath()).matches()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "url must be a Discord webhook URL");
    }

    return uri.toString();
  }
}
