package me.cocoblue.chzzkeventtodiscord.service;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import jakarta.transaction.Transactional;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.config.ChzzkOAuthProperties;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkOAuthTokenEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkOAuthTokenRepository;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.security.SecretEncryptionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * {@code ChzzkAuthService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * d1f0cd9.
 *
 * @since unreleased after Ver.0.1.4
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ChzzkAuthService {
  private static final String USER_AGENT = "streaming-alert-service/0.1.4";

  private final ChzzkOAuthProperties chzzkOAuthProperties;
  private final ChzzkOAuthTokenRepository chzzkOAuthTokenRepository;
  private final ChzzkChannelRepository chzzkChannelRepository;
  private final SecretEncryptionService secretEncryptionService;

  /**
   * {@code buildAuthorizationUrl}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  public String buildAuthorizationUrl(String state) {
    if (!StringUtils.hasText(state)) {
      throw new ResponseStatusException(BAD_REQUEST, "state is required");
    }

    return UriComponentsBuilder.fromHttpUrl(chzzkOAuthProperties.getAuthBaseUrl())
        .path("/account-interlock")
        .queryParam("clientId", chzzkOAuthProperties.getClientId())
        .queryParam("redirectUri", chzzkOAuthProperties.getRedirectUri())
        .queryParam("state", state)
        .build(true)
        .toUriString();
  }

  /**
   * {@code authenticateFromCallback}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public ChzzkPrincipal authenticateFromCallback(String code, String state) {
    if (!StringUtils.hasText(code)) {
      throw new ResponseStatusException(BAD_REQUEST, "code is required");
    }
    if (!StringUtils.hasText(state)) {
      throw new ResponseStatusException(BAD_REQUEST, "state is required");
    }

    final TokenResponse tokenResponse = requestToken(code, state);
    if (tokenResponse == null || !StringUtils.hasText(tokenResponse.resolveAccessToken())) {
      log.warn("CHZZK OAuth token response did not include an access token");
      throw new ResponseStatusException(BAD_GATEWAY, "failed to exchange oauth code");
    }

    final UserMeResponse userMeResponse = requestUserMe(tokenResponse.resolveAccessToken());
    final String channelId = userMeResponse == null ? null : userMeResponse.resolveChannelId();
    if (!StringUtils.hasText(channelId)) {
      log.warn("CHZZK user info response did not include a channel id");
      throw new ResponseStatusException(BAD_GATEWAY, "failed to resolve channel id");
    }

    upsertToken(channelId, tokenResponse);
    upsertChannel(userMeResponse, channelId);

    log.info("OAuth callback authenticated for channelId={}", channelId);
    return new ChzzkPrincipal(channelId, AppRole.USER);
  }

  /**
   * {@code getValidAccessToken}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public String getValidAccessToken(String channelId) {
    final ChzzkOAuthTokenEntity tokenEntity =
        chzzkOAuthTokenRepository
            .findById(channelId)
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "oauth token not found"));

    final ZonedDateTime now = nowUtc();
    final boolean accessTokenMissing = !StringUtils.hasText(tokenEntity.getAccessToken());
    final boolean accessTokenExpired =
        tokenEntity.getAccessTokenExpiresAt() != null
            && !tokenEntity.getAccessTokenExpiresAt().isAfter(now);
    if (!accessTokenMissing && !accessTokenExpired) {
      final String accessToken = decryptToken(tokenEntity.getAccessToken());
      if (encryptStoredTokensIfNeeded(tokenEntity)) {
        chzzkOAuthTokenRepository.save(tokenEntity);
      }
      return accessToken;
    }

    final boolean refreshTokenMissing = !StringUtils.hasText(tokenEntity.getRefreshToken());
    final boolean refreshTokenExpired =
        tokenEntity.getRefreshTokenExpiresAt() != null
            && !tokenEntity.getRefreshTokenExpiresAt().isAfter(now);
    if (refreshTokenMissing || refreshTokenExpired) {
      throw new ResponseStatusException(
          UNAUTHORIZED, "oauth access token expired and no valid refresh token exists");
    }

    final TokenResponse tokenResponse;
    try {
      tokenResponse = requestRefreshToken(decryptToken(tokenEntity.getRefreshToken()));
    } catch (Exception e) {
      throw new ResponseStatusException(BAD_GATEWAY, "failed to refresh oauth token", e);
    }
    if (tokenResponse == null || !StringUtils.hasText(tokenResponse.resolveAccessToken())) {
      throw new ResponseStatusException(BAD_GATEWAY, "failed to refresh oauth token");
    }

    upsertTokenByRefreshToken(channelId, tokenResponse);
    return tokenResponse.resolveAccessToken();
  }

  /**
   * {@code revokeCurrentUserTokens}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public void revokeCurrentUserTokens(String channelId) {
    final ChzzkOAuthTokenEntity tokenEntity =
        chzzkOAuthTokenRepository.findById(channelId).orElse(null);
    if (tokenEntity == null) {
      return;
    }

    final String refreshToken = decryptToken(tokenEntity.getRefreshToken());
    final String accessToken = decryptToken(tokenEntity.getAccessToken());
    if (StringUtils.hasText(refreshToken)) {
      try {
        revokeToken(refreshToken, "refresh_token");
      } catch (Exception e) {
        throw new ResponseStatusException(BAD_GATEWAY, "failed to revoke refresh token", e);
      }
    } else if (StringUtils.hasText(accessToken)) {
      try {
        revokeToken(accessToken, "access_token");
      } catch (Exception e) {
        throw new ResponseStatusException(BAD_GATEWAY, "failed to revoke access token", e);
      }
    }

    chzzkOAuthTokenRepository.delete(tokenEntity);
  }

  /**
   * {@code requestToken}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private TokenResponse requestToken(String code, String state) {
    final Map<String, String> requestBody = new LinkedHashMap<>();
    requestBody.put("grantType", "authorization_code");
    requestBody.put("clientId", chzzkOAuthProperties.getClientId());
    requestBody.put("clientSecret", chzzkOAuthProperties.getClientSecret());
    requestBody.put("code", code);
    requestBody.put("state", state);

    return requestTokenInternal(requestBody);
  }

  /**
   * {@code requestRefreshToken}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private TokenResponse requestRefreshToken(String refreshToken) {
    final Map<String, String> requestBody = new LinkedHashMap<>();
    requestBody.put("grantType", "refresh_token");
    requestBody.put("clientId", chzzkOAuthProperties.getClientId());
    requestBody.put("clientSecret", chzzkOAuthProperties.getClientSecret());
    requestBody.put("refreshToken", refreshToken);

    return requestTokenInternal(requestBody);
  }

  /**
   * {@code revokeToken}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void revokeToken(String token, String tokenTypeHint) {
    final Map<String, String> requestBody = new LinkedHashMap<>();
    requestBody.put("clientId", chzzkOAuthProperties.getClientId());
    requestBody.put("clientSecret", chzzkOAuthProperties.getClientSecret());
    requestBody.put("token", token);
    requestBody.put("tokenTypeHint", tokenTypeHint);

    WebClient.builder()
        .baseUrl(chzzkOAuthProperties.getTokenBaseUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
        .build()
        .post()
        .uri("/auth/v1/token/revoke")
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(requestBody)
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  /**
   * {@code requestTokenInternal}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private TokenResponse requestTokenInternal(Map<String, String> requestBody) {
    return WebClient.builder()
        .baseUrl(chzzkOAuthProperties.getTokenBaseUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
        .build()
        .post()
        .uri("/auth/v1/token")
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(requestBody)
        .retrieve()
        .onStatus(
            status -> status.isError(),
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .map(
                        body -> {
                          log.warn(
                              "CHZZK OAuth token request failed. status={}, body={}",
                              response.statusCode().value(),
                              summarizeResponseBody(body));
                          return new ResponseStatusException(
                              BAD_GATEWAY, "failed to exchange oauth token");
                        }))
        .bodyToMono(TokenResponse.class)
        .block();
  }

  /**
   * {@code requestUserMe}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private UserMeResponse requestUserMe(String accessToken) {
    return WebClient.builder()
        .baseUrl(chzzkOAuthProperties.getApiBaseUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
        .build()
        .get()
        .uri("/open/v1/users/me")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(
            status -> status.isError(),
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .map(
                        body -> {
                          log.warn(
                              "CHZZK user info request failed. status={}, body={}",
                              response.statusCode().value(),
                              summarizeResponseBody(body));
                          return new ResponseStatusException(
                              BAD_GATEWAY, "failed to resolve chzzk user info");
                        }))
        .bodyToMono(UserMeResponse.class)
        .block();
  }

  /**
   * {@code upsertToken}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void upsertToken(String channelId, TokenResponse tokenResponse) {
    final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
    final ChzzkOAuthTokenEntity tokenEntity =
        chzzkOAuthTokenRepository
            .findById(channelId)
            .orElseGet(() -> ChzzkOAuthTokenEntity.builder().channelId(channelId).build());

    tokenEntity.setAccessToken(encryptToken(tokenResponse.resolveAccessToken()));
    tokenEntity.setRefreshToken(encryptToken(tokenResponse.resolveRefreshToken()));
    tokenEntity.setTokenType(tokenResponse.resolveTokenType());
    tokenEntity.setScope(tokenResponse.resolveScope());
    tokenEntity.setAccessTokenExpiresAt(
        tokenResponse.resolveExpiresIn() == null
            ? null
            : now.plusSeconds(tokenResponse.resolveExpiresIn()));
    tokenEntity.setRefreshTokenExpiresAt(
        tokenResponse.resolveRefreshTokenExpiresIn() == null
            ? null
            : now.plusSeconds(tokenResponse.resolveRefreshTokenExpiresIn()));

    chzzkOAuthTokenRepository.save(tokenEntity);
  }

  /**
   * {@code upsertTokenByRefreshToken}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void upsertTokenByRefreshToken(String channelId, TokenResponse tokenResponse) {
    final ZonedDateTime now = nowUtc();
    final ChzzkOAuthTokenEntity tokenEntity =
        chzzkOAuthTokenRepository
            .findById(channelId)
            .orElseGet(() -> ChzzkOAuthTokenEntity.builder().channelId(channelId).build());

    if (StringUtils.hasText(tokenResponse.resolveAccessToken())) {
      tokenEntity.setAccessToken(encryptToken(tokenResponse.resolveAccessToken()));
    }
    if (StringUtils.hasText(tokenResponse.resolveRefreshToken())) {
      tokenEntity.setRefreshToken(encryptToken(tokenResponse.resolveRefreshToken()));
    }
    if (StringUtils.hasText(tokenResponse.resolveTokenType())) {
      tokenEntity.setTokenType(tokenResponse.resolveTokenType());
    }
    if (StringUtils.hasText(tokenResponse.resolveScope())) {
      tokenEntity.setScope(tokenResponse.resolveScope());
    }
    tokenEntity.setAccessTokenExpiresAt(
        tokenResponse.resolveExpiresIn() == null
            ? tokenEntity.getAccessTokenExpiresAt()
            : now.plusSeconds(tokenResponse.resolveExpiresIn()));
    tokenEntity.setRefreshTokenExpiresAt(
        tokenResponse.resolveRefreshTokenExpiresIn() == null
            ? tokenEntity.getRefreshTokenExpiresAt()
            : now.plusSeconds(tokenResponse.resolveRefreshTokenExpiresIn()));

    encryptStoredTokensIfNeeded(tokenEntity);
    chzzkOAuthTokenRepository.save(tokenEntity);
  }

  /**
   * {@code upsertChannel}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void upsertChannel(UserMeResponse userMeResponse, String channelId) {
    final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
    final String resolvedChannelName =
        userMeResponse == null ? null : userMeResponse.resolveChannelName();

    final ChzzkChannelEntity channelEntity =
        chzzkChannelRepository
            .findById(channelId)
            .map(
                existing -> {
                  if (StringUtils.hasText(resolvedChannelName)) {
                    existing.setChannelName(resolvedChannelName);
                  }
                  existing.setLastCheckTime(now);
                  return existing;
                })
            .orElseGet(
                () ->
                    ChzzkChannelEntity.builder()
                        .channelId(channelId)
                        .channelName(
                            StringUtils.hasText(resolvedChannelName)
                                ? resolvedChannelName
                                : channelId)
                        .profileUrl(null)
                        .isVerifiedMark(false)
                        .channelDescription(null)
                        .subscriptionAvailability(false)
                        .isLive(false)
                        .followerCount(0)
                        .lastCheckTime(now)
                        .build());

    chzzkChannelRepository.save(channelEntity);
  }

  /**
   * {@code nowUtc}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private static ZonedDateTime nowUtc() {
    return ZonedDateTime.now(ZoneId.of("UTC"));
  }

  /**
   * {@code encryptStoredTokensIfNeeded}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private boolean encryptStoredTokensIfNeeded(ChzzkOAuthTokenEntity tokenEntity) {
    boolean changed = false;
    if (StringUtils.hasText(tokenEntity.getAccessToken())
        && !secretEncryptionService.isEncrypted(tokenEntity.getAccessToken())) {
      tokenEntity.setAccessToken(secretEncryptionService.encrypt(tokenEntity.getAccessToken()));
      changed = true;
    }
    if (StringUtils.hasText(tokenEntity.getRefreshToken())
        && !secretEncryptionService.isEncrypted(tokenEntity.getRefreshToken())) {
      tokenEntity.setRefreshToken(secretEncryptionService.encrypt(tokenEntity.getRefreshToken()));
      changed = true;
    }
    return changed;
  }

  /**
   * {@code encryptToken}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private String encryptToken(String token) {
    return StringUtils.hasText(token) ? secretEncryptionService.encrypt(token) : token;
  }

  /**
   * {@code decryptToken}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private String decryptToken(String token) {
    return StringUtils.hasText(token) ? secretEncryptionService.decryptIfNeeded(token) : token;
  }

  /**
   * {@code summarizeResponseBody}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private static String summarizeResponseBody(String responseBody) {
    if (!StringUtils.hasText(responseBody)) {
      return "<empty>";
    }
    return "<redacted, " + responseBody.length() + " chars>";
  }

  /**
   * {@code TokenResponse}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private record TokenResponse(
      @com.fasterxml.jackson.annotation.JsonAlias({"accessToken", "access_token"})
          String accessToken,
      @com.fasterxml.jackson.annotation.JsonAlias({"refreshToken", "refresh_token"})
          String refreshToken,
      @com.fasterxml.jackson.annotation.JsonAlias({"tokenType", "token_type"}) String tokenType,
      String scope,
      @com.fasterxml.jackson.annotation.JsonAlias({"expiresIn", "expires_in"}) Long expiresIn,
      @com.fasterxml.jackson.annotation.JsonAlias({
            "refreshTokenExpiresIn",
            "refresh_token_expires_in"
          })
          Long refreshTokenExpiresIn,
      TokenContent content) {
    /**
     * {@code resolveAccessToken}은 CHZZK 토큰 응답에서 access token을 추출합니다.
     *
     * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거
     * 커밋 3c42b97.
     *
     * @since unreleased after Ver.0.1.4
     */
    String resolveAccessToken() {
      return StringUtils.hasText(accessToken)
          ? accessToken
          : content == null ? null : content.accessToken();
    }

    /**
     * {@code resolveRefreshToken}은 CHZZK 토큰 응답에서 refresh token을 추출합니다.
     *
     * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거
     * 커밋 3c42b97.
     *
     * @since unreleased after Ver.0.1.4
     */
    String resolveRefreshToken() {
      return StringUtils.hasText(refreshToken)
          ? refreshToken
          : content == null ? null : content.refreshToken();
    }

    /**
     * {@code resolveTokenType}은 CHZZK 토큰 응답에서 token type을 추출합니다.
     *
     * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거
     * 커밋 3c42b97.
     *
     * @since unreleased after Ver.0.1.4
     */
    String resolveTokenType() {
      return StringUtils.hasText(tokenType)
          ? tokenType
          : content == null ? null : content.tokenType();
    }

    /**
     * {@code resolveScope}은 CHZZK 토큰 응답에서 scope를 추출합니다.
     *
     * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거
     * 커밋 3c42b97.
     *
     * @since unreleased after Ver.0.1.4
     */
    String resolveScope() {
      return StringUtils.hasText(scope) ? scope : content == null ? null : content.scope();
    }

    /**
     * {@code resolveExpiresIn}은 CHZZK 토큰 응답에서 access token 만료 시간을 추출합니다.
     *
     * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거
     * 커밋 3c42b97.
     *
     * @since unreleased after Ver.0.1.4
     */
    Long resolveExpiresIn() {
      return expiresIn == null && content != null ? content.expiresIn() : expiresIn;
    }

    /**
     * {@code resolveRefreshTokenExpiresIn}은 CHZZK 토큰 응답에서 refresh token 만료 시간을 추출합니다.
     *
     * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거
     * 커밋 3c42b97.
     *
     * @since unreleased after Ver.0.1.4
     */
    Long resolveRefreshTokenExpiresIn() {
      return refreshTokenExpiresIn == null && content != null
          ? content.refreshTokenExpiresIn()
          : refreshTokenExpiresIn;
    }
  }

  /**
   * {@code TokenContent}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private record TokenContent(
      @com.fasterxml.jackson.annotation.JsonAlias({"accessToken", "access_token"})
          String accessToken,
      @com.fasterxml.jackson.annotation.JsonAlias({"refreshToken", "refresh_token"})
          String refreshToken,
      @com.fasterxml.jackson.annotation.JsonAlias({"tokenType", "token_type"}) String tokenType,
      String scope,
      @com.fasterxml.jackson.annotation.JsonAlias({"expiresIn", "expires_in"}) Long expiresIn,
      @com.fasterxml.jackson.annotation.JsonAlias({
            "refreshTokenExpiresIn",
            "refresh_token_expires_in"
          })
          Long refreshTokenExpiresIn) {}

  /**
   * {@code UserMeResponse}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private record UserMeResponse(
      @com.fasterxml.jackson.annotation.JsonAlias({"channelId", "id"}) String channelId,
      @com.fasterxml.jackson.annotation.JsonAlias({"channelName", "name"}) String channelName,
      UserMeContent content) {
    /**
     * {@code resolveChannelId}은 CHZZK 채널 응답에서 채널 ID를 추출합니다.
     *
     * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거
     * 커밋 7fec3f1.
     *
     * @since unreleased after Ver.0.1.4
     */
    String resolveChannelId() {
      if (StringUtils.hasText(channelId)) {
        return channelId;
      }
      return content == null ? null : content.channelId();
    }

    /**
     * {@code resolveChannelName}은 CHZZK 채널 응답에서 채널 이름을 추출합니다.
     *
     * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거
     * 커밋 7fec3f1.
     *
     * @since unreleased after Ver.0.1.4
     */
    String resolveChannelName() {
      if (StringUtils.hasText(channelName)) {
        return channelName;
      }
      return content == null ? null : content.channelName();
    }
  }

  /**
   * {@code UserMeContent}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  private record UserMeContent(
      @com.fasterxml.jackson.annotation.JsonAlias({"channelId", "id"}) String channelId,
      @com.fasterxml.jackson.annotation.JsonAlias({"channelName", "name"}) String channelName) {}
}
