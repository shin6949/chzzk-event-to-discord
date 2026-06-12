package me.cocoblue.chzzkeventtodiscord.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * {@code ChzzkOAuthProperties}는 Spring 설정 값을 바인딩합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 7fec3f1.
 *
 * @since unreleased after Ver.0.1.4
 */
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
