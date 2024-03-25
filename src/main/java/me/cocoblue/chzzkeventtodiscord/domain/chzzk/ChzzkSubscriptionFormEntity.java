package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Data
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
    @JoinColumn(name = "channel_id", foreignKey = @ForeignKey(name = "FK_CHZZK_SUBSCRIPTION_FORM_CHANNEL_ID"), nullable = false)
    private ChzzkChannelEntity chzzkChannelEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ChzzkSubscriptionType chzzkSubscriptionType;

    @ManyToOne()
    @JoinColumn(name = "webhook_id", foreignKey = @ForeignKey(name = "FK_CHZZK_SUBSCRIPTION_FORM_WEBHOOK_ID"), nullable = false)
    private DiscordWebhookDataEntity webhookId;

    // 누가 이 폼을 만들었는지
    @ManyToOne()
    @JoinColumn(name = "form_owner", foreignKey = @ForeignKey(name = "FK_CHZZK_SUBSCRIPTION_FORM_OWNER_CHANNEL_ID"), nullable = false)
    private ChzzkChannelEntity formOwner;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private LanguageIsoData languageIsoData;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @Column(name = "interval_minute", nullable = false, length = 11)
    @ColumnDefault("10")
    private int intervalMinute;

    @Column(name = "enabled", nullable = false, length = 1)
    @ColumnDefault("false")
    private boolean enabled;

    @ManyToOne()
    @JoinColumn(name = "bot_profile_id", foreignKey = @ForeignKey(name = "FK_CHZZK_SUBSCRIPTION_FORM_BOT_PROFILE_ID"), nullable = false)
    private DiscordBotProfileDataEntity botProfileId;

    @Column(length = 2000, name = "content")
    private String content;

    @Column(name = "color_hex", nullable = false, length = 11)
    private String colorHex;

    public int getDecimalColor() {
        return Integer.parseInt(getColorHex(), 16);
    }
}