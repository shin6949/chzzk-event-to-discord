package me.cocoblue.chzzkeventtodiscord.service.youtube;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "youtube")
public class YouTubeApiProperties {
    private String apiBaseUrl = "https://www.googleapis.com/youtube/v3";
    private String apiKey = "";
    private int checkInterval = 60;
}
