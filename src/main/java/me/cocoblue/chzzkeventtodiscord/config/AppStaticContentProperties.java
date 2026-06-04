package me.cocoblue.chzzkeventtodiscord.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.static-content")
public class AppStaticContentProperties {
    private String urlPrefix = "";
}
