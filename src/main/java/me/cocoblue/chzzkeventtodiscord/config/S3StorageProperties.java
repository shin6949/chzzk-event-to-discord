package me.cocoblue.chzzkeventtodiscord.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "storage.s3")
public class S3StorageProperties {
    private String endpoint = "http://localhost:9000";
    private String region = "ap-northeast-2";
    private String bucket = "chzzk-event-assets";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private boolean forcePathStyle = true;
    private boolean disableChunkedEncoding = true;
    private String prefix = "bot-profiles";
    private long connectionTimeoutMillis = 5000;
    private long socketTimeoutMillis = 60000;
    private long apiCallAttemptTimeoutMillis = 120000;
    private long apiCallTimeoutMillis = 120000;
    private boolean availabilityCheckEnabled = true;
    private boolean requiredOnStartup = false;
    private long availabilityCheckIntervalMillis = 60000;
    private long availabilityCheckTimeoutMillis = 5000;
}
