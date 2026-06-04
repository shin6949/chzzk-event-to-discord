package me.cocoblue.chzzkeventtodiscord.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.AppJwtProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthCookieService {
    private static final String ROOT_PATH = "/";
    private static final String AUTH_PATH = "/api/v1/auth";
    private static final String CHZZK_AUTH_PATH = "/api/v1/auth/chzzk";

    private final AppJwtProperties properties;

    public Optional<String> resolveAccessToken(HttpServletRequest request) {
        return resolveCookieValue(request, properties.getAccessCookieName());
    }

    public Optional<String> resolveRefreshToken(HttpServletRequest request) {
        return resolveCookieValue(request, properties.getRefreshCookieName());
    }

    public Optional<String> resolveOAuthState(HttpServletRequest request) {
        return resolveCookieValue(request, properties.getOauthStateCookieName());
    }

    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, buildCookie(
            properties.getAccessCookieName(),
            token,
            properties.getAccessTokenTtl(),
            ROOT_PATH
        ));
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, buildCookie(
            properties.getRefreshCookieName(),
            token,
            properties.getRefreshTokenTtl(),
            AUTH_PATH
        ));
    }

    public void addOAuthStateCookie(HttpServletResponse response, String state) {
        addCookie(response, buildCookie(
            properties.getOauthStateCookieName(),
            state,
            properties.getOauthStateTtl(),
            CHZZK_AUTH_PATH
        ));
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, buildExpiredCookie(properties.getAccessCookieName(), ROOT_PATH));
        addCookie(response, buildExpiredCookie(properties.getRefreshCookieName(), AUTH_PATH));
    }

    public void clearOAuthStateCookie(HttpServletResponse response) {
        addCookie(response, buildExpiredCookie(properties.getOauthStateCookieName(), CHZZK_AUTH_PATH));
    }

    private Optional<String> resolveCookieValue(HttpServletRequest request, String name) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
            .filter(cookie -> name.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    private ResponseCookie buildCookie(String name, String value, Duration maxAge, String path) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(properties.isCookieSecure())
            .sameSite(properties.getCookieSameSite())
            .path(path)
            .maxAge(maxAge)
            .build();
    }

    private ResponseCookie buildExpiredCookie(String name, String path) {
        return buildCookie(name, "", Duration.ZERO, path);
    }

    private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
