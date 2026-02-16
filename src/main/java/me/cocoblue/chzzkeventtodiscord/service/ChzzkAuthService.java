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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChzzkAuthService {
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
        if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
            throw new ResponseStatusException(BAD_GATEWAY, "failed to exchange oauth code");
        }

        final UserMeResponse userMeResponse = requestUserMe(tokenResponse.accessToken());
        final String channelId = userMeResponse == null ? null : userMeResponse.resolveChannelId();
        if (!StringUtils.hasText(channelId)) {
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
        if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
            throw new ResponseStatusException(BAD_GATEWAY, "failed to refresh oauth token");
        }

        upsertTokenByRefreshToken(channelId, tokenResponse);
        return tokenResponse.accessToken();
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
                revokeToken(tokenEntity.getRefreshToken());
            } catch (Exception e) {
                throw new ResponseStatusException(BAD_GATEWAY, "failed to revoke refresh token", e);
            }
        } else if (StringUtils.hasText(tokenEntity.getAccessToken())) {
            try {
                revokeToken(tokenEntity.getAccessToken());
            } catch (Exception e) {
                throw new ResponseStatusException(BAD_GATEWAY, "failed to revoke access token", e);
            }
        }

        chzzkOAuthTokenRepository.delete(tokenEntity);
    }

    private TokenResponse requestToken(String code, String state) {
        final LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grantType", "authorization_code");
        formData.add("clientId", chzzkOAuthProperties.getClientId());
        formData.add("clientSecret", chzzkOAuthProperties.getClientSecret());
        formData.add("code", code);
        formData.add("state", state);
        if (StringUtils.hasText(chzzkOAuthProperties.getRedirectUri())) {
            formData.add("redirectUri", chzzkOAuthProperties.getRedirectUri());
        }

        return requestTokenInternal(formData);
    }

    private TokenResponse requestRefreshToken(String refreshToken) {
        final LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grantType", "refresh_token");
        formData.add("clientId", chzzkOAuthProperties.getClientId());
        formData.add("clientSecret", chzzkOAuthProperties.getClientSecret());
        formData.add("refreshToken", refreshToken);

        return requestTokenInternal(formData);
    }

    private void revokeToken(String token) {
        final LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("clientId", chzzkOAuthProperties.getClientId());
        formData.add("clientSecret", chzzkOAuthProperties.getClientSecret());
        formData.add("token", token);
        formData.add("refreshToken", token);

        WebClient.builder()
            .baseUrl(chzzkOAuthProperties.getTokenBaseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build()
            .post()
            .uri("/auth/v1/token/revoke")
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .toBodilessEntity()
            .block();
    }

    private TokenResponse requestTokenInternal(LinkedMultiValueMap<String, String> formData) {
        return WebClient.builder()
            .baseUrl(chzzkOAuthProperties.getTokenBaseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build()
            .post()
            .uri("/auth/v1/token")
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(TokenResponse.class)
            .block();
    }

    private UserMeResponse requestUserMe(String accessToken) {
        return WebClient.builder()
            .baseUrl(chzzkOAuthProperties.getApiBaseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .get()
            .uri("/open/v1/users/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(UserMeResponse.class)
            .block();
    }

    private void upsertToken(String channelId, TokenResponse tokenResponse) {
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        final ChzzkOAuthTokenEntity tokenEntity = chzzkOAuthTokenRepository.findById(channelId)
            .orElseGet(() -> ChzzkOAuthTokenEntity.builder().channelId(channelId).build());

        tokenEntity.setAccessToken(tokenResponse.accessToken());
        tokenEntity.setRefreshToken(tokenResponse.refreshToken());
        tokenEntity.setTokenType(tokenResponse.tokenType());
        tokenEntity.setScope(tokenResponse.scope());
        tokenEntity.setAccessTokenExpiresAt(
            tokenResponse.expiresIn() == null ? null : now.plusSeconds(tokenResponse.expiresIn())
        );
        tokenEntity.setRefreshTokenExpiresAt(
            tokenResponse.refreshTokenExpiresIn() == null ? null : now.plusSeconds(tokenResponse.refreshTokenExpiresIn())
        );

        chzzkOAuthTokenRepository.save(tokenEntity);
    }

    private void upsertTokenByRefreshToken(String channelId, TokenResponse tokenResponse) {
        final ZonedDateTime now = nowUtc();
        final ChzzkOAuthTokenEntity tokenEntity = chzzkOAuthTokenRepository.findById(channelId)
            .orElseGet(() -> ChzzkOAuthTokenEntity.builder().channelId(channelId).build());

        if (StringUtils.hasText(tokenResponse.accessToken())) {
            tokenEntity.setAccessToken(tokenResponse.accessToken());
        }
        if (StringUtils.hasText(tokenResponse.refreshToken())) {
            tokenEntity.setRefreshToken(tokenResponse.refreshToken());
        }
        if (StringUtils.hasText(tokenResponse.tokenType())) {
            tokenEntity.setTokenType(tokenResponse.tokenType());
        }
        if (StringUtils.hasText(tokenResponse.scope())) {
            tokenEntity.setScope(tokenResponse.scope());
        }
        tokenEntity.setAccessTokenExpiresAt(
            tokenResponse.expiresIn() == null ? tokenEntity.getAccessTokenExpiresAt() : now.plusSeconds(tokenResponse.expiresIn())
        );
        tokenEntity.setRefreshTokenExpiresAt(
            tokenResponse.refreshTokenExpiresIn() == null ? tokenEntity.getRefreshTokenExpiresAt() : now.plusSeconds(tokenResponse.refreshTokenExpiresIn())
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

    private record TokenResponse(
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
        UserMeContent content
    ) {
        String resolveChannelId() {
            if (StringUtils.hasText(channelId)) {
                return channelId;
            }
            return content == null ? null : content.channelId();
        }

        String resolveChannelName() {
            return content == null ? null : content.channelName();
        }
    }

    private record UserMeContent(
        @com.fasterxml.jackson.annotation.JsonAlias({"channelId", "id"}) String channelId,
        @com.fasterxml.jackson.annotation.JsonAlias({"channelName", "name"}) String channelName
    ) {
    }
}
