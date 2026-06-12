package me.cocoblue.chzzkeventtodiscord.domain.discord;

import jakarta.persistence.*;
import lombok.*;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;

/**
 * {@code DiscordWebhookDataEntity}는 데이터베이스 테이블 매핑을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "discord_webhook_data")
@Builder
public class DiscordWebhookDataEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "name", length = 500, nullable = false)
  private String name;

  @Column(name = "webhook_url", length = 30000, nullable = false)
  @ToString.Exclude
  private String webhookUrl;

  @Column(name = "meno", length = 500)
  private String meno;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(
      name = "owner_id",
      foreignKey = @ForeignKey(name = "fk_discord_webhook_data_owner_id"),
      nullable = false)
  private ChzzkChannelEntity ownerId;
}
