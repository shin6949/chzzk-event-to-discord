package me.cocoblue.chzzkeventtodiscord.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code S3StorageProperties}는 Spring 설정 값을 바인딩합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Data
@ConfigurationProperties(prefix = "storage.s3")
public class S3StorageProperties {
  private String endpoint = "http://localhost:9000";
  private String region = "ap-northeast-2";
  private String bucket = "streaming-alert-service-assets";
  private String accessKey = "";
  private String secretKey = "";
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
