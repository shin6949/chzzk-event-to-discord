package me.cocoblue.chzzkeventtodiscord.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "storage.upload")
public class StorageUploadProperties {
    private long maxBytes = 5 * 1024 * 1024;
    private List<String> allowedTypes = new ArrayList<>(List.of("image/png", "image/jpeg", "image/webp"));
}
