package me.cocoblue.chzzkeventtodiscord.domain.youtube;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.youtube.YouTubeSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "youtube_subscription")
public class YouTubeSubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "youtube_channel_id", foreignKey = @ForeignKey(name = "fk_youtube_subscription_channel_id"))
    private YouTubeChannelEntity youtubeChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private YouTubeSubscriptionType type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "webhook_id", foreignKey = @ForeignKey(name = "fk_youtube_subscription_webhook_id"))
    private DiscordWebhookDataEntity webhook;

    @ManyToOne(optional = false)
    @JoinColumn(name = "form_owner", foreignKey = @ForeignKey(name = "fk_youtube_subscription_owner_channel_id"))
    private ChzzkChannelEntity formOwner;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 32)
    private LanguageIsoData languageIsoData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "interval_minute", nullable = false)
    @ColumnDefault("10")
    private int intervalMinute;

    @Column(name = "enabled", nullable = false)
    @ColumnDefault("1")
    private boolean enabled;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bot_profile_id", foreignKey = @ForeignKey(name = "fk_youtube_subscription_bot_profile_id"))
    private DiscordBotProfileDataEntity botProfile;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "color_hex", nullable = false, length = 11)
    @ColumnDefault("ff0000")
    private String colorHex;

    public int getDecimalColor() {
        return Integer.parseInt(getColorHex(), 16);
    }
}
