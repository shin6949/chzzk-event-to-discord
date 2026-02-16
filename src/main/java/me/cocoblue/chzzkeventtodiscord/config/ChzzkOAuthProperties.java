package me.cocoblue.chzzkeventtodiscord.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "chzzk.oauth")
public class ChzzkOAuthProperties {
    private String authBaseUrl = "https://chzzk.naver.com";
    private String tokenBaseUrl = "https://openapi.chzzk.naver.com";
    private String apiBaseUrl = "https://openapi.chzzk.naver.com";
    private String clientId = "";
    private String clientSecret = "";
    private String redirectUri = "";
}
