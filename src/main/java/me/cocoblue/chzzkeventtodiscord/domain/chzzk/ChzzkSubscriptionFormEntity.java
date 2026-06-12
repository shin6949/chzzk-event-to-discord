package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

/**
 * {@code ChzzkSubscriptionFormEntity}는 데이터베이스 테이블 매핑을 표현합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "chzzk_subscription_form")
@Inheritance(strategy = InheritanceType.JOINED) // 조인 전략 사용
public class ChzzkSubscriptionFormEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(cascade = CascadeType.DETACH)
  @JoinColumn(
      name = "channel_id",
      foreignKey = @ForeignKey(name = "FK_CHZZK_SUBSCRIPTION_FORM_CHANNEL_ID"),
      nullable = false)
  private ChzzkChannelEntity chzzkChannelEntity;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ChzzkSubscriptionType chzzkSubscriptionType;

  @ManyToOne()
  @JoinColumn(
      name = "webhook_id",
      foreignKey = @ForeignKey(name = "FK_CHZZK_SUBSCRIPTION_FORM_WEBHOOK_ID"),
      nullable = false)
  private DiscordWebhookDataEntity webhookId;

  // 누가 이 폼을 만들었는지
  @ManyToOne()
  @JoinColumn(
      name = "form_owner",
      foreignKey = @ForeignKey(name = "FK_CHZZK_SUBSCRIPTION_FORM_OWNER_CHANNEL_ID"),
      nullable = false)
  private ChzzkChannelEntity formOwner;

  @Enumerated(EnumType.STRING)
  @Column(name = "language", nullable = false)
  private LanguageIsoData languageIsoData;

  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  private ZonedDateTime createdAt;

  @Column(name = "interval_minute", nullable = false)
  @ColumnDefault("10")
  private int intervalMinute;

  @Column(name = "enabled", nullable = false)
  @ColumnDefault("0")
  private boolean enabled;

  @ManyToOne()
  @JoinColumn(
      name = "bot_profile_id",
      foreignKey = @ForeignKey(name = "FK_CHZZK_SUBSCRIPTION_FORM_BOT_PROFILE_ID"),
      nullable = false)
  private DiscordBotProfileDataEntity botProfileId;

  @Column(length = 2000, name = "content")
  private String content;

  @Column(name = "color_hex", nullable = false, length = 11)
  @ColumnDefault("000000")
  private String colorHex;

  /**
   * {@code getDecimalColor}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-03-03 02:51:29 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 4bc1c09.
   *
   * @since Ver.0.1
   */
  public int getDecimalColor() {
    return Integer.parseInt(getColorHex(), 16);
  }
}
