package me.cocoblue.chzzkeventtodiscord.domain.soop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "soop_subscription")
public class SoopSubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "soop_user_id", nullable = false, length = 255)
    private String soopUserId;

    @Column(name = "soop_channel_name", nullable = false, length = 255)
    private String soopChannelName;

    @Column(name = "profile_image_url", length = 30000)
    private String profileImageUrl;

    @Column(name = "is_live", nullable = false)
    private boolean live;

    @Column(name = "live_title", length = 500)
    private String liveTitle;

    @Column(name = "broad_no", length = 255)
    private String broadNo;

    @Column(name = "live_url", length = 30000)
    private String liveUrl;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "notify_online", nullable = false)
    private boolean notifyOnline;

    @Column(name = "notify_offline", nullable = false)
    private boolean notifyOffline;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "color_hex", nullable = false, length = 11)
    private String colorHex;

    @ManyToOne
    @JoinColumn(name = "webhook_id", foreignKey = @ForeignKey(name = "fk_soop_subscription_webhook_id"), nullable = false)
    private DiscordWebhookDataEntity webhook;

    @ManyToOne
    @JoinColumn(name = "bot_profile_id", foreignKey = @ForeignKey(name = "fk_soop_subscription_bot_profile_id"), nullable = false)
    private DiscordBotProfileDataEntity botProfile;

    @ManyToOne
    @JoinColumn(name = "form_owner", foreignKey = @ForeignKey(name = "fk_soop_subscription_owner_channel_id"), nullable = false)
    private ChzzkChannelEntity formOwner;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @Column(name = "last_check_time", nullable = false)
    @UpdateTimestamp
    private ZonedDateTime lastCheckTime;

    public int getDecimalColor() {
        return Integer.parseInt(colorHex, 16);
    }
}
