package me.cocoblue.chzzkeventtodiscord.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code DiscordEmbed}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordEmbed {
  @JsonProperty("author")
  private Author author;

  @JsonProperty("title")
  private String title;

  @JsonProperty("url")
  private String url;

  @JsonProperty("description")
  private String description;

  @JsonProperty("color")
  private String color;

  @JsonProperty("fields")
  private List<Field> fields;

  @JsonProperty("thumbnail")
  private Thumbnail thumbnail;

  @JsonProperty("image")
  private Image image;

  @JsonProperty("footer")
  private Footer footer;

  @JsonProperty("timestamp")
  private String timestamp;

  /**
   * {@code Author}는 관련 도메인 책임을 캡슐화합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Author {
    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;

    @JsonProperty("icon_url")
    private String iconUrl;
  }

  /**
   * {@code Field}는 관련 도메인 책임을 캡슐화합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Field {
    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;

    @JsonProperty("inline")
    private Boolean inline;
  }

  /**
   * {@code Footer}는 관련 도메인 책임을 캡슐화합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Footer {
    @JsonProperty("text")
    private String text;

    @JsonProperty("icon_url")
    private String iconUrl;
  }

  /**
   * {@code Webhook}는 관련 도메인 책임을 캡슐화합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Webhook {
    @JsonProperty("username")
    private String username;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("content")
    private String content;

    @JsonProperty("embeds")
    private List<DiscordEmbed> embeds;

    @Builder.Default
    @JsonProperty("allowed_mentions")
    private AllowedMentions allowedMentions = AllowedMentions.none();

    /**
     * {@code Webhook} 생성자는 인스턴스 초기 상태를 구성합니다.
     *
     * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
     * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
     *
     * @since unreleased after Ver.0.1.4
     */
    public Webhook(String username, String avatarUrl, String content, List<DiscordEmbed> embeds) {
      this.username = username;
      this.avatarUrl = avatarUrl;
      this.content = content;
      this.embeds = embeds;
      this.allowedMentions = AllowedMentions.none();
    }
  }

  /**
   * {@code AllowedMentions}는 관련 도메인 책임을 캡슐화합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AllowedMentions {
    @JsonProperty("parse")
    private List<String> parse;

    /**
     * {@code none}은 해당 클래스의 세부 동작을 수행합니다.
     *
     * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
     * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
     *
     * @since unreleased after Ver.0.1.4
     */
    public static AllowedMentions none() {
      return new AllowedMentions(List.of());
    }
  }

  /**
   * {@code Thumbnail}는 관련 도메인 책임을 캡슐화합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Thumbnail {
    @JsonProperty("url")
    private String url;
  }

  /**
   * {@code Image}는 관련 도메인 책임을 캡슐화합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Image {
    @JsonProperty("url")
    private String url;

    @JsonProperty("height")
    private int height;

    @JsonProperty("width")
    private int width;
  }
}
