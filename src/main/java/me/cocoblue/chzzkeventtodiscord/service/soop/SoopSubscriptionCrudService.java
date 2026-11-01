package me.cocoblue.chzzkeventtodiscord.service.soop;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordBotProfileDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.dto.soop.SoopLiveStatusDto;
import me.cocoblue.chzzkeventtodiscord.dto.soop.SoopSubscriptionRequestDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SoopSubscriptionCrudService {
    private static final String DEFAULT_COLOR_HEX = "9146FF";

    private final SoopSubscriptionRepository soopSubscriptionRepository;
    private final ChzzkChannelRepository chzzkChannelRepository;
    private final DiscordWebhookDataRepository discordWebhookDataRepository;
    private final DiscordBotProfileDataRepository discordBotProfileDataRepository;
    private final SoopLiveStatusService soopLiveStatusService;

    @Transactional
    public SoopSubscriptionEntity create(SoopSubscriptionRequestDto request, ChzzkPrincipal principal) {
        final ChzzkChannelEntity owner = chzzkChannelRepository.findById(principal.channelId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "form owner channel was not found"));
        final DiscordWebhookDataEntity webhook = resolveWebhook(request.getWebhookId(), principal);
        final DiscordBotProfileDataEntity botProfile = resolveBotProfile(request.getBotProfileId(), principal);
        final SoopLiveStatusDto status = soopLiveStatusService.getLiveStatus(requireSoopUserId(request.getSoopUserId()));

        final SoopSubscriptionEntity entity = SoopSubscriptionEntity.builder()
            .soopUserId(status.userId())
            .soopChannelName(resolveChannelName(request.getSoopChannelName(), status))
            .profileImageUrl(status.profileImageUrl())
            .live(status.live())
            .liveTitle(status.title())
            .broadNo(status.broadNo())
            .liveUrl(status.liveUrl())
            .enabled(request.getEnabled() == null || request.getEnabled())
            .notifyOnline(request.getNotifyOnline() == null || request.getNotifyOnline())
            .notifyOffline(request.getNotifyOffline() == null || request.getNotifyOffline())
            .content(request.getContent())
            .colorHex(resolveColorHex(request.getColorHex()))
            .webhook(webhook)
            .botProfile(botProfile)
            .formOwner(owner)
            .build();

        return soopSubscriptionRepository.saveAndFlush(entity);
    }

    @Transactional
    public Page<SoopSubscriptionEntity> list(ChzzkPrincipal principal, Pageable pageable) {
        if (principal.role() == AppRole.ADMIN) {
            return soopSubscriptionRepository.findAll(pageable);
        }
        return soopSubscriptionRepository.findAllByFormOwner_ChannelId(principal.channelId(), pageable);
    }

    @Transactional
    public void delete(Long id, ChzzkPrincipal principal) {
        final SoopSubscriptionEntity entity = findById(id);
        ensureWritable(entity, principal);
        soopSubscriptionRepository.delete(entity);
    }

    private DiscordWebhookDataEntity resolveWebhook(Long webhookId, ChzzkPrincipal principal) {
        if (webhookId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "webhookId is required");
        }
        final DiscordWebhookDataEntity webhook = discordWebhookDataRepository.findById(webhookId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "webhook was not found"));
        if (principal.role() != AppRole.ADMIN && !principal.channelId().equals(webhook.getOwnerId().getChannelId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "webhook is not owned by the current user");
        }
        return webhook;
    }

    private DiscordBotProfileDataEntity resolveBotProfile(Long botProfileId, ChzzkPrincipal principal) {
        if (botProfileId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "botProfileId is required");
        }
        final DiscordBotProfileDataEntity botProfile = discordBotProfileDataRepository.findById(botProfileId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "bot profile was not found"));
        if (principal.role() != AppRole.ADMIN && !principal.channelId().equals(botProfile.getOwnerId().getChannelId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "bot profile is not owned by the current user");
        }
        return botProfile;
    }

    private SoopSubscriptionEntity findById(Long id) {
        return soopSubscriptionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SOOP subscription was not found"));
    }

    private void ensureWritable(SoopSubscriptionEntity entity, ChzzkPrincipal principal) {
        if (principal.role() == AppRole.ADMIN) {
            return;
        }
        if (!principal.channelId().equals(entity.getFormOwner().getChannelId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SOOP subscription is not owned by the current user");
        }
    }

    private String requireSoopUserId(String value) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "soopUserId is required");
        }
        return value.trim();
    }

    private String resolveChannelName(String requestedName, SoopLiveStatusDto status) {
        if (StringUtils.hasText(requestedName)) {
            return requestedName.trim();
        }
        return StringUtils.hasText(status.userNick()) ? status.userNick() : status.userId();
    }

    private String resolveColorHex(String value) {
        if (!StringUtils.hasText(value)) {
            return DEFAULT_COLOR_HEX;
        }
        final String normalized = value.trim().replace("#", "");
        if (!normalized.matches("[0-9a-fA-F]{6}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "colorHex must be a 6-digit hex color");
        }
        return normalized.toUpperCase();
    }
}
