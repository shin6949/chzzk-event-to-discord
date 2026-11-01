package me.cocoblue.chzzkeventtodiscord.service.youtube;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeNotificationLogRepository;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.youtube.YouTubeSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.dto.youtube.YouTubeDtos;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class YouTubeSubscriptionService {
    private static final int DEFAULT_INTERVAL_MINUTE = 10;
    private static final String DEFAULT_COLOR_HEX = "ff0000";

    private final YouTubeSubscriptionRepository subscriptionRepository;
    private final YouTubeChannelRepository youtubeChannelRepository;
    private final ChzzkChannelRepository chzzkChannelRepository;
    private final DiscordWebhookDataRepository webhookRepository;
    private final DiscordBotProfileDataRepository botProfileRepository;
    private final YouTubeNotificationLogRepository notificationLogRepository;
    private final YouTubeApiService apiService;

    @Transactional
    public YouTubeSubscriptionEntity create(YouTubeDtos.SubscriptionRequest request, ChzzkPrincipal principal) {
        String ownerChannelId = resolveOwnerChannelId(request.getFormOwnerChannelId(), principal);
        ChzzkChannelEntity owner = resolveOwner(ownerChannelId);
        DiscordWebhookDataEntity webhook = resolveWebhook(request.getWebhookId(), ownerChannelId, principal);
        DiscordBotProfileDataEntity botProfile = resolveBotProfile(request.getBotProfileId(), ownerChannelId, principal);
        YouTubeChannelEntity youtubeChannel = resolveOrCreateYouTubeChannel(request.getYoutubeChannelId());
        if (request.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type is required");
        }
        return subscriptionRepository.saveAndFlush(YouTubeSubscriptionEntity.builder()
            .youtubeChannel(youtubeChannel)
            .type(request.getType())
            .webhook(webhook)
            .formOwner(owner)
            .languageIsoData(request.getLanguage() == null ? LanguageIsoData.Korean : request.getLanguage())
            .intervalMinute(request.getIntervalMinute() == null ? DEFAULT_INTERVAL_MINUTE : request.getIntervalMinute())
            .enabled(request.getEnabled() == null || request.getEnabled())
            .botProfile(botProfile)
            .content(request.getContent())
            .colorHex(StringUtils.hasText(request.getColorHex()) ? request.getColorHex() : DEFAULT_COLOR_HEX)
            .build());
    }

    @Transactional
    public Page<YouTubeSubscriptionEntity> list(ChzzkPrincipal principal, Pageable pageable) {
        if (principal.role() == AppRole.ADMIN) {
            return subscriptionRepository.findAll(pageable);
        }
        return subscriptionRepository.findAllByFormOwner_ChannelId(principal.channelId(), pageable);
    }

    @Transactional
    public YouTubeSubscriptionEntity get(Long id, ChzzkPrincipal principal) {
        YouTubeSubscriptionEntity entity = findById(id);
        ensureReadable(entity, principal);
        return entity;
    }

    @Transactional
    public void delete(Long id, ChzzkPrincipal principal) {
        YouTubeSubscriptionEntity entity = findById(id);
        ensureReadable(entity, principal);
        notificationLogRepository.deleteBySubscription(entity);
        subscriptionRepository.delete(entity);
    }

    private YouTubeSubscriptionEntity findById(Long id) {
        return subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "YouTube subscription not found"));
    }

    private YouTubeChannelEntity resolveOrCreateYouTubeChannel(String channelId) {
        if (!StringUtils.hasText(channelId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "youtubeChannelId is required");
        }
        return youtubeChannelRepository.findById(channelId).orElseGet(() -> {
            YouTubeChannelState state = apiService.getChannelState(channelId);
            YouTubeChannelSnapshot channel = state.channel();
            YouTubeVideoSnapshot liveVideo = state.liveVideo();
            YouTubeVideoSnapshot latestVideo = state.latestVideo();
            return youtubeChannelRepository.save(YouTubeChannelEntity.builder()
                .channelId(channel.channelId())
                .title(channel.title())
                .thumbnailUrl(channel.thumbnailUrl())
                .currentlyLive(state.live())
                .currentLiveVideoId(liveVideo == null ? null : liveVideo.videoId())
                .lastVideoId(latestVideo == null ? null : latestVideo.videoId())
                .build());
        });
    }

    private String resolveOwnerChannelId(String requestedOwnerChannelId, ChzzkPrincipal principal) {
        if (principal.role() == AppRole.ADMIN) {
            if (!StringUtils.hasText(requestedOwnerChannelId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "formOwnerChannelId is required");
            }
            return requestedOwnerChannelId;
        }
        if (StringUtils.hasText(requestedOwnerChannelId) && !Objects.equals(requestedOwnerChannelId, principal.channelId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "formOwnerChannelId does not match authenticated user");
        }
        return principal.channelId();
    }

    private ChzzkChannelEntity resolveOwner(String ownerChannelId) {
        return chzzkChannelRepository.findById(ownerChannelId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "formOwnerChannelId not found"));
    }

    private DiscordWebhookDataEntity resolveWebhook(Long webhookId, String ownerChannelId, ChzzkPrincipal principal) {
        if (webhookId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "webhookId is required");
        }
        DiscordWebhookDataEntity webhook = webhookRepository.findById(webhookId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "webhook not found"));
        ensureRelatedResourceOwner(webhook.getOwnerId().getChannelId(), ownerChannelId, principal, "webhook");
        return webhook;
    }

    private DiscordBotProfileDataEntity resolveBotProfile(Long botProfileId, String ownerChannelId, ChzzkPrincipal principal) {
        if (botProfileId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "botProfileId is required");
        }
        DiscordBotProfileDataEntity botProfile = botProfileRepository.findById(botProfileId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "botProfile not found"));
        ensureRelatedResourceOwner(botProfile.getOwnerId().getChannelId(), ownerChannelId, principal, "botProfile");
        return botProfile;
    }

    private void ensureRelatedResourceOwner(String actualOwnerChannelId, String expectedOwnerChannelId, ChzzkPrincipal principal, String resourceName) {
        if (principal.role() != AppRole.ADMIN && !Objects.equals(actualOwnerChannelId, expectedOwnerChannelId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, resourceName + " does not belong to authenticated owner");
        }
    }

    private void ensureReadable(YouTubeSubscriptionEntity entity, ChzzkPrincipal principal) {
        if (principal.role() != AppRole.ADMIN && !Objects.equals(entity.getFormOwner().getChannelId(), principal.channelId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "YouTube subscription is not readable");
        }
    }
}
