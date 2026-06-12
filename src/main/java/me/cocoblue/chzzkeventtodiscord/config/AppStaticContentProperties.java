package me.cocoblue.chzzkeventtodiscord.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code AppStaticContentProperties}는 Spring 설정 값을 바인딩합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Data
@ConfigurationProperties(prefix = "app.static-content")
public class AppStaticContentProperties {
  private String urlPrefix = "";
}
