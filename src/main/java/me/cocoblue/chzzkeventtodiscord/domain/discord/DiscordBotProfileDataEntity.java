package me.cocoblue.chzzkeventtodiscord.domain.discord;

import jakarta.persistence.*;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;

/**
 * {@code DiscordBotProfileDataEntity}는 데이터베이스 테이블 매핑을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "discord_bot_profile_data")
public class DiscordBotProfileDataEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne()
  @JoinColumn(
      name = "owner_id",
      foreignKey = @ForeignKey(name = "FK_BOT_PROFILE_DATA_OWNER_ID"),
      nullable = false)
  private ChzzkChannelEntity ownerId;

  @Column(name = "username", length = 100, nullable = false)
  private String username;

  @Column(name = "alias", length = 500, nullable = false)
  private String alias;

  @Column(name = "avatar_url", length = 30000, nullable = false)
  private String avatarUrl;

  /**
   * {@code fillAliasWhenMissing}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @PrePersist
  @PreUpdate
  private void fillAliasWhenMissing() {
    if (alias == null || alias.isBlank()) {
      alias = username;
    }
  }
}
