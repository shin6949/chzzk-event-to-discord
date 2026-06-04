package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.config.ChzzkOAuthProperties;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkOAuthTokenEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkOAuthTokenRepository;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChzzkAuthService {
    private static final String USER_AGENT = "chzzk-event-to-discord/0.1.4";

    private final ChzzkOAuthProperties chzzkOAuthProperties;
    private final ChzzkOAuthTokenRepository chzzkOAuthTokenRepository;
    private final ChzzkChannelRepository chzzkChannelRepository;

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

    @Transactional
    public String getValidAccessToken(String channelId) {
        final ChzzkOAuthTokenEntity tokenEntity = chzzkOAuthTokenRepository.findById(channelId)
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "oauth token not found"));

        final ZonedDateTime now = nowUtc();
        final boolean accessTokenMissing = !StringUtils.hasText(tokenEntity.getAccessToken());
        final boolean accessTokenExpired = tokenEntity.getAccessTokenExpiresAt() != null && !tokenEntity.getAccessTokenExpiresAt().isAfter(now);
        if (!accessTokenMissing && !accessTokenExpired) {
            return tokenEntity.getAccessToken();
        }

        final boolean refreshTokenMissing = !StringUtils.hasText(tokenEntity.getRefreshToken());
        final boolean refreshTokenExpired = tokenEntity.getRefreshTokenExpiresAt() != null && !tokenEntity.getRefreshTokenExpiresAt().isAfter(now);
        if (refreshTokenMissing || refreshTokenExpired) {
            throw new ResponseStatusException(UNAUTHORIZED, "oauth access token expired and no valid refresh token exists");
        }

        final TokenResponse tokenResponse;
        try {
            tokenResponse = requestRefreshToken(tokenEntity.getRefreshToken());
        } catch (Exception e) {
            throw new ResponseStatusException(BAD_GATEWAY, "failed to refresh oauth token", e);
        }
        if (tokenResponse == null || !StringUtils.hasText(tokenResponse.resolveAccessToken())) {
            throw new ResponseStatusException(BAD_GATEWAY, "failed to refresh oauth token");
        }

        upsertTokenByRefreshToken(channelId, tokenResponse);
        return tokenResponse.resolveAccessToken();
    }

    @Transactional
    public void revokeCurrentUserTokens(String channelId) {
        final ChzzkOAuthTokenEntity tokenEntity = chzzkOAuthTokenRepository.findById(channelId)
            .orElse(null);
        if (tokenEntity == null) {
            return;
        }

        if (StringUtils.hasText(tokenEntity.getRefreshToken())) {
            try {
                revokeToken(tokenEntity.getRefreshToken(), "refresh_token");
            } catch (Exception e) {
                throw new ResponseStatusException(BAD_GATEWAY, "failed to revoke refresh token", e);
            }
        } else if (StringUtils.hasText(tokenEntity.getAccessToken())) {
            try {
                revokeToken(tokenEntity.getAccessToken(), "access_token");
            } catch (Exception e) {
                throw new ResponseStatusException(BAD_GATEWAY, "failed to revoke access token", e);
            }
        }

        chzzkOAuthTokenRepository.delete(tokenEntity);
    }

    private TokenResponse requestToken(String code, String state) {
        final Map<String, String> requestBody = new LinkedHashMap<>();
        requestBody.put("grantType", "authorization_code");
        requestBody.put("clientId", chzzkOAuthProperties.getClientId());
        requestBody.put("clientSecret", chzzkOAuthProperties.getClientSecret());
        requestBody.put("code", code);
        requestBody.put("state", state);

        return requestTokenInternal(requestBody);
    }

    private TokenResponse requestRefreshToken(String refreshToken) {
        final Map<String, String> requestBody = new LinkedHashMap<>();
        requestBody.put("grantType", "refresh_token");
        requestBody.put("clientId", chzzkOAuthProperties.getClientId());
        requestBody.put("clientSecret", chzzkOAuthProperties.getClientSecret());
        requestBody.put("refreshToken", refreshToken);

        return requestTokenInternal(requestBody);
    }

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
                response -> response.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .map(body -> {
                        log.warn(
                            "CHZZK OAuth token request failed. status={}, body={}",
                            response.statusCode().value(),
                            abbreviateResponseBody(body)
                        );
                        return new ResponseStatusException(BAD_GATEWAY, "failed to exchange oauth token");
                    })
            )
            .bodyToMono(TokenResponse.class)
            .block();
    }

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
                response -> response.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .map(body -> {
                        log.warn(
                            "CHZZK user info request failed. status={}, body={}",
                            response.statusCode().value(),
                            abbreviateResponseBody(body)
                        );
                        return new ResponseStatusException(BAD_GATEWAY, "failed to resolve chzzk user info");
                    })
            )
            .bodyToMono(UserMeResponse.class)
            .block();
    }

    private void upsertToken(String channelId, TokenResponse tokenResponse) {
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        final ChzzkOAuthTokenEntity tokenEntity = chzzkOAuthTokenRepository.findById(channelId)
            .orElseGet(() -> ChzzkOAuthTokenEntity.builder().channelId(channelId).build());

        tokenEntity.setAccessToken(tokenResponse.resolveAccessToken());
        tokenEntity.setRefreshToken(tokenResponse.resolveRefreshToken());
        tokenEntity.setTokenType(tokenResponse.resolveTokenType());
        tokenEntity.setScope(tokenResponse.resolveScope());
        tokenEntity.setAccessTokenExpiresAt(
            tokenResponse.resolveExpiresIn() == null ? null : now.plusSeconds(tokenResponse.resolveExpiresIn())
        );
        tokenEntity.setRefreshTokenExpiresAt(
            tokenResponse.resolveRefreshTokenExpiresIn() == null ? null : now.plusSeconds(tokenResponse.resolveRefreshTokenExpiresIn())
        );

        chzzkOAuthTokenRepository.save(tokenEntity);
    }

    private void upsertTokenByRefreshToken(String channelId, TokenResponse tokenResponse) {
        final ZonedDateTime now = nowUtc();
        final ChzzkOAuthTokenEntity tokenEntity = chzzkOAuthTokenRepository.findById(channelId)
            .orElseGet(() -> ChzzkOAuthTokenEntity.builder().channelId(channelId).build());

        if (StringUtils.hasText(tokenResponse.resolveAccessToken())) {
            tokenEntity.setAccessToken(tokenResponse.resolveAccessToken());
        }
        if (StringUtils.hasText(tokenResponse.resolveRefreshToken())) {
            tokenEntity.setRefreshToken(tokenResponse.resolveRefreshToken());
        }
        if (StringUtils.hasText(tokenResponse.resolveTokenType())) {
            tokenEntity.setTokenType(tokenResponse.resolveTokenType());
        }
        if (StringUtils.hasText(tokenResponse.resolveScope())) {
            tokenEntity.setScope(tokenResponse.resolveScope());
        }
        tokenEntity.setAccessTokenExpiresAt(
            tokenResponse.resolveExpiresIn() == null ? tokenEntity.getAccessTokenExpiresAt() : now.plusSeconds(tokenResponse.resolveExpiresIn())
        );
        tokenEntity.setRefreshTokenExpiresAt(
            tokenResponse.resolveRefreshTokenExpiresIn() == null ? tokenEntity.getRefreshTokenExpiresAt() : now.plusSeconds(tokenResponse.resolveRefreshTokenExpiresIn())
        );

        chzzkOAuthTokenRepository.save(tokenEntity);
    }

    private void upsertChannel(UserMeResponse userMeResponse, String channelId) {
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        final String resolvedChannelName = userMeResponse == null ? null : userMeResponse.resolveChannelName();

        final ChzzkChannelEntity channelEntity = chzzkChannelRepository.findById(channelId)
            .map(existing -> {
                if (StringUtils.hasText(resolvedChannelName)) {
                    existing.setChannelName(resolvedChannelName);
                }
                existing.setLastCheckTime(now);
                return existing;
            })
            .orElseGet(() -> ChzzkChannelEntity.builder()
                .channelId(channelId)
                .channelName(StringUtils.hasText(resolvedChannelName) ? resolvedChannelName : channelId)
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

    private static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    private static String abbreviateResponseBody(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "<empty>";
        }
        return responseBody.length() <= 500 ? responseBody : responseBody.substring(0, 500) + "...";
    }

    private record TokenResponse(
        @com.fasterxml.jackson.annotation.JsonAlias({"accessToken", "access_token"}) String accessToken,
        @com.fasterxml.jackson.annotation.JsonAlias({"refreshToken", "refresh_token"}) String refreshToken,
        @com.fasterxml.jackson.annotation.JsonAlias({"tokenType", "token_type"}) String tokenType,
        String scope,
        @com.fasterxml.jackson.annotation.JsonAlias({"expiresIn", "expires_in"}) Long expiresIn,
        @com.fasterxml.jackson.annotation.JsonAlias({"refreshTokenExpiresIn", "refresh_token_expires_in"}) Long refreshTokenExpiresIn,
        TokenContent content
    ) {
        String resolveAccessToken() {
            return StringUtils.hasText(accessToken) ? accessToken : content == null ? null : content.accessToken();
        }

        String resolveRefreshToken() {
            return StringUtils.hasText(refreshToken) ? refreshToken : content == null ? null : content.refreshToken();
        }

        String resolveTokenType() {
            return StringUtils.hasText(tokenType) ? tokenType : content == null ? null : content.tokenType();
        }

        String resolveScope() {
            return StringUtils.hasText(scope) ? scope : content == null ? null : content.scope();
        }

        Long resolveExpiresIn() {
            return expiresIn == null && content != null ? content.expiresIn() : expiresIn;
        }

        Long resolveRefreshTokenExpiresIn() {
            return refreshTokenExpiresIn == null && content != null ? content.refreshTokenExpiresIn() : refreshTokenExpiresIn;
        }
    }

    private record TokenContent(
        @com.fasterxml.jackson.annotation.JsonAlias({"accessToken", "access_token"}) String accessToken,
        @com.fasterxml.jackson.annotation.JsonAlias({"refreshToken", "refresh_token"}) String refreshToken,
        @com.fasterxml.jackson.annotation.JsonAlias({"tokenType", "token_type"}) String tokenType,
        String scope,
        @com.fasterxml.jackson.annotation.JsonAlias({"expiresIn", "expires_in"}) Long expiresIn,
        @com.fasterxml.jackson.annotation.JsonAlias({"refreshTokenExpiresIn", "refresh_token_expires_in"}) Long refreshTokenExpiresIn
    ) {
    }

    private record UserMeResponse(
        @com.fasterxml.jackson.annotation.JsonAlias({"channelId", "id"}) String channelId,
        @com.fasterxml.jackson.annotation.JsonAlias({"channelName", "name"}) String channelName,
        UserMeContent content
    ) {
        String resolveChannelId() {
            if (StringUtils.hasText(channelId)) {
                return channelId;
            }
            return content == null ? null : content.channelId();
        }

        String resolveChannelName() {
            if (StringUtils.hasText(channelName)) {
                return channelName;
            }
            return content == null ? null : content.channelName();
        }
    }

    private record UserMeContent(
        @com.fasterxml.jackson.annotation.JsonAlias({"channelId", "id"}) String channelId,
        @com.fasterxml.jackson.annotation.JsonAlias({"channelName", "name"}) String channelName
    ) {
    }
}
