package me.cocoblue.chzzkeventtodiscord.config;

import me.cocoblue.chzzkeventtodiscord.service.youtube.YouTubeApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(YouTubeApiProperties.class)
public class YouTubeConfig {
}
