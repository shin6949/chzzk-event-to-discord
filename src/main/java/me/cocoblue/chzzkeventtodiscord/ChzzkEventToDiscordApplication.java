package me.cocoblue.chzzkeventtodiscord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code ChzzkEventToDiscordApplication}는 Spring Boot 애플리케이션의 시작점과 서블릿 초기화를 담당합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
public class ChzzkEventToDiscordApplication extends SpringBootServletInitializer {
  public static String CHZZK_API_URL = "https://api.chzzk.naver.com";

  /**
   * {@code main}은 애플리케이션 실행을 시작합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  public static void main(String[] args) {
    SpringApplication.run(ChzzkEventToDiscordApplication.class, args);
  }

  /**
   * {@code configure}는 서블릿 컨테이너 초기화 시 애플리케이션 소스를 등록합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(ChzzkEventToDiscordApplication.class);
  }
}
