package me.cocoblue.chzzkeventtodiscord.domain.twitch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "twitch_event_subscription")
public class TwitchEventSubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "owner_channel_id", nullable = false)
    private String ownerChannelId;

    @Column(name = "broadcaster_user_id", nullable = false)
    private String broadcasterUserId;

    @Column(name = "broadcaster_login", nullable = false)
    private String broadcasterLogin;

    @Column(name = "broadcaster_display_name", nullable = false)
    private String broadcasterDisplayName;

    @Column(name = "discord_webhook_url", nullable = false, length = 30000)
    private String discordWebhookUrl;

    @Column(name = "eventsub_online_id")
    private String eventsubOnlineId;

    @Column(name = "eventsub_offline_id")
    private String eventsubOfflineId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;
}
