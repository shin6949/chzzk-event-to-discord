package me.cocoblue.chzzkeventtodiscord.config.soop;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "soop")
public record SoopProperties(
    String apiBaseUrl,
    String clientId,
    int checkInterval,
    int maxPages
) {
    public SoopProperties {
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            apiBaseUrl = "https://openapi.sooplive.com";
        }
        if (clientId == null) {
            clientId = "";
        }
        if (checkInterval <= 0) {
            checkInterval = 30;
        }
        if (maxPages <= 0) {
            maxPages = 20;
        }
    }

    public boolean hasClientId() {
        return !clientId.isBlank();
    }
}
