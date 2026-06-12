package me.cocoblue.chzzkeventtodiscord.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code StorageUploadProperties}는 Spring 설정 값을 바인딩합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Data
@ConfigurationProperties(prefix = "storage.upload")
public class StorageUploadProperties {
  private long maxBytes = 5 * 1024 * 1024;
  private List<String> allowedTypes =
      new ArrayList<>(List.of("image/png", "image/jpeg", "image/webp"));
  private int maxWidth = 4096;
  private int maxHeight = 4096;
  private long maxPixels = 4096L * 4096L;
}
