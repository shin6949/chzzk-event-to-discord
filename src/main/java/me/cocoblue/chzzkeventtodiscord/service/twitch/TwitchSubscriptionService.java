package me.cocoblue.chzzkeventtodiscord.service.twitch;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.twitch.TwitchEventSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.twitch.TwitchEventSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordEmbed;
import me.cocoblue.chzzkeventtodiscord.dto.twitch.TwitchEventSubDtos;
import me.cocoblue.chzzkeventtodiscord.dto.twitch.TwitchSubscriptionRequestDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.service.DiscordWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TwitchSubscriptionService {
    private static final String STREAM_ONLINE = "stream.online";
    private static final String STREAM_OFFLINE = "stream.offline";

    private final TwitchEventSubscriptionRepository repository;
    private final TwitchApiClient twitchApiClient;
    private final DiscordWebhookService discordWebhookService;

    @Transactional
    public TwitchEventSubscriptionEntity create(TwitchSubscriptionRequestDto request, ChzzkPrincipal principal) {
        if (!StringUtils.hasText(request.getBroadcasterLogin()) && !StringUtils.hasText(request.getBroadcasterUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "broadcasterLogin or broadcasterUserId is required");
        }
        if (!StringUtils.hasText(request.getDiscordWebhookUrl())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "discordWebhookUrl is required");
        }

        final TwitchEventSubDtos.TwitchUser user = twitchApiClient.resolveBroadcaster(
            request.getBroadcasterLogin(),
            request.getBroadcasterUserId()
        );
        final String onlineId = twitchApiClient.createStreamSubscription(STREAM_ONLINE, user.id());
        final String offlineId = twitchApiClient.createStreamSubscription(STREAM_OFFLINE, user.id());

        final TwitchEventSubscriptionEntity entity = TwitchEventSubscriptionEntity.builder()
            .ownerChannelId(principal.channelId())
            .broadcasterUserId(user.id())
            .broadcasterLogin(user.login())
            .broadcasterDisplayName(user.displayName())
            .discordWebhookUrl(request.getDiscordWebhookUrl().trim())
            .eventsubOnlineId(onlineId)
            .eventsubOfflineId(offlineId)
            .enabled(request.getEnabled() == null || request.getEnabled())
            .build();
        return repository.saveAndFlush(entity);
    }

    @Transactional
    public List<TwitchEventSubscriptionEntity> list(ChzzkPrincipal principal) {
        if (principal.role() == AppRole.ADMIN) {
            return repository.findAll();
        }
        return repository.findAllByOwnerChannelIdOrderByCreatedAtDesc(principal.channelId());
    }

    @Transactional
    public void delete(Long id, ChzzkPrincipal principal) {
        final TwitchEventSubscriptionEntity entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Twitch subscription not found"));
        if (principal.role() != AppRole.ADMIN && !entity.getOwnerChannelId().equals(principal.channelId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Twitch subscription not owned by authenticated user");
        }
        revokeRemoteSubscription(entity.getEventsubOnlineId());
        revokeRemoteSubscription(entity.getEventsubOfflineId());
        repository.delete(entity);
    }

    @Transactional
    public void notifyDiscord(String eventSubSubscriptionId, String eventType, Map<String, Object> event) {
        final TwitchEventSubscriptionEntity entity = findEnabledSubscription(eventSubSubscriptionId, eventType);

        final String broadcasterName = String.valueOf(event.getOrDefault("broadcaster_user_name", entity.getBroadcasterDisplayName()));
        final boolean online = STREAM_ONLINE.equals(eventType);
        final String title = online ? "Twitch live started" : "Twitch live ended";
        final String url = "https://www.twitch.tv/" + entity.getBroadcasterLogin();
        final DiscordEmbed embed = DiscordEmbed.builder()
            .title(title)
            .url(url)
            .description(broadcasterName + (online ? " started streaming on Twitch." : " ended their Twitch stream."))
            .color(online ? "9146FF" : "6C757D")
            .timestamp(OffsetDateTime.now().toString())
            .build();
        final DiscordEmbed.Webhook webhook = DiscordEmbed.Webhook.builder()
            .username("Twitch EventSub")
            .content(entity.getBroadcasterDisplayName() + (online ? " is now live on Twitch." : " is now offline on Twitch."))
            .embeds(List.of(embed))
            .build();
        discordWebhookService.sendDiscordWebhook(webhook, entity.getDiscordWebhookUrl());
    }

    private TwitchEventSubscriptionEntity findEnabledSubscription(String eventSubSubscriptionId, String eventType) {
        if (!StringUtils.hasText(eventSubSubscriptionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Twitch EventSub subscription id is required");
        }
        if (STREAM_ONLINE.equals(eventType)) {
            return repository.findByEventsubOnlineIdAndEnabledTrue(eventSubSubscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Twitch subscription not found"));
        }
        if (STREAM_OFFLINE.equals(eventType)) {
            return repository.findByEventsubOfflineIdAndEnabledTrue(eventSubSubscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Twitch subscription not found"));
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported Twitch EventSub type");
    }

    private void revokeRemoteSubscription(String eventSubSubscriptionId) {
        if (StringUtils.hasText(eventSubSubscriptionId)) {
            twitchApiClient.deleteEventSubSubscription(eventSubSubscriptionId);
        }
    }
}
