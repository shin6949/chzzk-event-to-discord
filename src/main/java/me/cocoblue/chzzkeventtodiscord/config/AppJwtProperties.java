package me.cocoblue.chzzkeventtodiscord.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "app.auth.jwt")
public class AppJwtProperties {
    private String secret = "local-development-jwt-secret-change-me-32-bytes-minimum";
    private String issuer = "chzzk-event-to-discord";
    private Duration accessTokenTtl = Duration.ofHours(1);
    private Duration refreshTokenTtl = Duration.ofDays(30);
    private Duration oauthStateTtl = Duration.ofMinutes(5);
    private String accessCookieName = "chzzk_app_access";
    private String refreshCookieName = "chzzk_app_refresh";
    private String oauthStateCookieName = "chzzk_oauth_state";
    private boolean cookieSecure = false;
    private String cookieSameSite = "Lax";
}
