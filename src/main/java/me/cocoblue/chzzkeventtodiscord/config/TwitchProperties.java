package me.cocoblue.chzzkeventtodiscord.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "twitch")
public record TwitchProperties(
    String apiBaseUrl,
    String authBaseUrl,
    String clientId,
    String clientSecret,
    String eventsubCallbackBaseUrl,
    String eventsubSecret
) {
    public String normalizedApiBaseUrl() {
        return normalize(apiBaseUrl, "https://api.twitch.tv");
    }

    public String normalizedAuthBaseUrl() {
        return normalize(authBaseUrl, "https://id.twitch.tv");
    }

    public String callbackUrl() {
        final String baseUrl = normalize(eventsubCallbackBaseUrl, "");
        if (baseUrl.isBlank()) {
            return "";
        }
        return baseUrl + "/api/v1/twitch/eventsub";
    }

    public boolean isRegistrationConfigured() {
        return hasText(clientId) && hasText(clientSecret) && hasText(eventsubSecret) && hasText(callbackUrl());
    }

    private static String normalize(String value, String fallback) {
        final String resolved = hasText(value) ? value.trim() : fallback;
        if (resolved.endsWith("/")) {
            return resolved.substring(0, resolved.length() - 1);
        }
        return resolved;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
