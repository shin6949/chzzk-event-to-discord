package me.cocoblue.chzzkeventtodiscord.service;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.AppStaticContentProperties;
import me.cocoblue.chzzkeventtodiscord.config.S3StorageProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * {@code StaticContentUrlResolver}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Component
@RequiredArgsConstructor
public class StaticContentUrlResolver {
  private final AppStaticContentProperties appStaticContentProperties;
  private final S3StorageProperties s3StorageProperties;

  /**
   * {@code resolve}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public String resolve(String objectKeyOrUrl) {
    if (!StringUtils.hasText(objectKeyOrUrl)) {
      return objectKeyOrUrl;
    }
    if (objectKeyOrUrl.startsWith("http://") || objectKeyOrUrl.startsWith("https://")) {
      return objectKeyOrUrl;
    }

    return trimTrailingSlash(resolveBaseUrl()) + "/" + trimLeadingSlash(objectKeyOrUrl);
  }

  /**
   * {@code isManagedObjectKey}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public boolean isManagedObjectKey(String objectKeyOrUrl) {
    return StringUtils.hasText(objectKeyOrUrl)
        && !objectKeyOrUrl.startsWith("http://")
        && !objectKeyOrUrl.startsWith("https://");
  }

  /**
   * {@code resolveBaseUrl}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String resolveBaseUrl() {
    if (StringUtils.hasText(appStaticContentProperties.getUrlPrefix())) {
      return appStaticContentProperties.getUrlPrefix();
    }
    return trimTrailingSlash(s3StorageProperties.getEndpoint())
        + "/"
        + s3StorageProperties.getBucket();
  }

  /**
   * {@code trimTrailingSlash}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String trimTrailingSlash(String value) {
    String next = value == null ? "" : value.trim();
    while (next.endsWith("/")) {
      next = next.substring(0, next.length() - 1);
    }
    return next;
  }

  /**
   * {@code trimLeadingSlash}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private String trimLeadingSlash(String value) {
    String next = value.trim();
    while (next.startsWith("/")) {
      next = next.substring(1);
    }
    return next;
  }
}
