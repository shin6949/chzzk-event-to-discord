package me.cocoblue.chzzkeventtodiscord.service.soop;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import me.cocoblue.chzzkeventtodiscord.dto.soop.SoopLiveStatusDto;
import me.cocoblue.chzzkeventtodiscord.service.DiscordWebhookService;
import me.cocoblue.chzzkeventtodiscord.service.StaticContentUrlResolver;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SoopEventSender {
    private final DiscordWebhookService discordWebhookService;
    private final StaticContentUrlResolver staticContentUrlResolver;

    public void sendOnlineEvent(SoopSubscriptionEntity subscription, SoopLiveStatusDto status) {
        send(subscription, status, "SOOP live started", "🟢 " + status.userNick() + " started streaming on SOOP.");
    }

    public void sendOfflineEvent(SoopSubscriptionEntity subscription, SoopLiveStatusDto status) {
        send(subscription, status, "SOOP live ended", "⚫ " + subscription.getSoopChannelName() + " ended the SOOP live stream.");
    }

    private void send(SoopSubscriptionEntity subscription, SoopLiveStatusDto status, String title, String description) {
        final DiscordEmbed embed = DiscordEmbed.builder()
            .title(title)
            .url(status.liveUrl() == null ? subscription.getLiveUrl() : status.liveUrl())
            .description(description)
            .color(String.valueOf(subscription.getDecimalColor()))
            .thumbnail(status.profileImageUrl() == null ? null : DiscordEmbed.Thumbnail.builder().url(status.profileImageUrl()).build())
            .fields(List.of(
                DiscordEmbed.Field.builder().name("SOOP ID").value(subscription.getSoopUserId()).inline(true).build(),
                DiscordEmbed.Field.builder().name("Title").value(status.title() == null ? "-" : status.title()).inline(false).build()
            ))
            .timestamp(OffsetDateTime.now().toString())
            .build();

        final DiscordEmbed.Webhook webhook = DiscordEmbed.Webhook.builder()
            .username(subscription.getBotProfile().getUsername())
            .avatarUrl(staticContentUrlResolver.resolve(subscription.getBotProfile().getAvatarUrl()))
            .content(subscription.getContent())
            .embeds(List.of(embed))
            .build();

        discordWebhookService.sendDiscordWebhook(webhook, subscription.getWebhook().getWebhookUrl());
    }
}
