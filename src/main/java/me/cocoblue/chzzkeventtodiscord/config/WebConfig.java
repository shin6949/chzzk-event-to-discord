package me.cocoblue.chzzkeventtodiscord.config;

import dev.akkinoc.util.YamlResourceBundle;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

/**
 * {@code WebConfig}는 Spring Bean과 애플리케이션 설정을 구성합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Log4j2
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
  /**
   * {@code localeResolver}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  @Bean
  public CookieLocaleResolver localeResolver() {
    final CookieLocaleResolver localeResolver = new CookieLocaleResolver();
    localeResolver.setDefaultLocale(Locale.ENGLISH);
    localeResolver.setCookieName("locale");
    localeResolver.setCookieMaxAge(60 * 60);
    localeResolver.setCookiePath("/");

    return localeResolver;
  }

  /**
   * {@code messageSource}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  @Bean
  public MessageSource messageSource(@Value("${message.basenames}") List<String> basenames) {
    final YamlMessageSource messageSource = new YamlMessageSource();
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setFallbackToSystemLocale(false);
    messageSource.setDefaultLocale(Locale.ENGLISH);

    String[] messageList = new String[basenames.size()];
    messageList = basenames.toArray(messageList);
    messageSource.setBasenames(messageList);
    return messageSource;
  }

  /**
   * {@code pageableCustomizer}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Bean
  public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer(
      AppSecurityProperties properties) {
    return resolver -> resolver.setMaxPageSize(properties.getMaxPageSize());
  }
}

/**
 * {@code YamlMessageSource}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
class YamlMessageSource extends ResourceBundleMessageSource {
  /**
   * {@code doGetBundle}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  @Override
  protected ResourceBundle doGetBundle(String basename, Locale locale)
      throws MissingResourceException {
    return ResourceBundle.getBundle(basename, locale, YamlResourceBundle.Control.INSTANCE);
  }
}
